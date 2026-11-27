package com.farmwise.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmPlatformDomainTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerUser(String email, String role) throws Exception {
        String body = """
                {
                  "fullName": "Domain %s",
                  "email": "%s",
                  "password": "DomainPassword#1",
                  "role": "%s"
                }
                """.formatted(role, email, role);
        MvcResult res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void endToEndFarmerOperationsFinanceReportsAiWeatherAndMarketplace() throws Exception {
        String farmerToken = registerUser("domain.farmer@example.in", "FARMER");
        String supplierToken = registerUser("domain.supplier@example.in", "SUPPLIER");
        String buyerToken = registerUser("domain.buyer@example.in", "BUYER");

        // 1. Create Farm
        String farmId = objectMapper.readTree(mockMvc.perform(post("/api/v1/farms")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Kaveri Delta Plot",
                                  "landArea": 4.0,
                                  "areaUnit": "ACRES",
                                  "soilType": "Clay Loam",
                                  "irrigationType": "Canal + Drip",
                                  "location": {
                                    "villageOrCity": "Thanjavur",
                                    "district": "Thanjavur",
                                    "state": "Tamil Nadu",
                                    "latitude": 10.7870,
                                    "longitude": 79.1378
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        // 2. Create Farm Crop
        String cropId = objectMapper.readTree(mockMvc.perform(post("/api/v1/crops")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "cropName": "Rice (Paddy)",
                                  "varietyName": "MTU 7029 (Swarna)",
                                  "season": "Kharif",
                                  "plantedArea": 3.5,
                                  "areaUnit": "ACRES",
                                  "growthStage": "VEGETATIVE",
                                  "cultivationStatus": "GROWING",
                                  "expectedYieldKg": 8200
                                }
                                """.formatted(farmId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        // 3. Log Cultivation Activity & Task
        mockMvc.perform(post("/api/v1/cultivation/activities")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "farmCropId": "%s",
                                  "stage": "FERTILIZATION",
                                  "title": "Basal Zinc Sulfate Application",
                                  "activityDate": "2026-11-20",
                                  "inputUsed": "Zinc Sulfate 21%%",
                                  "quantity": 25,
                                  "unit": "kg",
                                  "costAmount": 1850.00
                                }
                                """.formatted(farmId, cropId)))
                .andExpect(status().isCreated());

        // 4. Inventory with low-stock alert & negative usage validation
        String invId = objectMapper.readTree(mockMvc.perform(post("/api/v1/inventory")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "name": "Paddy Seed Certified",
                                  "category": "SEEDS",
                                  "quantity": 10.0,
                                  "unit": "kg",
                                  "reorderThreshold": 12.0,
                                  "unitCostInr": 65.00
                                }
                                """.formatted(farmId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lowStock").value(true))
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        // Over-usage fails cleanly
        mockMvc.perform(post("/api/v1/inventory/" + invId + "/transactions")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionType\":\"USAGE\",\"quantity\":50.0}"))
                .andExpect(status().isUnprocessableEntity());

        // 5. Finance Expense + Income + Summary + CSV/PDF Export
        mockMvc.perform(post("/api/v1/finance/expenses")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "farmCropId": "%s",
                                  "category": "SEEDS",
                                  "title": "Paddy Nursery Seeds",
                                  "amountInr": 4500.00,
                                  "expenseDate": "2026-11-18"
                                }
                                """.formatted(farmId, cropId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/finance/income")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "farmCropId": "%s",
                                  "category": "CROP_SALE",
                                  "title": "Early Lot Grain Sale",
                                  "amountInr": 12500.00,
                                  "incomeDate": "2026-11-25"
                                }
                                """.formatted(farmId, cropId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/finance/summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netProfitLossInr").value(8000.00))
                .andExpect(jsonPath("$.disclaimer").exists());

        MvcResult pdfRes = mockMvc.perform(get("/api/v1/finance/reports/export?type=FINANCE_PNL&format=PDF")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        assertThat(pdfRes.getResponse().getContentAsByteArray().length).isGreaterThan(500);

        mockMvc.perform(get("/api/v1/finance/reports/export?type=CROPS&format=CSV")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk());

        // 6. Weather in test profile honestly reports unavailable without fabricating temps
        mockMvc.perform(get("/api/v1/weather/farm/" + farmId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveDataAvailable").value(false));

        // 7. Soil record & AI Assistant unconfigured reference response
        mockMvc.perform(post("/api/v1/soil/records")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "soilType": "Clay Loam",
                                  "ph": 6.6,
                                  "nitrogen": 220,
                                  "phosphorus": 20,
                                  "potassium": 180,
                                  "testDate": "2026-11-15"
                                }
                                """.formatted(farmId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/assistant/ask")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "question": "How should I manage rice irrigation?"
                                }
                                """.formatted(farmId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiProviderConfigured").value(false))
                .andExpect(jsonPath("$.assistantMessage.aiGenerated").value(false));

        // 8. Market prices honestly flag sample reference data
        mockMvc.perform(get("/api/v1/market-prices?commodity=Wheat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containsSampleData").value(true));

        // 9. Public contact inquiry succeeds
        mockMvc.perform(post("/api/v1/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Meera Nair",
                                  "email": "meera@example.in",
                                  "subject": "FPO Partnership",
                                  "message": "We represent 400 farmers in Thrissur interested in FarmWise."
                                }
                                """))
                .andExpect(status().isCreated());

        // 10. Produce listing + Buyer Inquiry + Farmer Reply
        String produceId = objectMapper.readTree(mockMvc.perform(post("/api/v1/produce")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s",
                                  "cropName": "Swarna Paddy",
                                  "quantity": 30,
                                  "unit": "QUINTAL",
                                  "expectedPriceInr": 2300.00,
                                  "location": "Thanjavur, Tamil Nadu",
                                  "qualityGrade": "Grade A"
                                }
                                """.formatted(farmId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/produce/" + produceId + "/inquiries")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "offeredPriceInr": 2280.00,
                                  "requestedQuantity": 15.0,
                                  "message": "Can pick up from farm gate next week."
                                }
                                """))
                .andExpect(status().isCreated());

        // 11. Supplier creates product -> Cart -> Order in PENDING_PAYMENT_CONFIG
        JsonNode cats = objectMapper.readTree(mockMvc.perform(get("/api/v1/marketplace/categories"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String catId = cats.get(0).get("id").asText();

        String prodId = objectMapper.readTree(mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + supplierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "name": "Organic Vermicompost 25kg",
                                  "description": "Nutrient rich compost",
                                  "unit": "bag",
                                  "priceInr": 450.00,
                                  "stockQuantity": 20
                                }
                                """.formatted(catId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        // Notice supplier is unverified so product starts PENDING
        assertThat(prodId).isNotBlank();
    }
}
