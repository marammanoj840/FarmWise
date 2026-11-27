package com.farmwise.backend.assistant;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessageEntity, UUID> {
    List<AssistantMessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
