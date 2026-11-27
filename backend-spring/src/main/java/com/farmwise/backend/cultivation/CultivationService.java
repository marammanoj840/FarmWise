package com.farmwise.backend.cultivation;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.crop.CropService;
import com.farmwise.backend.cultivation.CultivationDtos.ActivityRequest;
import com.farmwise.backend.cultivation.CultivationDtos.ActivityResponse;
import com.farmwise.backend.cultivation.CultivationDtos.TaskRequest;
import com.farmwise.backend.cultivation.CultivationDtos.TaskResponse;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CultivationService {

    private final CultivationActivityRepository activityRepository;
    private final CultivationTaskRepository taskRepository;
    private final FarmService farmService;
    private final CropService cropService;
    private final Clock utcClock;

    public CultivationService(
            CultivationActivityRepository activityRepository,
            CultivationTaskRepository taskRepository,
            FarmService farmService,
            CropService cropService,
            Clock utcClock) {
        this.activityRepository = activityRepository;
        this.taskRepository = taskRepository;
        this.farmService = farmService;
        this.cropService = cropService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ActivityResponse> listActivities(UUID farmId, int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<CultivationActivityEntity> activities = farmId != null
                ? activityRepository.findByFarmerIdAndFarmIdOrderByActivityDateDesc(farmerId, farmId, pageable)
                : activityRepository.findByFarmerIdOrderByActivityDateDesc(farmerId, pageable);
        return PagedResponse.from(activities.map(this::toActivityResponse));
    }

    @Transactional
    public ActivityResponse createActivity(ActivityRequest req) {
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        if (req.farmCropId() != null) {
            cropService.requireOwnedFarmCrop(req.farmCropId());
        }
        CultivationActivityEntity entity = new CultivationActivityEntity();
        entity.setFarmId(farm.getId());
        entity.setFarmCropId(req.farmCropId());
        entity.setFarmerId(farm.getFarmerId());
        applyActivityFields(entity, req);
        return toActivityResponse(activityRepository.save(entity));
    }

    @Transactional
    public MessageResponse deleteActivity(UUID id) {
        CultivationActivityEntity entity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivation activity not found."));
        SecurityUtils.assertOwnerOrAdmin(entity.getFarmerId(), "cultivation activity");
        activityRepository.delete(entity);
        return new MessageResponse("Cultivation activity deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> listTasks(String status, int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<CultivationTaskEntity> tasks = (status != null && !status.isBlank())
                ? taskRepository.findByFarmerIdAndStatusOrderByDueDateAsc(
                        farmerId, status.trim().toUpperCase(), pageable)
                : taskRepository.findByFarmerIdOrderByDueDateAsc(farmerId, pageable);
        return PagedResponse.from(tasks.map(this::toTaskResponse));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest req) {
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        if (req.farmCropId() != null) {
            cropService.requireOwnedFarmCrop(req.farmCropId());
        }
        CultivationTaskEntity task = new CultivationTaskEntity();
        task.setFarmId(farm.getId());
        task.setFarmCropId(req.farmCropId());
        task.setFarmerId(farm.getFarmerId());
        applyTaskFields(task, req);
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID id, String rawStatus) {
        CultivationTaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivation task not found."));
        SecurityUtils.assertOwnerOrAdmin(task.getFarmerId(), "cultivation task");
        String status = rawStatus.trim().toUpperCase();
        task.setStatus(status);
        if ("COMPLETED".equals(status) && task.getCompletedAt() == null) {
            task.setCompletedAt(OffsetDateTime.now(utcClock));
        } else if (!"COMPLETED".equals(status)) {
            task.setCompletedAt(null);
        }
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public MessageResponse deleteTask(UUID id) {
        CultivationTaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivation task not found."));
        SecurityUtils.assertOwnerOrAdmin(task.getFarmerId(), "cultivation task");
        taskRepository.delete(task);
        return new MessageResponse("Cultivation task deleted.", OffsetDateTime.now(utcClock));
    }

    private void applyActivityFields(CultivationActivityEntity entity, ActivityRequest req) {
        entity.setStage(req.stage().trim().toUpperCase());
        entity.setTitle(req.title().trim());
        entity.setActivityDate(req.activityDate());
        entity.setInputUsed(req.inputUsed());
        entity.setQuantity(req.quantity());
        entity.setUnit(req.unit());
        entity.setCostAmount(req.costAmount() == null ? BigDecimal.ZERO : req.costAmount());
        entity.setNotes(req.notes());
    }

    private void applyTaskFields(CultivationTaskEntity task, TaskRequest req) {
        task.setStage(req.stage().trim().toUpperCase());
        task.setTitle(req.title().trim());
        task.setDueDate(req.dueDate());
        task.setReminderAt(req.reminderAt());
        task.setPriority(req.priority() != null && !req.priority().isBlank()
                ? req.priority().trim().toUpperCase()
                : "MEDIUM");
        String status = req.status() != null && !req.status().isBlank()
                ? req.status().trim().toUpperCase()
                : "PENDING";
        task.setStatus(status);
        task.setNotes(req.notes());
        if ("COMPLETED".equals(status) && task.getCompletedAt() == null) {
            task.setCompletedAt(OffsetDateTime.now(utcClock));
        }
    }

    private ActivityResponse toActivityResponse(CultivationActivityEntity a) {
        return new ActivityResponse(
                a.getId(),
                a.getFarmId(),
                a.getFarmCropId(),
                a.getStage(),
                a.getTitle(),
                a.getActivityDate(),
                a.getInputUsed(),
                a.getQuantity(),
                a.getUnit(),
                a.getCostAmount(),
                a.getNotes(),
                a.getCreatedAt());
    }

    private TaskResponse toTaskResponse(CultivationTaskEntity t) {
        return new TaskResponse(
                t.getId(),
                t.getFarmId(),
                t.getFarmCropId(),
                t.getStage(),
                t.getTitle(),
                t.getDueDate(),
                t.getReminderAt(),
                t.getPriority(),
                t.getStatus(),
                t.getNotes(),
                t.getCompletedAt(),
                t.getCreatedAt());
    }
}
