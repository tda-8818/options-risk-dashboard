/**
 * AiInsightRequest.java
 *
 * Input container for AI insight requests.
 * Carries the calculated Greeks and the user's natural language
 * question to be sent to the DeepSeek API for interpretation.
 *
 * When the user clicks "Explain my Greeks" on the dashboard,
 * the frontend sends this as a JSON body to POST /api/ai/explain.
 * Spring deserialises it into this object automatically.
 *
 * Example JSON:
 * {
 *   "greeks": {
 *     "price": 10.86,
 *     "delta": 0.53,
 *     "gamma": 0.015,
 *     "theta": -0.07,
 *     "vega": 0.52,
 *     "rho": 0.32
 *   },
 *   "question": "Explain my delta exposure in plain English",
 *   "language": "EN"
 * }
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.model;

public class AiInsightRequest {

    /**
     * The calculated Greeks to be explained by DeepSeek.
     * Injected into the prompt so the AI has full context.
     */
    private OptionGreeks greeks;

    /**
     * The user's natural language question about the position.
     * e.g. "What does my gamma exposure mean for hedging?"
     *      "Should I worry about theta decay this week?"
     */
    private String question;

    /**
     * Language preference for the response.
     * "EN" for English, "ZH" for Mandarin.
     * Defaults to English if not provided.
     */
    private String language = "EN";

    /**
     * Optional market outlook for strategy recommendations.
     * e.g. "bearish", "bullish with high volatility expected"
     */
    private String marketOutlook;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AiInsightRequest() {}

    public AiInsightRequest(OptionGreeks greeks, String question,
                            String language, String marketOutlook) {
        this.greeks        = greeks;
        this.question      = question;
        this.language      = language;
        this.marketOutlook = marketOutlook;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public OptionGreeks getGreeks()              { return greeks; }
    public void setGreeks(OptionGreeks greeks)   { this.greeks = greeks; }

    public String getQuestion()                  { return question; }
    public void setQuestion(String question)     { this.question = question; }

    public String getLanguage()                  { return language; }
    public void setLanguage(String language)     { this.language = language; }

    public String getMarketOutlook()             { return marketOutlook; }
    public void setMarketOutlook(String outlook) { this.marketOutlook = outlook; }

    @Override
    public String toString() {
        return String.format("AiInsightRequest{question='%s', language='%s', greeks=%s}",
                question, language, greeks);
    }
}