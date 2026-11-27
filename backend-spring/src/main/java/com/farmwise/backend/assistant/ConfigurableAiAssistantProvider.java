package com.farmwise.backend.assistant;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import com.farmwise.backend.soil.CropKnowledgeBaseEntity;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ConfigurableAiAssistantProvider implements AiAssistantProvider {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableAiAssistantProvider.class);

    private final IntegrationProperties properties;
    private final RestClient restClient;

    public ConfigurableAiAssistantProvider(IntegrationProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public String providerName() {
        return properties.getAi().getProvider();
    }

    @Override
    public boolean isConfigured() {
        return properties.getAi().isConfigured()
                && properties.getAi().getApiUrl() != null
                && !properties.getAi().getApiUrl().isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProviderReply generateReply(
            String question,
            String farmContextSummary,
            List<CropKnowledgeBaseEntity> matchedKnowledge) {
        if (isConfigured()) {
            try {
                String systemPrompt = "You are FarmWise Agricultural Assistant for Indian farmers. "
                        + "Answer conservatively based on agronomic principles, clearly mention uncertainty, "
                        + "never guarantee harvest or financial outcomes, and recommend verified agricultural experts "
                        + "for chemical or disease diagnosis. Authorized farm context: " + farmContextSummary;

                Map<String, Object> body = Map.of(
                        "model", properties.getAi().getModel().isBlank() ? "gpt-4o-mini" : properties.getAi().getModel(),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", question)));

                Map<String, Object> response = restClient.post()
                        .uri(properties.getAi().getApiUrl())
                        .header("Authorization", "Bearer " + properties.getAi().getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

                if (response != null && response.get("choices") instanceof List<?> choices && !choices.isEmpty()) {
                    Object first = choices.get(0);
                    if (first instanceof Map<?, ?> choice && choice.get("message") instanceof Map<?, ?> msg) {
                        Object content = msg.get("content");
                        if (content != null && !String.valueOf(content).isBlank()) {
                            return new ProviderReply(String.valueOf(content), true, providerName());
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Configured AI provider call failed; falling back to agronomic reference guidance: {}",
                        ex.getMessage());
            }
        }

        return new ProviderReply(
                buildDeterministicReferenceGuidance(question, farmContextSummary, matchedKnowledge),
                false,
                "agronomic-reference-fallback");
    }

    private String buildDeterministicReferenceGuidance(
            String question, String farmContextSummary, List<CropKnowledgeBaseEntity> matchedKnowledge) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Non-AI Agronomic Reference Mode — External AI provider credentials are not configured]\n\n");
        if (farmContextSummary != null && !farmContextSummary.isBlank()) {
            sb.append("Authorized Farm Context: ").append(farmContextSummary).append("\n\n");
        }
        if (!matchedKnowledge.isEmpty()) {
            sb.append("Relevant Crop Knowledge Base Guidance:\n");
            for (CropKnowledgeBaseEntity kb : matchedKnowledge) {
                sb.append("- **").append(kb.getCropName()).append("** (")
                        .append(kb.getSowingSeason()).append(" | Ideal pH: ").append(kb.getIdealPhRange()).append(")\n")
                        .append("  • Soils: ").append(kb.getSuitableSoilTypes()).append("\n")
                        .append("  • Irrigation: ").append(kb.getIrrigationSchedule()).append("\n")
                        .append("  • Nutrients: ").append(kb.getNutrientGuidance()).append("\n")
                        .append("  • Pests/Diseases: ").append(kb.getCommonPestsDiseases()).append("\n");
            }
        } else {
            String lower = question.toLowerCase();
            sb.append("Practical Reference Checklist:\n");
            if (lower.contains("irrigat") || lower.contains("water")) {
                sb.append("• Check soil moisture at root zone depth (10-15 cm) before irrigating.\n")
                        .append("• Prefer early morning or evening irrigation and review live Weather forecasts before watering.\n");
            }
            if (lower.contains("fertiliz") || lower.contains("npk") || lower.contains("urea")) {
                sb.append("• Base NPK dosage on an accredited Soil Health Card or lab test rather than blanket application.\n")
                        .append("• Split nitrogen doses around active vegetative stages to reduce leaching.\n");
            }
            if (lower.contains("pest") || lower.contains("disease") || lower.contains("leaf") || lower.contains("blight")) {
                sb.append("• Log symptoms in Soil & Crop Intelligence and inspect underside of leaves for vectors.\n")
                        .append("• Avoid spraying during high wind or imminent rain, and verify chemical labels with a certified agronomist.\n");
            }
            if (sb.toString().endsWith("Checklist:\n")) {
                sb.append("• Use the Crop Knowledge Base for soil pH, irrigation schedules, and pest checklists.\n")
                        .append("• Connect with a verified FarmWise Expert in the Experts portal for field-specific recommendations.\n");
            }
        }
        sb.append("\nAdvisory Note: FarmWise does not guarantee crop yields or financial outcomes. "
                + "Consult a verified agricultural expert or local Krishi Vigyan Kendra (KVK) before field interventions.");
        return sb.toString();
    }
}
