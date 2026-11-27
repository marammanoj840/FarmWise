package com.farmwise.backend.marketprice;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketPriceService {

    private final MarketPriceRepository priceRepository;
    private final IntegrationProperties properties;

    public MarketPriceService(MarketPriceRepository priceRepository, IntegrationProperties properties) {
        this.priceRepository = priceRepository;
        this.properties = properties;
    }

    public record MarketPriceItem(
            UUID id,
            String commodity,
            String variety,
            String marketName,
            String district,
            String state,
            BigDecimal minPriceInr,
            BigDecimal maxPriceInr,
            BigDecimal modalPriceInr,
            String unit,
            LocalDate priceDate,
            String dataSource,
            boolean sampleData,
            OffsetDateTime fetchedAt) {
    }

    public record MarketPriceFeedResponse(
            boolean liveFeedConfigured,
            String providerName,
            boolean containsSampleData,
            String notice,
            List<String> commodities,
            List<String> markets,
            List<MarketPriceItem> prices) {
    }

    @Transactional(readOnly = true)
    public MarketPriceFeedResponse searchPrices(String commodity, String state, String market) {
        List<MarketPriceEntity> all = priceRepository.findAllByOrderByPriceDateDescCommodityAsc();

        List<String> commodities = all.stream().map(MarketPriceEntity::getCommodity).distinct().sorted().toList();
        List<String> markets = all.stream().map(MarketPriceEntity::getMarketName).distinct().sorted().toList();

        List<MarketPriceItem> filtered = all.stream()
                .filter(p -> commodity == null || commodity.isBlank()
                        || p.getCommodity().toLowerCase().contains(commodity.trim().toLowerCase()))
                .filter(p -> state == null || state.isBlank()
                        || p.getState().toLowerCase().contains(state.trim().toLowerCase()))
                .filter(p -> market == null || market.isBlank()
                        || p.getMarketName().toLowerCase().contains(market.trim().toLowerCase()))
                .map(this::toItem)
                .toList();

        boolean liveConfigured = properties.getMarket().isLiveConfigured();
        boolean hasSample = filtered.stream().anyMatch(MarketPriceItem::sampleData);
        String notice = liveConfigured
                ? "Connected to configured market feed adapter: " + properties.getMarket().getProvider()
                : "External Agmarknet / eNAM API credentials are not configured. "
                        + "Displaying clearly labeled SAMPLE reference mandi prices for demonstration and planning layout.";

        return new MarketPriceFeedResponse(
                liveConfigured,
                properties.getMarket().getProvider(),
                hasSample || !liveConfigured,
                notice,
                commodities,
                markets,
                filtered);
    }

    private MarketPriceItem toItem(MarketPriceEntity e) {
        return new MarketPriceItem(
                e.getId(),
                e.getCommodity(),
                e.getVariety(),
                e.getMarketName(),
                e.getDistrict(),
                e.getState(),
                e.getMinPriceInr(),
                e.getMaxPriceInr(),
                e.getModalPriceInr(),
                e.getUnit(),
                e.getPriceDate(),
                e.getDataSource(),
                e.isSampleData(),
                e.getFetchedAt());
    }
}
