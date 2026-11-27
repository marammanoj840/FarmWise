package com.farmwise.backend.config;

import com.farmwise.backend.crop.CropEntity;
import com.farmwise.backend.crop.CropRepository;
import com.farmwise.backend.crop.FarmCropEntity;
import com.farmwise.backend.crop.FarmCropRepository;
import com.farmwise.backend.cultivation.CultivationActivityEntity;
import com.farmwise.backend.cultivation.CultivationActivityRepository;
import com.farmwise.backend.cultivation.CultivationTaskEntity;
import com.farmwise.backend.cultivation.CultivationTaskRepository;
import com.farmwise.backend.entity.RoleEntity;
import com.farmwise.backend.expert.ConsultationRequestEntity;
import com.farmwise.backend.expert.ConsultationRequestRepository;
import com.farmwise.backend.expert.ExpertEntity;
import com.farmwise.backend.expert.ExpertRepository;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmLocationEntity;
import com.farmwise.backend.farm.FarmLocationRepository;
import com.farmwise.backend.farm.FarmRepository;
import com.farmwise.backend.finance.ExpenseEntity;
import com.farmwise.backend.finance.ExpenseRepository;
import com.farmwise.backend.finance.IncomeEntity;
import com.farmwise.backend.finance.IncomeRepository;
import com.farmwise.backend.inventory.InventoryItemEntity;
import com.farmwise.backend.inventory.InventoryItemRepository;
import com.farmwise.backend.marketplace.BuyerEntity;
import com.farmwise.backend.marketplace.BuyerRepository;
import com.farmwise.backend.marketplace.ProduceListingEntity;
import com.farmwise.backend.marketplace.ProduceListingRepository;
import com.farmwise.backend.marketplace.ProductCategoryEntity;
import com.farmwise.backend.marketplace.ProductCategoryRepository;
import com.farmwise.backend.marketplace.ProductEntity;
import com.farmwise.backend.marketplace.ProductRepository;
import com.farmwise.backend.marketplace.SupplierEntity;
import com.farmwise.backend.marketplace.SupplierRepository;
import com.farmwise.backend.notification.NotificationEntity;
import com.farmwise.backend.notification.NotificationPreferenceEntity;
import com.farmwise.backend.notification.NotificationPreferenceRepository;
import com.farmwise.backend.notification.NotificationRepository;
import com.farmwise.backend.repository.RoleRepository;
import com.farmwise.backend.soil.SoilRecordEntity;
import com.farmwise.backend.soil.SoilRecordRepository;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserProfileEntity;
import com.farmwise.backend.user.UserProfileRepository;
import com.farmwise.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app", name = "seed-demo-data", havingValue = "true")
public class DevDemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDemoDataSeeder.class);

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final NotificationPreferenceRepository prefRepository;
    private final NotificationRepository notificationRepository;
    private final FarmRepository farmRepository;
    private final FarmLocationRepository locationRepository;
    private final CropRepository cropRepository;
    private final FarmCropRepository farmCropRepository;
    private final CultivationActivityRepository activityRepository;
    private final CultivationTaskRepository taskRepository;
    private final InventoryItemRepository inventoryRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final SoilRecordRepository soilRepository;
    private final SupplierRepository supplierRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final ProduceListingRepository produceRepository;
    private final ExpertRepository expertRepository;
    private final ConsultationRequestRepository consultationRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultDemoPassword;

    public DevDemoDataSeeder(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            RoleRepository roleRepository,
            NotificationPreferenceRepository prefRepository,
            NotificationRepository notificationRepository,
            FarmRepository farmRepository,
            FarmLocationRepository locationRepository,
            CropRepository cropRepository,
            FarmCropRepository farmCropRepository,
            CultivationActivityRepository activityRepository,
            CultivationTaskRepository taskRepository,
            InventoryItemRepository inventoryRepository,
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            SoilRecordRepository soilRepository,
            SupplierRepository supplierRepository,
            ProductCategoryRepository categoryRepository,
            ProductRepository productRepository,
            BuyerRepository buyerRepository,
            ProduceListingRepository produceRepository,
            ExpertRepository expertRepository,
            ConsultationRequestRepository consultationRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.default-password:}") String defaultDemoPassword) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.prefRepository = prefRepository;
        this.notificationRepository = notificationRepository;
        this.farmRepository = farmRepository;
        this.locationRepository = locationRepository;
        this.cropRepository = cropRepository;
        this.farmCropRepository = farmCropRepository;
        this.activityRepository = activityRepository;
        this.taskRepository = taskRepository;
        this.inventoryRepository = inventoryRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.soilRepository = soilRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.buyerRepository = buyerRepository;
        this.produceRepository = produceRepository;
        this.expertRepository = expertRepository;
        this.consultationRepository = consultationRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultDemoPassword = defaultDemoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0 || defaultDemoPassword == null || defaultDemoPassword.isBlank()) {
            return;
        }
        log.info("Seeding FarmWise development demo ecosystem records...");

        String encoded = passwordEncoder.encode(defaultDemoPassword);
        UserEntity admin = createUser("admin.demo@farmwise.in", "Aarav Deshmukh (Admin)", "+91-98100-00001", "ADMIN", encoded, "Pune", "Maharashtra");
        UserEntity farmer = createUser("farmer.demo@farmwise.in", "Harpreet Singh Gill (Demo Farmer)", "+91-98155-12034", "FARMER", encoded, "Jagraon", "Punjab");
        UserEntity expertUser = createUser("expert.demo@farmwise.in", "Dr. Kavita Kulkarni", "+91-98220-44551", "EXPERT", encoded, "Pune", "Maharashtra");
        UserEntity pendingExpertUser = createUser("expert.pending@farmwise.in", "Rohan Verma", "+91-98220-44552", "EXPERT", encoded, "Indore", "Madhya Pradesh");
        UserEntity supplierUser = createUser("supplier.demo@farmwise.in", "Kisan Agro Supplies Co.", "+91-98900-77123", "SUPPLIER", encoded, "Nashik", "Maharashtra");
        UserEntity buyerUser = createUser("buyer.demo@farmwise.in", "Sahyadri Fresh Procurement", "+91-99200-88341", "BUYER", encoded, "Mumbai", "Maharashtra");

        // Farms & Locations
        FarmEntity farm1 = new FarmEntity();
        farm1.setFarmerId(farmer.getId());
        farm1.setName("Satluj Plains Estate (Demo)");
        farm1.setLandArea(12.5);
        farm1.setAreaUnit("ACRES");
        farm1.setSoilType("Alluvial Loam");
        farm1.setIrrigationType("Tube Well + Drip");
        farm1.setDescription("Primary irrigated cereal and oilseed holding near Jagraon.");
        farm1 = farmRepository.save(farm1);

        FarmLocationEntity loc1 = new FarmLocationEntity();
        loc1.setFarmId(farm1.getId());
        loc1.setVillageOrCity("Jagraon");
        loc1.setDistrict("Ludhiana");
        loc1.setState("Punjab");
        loc1.setCountry("India");
        loc1.setPostalCode("142026");
        loc1.setLatitude(30.7885);
        loc1.setLongitude(75.4730);
        loc1.setClimateZone("Semi-Arid North-Western Plains");
        locationRepository.save(loc1);

        FarmEntity farm2 = new FarmEntity();
        farm2.setFarmerId(farmer.getId());
        farm2.setName("Malwa Horticulture Plot (Demo)");
        farm2.setLandArea(4.2);
        farm2.setAreaUnit("ACRES");
        farm2.setSoilType("Medium Black Soil");
        farm2.setIrrigationType("Solar Drip");
        farm2.setDescription("High-value vegetable and pulse block with drip fertigation.");
        farm2 = farmRepository.save(farm2);

        FarmLocationEntity loc2 = new FarmLocationEntity();
        loc2.setFarmId(farm2.getId());
        loc2.setVillageOrCity("Mhow");
        loc2.setDistrict("Indore");
        loc2.setState("Madhya Pradesh");
        loc2.setCountry("India");
        loc2.setPostalCode("453441");
        loc2.setLatitude(22.5524);
        loc2.setLongitude(75.7565);
        loc2.setClimateZone("Central Plateau");
        locationRepository.save(loc2);

        Map<String, CropEntity> cropsByName = cropRepository.findAll().stream()
                .collect(Collectors.toMap(CropEntity::getName, c -> c));

        FarmCropEntity fc1 = new FarmCropEntity();
        fc1.setFarmId(farm1.getId());
        fc1.setFarmerId(farmer.getId());
        if (cropsByName.containsKey("Wheat")) {
            fc1.setCropId(cropsByName.get("Wheat").getId());
        }
        fc1.setCropName("Wheat");
        fc1.setVarietyName("DBW 187 (Karan Vandana)");
        fc1.setSeason("Rabi");
        fc1.setPlantedArea(8.0);
        fc1.setAreaUnit("ACRES");
        fc1.setSowingDate(LocalDate.now().minusDays(22));
        fc1.setExpectedHarvestDate(LocalDate.now().plusDays(106));
        fc1.setGrowthStage("CROWN_ROOT_INITIATION");
        fc1.setCultivationStatus("GROWING");
        fc1.setExpectedYieldKg(18000.0);
        fc1.setFieldObservations("Uniform germination across Blocks A and B; first irrigation scheduled.");
        fc1 = farmCropRepository.save(fc1);

        FarmCropEntity fc2 = new FarmCropEntity();
        fc2.setFarmId(farm2.getId());
        fc2.setFarmerId(farmer.getId());
        if (cropsByName.containsKey("Tomato")) {
            fc2.setCropId(cropsByName.get("Tomato").getId());
        }
        fc2.setCropName("Tomato");
        fc2.setVarietyName("Arka Rakshak");
        fc2.setSeason("Rabi");
        fc2.setPlantedArea(2.5);
        fc2.setAreaUnit("ACRES");
        fc2.setSowingDate(LocalDate.now().minusDays(45));
        fc2.setExpectedHarvestDate(LocalDate.now().plusDays(42));
        fc2.setGrowthStage("FLOWERING_FRUIT_SET");
        fc2.setCultivationStatus("GROWING");
        fc2.setExpectedYieldKg(26000.0);
        fc2.setFieldObservations("Yellow sticky traps installed; drip calcium nitrate applied.");
        fc2 = farmCropRepository.save(fc2);

        // Cultivation activities & tasks
        CultivationActivityEntity act1 = new CultivationActivityEntity();
        act1.setFarmId(farm1.getId());
        act1.setFarmCropId(fc1.getId());
        act1.setFarmerId(farmer.getId());
        act1.setStage("SOWING");
        act1.setTitle("Zero-Till Wheat Sowing (DBW 187)");
        act1.setActivityDate(LocalDate.now().minusDays(22));
        act1.setInputUsed("DBW 187 Certified Seed + DAP");
        act1.setQuantity(320.0);
        act1.setUnit("kg");
        act1.setCostAmount(new BigDecimal("16400.00"));
        act1.setNotes("Sown at 20 cm row spacing with happy seeder.");
        activityRepository.save(act1);

        CultivationTaskEntity task1 = new CultivationTaskEntity();
        task1.setFarmId(farm1.getId());
        task1.setFarmCropId(fc1.getId());
        task1.setFarmerId(farmer.getId());
        task1.setStage("IRRIGATION");
        task1.setTitle("Crown Root Initiation (CRI) First Irrigation & Top Dressing");
        task1.setDueDate(LocalDate.now().plusDays(2));
        task1.setPriority("HIGH");
        task1.setStatus("PENDING");
        task1.setNotes("Check moisture and apply split Urea dose after light irrigation.");
        taskRepository.save(task1);

        CultivationTaskEntity task2 = new CultivationTaskEntity();
        task2.setFarmId(farm2.getId());
        task2.setFarmCropId(fc2.getId());
        task2.setFarmerId(farmer.getId());
        task2.setStage("PEST_MANAGEMENT");
        task2.setTitle("Inspect Tomato Foliage for Early Blight & Whitefly");
        task2.setDueDate(LocalDate.now().plusDays(4));
        task2.setPriority("MEDIUM");
        task2.setStatus("PENDING");
        task2.setNotes("Replace pheromone lures if needed.");
        taskRepository.save(task2);

        // Soil record
        SoilRecordEntity soil1 = new SoilRecordEntity();
        soil1.setFarmId(farm1.getId());
        soil1.setFarmerId(farmer.getId());
        soil1.setSoilType("Alluvial Loam");
        soil1.setPh(7.1);
        soil1.setNitrogen(265.0);
        soil1.setPhosphorus(24.5);
        soil1.setPotassium(195.0);
        soil1.setOrganicCarbonPct(0.58);
        soil1.setMoisturePct(26.0);
        soil1.setFertilityRating("GOOD");
        soil1.setLabName("PAU Soil Testing Lab, Ludhiana (Demo)");
        soil1.setTestDate(LocalDate.now().minusDays(35));
        soil1.setObservations("Balanced pH; moderate organic carbon — add compost before next Kharif.");
        soilRepository.save(soil1);

        // Inventory
        InventoryItemEntity inv1 = new InventoryItemEntity();
        inv1.setFarmerId(farmer.getId());
        inv1.setFarmId(farm1.getId());
        inv1.setName("Neem Coated Urea (45 kg Bag)");
        inv1.setCategory("FERTILIZERS");
        inv1.setQuantity(14);
        inv1.setUnit("bags");
        inv1.setReorderThreshold(5);
        inv1.setUnitCostInr(new BigDecimal("266.50"));
        inv1.setSupplierName("IFFCO Cooperative");
        inventoryRepository.save(inv1);

        InventoryItemEntity inv2 = new InventoryItemEntity();
        inv2.setFarmerId(farmer.getId());
        inv2.setFarmId(farm2.getId());
        inv2.setName("16mm Inline Drip Lateral Coil (500m)");
        inv2.setCategory("IRRIGATION_MATERIALS");
        inv2.setQuantity(2);
        inv2.setUnit("coils");
        inv2.setReorderThreshold(3);
        inv2.setUnitCostInr(new BigDecimal("4250.00"));
        inv2.setSupplierName("Kisan Agro Supplies Co.");
        inventoryRepository.save(inv2);

        // Finance
        ExpenseEntity exp1 = new ExpenseEntity();
        exp1.setFarmerId(farmer.getId());
        exp1.setFarmId(farm1.getId());
        exp1.setFarmCropId(fc1.getId());
        exp1.setCategory("SEEDS");
        exp1.setTitle("DBW 187 Certified Wheat Seeds (Demo)");
        exp1.setAmountInr(new BigDecimal("12800.00"));
        exp1.setExpenseDate(LocalDate.now().minusDays(25));
        exp1.setVendorName("Punjab State Seed Corp");
        exp1.setPaymentMethod("UPI");
        expenseRepository.save(exp1);

        ExpenseEntity exp2 = new ExpenseEntity();
        exp2.setFarmerId(farmer.getId());
        exp2.setFarmId(farm2.getId());
        exp2.setFarmCropId(fc2.getId());
        exp2.setCategory("FERTILIZERS");
        exp2.setTitle("Water-Soluble Calcium Nitrate & Micronutrients (Demo)");
        exp2.setAmountInr(new BigDecimal("7450.00"));
        exp2.setExpenseDate(LocalDate.now().minusDays(12));
        exp2.setVendorName("Kisan Agro Supplies Co.");
        exp2.setPaymentMethod("BANK_TRANSFER");
        expenseRepository.save(exp2);

        IncomeEntity inc1 = new IncomeEntity();
        inc1.setFarmerId(farmer.getId());
        inc1.setFarmId(farm1.getId());
        inc1.setCategory("CROP_SALE");
        inc1.setTitle("Previous Kharif Paddy Harvest Sale (Demo)");
        inc1.setAmountInr(new BigDecimal("148500.00"));
        inc1.setIncomeDate(LocalDate.now().minusDays(40));
        inc1.setBuyerName("Khanna Mandi Procurement");
        inc1.setQuantitySold(64.0);
        inc1.setUnit("QUINTAL");
        incomeRepository.save(inc1);

        // Supplier + Products
        SupplierEntity supplier = new SupplierEntity();
        supplier.setUserId(supplierUser.getId());
        supplier.setBusinessName("Kisan Agro Supplies Co. (Demo)");
        supplier.setDescription("Certified seeds, drip irrigation kits, and bio-inputs across India.");
        supplier.setCity("Nashik");
        supplier.setState("Maharashtra");
        supplier.setPhone("+91-98900-77123");
        supplier.setVerificationStatus("APPROVED");
        supplier.setVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        supplier = supplierRepository.save(supplier);

        Map<String, ProductCategoryEntity> cats = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ProductCategoryEntity::getSlug, c -> c));

        createDemoProduct(supplier.getId(), cats.get("seeds"), "Arka Rakshak F1 Tomato Seeds (10g Pack)", "arka-rakshak-f1-tomato-seeds-demo",
                "High-yielding triple-disease-resistant hybrid tomato seeds suitable for open field and polyhouse cultivation.", "pack", new BigDecimal("680.00"), 120, "IIHR / Kisan",
                "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=900&q=80");
        createDemoProduct(supplier.getId(), cats.get("fertilizers"), "Humic + Seaweed Bio-Stimulant Granules (10 kg)", "humic-seaweed-granules-demo",
                "Improves root zone aeration, cation exchange, and micronutrient uptake across loamy and black soils.", "10 kg bucket", new BigDecimal("1150.00"), 65, "GreenRoot",
                "https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=900&q=80");
        createDemoProduct(supplier.getId(), cats.get("crop-protection"), "Cold-Pressed Neem Oil 1500 PPM (1 Litre)", "cold-pressed-neem-oil-demo",
                "Botanical insect growth regulator for aphids, whiteflies, and thrips management.", "1 L bottle", new BigDecimal("540.00"), 95, "BioShield",
                "https://images.unsplash.com/photo-1530836369250-ef72a3f5cda8?auto=format&fit=crop&w=900&q=80");
        createDemoProduct(supplier.getId(), cats.get("irrigation-equipment"), "1-Acre Complete Drip Irrigation Starter Kit", "one-acre-drip-starter-kit-demo",
                "Includes 16mm inline laterals, screen filter, venturi injector, and flush valves for row crops.", "kit", new BigDecimal("14800.00"), 18, "JalFlow",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&w=900&q=80");
        createDemoProduct(supplier.getId(), cats.get("farming-equipment"), "Battery Knapsack Power Sprayer (16L, Dual Pump)", "battery-knapsack-sprayer-demo",
                "12V rechargeable lithium-ion sprayer with brass nozzles and pressure gauge.", "unit", new BigDecimal("3650.00"), 34, "AgriTech",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=900&q=80");
        createDemoProduct(supplier.getId(), cats.get("agricultural-tools"), "Digital 3-in-1 Soil pH, Moisture & Sunlight Meter", "soil-ph-moisture-meter-demo",
                "Portable field probe for quick pH and root-zone moisture screening.", "unit", new BigDecimal("890.00"), 80, "KrishiSense",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?auto=format&fit=crop&w=900&q=80");

        // Buyer & Produce Listings
        BuyerEntity buyer = new BuyerEntity();
        buyer.setUserId(buyerUser.getId());
        buyer.setOrganizationName("Sahyadri Fresh Procurement (Demo)");
        buyer.setBuyerType("AGGREGATOR");
        buyer.setCity("Mumbai");
        buyer.setState("Maharashtra");
        buyer.setPreferredCrops("Wheat, Rice, Tomato, Onion, Soybean");
        buyer.setVerificationStatus("APPROVED");
        buyerRepository.save(buyer);

        ProduceListingEntity produce1 = new ProduceListingEntity();
        produce1.setFarmerId(farmer.getId());
        produce1.setFarmId(farm1.getId());
        produce1.setCropName("Pusa Basmati 1121 Paddy");
        produce1.setVariety("Aged Kharif Lot");
        produce1.setQuantity(45.0);
        produce1.setUnit("QUINTAL");
        produce1.setExpectedPriceInr(new BigDecimal("3950.00"));
        produce1.setLocation("Jagraon, Ludhiana, Punjab");
        produce1.setQualityGrade("Grade A (Moisture < 13%)");
        produce1.setQualityNotes("Machine harvested, cleaned, and stored in dry ventilated godown.");
        produce1.setAvailableFrom(LocalDate.now());
        produce1.setHarvestDate(LocalDate.now().minusDays(28));
        produce1.setListingStatus("ACTIVE");
        produce1.setModerationStatus("APPROVED");
        produceRepository.save(produce1);

        ProduceListingEntity produce2 = new ProduceListingEntity();
        produce2.setFarmerId(farmer.getId());
        produce2.setFarmId(farm2.getId());
        produce2.setCropName("Hybrid Red Tomato");
        produce2.setVariety("Arka Rakshak");
        produce2.setQuantity(18.0);
        produce2.setUnit("QUINTAL");
        produce2.setExpectedPriceInr(new BigDecimal("1850.00"));
        produce2.setLocation("Mhow, Indore, Madhya Pradesh");
        produce2.setQualityGrade("Crate Packed Grade A");
        produce2.setQualityNotes("Drip-irrigated uniform red harvest; farm-gate pickup available.");
        produce2.setAvailableFrom(LocalDate.now().plusDays(15));
        produce2.setListingStatus("ACTIVE");
        produce2.setModerationStatus("APPROVED");
        produceRepository.save(produce2);

        // Experts (1 verified by admin, 1 pending verification to prove honest verification flag!)
        ExpertEntity verifiedExpert = new ExpertEntity();
        verifiedExpert.setUserId(expertUser.getId());
        verifiedExpert.setHeadline("Senior Agronomist & Soil Fertility Specialist");
        verifiedExpert.setSpecializations("Soil Health, Integrated Pest Management, Wheat & Horticulture");
        verifiedExpert.setQualifications("Ph.D. Agronomy (MPKV Rahuri)");
        verifiedExpert.setExperienceYears(14);
        verifiedExpert.setLanguages("English, Hindi, Marathi");
        verifiedExpert.setCity("Pune");
        verifiedExpert.setState("Maharashtra");
        verifiedExpert.setConsultationFeeInr(new BigDecimal("499.00"));
        verifiedExpert.setBio("Advising smallholder and commercial farms on soil nutrition, micro-irrigation, and IPM.");
        verifiedExpert.setVerificationStatus("APPROVED");
        verifiedExpert.setVerifiedBy(admin.getId());
        verifiedExpert.setVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        verifiedExpert.setAvailable(true);
        verifiedExpert = expertRepository.save(verifiedExpert);

        ExpertEntity pendingExpert = new ExpertEntity();
        pendingExpert.setUserId(pendingExpertUser.getId());
        pendingExpert.setHeadline("Plant Protection & Crop Advisory Consultant");
        pendingExpert.setSpecializations("Soybean, Chickpea, Drip Irrigation");
        pendingExpert.setQualifications("M.Sc. Plant Pathology");
        pendingExpert.setExperienceYears(5);
        pendingExpert.setLanguages("English, Hindi");
        pendingExpert.setCity("Indore");
        pendingExpert.setState("Madhya Pradesh");
        pendingExpert.setConsultationFeeInr(new BigDecimal("350.00"));
        pendingExpert.setBio("Pending credential verification — demonstrates unverified expert badge behavior.");
        pendingExpert.setVerificationStatus("PENDING");
        pendingExpert.setAvailable(true);
        expertRepository.save(pendingExpert);

        // Consultation
        ConsultationRequestEntity consult = new ConsultationRequestEntity();
        consult.setFarmerId(farmer.getId());
        consult.setExpertId(verifiedExpert.getId());
        consult.setFarmId(farm1.getId());
        consult.setTopic("Optimal Split Nitrogen Schedule at CRI Stage");
        consult.setCropName("Wheat");
        consult.setQuestion("With soil organic carbon at 0.58% and DBW 187 at 22 DAS, how should I split the remaining nitrogen dose?");
        consult.setUrgency("NORMAL");
        consult.setStatus("PENDING");
        consultationRepository.save(consult);

        // Notification
        NotificationEntity note = new NotificationEntity();
        note.setUserId(farmer.getId());
        note.setNotificationType("INVENTORY_ALERT");
        note.setTitle("Low Stock Alert: 16mm Inline Drip Lateral Coil");
        note.setMessage("Available quantity (2 coils) is below your reorder threshold (3 coils).");
        note.setActionUrl("/app/inventory");
        notificationRepository.save(note);

        log.info("FarmWise dev demo seeding complete.");
    }

    private UserEntity createUser(
            String email, String fullName, String phone, String roleName, String encodedPwd, String city, String state) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing role " + roleName));
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setPrimaryRole(roleName);
        user.setPasswordHash(encodedPwd);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.getRoles().add(role);
        user = userRepository.save(user);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(user.getId());
        profile.setVillageOrCity(city);
        profile.setState(state);
        profile.setCountry("India");
        profile.setPreferredLanguage("en");
        profile.setPreferredCurrency("INR");
        profileRepository.save(profile);

        NotificationPreferenceEntity prefs = new NotificationPreferenceEntity();
        prefs.setUserId(user.getId());
        prefRepository.save(prefs);

        return user;
    }

    private void createDemoProduct(
            java.util.UUID supplierId,
            ProductCategoryEntity category,
            String name,
            String slug,
            String desc,
            String unit,
            BigDecimal price,
            int stock,
            String brand,
            String imageUrl) {
        if (category == null) {
            return;
        }
        ProductEntity p = new ProductEntity();
        p.setSupplierId(supplierId);
        p.setCategoryId(category.getId());
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(desc);
        p.setUnit(unit);
        p.setPriceInr(price);
        p.setStockQuantity(stock);
        p.setBrand(brand);
        p.setImageUrl(imageUrl);
        p.setApprovalStatus("APPROVED");
        p.setActive(true);
        productRepository.save(p);
    }
}
