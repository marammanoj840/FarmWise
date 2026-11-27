package com.farmwise.backend.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AssistantDtos {

    private AssistantDtos() {
    }

    public record AskAssistantRequest(
            UUID conversationId,
            UUID farmId,
            @NotBlank @Size(max = 2000) String question) {
    }

    public record MessageItem(
            UUID id,
            String senderRole,
            String content,
            boolean aiGenerated,
            String contextSummary,
            OffsetDateTime createdAt) {
    }

    public record ConversationSummary(
            UUID id,
            UUID farmId,
            String title,
            String providerName,
            OffsetDateTime updatedAt) {
    }

    public record AssistantStatusResponse(
            boolean aiProviderConfigured,
            String configuredProvider,
            String notice) {
    }

    public record AskAssistantResponse(
            UUID conversationId,
            String title,
            boolean aiProviderConfigured,
            String providerName,
            String contextSummary,
            MessageItem userMessage,
            MessageItem assistantMessage,
            List<String> referenceTopics) {
    }
}
