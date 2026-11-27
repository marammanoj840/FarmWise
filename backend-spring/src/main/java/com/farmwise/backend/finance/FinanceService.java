package com.farmwise.backend.finance;

import com.farmwise.backend.common.ExportUtils;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.crop.CropService;
import com.farmwise.backend.crop.FarmCropEntity;
import com.farmwise.backend.crop.FarmCropRepository;
import com.farmwise.backend.cultivation.CultivationActivityEntity;
import com.farmwise.backend.cultivation.CultivationActivityRepository;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmLocationRepository;
import com.farmwise.backend.farm.FarmRepository;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.finance.FinanceDtos.CropFinancialSummaryItem;
import com.farmwise.backend.finance.FinanceDtos.ExpenseRequest;
import com.farmwise.backend.finance.FinanceDtos.ExpenseResponse;
import com.farmwise.backend.finance.FinanceDtos.FarmFinancialSummaryItem;
import com.farmwise.backend.finance.FinanceDtos.FinanceSummaryResponse;
import com.farmwise.backend.finance.FinanceDtos.IncomeRequest;
import com.farmwise.backend.finance.FinanceDtos.IncomeResponse;
import com.farmwise.backend.inventory.InventoryItemEntity;
import com.farmwise.backend.inventory.InventoryItemRepository;
import com.farmwise.backend.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    public static final String FINANCIAL_DISCLAIMER =
            "Financial summaries and profit/loss figures reflect recorded transactions and operational estimates only. "
                    + "They do not guarantee agricultural or financial outcomes.";

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final FarmRepository farmRepository;
    private final FarmLocationRepository farmLocationRepository;
    private final FarmCropRepository farmCropRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final CultivationActivityRepository activityRepository;
    private final FarmService farmService;
    private final CropService cropService;
    private final Clock utcClock;

    public FinanceService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            FarmRepository farmRepository,
            FarmLocationRepository farmLocationRepository,
            FarmCropRepository farmCropRepository,
            InventoryItemRepository inventoryItemRepository,
            CultivationActivityRepository activityRepository,
            FarmService farmService,
            CropService cropService,
            Clock utcClock) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.farmRepository = farmRepository;
        this.farmLocationRepository = farmLocationRepository;
        this.farmCropRepository = farmCropRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.activityRepository = activityRepository;
        this.farmService = farmService;
        this.cropService = cropService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listExpenses(int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<ExpenseEntity> expenses = expenseRepository.findByFarmerIdOrderByExpenseDateDescCreatedAtDesc(
                farmerId, pageable);
        Map<UUID, String> farms = farmNameMap(farmerId);
        Map<UUID, String> crops = cropNameMap(farmerId);
        return PagedResponse.from(expenses.map(e -> toExpenseResponse(e, farms, crops)));
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        if (req.farmCropId() != null) {
            cropService.requireOwnedFarmCrop(req.farmCropId());
        }
        ExpenseEntity entity = new ExpenseEntity();
        entity.setFarmerId(farmerId);
        entity.setFarmId(req.farmId());
        entity.setFarmCropId(req.farmCropId());
        entity.setCategory(req.category().trim().toUpperCase());
        entity.setTitle(req.title().trim());
        entity.setAmountInr(req.amountInr());
        entity.setCurrency("INR");
        entity.setExpenseDate(req.expenseDate());
        entity.setVendorName(req.vendorName());
        entity.setPaymentMethod(req.paymentMethod());
        entity.setNotes(req.notes());
        entity = expenseRepository.save(entity);
        return toExpenseResponse(entity, farmNameMap(farmerId), cropNameMap(farmerId));
    }

    @Transactional
    public MessageResponse deleteExpense(UUID id) {
        ExpenseEntity entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found."));
        SecurityUtils.assertOwnerOrAdmin(entity.getFarmerId(), "expense");
        expenseRepository.delete(entity);
        return new MessageResponse("Expense deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public PagedResponse<IncomeResponse> listIncome(int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<IncomeEntity> incomePage = incomeRepository.findByFarmerIdOrderByIncomeDateDescCreatedAtDesc(
                farmerId, pageable);
        Map<UUID, String> farms = farmNameMap(farmerId);
        Map<UUID, String> crops = cropNameMap(farmerId);
        return PagedResponse.from(incomePage.map(i -> toIncomeResponse(i, farms, crops)));
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        if (req.farmCropId() != null) {
            cropService.requireOwnedFarmCrop(req.farmCropId());
        }
        IncomeEntity entity = new IncomeEntity();
        entity.setFarmerId(farmerId);
        entity.setFarmId(req.farmId());
        entity.setFarmCropId(req.farmCropId());
        entity.setCategory(req.category().trim().toUpperCase());
        entity.setTitle(req.title().trim());
        entity.setAmountInr(req.amountInr());
        entity.setCurrency("INR");
        entity.setIncomeDate(req.incomeDate());
        entity.setBuyerName(req.buyerName());
        entity.setQuantitySold(req.quantitySold());
        entity.setUnit(req.unit());
        entity.setNotes(req.notes());
        entity = incomeRepository.save(entity);
        return toIncomeResponse(entity, farmNameMap(farmerId), cropNameMap(farmerId));
    }

    @Transactional
    public MessageResponse deleteIncome(UUID id) {
        IncomeEntity entity = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income record not found."));
        SecurityUtils.assertOwnerOrAdmin(entity.getFarmerId(), "income");
        incomeRepository.delete(entity);
        return new MessageResponse("Income record deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary() {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        List<ExpenseEntity> expenses = expenseRepository.findByFarmerIdOrderByExpenseDateDesc(farmerId);
        List<IncomeEntity> incomes = incomeRepository.findByFarmerIdOrderByIncomeDateDesc(farmerId);
        Map<UUID, String> farms = farmNameMap(farmerId);
        Map<UUID, String> crops = cropNameMap(farmerId);

        BigDecimal totalExpenses = expenses.stream()
                .map(ExpenseEntity::getAmountInr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIncome = incomes.stream()
                .map(IncomeEntity::getAmountInr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expByCategory = new LinkedHashMap<>();
        for (ExpenseEntity e : expenses) {
            expByCategory.merge(e.getCategory(), e.getAmountInr(), BigDecimal::add);
        }

        Map<String, BigDecimal> incByCategory = new LinkedHashMap<>();
        for (IncomeEntity i : incomes) {
            incByCategory.merge(i.getCategory(), i.getAmountInr(), BigDecimal::add);
        }

        List<FarmFinancialSummaryItem> byFarm = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : farms.entrySet()) {
            UUID fId = entry.getKey();
            BigDecimal fInc = incomes.stream()
                    .filter(i -> fId.equals(i.getFarmId()))
                    .map(IncomeEntity::getAmountInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal fExp = expenses.stream()
                    .filter(e -> fId.equals(e.getFarmId()))
                    .map(ExpenseEntity::getAmountInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            byFarm.add(new FarmFinancialSummaryItem(fId, entry.getValue(), fInc, fExp, fInc.subtract(fExp)));
        }

        List<CropFinancialSummaryItem> byCrop = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : crops.entrySet()) {
            UUID cId = entry.getKey();
            BigDecimal cInc = incomes.stream()
                    .filter(i -> cId.equals(i.getFarmCropId()))
                    .map(IncomeEntity::getAmountInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cExp = expenses.stream()
                    .filter(e -> cId.equals(e.getFarmCropId()))
                    .map(ExpenseEntity::getAmountInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            byCrop.add(new CropFinancialSummaryItem(cId, entry.getValue(), cInc, cExp, cInc.subtract(cExp)));
        }

        return new FinanceSummaryResponse(
                "INR",
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses),
                expByCategory,
                incByCategory,
                byFarm,
                byCrop,
                FINANCIAL_DISCLAIMER);
    }

    @Transactional(readOnly = true)
    public byte[] exportReport(String reportType, String format) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        String type = reportType.trim().toUpperCase();
        String fmt = format.trim().toUpperCase();
        if (!"CSV".equals(fmt) && !"PDF".equals(fmt)) {
            throw new BusinessValidationException("Report format must be CSV or PDF.");
        }

        List<String> headers;
        List<List<String>> rows = new ArrayList<>();
        String title;
        String subtitle;
        String disclaimer = FINANCIAL_DISCLAIMER;

        Map<UUID, String> farms = farmNameMap(farmerId);
        Map<UUID, String> crops = cropNameMap(farmerId);

        switch (type) {
            case "EXPENSES" -> {
                title = "Expense Report";
                subtitle = "Recorded farm expenses (INR)";
                headers = List.of("Date", "Title", "Category", "Farm", "Crop", "Vendor", "Amount (INR)");
                for (ExpenseEntity e : expenseRepository.findByFarmerIdOrderByExpenseDateDesc(farmerId)) {
                    rows.add(List.of(
                            e.getExpenseDate().toString(),
                            e.getTitle(),
                            e.getCategory(),
                            e.getFarmId() != null ? farms.getOrDefault(e.getFarmId(), "-") : "-",
                            e.getFarmCropId() != null ? crops.getOrDefault(e.getFarmCropId(), "-") : "-",
                            str(e.getVendorName()),
                            e.getAmountInr().toPlainString()));
                }
            }
            case "INCOME", "FINANCE_PNL" -> {
                title = "Profit & Loss Ledger";
                subtitle = "Income and expense records (INR)";
                headers = List.of("Date", "Type", "Category", "Title", "Farm", "Crop", "Amount (INR)");
                for (IncomeEntity i : incomeRepository.findByFarmerIdOrderByIncomeDateDesc(farmerId)) {
                    rows.add(List.of(
                            i.getIncomeDate().toString(),
                            "INCOME",
                            i.getCategory(),
                            i.getTitle(),
                            i.getFarmId() != null ? farms.getOrDefault(i.getFarmId(), "-") : "-",
                            i.getFarmCropId() != null ? crops.getOrDefault(i.getFarmCropId(), "-") : "-",
                            "+" + i.getAmountInr().toPlainString()));
                }
                for (ExpenseEntity e : expenseRepository.findByFarmerIdOrderByExpenseDateDesc(farmerId)) {
                    rows.add(List.of(
                            e.getExpenseDate().toString(),
                            "EXPENSE",
                            e.getCategory(),
                            e.getTitle(),
                            e.getFarmId() != null ? farms.getOrDefault(e.getFarmId(), "-") : "-",
                            e.getFarmCropId() != null ? crops.getOrDefault(e.getFarmCropId(), "-") : "-",
                            "-" + e.getAmountInr().toPlainString()));
                }
            }
            case "FARMS" -> {
                title = "Farm Registry Report";
                subtitle = "Registered farm holdings and infrastructure";
                disclaimer = "Operational farm data recorded by the account holder.";
                headers = List.of("Farm Name", "Area", "Unit", "Soil Type", "Irrigation", "District / State");
                for (FarmEntity f : farmRepository.findByFarmerId(farmerId)) {
                    String loc = farmLocationRepository.findByFarmId(f.getId())
                            .map(l -> l.getDistrict() + ", " + l.getState())
                            .orElse("-");
                    rows.add(List.of(
                            f.getName(),
                            String.valueOf(f.getLandArea()),
                            f.getAreaUnit(),
                            f.getSoilType(),
                            f.getIrrigationType(),
                            loc));
                }
            }
            case "CROPS", "HARVEST" -> {
                title = "CROPS".equals(type) ? "Crop Cultivation Report" : "Harvest Output Report";
                subtitle = "Crop cycles, stages, and yield figures";
                disclaimer = "Expected yields are operational estimates; actual harvests reflect recorded entries.";
                headers = List.of(
                        "Farm", "Crop", "Variety", "Season", "Area", "Stage", "Status",
                        "Expected Yield (kg)", "Actual Yield (kg)");
                for (FarmCropEntity fc : farmCropRepository.findByFarmerId(farmerId)) {
                    rows.add(List.of(
                            farms.getOrDefault(fc.getFarmId(), "-"),
                            fc.getCropName(),
                            str(fc.getVarietyName()),
                            fc.getSeason(),
                            fc.getPlantedArea() + " " + fc.getAreaUnit(),
                            fc.getGrowthStage(),
                            fc.getCultivationStatus(),
                            fc.getExpectedYieldKg() != null ? String.valueOf(fc.getExpectedYieldKg()) : "-",
                            fc.getActualYieldKg() != null ? String.valueOf(fc.getActualYieldKg()) : "-"));
                }
            }
            case "INVENTORY" -> {
                title = "Inventory & Resource Report";
                subtitle = "Current stock, reorder thresholds, and unit valuation (INR)";
                disclaimer = "Inventory quantities reflect recorded purchases and field usage.";
                headers = List.of("Item", "Category", "Quantity", "Unit", "Reorder Threshold", "Unit Cost (INR)", "Status");
                for (InventoryItemEntity item : inventoryItemRepository.findByFarmerId(farmerId)) {
                    boolean low = item.getReorderThreshold() > 0 && item.getQuantity() <= item.getReorderThreshold();
                    rows.add(List.of(
                            item.getName(),
                            item.getCategory(),
                            String.valueOf(item.getQuantity()),
                            item.getUnit(),
                            String.valueOf(item.getReorderThreshold()),
                            item.getUnitCostInr().toPlainString(),
                            low ? "LOW STOCK" : "OK"));
                }
            }
            case "ACTIVITIES" -> {
                title = "Cultivation Activity Report";
                subtitle = "Logged field operations and direct costs (INR)";
                disclaimer = "Activity logs reflect farmer-entered operations.";
                headers = List.of("Date", "Farm", "Stage", "Activity", "Input Used", "Cost (INR)");
                for (CultivationActivityEntity act : activityRepository.findByFarmerIdOrderByActivityDateDesc(farmerId)) {
                    rows.add(List.of(
                            act.getActivityDate().toString(),
                            farms.getOrDefault(act.getFarmId(), "-"),
                            act.getStage(),
                            act.getTitle(),
                            str(act.getInputUsed()),
                            act.getCostAmount().toPlainString()));
                }
            }
            default -> throw new BusinessValidationException(
                    "Unsupported report type. Choose EXPENSES, INCOME, FINANCE_PNL, FARMS, CROPS, INVENTORY, HARVEST, or ACTIVITIES.");
        }

        return "CSV".equals(fmt)
                ? ExportUtils.toCsvBytes(headers, rows)
                : ExportUtils.toPdfBytes(title, subtitle, headers, rows, disclaimer);
    }

    private Map<UUID, String> farmNameMap(UUID farmerId) {
        return farmRepository.findByFarmerId(farmerId).stream()
                .collect(Collectors.toMap(FarmEntity::getId, FarmEntity::getName, (a, b) -> a));
    }

    private Map<UUID, String> cropNameMap(UUID farmerId) {
        return farmCropRepository.findByFarmerId(farmerId).stream()
                .collect(Collectors.toMap(FarmCropEntity::getId, FarmCropEntity::getCropName, (a, b) -> a));
    }

    private ExpenseResponse toExpenseResponse(
            ExpenseEntity e, Map<UUID, String> farms, Map<UUID, String> crops) {
        return new ExpenseResponse(
                e.getId(),
                e.getFarmId(),
                e.getFarmId() != null ? farms.get(e.getFarmId()) : null,
                e.getFarmCropId(),
                e.getFarmCropId() != null ? crops.get(e.getFarmCropId()) : null,
                e.getCategory(),
                e.getTitle(),
                e.getAmountInr(),
                e.getCurrency(),
                e.getExpenseDate(),
                e.getVendorName(),
                e.getPaymentMethod(),
                e.getNotes(),
                e.getCreatedAt());
    }

    private IncomeResponse toIncomeResponse(
            IncomeEntity i, Map<UUID, String> farms, Map<UUID, String> crops) {
        return new IncomeResponse(
                i.getId(),
                i.getFarmId(),
                i.getFarmId() != null ? farms.get(i.getFarmId()) : null,
                i.getFarmCropId(),
                i.getFarmCropId() != null ? crops.get(i.getFarmCropId()) : null,
                i.getCategory(),
                i.getTitle(),
                i.getAmountInr(),
                i.getCurrency(),
                i.getIncomeDate(),
                i.getBuyerName(),
                i.getQuantitySold(),
                i.getUnit(),
                i.getNotes(),
                i.getCreatedAt());
    }

    private static String str(String v) {
        return v == null || v.isBlank() ? "-" : v;
    }
}
