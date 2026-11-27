package com.farmwise.backend.inventory;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.inventory.InventoryDtos.InventoryItemRequest;
import com.farmwise.backend.inventory.InventoryDtos.InventoryItemResponse;
import com.farmwise.backend.inventory.InventoryDtos.InventoryTransactionRequest;
import com.farmwise.backend.inventory.InventoryDtos.InventoryTransactionResponse;
import com.farmwise.backend.notification.NotificationService;
import com.farmwise.backend.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryTransactionRepository txRepository;
    private final FarmService farmService;
    private final NotificationService notificationService;
    private final Clock utcClock;

    public InventoryService(
            InventoryItemRepository itemRepository,
            InventoryTransactionRepository txRepository,
            FarmService farmService,
            NotificationService notificationService,
            Clock utcClock) {
        this.itemRepository = itemRepository;
        this.txRepository = txRepository;
        this.farmService = farmService;
        this.notificationService = notificationService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryItemResponse> list(String category, int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<InventoryItemEntity> items = (category != null && !category.isBlank())
                ? itemRepository.findByFarmerIdAndCategoryIgnoreCaseOrderByUpdatedAtDesc(
                        farmerId, category.trim(), pageable)
                : itemRepository.findByFarmerIdOrderByUpdatedAtDesc(farmerId, pageable);
        return PagedResponse.from(items.map(this::toItemResponse));
    }

    @Transactional
    public InventoryItemResponse create(InventoryItemRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        InventoryItemEntity item = new InventoryItemEntity();
        item.setFarmerId(farmerId);
        applyFields(item, req);
        item = itemRepository.save(item);

        if (item.getQuantity() > 0) {
            InventoryTransactionEntity initialTx = new InventoryTransactionEntity();
            initialTx.setInventoryItemId(item.getId());
            initialTx.setFarmerId(farmerId);
            initialTx.setTransactionType("PURCHASE");
            initialTx.setQuantityDelta(item.getQuantity());
            initialTx.setUnitCostInr(item.getUnitCostInr());
            initialTx.setReferenceNote("Initial stock entry");
            initialTx.setTransactionDate(LocalDate.now(utcClock));
            txRepository.save(initialTx);
        }
        checkAndAlertLowStock(item);
        return toItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse update(UUID id, InventoryItemRequest req) {
        InventoryItemEntity item = requireOwnedItem(id);
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        applyFields(item, req);
        item = itemRepository.save(item);
        checkAndAlertLowStock(item);
        return toItemResponse(item);
    }

    @Transactional
    public MessageResponse delete(UUID id) {
        InventoryItemEntity item = requireOwnedItem(id);
        itemRepository.delete(item);
        return new MessageResponse("Inventory item deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional
    public InventoryItemResponse recordTransaction(UUID id, InventoryTransactionRequest req) {
        InventoryItemEntity item = requireOwnedItem(id);
        String type = req.transactionType().trim().toUpperCase();
        double qty = req.quantity();
        double delta;
        if ("PURCHASE".equals(type) || "RESTOCK".equals(type)) {
            delta = qty;
        } else if ("USAGE".equals(type) || "CONSUMPTION".equals(type)) {
            if (item.getQuantity() < qty) {
                throw new BusinessValidationException(
                        "Insufficient inventory stock. Available: " + item.getQuantity() + " " + item.getUnit());
            }
            delta = -qty;
        } else {
            throw new BusinessValidationException("Transaction type must be PURCHASE or USAGE.");
        }

        item.setQuantity(Math.round((item.getQuantity() + delta) * 1000.0) / 1000.0);
        if (req.unitCostInr() != null && "PURCHASE".equals(type)) {
            item.setUnitCostInr(req.unitCostInr());
        }
        item = itemRepository.save(item);

        InventoryTransactionEntity tx = new InventoryTransactionEntity();
        tx.setInventoryItemId(item.getId());
        tx.setFarmerId(item.getFarmerId());
        tx.setTransactionType(type);
        tx.setQuantityDelta(delta);
        tx.setUnitCostInr(req.unitCostInr() != null ? req.unitCostInr() : item.getUnitCostInr());
        tx.setReferenceNote(req.referenceNote());
        tx.setTransactionDate(req.transactionDate() != null ? req.transactionDate() : LocalDate.now(utcClock));
        txRepository.save(tx);

        checkAndAlertLowStock(item);
        return toItemResponse(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getHistory(UUID itemId) {
        requireOwnedItem(itemId);
        return txRepository.findByInventoryItemIdOrderByTransactionDateDescCreatedAtDesc(itemId).stream()
                .map(tx -> new InventoryTransactionResponse(
                        tx.getId(),
                        tx.getInventoryItemId(),
                        tx.getTransactionType(),
                        tx.getQuantityDelta(),
                        tx.getUnitCostInr(),
                        tx.getReferenceNote(),
                        tx.getTransactionDate(),
                        tx.getCreatedAt()))
                .toList();
    }

    private InventoryItemEntity requireOwnedItem(UUID id) {
        InventoryItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found."));
        SecurityUtils.assertOwnerOrAdmin(item.getFarmerId(), "inventory item");
        return item;
    }

    private void applyFields(InventoryItemEntity item, InventoryItemRequest req) {
        item.setFarmId(req.farmId());
        item.setName(req.name().trim());
        item.setCategory(req.category().trim().toUpperCase());
        item.setSku(req.sku());
        item.setQuantity(req.quantity());
        item.setUnit(req.unit().trim());
        item.setReorderThreshold(req.reorderThreshold());
        item.setUnitCostInr(req.unitCostInr() != null ? req.unitCostInr() : BigDecimal.ZERO);
        item.setSupplierName(req.supplierName());
        item.setNotes(req.notes());
    }

    private void checkAndAlertLowStock(InventoryItemEntity item) {
        if (item.getReorderThreshold() > 0 && item.getQuantity() <= item.getReorderThreshold()) {
            notificationService.notify(
                    item.getFarmerId(),
                    "INVENTORY_ALERT",
                    "Low Stock: " + item.getName(),
                    item.getName() + " has " + item.getQuantity() + " " + item.getUnit()
                            + " remaining (threshold " + item.getReorderThreshold() + ").",
                    "/app/inventory");
        }
    }

    public InventoryItemResponse toItemResponse(InventoryItemEntity i) {
        boolean lowStock = i.getReorderThreshold() > 0 && i.getQuantity() <= i.getReorderThreshold();
        return new InventoryItemResponse(
                i.getId(),
                i.getFarmId(),
                i.getName(),
                i.getCategory(),
                i.getSku(),
                i.getQuantity(),
                i.getUnit(),
                i.getReorderThreshold(),
                lowStock,
                i.getUnitCostInr(),
                i.getSupplierName(),
                i.getNotes(),
                i.getUpdatedAt());
    }
}
