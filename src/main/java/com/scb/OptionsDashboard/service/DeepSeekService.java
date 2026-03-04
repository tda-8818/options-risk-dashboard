/**
 * DeepSeekService.java
 *
 * Integrates with the DeepSeek LLM API to provide AI-powered
 * natural language explanations of options Greeks and risk positions.
 *
 * WHY DEEPSEEK:
 * - Free tier, internationally available
 * - Open source — can be self-hosted inside a bank's infrastructure
 *   for data security and compliance (important for SCB)
 * - Uses the same API format as OpenAI — swapping to another provider
 *   (e.g. Qwen via Alibaba Cloud, or an internal SC GPT model) is a
 *   one-line config change
 *
 * HOW IT WORKS:
 * We POST a JSON request to DeepSeek's chat completions endpoint with:
 * - A system prompt that tells DeepSeek to act as a derivatives analyst
 * - A user prompt containing the actual Greeks values and question
 * DeepSeek returns a natural language response we display on the dashboard.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scb.OptionsDashboard.model.AiInsightRequest;
import com.scb.OptionsDashboard.model.OptionGreeks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeepSeekService {

    // ── Config from application.properties ────────────────────────────────────

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.base.url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ── Public methods ────────────────────────────────────────────────────────

    /**
     * Explains the calculated Greeks in plain English (or Mandarin).
     *
     * Builds a prompt containing the Greeks values and the user's question,
     * sends it to DeepSeek, and returns the natural language response.
     *
     * @param  request contains Greeks, user question, and language preference
     * @return         DeepSeek's plain language explanation
     */
    public String explainGreeks(AiInsightRequest request) {
        OptionGreeks g = request.getGreeks();

        // System prompt — tells DeepSeek what role to play
        String systemPrompt = buildSystemPrompt(request.getLanguage());

        // User prompt — injects the actual Greeks values and question
        String userPrompt = String.format(
                "Here are the calculated Greeks for this options position:\n" +
                        "  Theoretical Price: %.4f\n" +
                        "  Delta (Δ):  %.4f\n" +
                        "  Gamma (Γ):  %.4f\n" +
                        "  Theta (Θ):  %.4f (daily)\n" +
                        "  Vega  (ν):  %.4f (per 1%% vol move)\n" +
                        "  Rho   (ρ):  %.4f (per 1%% rate move)\n\n" +
                        "Question: %s",
                g.getPrice(), g.getDelta(), g.getGamma(),
                g.getTheta(), g.getVega(), g.getRho(),
                request.getQuestion()
        );

        return callDeepSeek(systemPrompt, userPrompt);
    }

    /**
     * Recommends an options strategy based on the user's market outlook.
     *
     * Example outlooks and what DeepSeek might recommend:
     * - "Bullish, low vol expected"     → Bull call spread
     * - "Bearish, high vol expected"    → Long put
     * - "Neutral, high vol expected"    → Long straddle
     * - "Neutral, low vol expected"     → Iron condor
     *
     * @param  request contains market outlook and language preference
     * @return         strategy recommendation with rationale
     */
    public String recommendStrategy(AiInsightRequest request) {
        String systemPrompt = buildSystemPrompt(request.getLanguage());

        String userPrompt = String.format(
                "Based on the following market outlook, recommend the most appropriate " +
                        "options strategy. Explain the strategy, its risk/reward profile, " +
                        "and why it suits this outlook.\n\n" +
                        "Market outlook: %s",
                request.getMarketOutlook()
        );

        return callDeepSeek(systemPrompt, userPrompt);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Builds the system prompt that defines DeepSeek's persona.
     * Switches between English and Mandarin based on language preference.
     *
     * @param  language "EN" or "ZH"
     * @return          system prompt string
     */
    private String buildSystemPrompt(String language) {
        if ("ZH".equalsIgnoreCase(language)) {
            // Mandarin prompt — relevant for the Tianjin/Greater China team
            return "你是一位资深的衍生品分析师，专注于期权风险管理。" +
                    "请用清晰、简洁的中文解释期权的希腊字母和风险敞口。" +
                    "确保非专业人士也能理解你的解释。回复请控制在200字以内。";
        } else {
            return "You are a senior derivatives analyst specialising in options " +
                    "risk management. Explain options Greeks and risk exposures in " +
                    "clear, concise English. Be precise but accessible — assume the " +
                    "audience understands basic finance but may not be options specialists. " +
                    "Keep responses under 200 words.";
        }
    }

    /**
     * Makes the HTTP POST request to the DeepSeek API.
     *
     * Uses the OpenAI-compatible chat completions format:
     * POST /chat/completions with a messages array containing
     * system and user roles.
     *
     * To swap to a different LLM provider (Qwen, SC GPT etc.),
     * just change llm.base.url and llm.model in application.properties.
     *
     * @param  systemPrompt the AI persona/context instruction
     * @param  userPrompt   the actual question and data
     * @return              DeepSeek's response text
     */
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        try {
            // Set request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey); // Authorization: Bearer <key>

            // Build the JSON request body
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 512);

            // Messages array: system sets context, user is the actual prompt
            ArrayNode messages = body.putArray("messages");

            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);

            // Send POST request
            String endpoint = baseUrl + "/chat/completions";
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, String.class
            );

            // Parse response — extract the text from choices[0].message.content
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new DeepSeekException("DeepSeek API call failed: " + e.getMessage(), e);
        }
    }

    // ── Custom exception ──────────────────────────────────────────────────────

    public static class DeepSeekException extends RuntimeException {
        public DeepSeekException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}