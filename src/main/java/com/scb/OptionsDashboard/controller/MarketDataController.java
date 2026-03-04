/**
 * MarketDataController.java
 *
 * REST controller that exposes live market data from Yahoo Finance via HTTP.
 *
 * WHAT IT DOES:
 * When the user types a ticker symbol on the pricing form, the frontend
 * calls GET /api/market/AAPL and this controller returns the current
 * spot price and implied volatility to auto-populate the form fields.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.controller;

import com.scb.OptionsDashboard.service.MarketDataService;
import com.scb.OptionsDashboard.service.MarketDataService.MarketDataException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Fetches spot price and implied volatility for a ticker.
     *
     * @PathVariable extracts {ticker} from the URL path.
     * e.g. GET /api/market/AAPL → fetches AAPL's current price.
     *
     * Example response:
     * {
     *   "ticker": "AAPL",
     *   "spotPrice": 175.23,
     *   "impliedVolatility": 0.20
     * }
     *
     * @param  ticker the stock ticker symbol from the URL
     * @return        200 OK with market data
     */
    @GetMapping("/{ticker}")
    public ResponseEntity<MarketDataResponse> getMarketData(@PathVariable String ticker) {
        double[] inputs = marketDataService.getMarketInputs(ticker);
        return ResponseEntity.ok(new MarketDataResponse(
                ticker.toUpperCase(),
                inputs[0],  // spot price
                inputs[1]   // implied volatility
        ));
    }

    /**
     * Catches MarketDataException and returns 404 instead of 500.
     * e.g. if the user types an invalid ticker like "XYZXYZ".
     */
    @ExceptionHandler(MarketDataException.class)
    public ResponseEntity<String> handleMarketDataError(MarketDataException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    /**
     * Response DTO — wraps the market data fields into a JSON object.
     * Java record = immutable class with auto-generated constructor,
     * getters and toString. Cleaner than a full class for simple DTOs.
     */
    public record MarketDataResponse(
            String ticker,
            double spotPrice,
            double impliedVolatility
    ) {}
}