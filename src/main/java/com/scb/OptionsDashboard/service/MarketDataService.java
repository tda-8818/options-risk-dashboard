/**
 * MarketDataService.java
 *
 * Fetches live market data from Alpha Vantage's official REST API.
 * Provides real spot prices for any ticker symbol so the pricing
 * form can be auto-populated with real market data.
 *
 * WHY ALPHA VANTAGE:
 * Free official API with a key — reliable and won't rate limit us
 * like Yahoo Finance does for server-side requests.
 * Free tier gives 25 requests/day, plenty for a demo.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MarketDataService {

    /**
     * Alpha Vantage endpoint for real-time quote data.
     * GLOBAL_QUOTE returns the latest price for a given ticker.
     * %s placeholders are replaced by the API key and ticker at runtime.
     */
    private static final String QUOTE_URL =
            "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s";

    /**
     * Fallback implied volatility — returned when we can't fetch live IV.
     * 20% is a reasonable default for large-cap stocks in normal conditions.
     */
    private static final double DEFAULT_VOLATILITY = 0.20;

    /**
     * Alpha Vantage API key — injected from application.properties.
     */
    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MarketDataService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the current spot price for a given ticker from Alpha Vantage.
     *
     * Alpha Vantage GLOBAL_QUOTE response looks like:
     * {
     *   "Global Quote": {
     *     "01. symbol": "AAPL",
     *     "05. price": "175.2300",
     *     ...
     *   }
     * }
     *
     * We navigate to "Global Quote" → "05. price" to get the price.
     *
     * @param  ticker stock ticker e.g. "AAPL", "SPY", "TSLA"
     * @return        current market price as a double
     * @throws MarketDataException if ticker is invalid or API is unreachable
     */
    public double getSpotPrice(String ticker) {
        try {
            String url = String.format(QUOTE_URL, ticker.toUpperCase(), apiKey);
            String json = restTemplate.getForObject(url, String.class);

            // Parse JSON and navigate to the price field
            JsonNode root = objectMapper.readTree(json);
            JsonNode quote = root.path("Global Quote");

            if (quote.isMissingNode() || quote.isEmpty()) {
                throw new MarketDataException("No data found for ticker: " + ticker);
            }

            String priceStr = quote.path("05. price").asText();

            if (priceStr.isEmpty()) {
                throw new MarketDataException("No price available for ticker: " + ticker);
            }

            return Double.parseDouble(priceStr);

        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch price for: " + ticker + " — " + e.getMessage(), e);
        }
    }

    /**
     * Returns a default implied volatility.
     * Alpha Vantage does offer IV data but it requires a premium plan.
     * For the demo, we return 20% and let the user adjust manually.
     *
     * @param  ticker stock ticker (kept for future use)
     * @return        implied volatility as decimal e.g. 0.20 = 20%
     */
    public double getImpliedVolatility(String ticker) {
        return DEFAULT_VOLATILITY;
    }

    /**
     * Convenience method: returns both spot price and IV together.
     *
     * @param  ticker stock ticker symbol
     * @return        double array: [spotPrice, impliedVolatility]
     */
    public double[] getMarketInputs(String ticker) {
        double spot = getSpotPrice(ticker);
        double iv   = getImpliedVolatility(ticker);
        return new double[]{ spot, iv };
    }

    /**
     * Custom exception for market data failures.
     */
    public static class MarketDataException extends RuntimeException {
        public MarketDataException(String message) {
            super(message);
        }
        public MarketDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}