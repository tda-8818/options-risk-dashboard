/**
 * BlackScholesService.java
 *
 * Core pricing engine implementing the Black-Scholes-Merton (BSM) model
 * for European options pricing.
 *
 * WHAT IT DOES:
 * Takes an Option object (spot price, strike, expiry, volatility, rate)
 * and calculates:
 *   1. The theoretical fair price of the option
 *   2. All five Greeks (Delta, Gamma, Theta, Vega, Rho)
 *
 * WHY @Service:
 * This annotation tells Spring to manage this class as a singleton bean.
 * Spring creates one instance at startup and injects it into any class
 * that needs it (like OptionsController). You never write
 * "new BlackScholesService()" — Spring handles that.
 *
 * BLACK-SCHOLES ASSUMPTIONS:
 * - European options only (exercisable at expiry, not before)
 * - No dividends during the option's life
 * - Constant volatility and risk-free rate
 * - Log-normally distributed asset returns
 * These are simplifications — real trading desks use more complex models
 * but BSM is the universal baseline and foundation of platforms like Murex.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.service;

import com.scb.OptionsDashboard.model.Option;
import com.scb.OptionsDashboard.model.Option.OptionType;
import com.scb.OptionsDashboard.model.OptionGreeks;
import com.scb.optionsdashboard.util.NormalDistribution;
import org.springframework.stereotype.Service;

@Service
public class BlackScholesService {

    /**
     * Calculates the theoretical price of a European option.
     *
     * THE FORMULA:
     *   Call price = S·N(d1) - K·e^(-rT)·N(d2)
     *   Put  price = K·e^(-rT)·N(-d2) - S·N(-d1)
     *
     * In plain English:
     * The call price = the expected value of receiving the asset
     * minus the present value of paying the strike price,
     * each weighted by the probability of the option expiring in the money.
     *
     * @param  option the option contract with all market inputs
     * @return        theoretical fair value in currency units
     */
    public double price(Option option) {
        validateInputs(option);

        double d1 = calculateD1(option);
        double d2 = calculateD2(d1, option);

        double S = option.getSpotPrice();
        double K = option.getStrikePrice();
        double r = option.getRiskFreeRate();
        double T = option.getTimeToExpiryYears();

        // Present value of the strike — discounted at the risk-free rate
        // e^(-rT) is the discount factor: $1 in the future is worth less today
        double pvStrike = K * Math.exp(-r * T);

        if (option.getType() == OptionType.CALL) {
            // Call: asset term - discounted strike term
            return S * NormalDistribution.cdf(d1) - pvStrike * NormalDistribution.cdf(d2);
        } else {
            // Put: discounted strike term - asset term (signs flipped)
            // N(-x) = 1 - N(x) by symmetry of the normal distribution
            return pvStrike * NormalDistribution.cdf(-d2) - S * NormalDistribution.cdf(-d1);
        }
    }

    /**
     * Calculates the theoretical price AND all five Greeks for an option.
     *
     * Greeks measure how sensitive the option price is to changes in
     * each market variable. Traders use them to understand and hedge risk.
     *
     * @param  option the option contract
     * @return        OptionGreeks object containing price + all five Greeks
     */
    public OptionGreeks greeks(Option option) {
        validateInputs(option);

        double d1  = calculateD1(option);
        double d2  = calculateD2(d1, option);

        double S   = option.getSpotPrice();
        double K   = option.getStrikePrice();
        double r   = option.getRiskFreeRate();
        double T   = option.getTimeToExpiryYears();
        double vol = option.getVolatility();

        // Precompute values used in multiple Greek formulas
        double sqrtT    = Math.sqrt(T);
        double pvStrike = K * Math.exp(-r * T);
        double nd1pdf   = NormalDistribution.pdf(d1); // N'(d1) — used in Gamma, Vega, Theta

        boolean isCall = option.getType() == OptionType.CALL;

        // ── DELTA ─────────────────────────────────────────────────────────────
        // How much the option price changes per $1 move in the underlying.
        // Call delta: 0 to 1. Put delta: -1 to 0.
        // A delta of 0.6 means the option moves $0.60 for every $1 move in spot.
        double delta = isCall
                ? NormalDistribution.cdf(d1)
                : NormalDistribution.cdf(d1) - 1.0;

        // ── GAMMA ─────────────────────────────────────────────────────────────
        // How fast delta changes per $1 move in the underlying.
        // Same formula for calls and puts — always positive.
        // High gamma = delta changes rapidly = needs frequent rehedging.
        double gamma = nd1pdf / (S * vol * sqrtT);

        // ── THETA ─────────────────────────────────────────────────────────────
        // Daily time decay — how much value the option loses per day.
        // Almost always negative (options lose value as time passes).
        // Calculated annually then divided by 365 for daily figure.
        double thetaAnnual;
        if (isCall) {
            thetaAnnual = (-S * nd1pdf * vol / (2 * sqrtT))
                    - (r * pvStrike * NormalDistribution.cdf(d2));
        } else {
            thetaAnnual = (-S * nd1pdf * vol / (2 * sqrtT))
                    + (r * pvStrike * NormalDistribution.cdf(-d2));
        }
        double theta = thetaAnnual / 365.0;

        // ── VEGA ──────────────────────────────────────────────────────────────
        // Sensitivity to a 1% change in implied volatility.
        // Same formula for calls and puts — always positive.
        // Divided by 100 to express per 1% move (industry convention).
        double vega = S * nd1pdf * sqrtT / 100.0;

        // ── RHO ───────────────────────────────────────────────────────────────
        // Sensitivity to a 1% change in the risk-free interest rate.
        // Divided by 100 to express per 1% move (industry convention).
        double rho;
        if (isCall) {
            rho = K * T * Math.exp(-r * T) * NormalDistribution.cdf(d2) / 100.0;
        } else {
            rho = -K * T * Math.exp(-r * T) * NormalDistribution.cdf(-d2) / 100.0;
        }

        double theoreticalPrice = price(option);

        return new OptionGreeks(theoreticalPrice, delta, gamma, theta, vega, rho);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Calculates d1 — first intermediate value in the Black-Scholes formula.
     *
     * Formula: d1 = [ln(S/K) + (r + σ²/2)·T] / (σ·√T)
     *
     * Intuitively: d1 is a normalised measure of how far in-the-money
     * the option is, adjusted for time and volatility.
     */
    private double calculateD1(Option o) {
        // ln(S/K) — log of the ratio of spot to strike
        // Positive when in-the-money, negative when out-of-the-money
        double logRatio = Math.log(o.getSpotPrice() / o.getStrikePrice());

        // (r + σ²/2)·T — drift term: expected return adjusted for time
        double drift = (o.getRiskFreeRate() + 0.5 * o.getVolatility() * o.getVolatility())
                * o.getTimeToExpiryYears();

        // σ·√T — volatility scaled by time
        double volSqrtT = o.getVolatility() * Math.sqrt(o.getTimeToExpiryYears());

        return (logRatio + drift) / volSqrtT;
    }

    /**
     * Calculates d2 from d1.
     *
     * Formula: d2 = d1 - σ·√T
     *
     * N(d2) is the risk-neutral probability that the option
     * expires in the money — this is what makes it meaningful.
     */
    private double calculateD2(double d1, Option o) {
        return d1 - o.getVolatility() * Math.sqrt(o.getTimeToExpiryYears());
    }

    /**
     * Validates that option inputs are financially meaningful.
     * Throws IllegalArgumentException with a clear message if not.
     * Called at the start of both price() and greeks().
     */
    private void validateInputs(Option o) {
        if (o.getSpotPrice()         <= 0) throw new IllegalArgumentException("Spot price must be positive");
        if (o.getStrikePrice()       <= 0) throw new IllegalArgumentException("Strike price must be positive");
        if (o.getVolatility()        <= 0) throw new IllegalArgumentException("Volatility must be positive");
        if (o.getTimeToExpiryYears() <= 0) throw new IllegalArgumentException("Time to expiry must be positive");
    }
}