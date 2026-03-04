/**
 * OptionGreeks.java
 *
 * Immutable result object holding the output of a Black-Scholes calculation.
 * Contains the theoretical option price and all five Greeks.
 *
 * Immutable means once created, the values cannot be changed — there are
 * no setters, only getters. This is intentional for a result object in a
 * financial system — calculated prices should not be modified after the fact.
 *
 * This object is also what gets serialised to JSON and sent back to the
 * frontend when the user requests a pricing calculation.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.model;

public class OptionGreeks {

    /**
     * Theoretical fair value of the option as calculated by Black-Scholes.
     * Expressed in the same currency as spot and strike price.
     */
    private double price;

    /**
     * DELTA (Δ) — how much the option price moves per $1 move in the underlying.
     * Range: 0 to +1 for calls, -1 to 0 for puts.
     *
     * Example: delta of 0.65 means if the stock goes up $1,
     * the option gains $0.65 in value.
     *
     * Traders use delta to hedge — if you're short 1 call with delta 0.65,
     * you buy 65 shares of stock to be "delta neutral".
     */
    private double delta;

    /**
     * GAMMA (Γ) — rate of change of delta per $1 move in the underlying.
     * Always positive for both calls and puts.
     *
     * Example: gamma of 0.04 means if the stock moves $1,
     * delta changes by 0.04.
     *
     * High gamma near expiry means delta changes rapidly —
     * the position needs frequent rehedging.
     */
    private double gamma;

    /**
     * THETA (Θ) — daily time decay in dollar terms.
     * Almost always negative — options lose value as time passes.
     *
     * Example: theta of -0.05 means the option loses $0.05
     * in value every day, all else being equal.
     *
     * Option sellers love theta — time decay works in their favour.
     * Option buyers fight theta every day they hold the position.
     */
    private double theta;

    /**
     * VEGA (ν) — sensitivity to a 1% change in implied volatility.
     * Always positive for both calls and puts.
     *
     * Example: vega of 0.12 means if implied volatility rises by 1%,
     * the option gains $0.12 in value.
     *
     * Before major events (earnings, Fed decisions), implied vol spikes —
     * long vega positions profit from this.
     *
     * Note: Vega is not actually a Greek letter — it's a finance industry
     * convention. The symbol used varies by institution.
     */
    private double vega;

    /**
     * RHO (ρ) — sensitivity to a 1% change in the risk-free interest rate.
     * Positive for calls, negative for puts.
     *
     * Generally the least impactful Greek in normal conditions,
     * but significant during periods of rapid rate changes (e.g. 2022-2023).
     */
    private double rho;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * No-args constructor required by Jackson to deserialise
     * OptionGreeks from incoming JSON request bodies.
     * Without this, the /api/ai/explain endpoint fails.
     */
    public OptionGreeks() {}

    /**
     * Creates an immutable result object with all pricing outputs.
     * Called by BlackScholesService after completing its calculations.
     *
     * @param price theoretical option price
     * @param delta price change per $1 spot move
     * @param gamma delta change per $1 spot move
     * @param theta daily time decay in dollars
     * @param vega  price change per 1% volatility move
     * @param rho   price change per 1% interest rate move
     */
    public OptionGreeks(double price, double delta, double gamma,
                        double theta, double vega, double rho) {
        this.price = price;
        this.delta = delta;
        this.gamma = gamma;
        this.theta = theta;
        this.vega  = vega;
        this.rho   = rho;
    }

    // ── Getters only — no setters (immutable) ─────────────────────────────────

    public double getPrice() { return price; }
    public double getDelta() { return delta; }
    public double getGamma() { return gamma; }
    public double getTheta() { return theta; }
    public double getVega()  { return vega;  }
    public double getRho()   { return rho;   }

    // ── toString ──────────────────────────────────────────────────────────────

    /**
     * Formatted summary of all Greeks — used in logging and
     * as context when building the AI prompt for DeepSeek.
     */
    @Override
    public String toString() {
        return String.format(
                "OptionGreeks{price=%.4f, Δ=%.4f, Γ=%.4f, Θ=%.4f, ν=%.4f, ρ=%.4f}",
                price, delta, gamma, theta, vega, rho
        );
    }
}