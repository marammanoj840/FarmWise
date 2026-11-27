package com.farmwise.backend.assistant;

import com.farmwise.backend.soil.CropKnowledgeBaseEntity;
import java.util.List;

public interface AiAssistantProvider {
    String providerName();

    boolean isConfigured();

    ProviderReply generateReply(
            String question,
            String farmContextSummary,
            List<CropKnowledgeBaseEntity> matchedKnowledge);

    record ProviderReply(
            String content,
            boolean aiGenerated,
            String providerUsed) {
    }
}
