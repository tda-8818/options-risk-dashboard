/**
 * Option.java
 *
 * Domain model representing a single European options contract.
 * This is the container for all inputs required by the Black-Scholes
 * pricing model — spot price, strike price, expiry, volatility, and rate.
 *
 * When the user submits the pricing form, Spring automatically converts
 * the JSON request body into an instance of this class.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.model;

public class Option {

    /**
     * Defines whether this is a CALL or PUT option.
     *
     * CALL — right to BUY the asset at the strike price.
     *        Profitable when the asset price rises above the strike.
     *
     * PUT  — right to SELL the asset at the strike price.
     *        Profitable when the asset price falls below the strike.
     *
     * Using an enum instead of a String prevents invalid values like
     * "call", "Call", "CALLS" from breaking the pricing engine.
     */
    public enum OptionType {
        CALL, PUT
    }

    /**
     * CALL or PUT — the type of this option contract.
     */
    private OptionType type;

    /**
     * Ticker symbol of the underlying asset (e.g. "AAPL", "SPY").
     * Used to fetch live market data from Yahoo Finance.
     */
    private String ticker;

    /**
     * Current market price of the underlying asset.
     * Known as "S" in the Black-Scholes formula.
     * e.g. if Apple stock is trading at $175, spotPrice = 175.0
     */
    private double spotPrice;

    /**
     * The agreed price at which the option can be exercised.
     * Known as "K" (strike) in the Black-Scholes formula.
     * e.g. a $180 call lets the holder buy the asset at $180
     * regardless of where the market price actually is.
     */
    private double strikePrice;

    /**
     * Time remaining until the option expires, in years.
     * Known as "T" in the Black-Scholes formula.
     * e.g. 30 days to expiry = 30/365 = 0.0822 years.
     */
    private double timeToExpiryYears;

    /**
     * Implied volatility — the market's expectation of how much
     * the asset will move, expressed as a decimal.
     * Known as "σ" (sigma) in the Black-Scholes formula.
     * e.g. 25% volatility = 0.25
     */
    private double volatility;

    /**
     * The annualised risk-free interest rate, as a decimal.
     * Known as "r" in the Black-Scholes formula.
     * Typically the yield on a government bond (e.g. US 10-year Treasury).
     * e.g. 5% rate = 0.05
     */
    private double riskFreeRate;

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * No-args constructor required by Jackson (the JSON library Spring uses)
     * to deserialise incoming JSON request bodies into this object.
     * Without this, POST requests to the pricing endpoint will fail.
     */
    public Option() {}

    /**
     * Full constructor for creating an Option with all inputs.
     * Used in unit tests to create Option objects directly in code.
     */
    public Option(OptionType type, String ticker, double spotPrice, double strikePrice,
                  double timeToExpiryYears, double volatility, double riskFreeRate) {
        this.type              = type;
        this.ticker            = ticker;
        this.spotPrice         = spotPrice;
        this.strikePrice       = strikePrice;
        this.timeToExpiryYears = timeToExpiryYears;
        this.volatility        = volatility;
        this.riskFreeRate      = riskFreeRate;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    // Required by Jackson to read and write the fields when converting
    // between Java objects and JSON.

    public OptionType getType()                    { return type; }
    public void setType(OptionType type)           { this.type = type; }

    public String getTicker()                      { return ticker; }
    public void setTicker(String ticker)           { this.ticker = ticker; }

    public double getSpotPrice()                   { return spotPrice; }
    public void setSpotPrice(double spotPrice)     { this.spotPrice = spotPrice; }

    public double getStrikePrice()                 { return strikePrice; }
    public void setStrikePrice(double k)           { this.strikePrice = k; }

    public double getTimeToExpiryYears()           { return timeToExpiryYears; }
    public void setTimeToExpiryYears(double t)     { this.timeToExpiryYears = t; }

    public double getVolatility()                  { return volatility; }
    public void setVolatility(double vol)          { this.volatility = vol; }

    public double getRiskFreeRate()                { return riskFreeRate; }
    public void setRiskFreeRate(double r)          { this.riskFreeRate = r; }

    // ── toString ──────────────────────────────────────────────────────────────

    /**
     * Human-readable summary of the option — useful for logging.
     * e.g. "Option{type=CALL, ticker=AAPL, S=175.00, K=180.00, T=0.2500, σ=0.25, r=0.0500}"
     */
    @Override
    public String toString() {
        return String.format(
                "Option{type=%s, ticker=%s, S=%.2f, K=%.2f, T=%.4f, σ=%.2f, r=%.4f}",
                type, ticker, spotPrice, strikePrice, timeToExpiryYears, volatility, riskFreeRate
        );
    }
}