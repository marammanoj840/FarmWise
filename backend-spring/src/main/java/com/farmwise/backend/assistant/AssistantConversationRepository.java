package com.farmwise.backend.assistant;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantConversationRepository extends JpaRepository<AssistantConversationEntity, UUID> {
    List<AssistantConversationEntity> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}
