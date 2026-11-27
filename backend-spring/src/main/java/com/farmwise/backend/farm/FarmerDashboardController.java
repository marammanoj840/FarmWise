package com.farmwise.backend.farm;

import com.farmwise.backend.crop.FarmCropEntity;
import com.farmwise.backend.crop.FarmCropRepository;
import com.farmwise.backend.cultivation.CultivationActivityEntity;
import com.farmwise.backend.cultivation.CultivationActivityRepository;
import com.farmwise.backend.cultivation.CultivationTaskEntity;
import com.farmwise.backend.cultivation.CultivationTaskRepository;
import com.farmwise.backend.finance.ExpenseEntity;
import com.farmwise.backend.finance.ExpenseRepository;
import com.farmwise.backend.finance.FinanceService;
import com.farmwise.backend.finance.IncomeEntity;
import com.farmwise.backend.finance.IncomeRepository;
import com.farmwise.backend.inventory.InventoryItemEntity;
import com.farmwise.backend.inventory.InventoryItemRepository;
import com.farmwise.backend.marketprice.MarketPriceService;
import com.farmwise.backend.marketprice.MarketPriceService.MarketPriceItem;
import com.farmwise.backend.notification.NotificationRepository;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.weather.WeatherDtos.WeatherResponse;
import com.farmwise.backend.weather.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farmer")
@Tag(name = "Farmer Dashboard", description = "Aggregated farmer operational overview")
public class FarmerDashboardController {

    private final FarmRepository farmRepository;
    private final FarmCropRepository farmCropRepository;
    private final CultivationActivityRepository activityRepository;
    private final CultivationTaskRepository taskRepository;
    private final InventoryItemRepository inventoryRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final NotificationRepository notificationRepository;
    private final WeatherService weatherService;
    private final MarketPriceService marketPriceService;

    public FarmerDashboardController(
            FarmRepository farmRepository,
            FarmCropRepository farmCropRepository,
            CultivationActivityRepository activityRepository,
            CultivationTaskRepository taskRepository,
            InventoryItemRepository inventoryRepository,
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            NotificationRepository notificationRepository,
            WeatherService weatherService,
            MarketPriceService marketPriceService) {
        this.farmRepository = farmRepository;
        this.farmCropRepository = farmCropRepository;
        this.activityRepository = activityRepository;
        this.taskRepository = taskRepository;
        this.inventoryRepository = inventoryRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.notificationRepository = notificationRepository;
        this.weatherService = weatherService;
        this.marketPriceService = marketPriceService;
    }

    public record DashboardSummaryResponse(
            long totalFarms,
            long activeCrops,
            double totalEstimatedYieldKg,
            long pendingTasksCount,
            long lowStockInventoryCount,
            long unreadNotificationsCount,
            BigDecimal totalExpensesInr,
            BigDecimal totalIncomeInr,
            BigDecimal netBalanceInr,
            String financeDisclaimer,
            WeatherResponse primaryFarmWeather,
            List<CultivationActivityEntity> recentActivities,
            List<CultivationTaskEntity> upcomingTasks,
            List<ExpenseEntity> recentExpenses,
            List<MarketPriceItem> marketPriceHighlights,
            boolean marketPricesAreSample) {
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    @Operation(summary = "Get aggregated farmer portal dashboard metrics")
    public ResponseEntity<DashboardSummaryResponse> dashboard() {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        List<FarmEntity> farms = farmRepository.findByFarmerId(farmerId);
        List<FarmCropEntity> crops = farmCropRepository.findByFarmerId(farmerId);
        long activeCropCount = crops.stream()
                .filter(c -> !"HARVESTED".equalsIgnoreCase(c.getCultivationStatus())
                        && !"CANCELLED".equalsIgnoreCase(c.getCultivationStatus()))
                .count();
        double estYield = crops.stream()
                .filter(c -> c.getExpectedYieldKg() != null)
                .mapToDouble(FarmCropEntity::getExpectedYieldKg)
                .sum();

        List<CultivationTaskEntity> upcomingTasks =
                taskRepository.findTop5ByFarmerIdAndStatusInOrderByDueDateAsc(
                        farmerId, List.of("PENDING", "IN_PROGRESS"));
        long pendingTasksCount =
                taskRepository.countByFarmerIdAndStatusIn(farmerId, List.of("PENDING", "IN_PROGRESS"));

        List<InventoryItemEntity> inventory = inventoryRepository.findByFarmerId(farmerId);
        long lowStockCount = inventory.stream()
                .filter(i -> i.getReorderThreshold() > 0 && i.getQuantity() <= i.getReorderThreshold())
                .count();

        List<ExpenseEntity> allExpenses = expenseRepository.findByFarmerIdOrderByExpenseDateDesc(farmerId);
        List<IncomeEntity> allIncome = incomeRepository.findByFarmerIdOrderByIncomeDateDesc(farmerId);
        BigDecimal totalExp = allExpenses.stream().map(ExpenseEntity::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInc = allIncome.stream().map(IncomeEntity::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CultivationActivityEntity> recentActivities =
                activityRepository.findByFarmerIdOrderByActivityDateDesc(farmerId).stream().limit(5).toList();
        List<ExpenseEntity> recentExpenses =
                expenseRepository.findTop5ByFarmerIdOrderByExpenseDateDescCreatedAtDesc(farmerId);

        long unreadNotifications = notificationRepository.countByUserIdAndReadFalse(farmerId);

        WeatherResponse primaryWeather = farms.isEmpty()
                ? weatherService.lookupLocationWeather("Ludhiana, Punjab", 30.9010, 75.8573)
                : weatherService.getFarmWeather(farms.get(0).getId());

        var priceFeed = marketPriceService.searchPrices(null, null, null);
        List<MarketPriceItem> highlights = priceFeed.prices().stream().limit(5).toList();

        return ResponseEntity.ok(new DashboardSummaryResponse(
                farms.size(),
                activeCropCount,
                estYield,
                pendingTasksCount,
                lowStockCount,
                unreadNotifications,
                totalExp,
                totalInc,
                totalInc.subtract(totalExp),
                FinanceService.FINANCIAL_DISCLAIMER,
                primaryWeather,
                recentActivities,
                upcomingTasks,
                recentExpenses,
                highlights,
                priceFeed.containsSampleData()));
    }
}
