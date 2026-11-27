package com.farmwise.backend.marketprice;

import com.farmwise.backend.marketprice.MarketPriceService.MarketPriceFeedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/market-prices")
@Tag(name = "Market Price Intelligence", description = "Mandi price comparison, historical trends, and Agmarknet/eNAM adapter")
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    public MarketPriceController(MarketPriceService marketPriceService) {
        this.marketPriceService = marketPriceService;
    }

    @GetMapping
    @Operation(summary = "Search crop-wise market prices by commodity, state, or mandi")
    public ResponseEntity<MarketPriceFeedResponse> search(
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String market) {
        return ResponseEntity.ok(marketPriceService.searchPrices(commodity, state, market));
    }
}
