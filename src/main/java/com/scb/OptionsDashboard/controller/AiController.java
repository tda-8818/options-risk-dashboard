/**
 * AiController.java
 *
 * REST controller exposing DeepSeek AI insights via HTTP.
 *
 * Two endpoints:
 * - POST /api/ai/explain    — explain Greeks in plain English
 * - POST /api/ai/recommend  — recommend a strategy based on market outlook
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.controller;

import com.scb.OptionsDashboard.model.AiInsightRequest;
import com.scb.OptionsDashboard.service.DeepSeekService;
import com.scb.OptionsDashboard.service.DeepSeekService.DeepSeekException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final DeepSeekService deepSeekService;

    public AiController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    /**
     * Explains the Greeks for an options position in plain English.
     *
     * Example request:
     * {
     *   "greeks": {
     *     "price": 10.86, "delta": 0.53, "gamma": 0.015,
     *     "theta": -0.07, "vega": 0.52,  "rho": 0.32
     *   },
     *   "question": "Explain my vega exposure",
     *   "language": "EN"
     * }
     */
    @PostMapping("/explain")
    public ResponseEntity<InsightResponse> explainGreeks(@RequestBody AiInsightRequest request) {
        String insight = deepSeekService.explainGreeks(request);
        return ResponseEntity.ok(new InsightResponse(insight));
    }

    /**
     * Recommends an options strategy based on market outlook.
     *
     * Example request:
     * {
     *   "marketOutlook": "Bearish on AAPL, expecting volatility spike",
     *   "language": "EN"
     * }
     */
    @PostMapping("/recommend")
    public ResponseEntity<InsightResponse> recommendStrategy(@RequestBody AiInsightRequest request) {
        String recommendation = deepSeekService.recommendStrategy(request);
        return ResponseEntity.ok(new InsightResponse(recommendation));
    }

    /**
     * Catches DeepSeekException and returns 503 instead of 500.
     */
    @ExceptionHandler(DeepSeekException.class)
    public ResponseEntity<InsightResponse> handleDeepSeekError(DeepSeekException ex) {
        return ResponseEntity.status(503).body(
                new InsightResponse("AI insight unavailable: " + ex.getMessage())
        );
    }

    /** Wraps DeepSeek's text response as JSON: { "insight": "..." } */
    public record InsightResponse(String insight) {}
}