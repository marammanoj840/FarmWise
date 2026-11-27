package com.farmwise.backend.marketprice;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceRepository extends JpaRepository<MarketPriceEntity, UUID> {
    List<MarketPriceEntity> findAllByOrderByPriceDateDescCommodityAsc();

    List<MarketPriceEntity> findByCommodityContainingIgnoreCaseOrderByPriceDateAsc(String commodity);
}
