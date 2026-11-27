package com.farmwise.backend.assistant;

import com.farmwise.backend.assistant.AiAssistantProvider.ProviderReply;
import com.farmwise.backend.assistant.AssistantDtos.AskAssistantRequest;
import com.farmwise.backend.assistant.AssistantDtos.AskAssistantResponse;
import com.farmwise.backend.assistant.AssistantDtos.AssistantStatusResponse;
import com.farmwise.backend.assistant.AssistantDtos.ConversationSummary;
import com.farmwise.backend.assistant.AssistantDtos.MessageItem;
import com.farmwise.backend.crop.FarmCropEntity;
import com.farmwise.backend.crop.FarmCropRepository;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.soil.CropKnowledgeBaseEntity;
import com.farmwise.backend.soil.CropKnowledgeBaseRepository;
import com.farmwise.backend.soil.SoilRecordEntity;
import com.farmwise.backend.soil.SoilRecordRepository;
import com.farmwise.backend.weather.WeatherRecordEntity;
import com.farmwise.backend.weather.WeatherRecordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistantService {

    private final AssistantConversationRepository conversationRepository;
    private final AssistantMessageRepository messageRepository;
    private final AiAssistantProvider aiProvider;
    private final FarmService farmService;
    private final FarmCropRepository farmCropRepository;
    private final SoilRecordRepository soilRecordRepository;
    private final WeatherRecordRepository weatherRecordRepository;
    private final CropKnowledgeBaseRepository knowledgeRepository;

    public AssistantService(
            AssistantConversationRepository conversationRepository,
            AssistantMessageRepository messageRepository,
            AiAssistantProvider aiProvider,
            FarmService farmService,
            FarmCropRepository farmCropRepository,
            SoilRecordRepository soilRecordRepository,
            WeatherRecordRepository weatherRecordRepository,
            CropKnowledgeBaseRepository knowledgeRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiProvider = aiProvider;
        this.farmService = farmService;
        this.farmCropRepository = farmCropRepository;
        this.soilRecordRepository = soilRecordRepository;
        this.weatherRecordRepository = weatherRecordRepository;
        this.knowledgeRepository = knowledgeRepository;
    }

    public AssistantStatusResponse getStatus() {
        boolean configured = aiProvider.isConfigured();
        String notice = configured
                ? "External AI provider '" + aiProvider.providerName() + "' is configured."
                : "No external AI API credentials are configured (AI_PROVIDER=none). "
                        + "FarmWise operates in transparent non-AI agronomic reference mode using your farm context and Crop Knowledge Base.";
        return new AssistantStatusResponse(configured, aiProvider.providerName(), notice);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> listConversations() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> new ConversationSummary(
                        c.getId(), c.getFarmId(), c.getTitle(), c.getProviderName(), c.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageItem> getMessages(UUID conversationId) {
        AssistantConversationEntity conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
        SecurityUtils.assertOwnerOrAdmin(conv.getUserId(), "assistant conversation");
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageItem)
                .toList();
    }

    @Transactional
    public AskAssistantResponse ask(AskAssistantRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();

        AssistantConversationEntity conv;
        if (req.conversationId() != null) {
            conv = conversationRepository.findById(req.conversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
            SecurityUtils.assertOwnerOrAdmin(conv.getUserId(), "assistant conversation");
        } else {
            conv = new AssistantConversationEntity();
            conv.setUserId(userId);
            conv.setFarmId(req.farmId());
            String trimmed = req.question().trim();
            conv.setTitle(trimmed.length() > 80 ? trimmed.substring(0, 80) + "..." : trimmed);
            conv.setProviderName(aiProvider.isConfigured() ? aiProvider.providerName() : "agronomic-reference");
            conv = conversationRepository.save(conv);
        }

        UUID contextFarmId = req.farmId() != null ? req.farmId() : conv.getFarmId();
        String contextSummary = buildAuthorizedFarmContext(contextFarmId);

        List<CropKnowledgeBaseEntity> allKb = knowledgeRepository.findAllByOrderByCropNameAsc();
        String lowerQuestion = req.question().toLowerCase();
        String lowerContext = contextSummary.toLowerCase();
        List<CropKnowledgeBaseEntity> matchedKb = allKb.stream()
                .filter(k -> lowerQuestion.contains(k.getCropName().toLowerCase().split(" ")[0])
                        || lowerContext.contains(k.getCropName().toLowerCase().split(" ")[0]))
                .toList();

        AssistantMessageEntity userMsg = new AssistantMessageEntity();
        userMsg.setConversationId(conv.getId());
        userMsg.setSenderRole("USER");
        userMsg.setContent(req.question().trim());
        userMsg.setAiGenerated(false);
        userMsg.setContextSummary(contextSummary);
        userMsg = messageRepository.save(userMsg);

        ProviderReply reply = aiProvider.generateReply(req.question().trim(), contextSummary, matchedKb);

        AssistantMessageEntity assistantMsg = new AssistantMessageEntity();
        assistantMsg.setConversationId(conv.getId());
        assistantMsg.setSenderRole("ASSISTANT");
        assistantMsg.setContent(reply.content());
        assistantMsg.setAiGenerated(reply.aiGenerated());
        assistantMsg.setContextSummary(contextSummary);
        assistantMsg = messageRepository.save(assistantMsg);

        conv.setProviderName(reply.providerUsed());
        conversationRepository.save(conv);

        List<String> topics = matchedKb.stream().map(CropKnowledgeBaseEntity::getCropName).toList();
        return new AskAssistantResponse(
                conv.getId(),
                conv.getTitle(),
                aiProvider.isConfigured(),
                reply.providerUsed(),
                contextSummary,
                toMessageItem(userMsg),
                toMessageItem(assistantMsg),
                topics);
    }

    private String buildAuthorizedFarmContext(UUID farmId) {
        if (farmId == null) {
            return "No specific farm selected.";
        }
        FarmEntity farm = farmService.requireOwnedFarm(farmId);
        List<FarmCropEntity> crops = farmCropRepository.findByFarmId(farmId);
        List<SoilRecordEntity> soils = soilRecordRepository.findByFarmIdOrderByTestDateDesc(farmId);
        List<WeatherRecordEntity> weather = weatherRecordRepository.findTop10ByFarmIdOrderByObservedAtDesc(farmId);

        StringBuilder sb = new StringBuilder();
        sb.append("Farm '").append(farm.getName()).append("' (")
                .append(farm.getLandArea()).append(" ").append(farm.getAreaUnit())
                .append(", Soil: ").append(farm.getSoilType())
                .append(", Irrigation: ").append(farm.getIrrigationType()).append(")");

        if (!crops.isEmpty()) {
            sb.append(" | Crops: ").append(crops.stream()
                    .map(c -> c.getCropName() + " [" + c.getGrowthStage() + "]")
                    .limit(5)
                    .reduce((a, b) -> a + ", " + b).orElse(""));
        }
        if (!soils.isEmpty()) {
            SoilRecordEntity latest = soils.get(0);
            sb.append(" | Latest Soil pH: ").append(latest.getPh());
        }
        if (!weather.isEmpty()) {
            WeatherRecordEntity w = weather.get(0);
            if (w.isLiveData() && w.getTemperatureC() != null) {
                sb.append(" | Recent Weather: ").append(w.getTemperatureC()).append("°C, ")
                        .append(w.getConditionSummary());
            }
        }
        String res = sb.toString();
        return res.length() <= 490 ? res : res.substring(0, 490);
    }

    private MessageItem toMessageItem(AssistantMessageEntity m) {
        return new MessageItem(
                m.getId(),
                m.getSenderRole(),
                m.getContent(),
                m.isAiGenerated(),
                m.getContextSummary(),
                m.getCreatedAt());
    }
}
