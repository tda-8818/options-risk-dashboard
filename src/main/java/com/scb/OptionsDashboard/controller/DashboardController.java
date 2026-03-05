/**
 * DashboardController.java
 *
 * Serves the HTML pages of the dashboard using Thymeleaf.
 *
 * WHY @Controller NOT @RestController:
 * @RestController automatically converts return values to JSON.
 * @Controller treats return values as template names — Thymeleaf
 * looks up the template in src/main/resources/templates/ and
 * renders it as an HTML page.
 *
 * The Model parameter lets us pass data from Java into the HTML
 * template — e.g. default form values, page titles etc.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    /**
     * Root URL — redirects to the pricer page.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/pricer";
    }

    /**
     * Serves the option pricer page.
     * Pre-populates the form with a realistic AAPL example.
     *
     * model.addAttribute("key", value) makes the value available
     * in the template as ${key}.
     *
     * @return resolves to src/main/resources/templates/pricer.html
     */
    @GetMapping("/pricer")
    public String pricer(Model model) {
        model.addAttribute("pageTitle",    "Options Pricer");
        model.addAttribute("defaultTicker", "AAPL");
        model.addAttribute("defaultSpot",   262.52);
        model.addAttribute("defaultStrike", 265.0);
        model.addAttribute("defaultExpiry", 0.25);
        model.addAttribute("defaultVol",    0.20);
        model.addAttribute("defaultRate",   0.05);
        return "pricer";  // → templates/pricer.html
    }

    /**
     * Serves the AI insights page.
     *
     * @return resolves to src/main/resources/templates/insights.html
     */
    @GetMapping("/insights")
    public String insights(Model model) {
        model.addAttribute("pageTitle", "AI Risk Insights");
        return "insights";  // → templates/insights.html
    }
}