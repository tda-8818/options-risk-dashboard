/**
 * OptionsController.java
 *
 * REST controller that exposes the Black-Scholes pricing engine via HTTP.
 *
 * WHAT IT DOES:
 * Receives an Option object as a JSON request body, passes it to
 * BlackScholesService, and returns the calculated price and Greeks as JSON.
 *
 * WHY @RestController:
 * Combines @Controller + @ResponseBody. The @ResponseBody part means
 * Java objects returned from methods are automatically converted to JSON
 * by Jackson (the JSON library included with Spring Boot).
 * Without this, Spring would look for a Thymeleaf template to render instead.
 *
 * WHY CONSTRUCTOR INJECTION:
 * We inject BlackScholesService via the constructor rather than creating
 * it with "new". This lets Spring manage the instance (singleton) and
 * makes it easy to mock in unit tests.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.controller;

import com.scb.OptionsDashboard.model.Option;
import com.scb.OptionsDashboard.model.OptionGreeks;
import com.scb.OptionsDashboard.service.BlackScholesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/options") // All endpoints in this controller are prefixed /api/options
public class OptionsController {

    private final BlackScholesService blackScholesService;

    /**
     * Spring automatically injects BlackScholesService here at startup
     * because it's annotated with @Service.
     */
    public OptionsController(BlackScholesService blackScholesService) {
        this.blackScholesService = blackScholesService;
    }

    /**
     * Calculates the theoretical price AND all five Greeks for an option.
     *
     * @RequestBody tells Spring to deserialise the incoming JSON into an Option object.
     * ResponseEntity lets us control the HTTP status code returned (200 OK here).
     *
     * Example request body:
     * {
     *   "type": "CALL",
     *   "ticker": "AAPL",
     *   "spotPrice": 175.0,
     *   "strikePrice": 180.0,
     *   "timeToExpiryYears": 0.25,
     *   "volatility": 0.25,
     *   "riskFreeRate": 0.05
     * }
     *
     * @param  option deserialised from the JSON request body
     * @return        200 OK with OptionGreeks as JSON
     */
    @PostMapping("/greeks")
    public ResponseEntity<OptionGreeks> calculateGreeks(@RequestBody Option option) {
        OptionGreeks greeks = blackScholesService.greeks(option);
        return ResponseEntity.ok(greeks);
    }

    /**
     * Simple health check — confirms the pricing engine is reachable.
     * Hit GET /api/options/health in your browser to test.
     *
     * @return 200 OK with a status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Black-Scholes pricing engine is running");
    }

    /**
     * Catches IllegalArgumentException thrown by BlackScholesService.validateInputs()
     * and returns 400 Bad Request instead of a 500 server error.
     *
     * Without this, invalid inputs (e.g. negative spot price) would return
     * an ugly 500 error. This makes the API behave properly.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}