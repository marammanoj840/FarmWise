package com.farmwise.backend.assistant;

import com.farmwise.backend.assistant.AssistantDtos.AskAssistantRequest;
import com.farmwise.backend.assistant.AssistantDtos.AskAssistantResponse;
import com.farmwise.backend.assistant.AssistantDtos.AssistantStatusResponse;
import com.farmwise.backend.assistant.AssistantDtos.ConversationSummary;
import com.farmwise.backend.assistant.AssistantDtos.MessageItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "AI Agricultural Assistant", description = "Provider-independent agricultural Q&A with authorized farm context")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/status")
    @Operation(summary = "Check AI provider configuration status")
    public ResponseEntity<AssistantStatusResponse> status() {
        return ResponseEntity.ok(assistantService.getStatus());
    }

    @GetMapping("/conversations")
    @Operation(summary = "List user assistant conversations")
    public ResponseEntity<List<ConversationSummary>> conversations() {
        return ResponseEntity.ok(assistantService.listConversations());
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Get message history for a conversation")
    public ResponseEntity<List<MessageItem>> messages(@PathVariable UUID id) {
        return ResponseEntity.ok(assistantService.getMessages(id));
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask a cultivation, crop, irrigation, fertilizer, or pest question")
    public ResponseEntity<AskAssistantResponse> ask(@Valid @RequestBody AskAssistantRequest request) {
        return ResponseEntity.ok(assistantService.ask(request));
    }
}
