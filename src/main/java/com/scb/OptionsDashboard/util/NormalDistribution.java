/**
 * NormalDistribution.java
 *
 * Utility class providing the standard normal distribution functions
 * required by the Black-Scholes pricing model.
 *
 * Java's Math library doesn't include these functions, so we implement
 * them here. Both methods are static — you call them directly on the
 * class without creating an instance:
 *   NormalDistribution.cdf(0.5)
 *   NormalDistribution.pdf(0.5)
 *
 * WHY WE NEED THIS:
 * Black-Scholes uses N(d1) and N(d2) — these are cumulative normal
 * distribution values. They represent probabilities: essentially
 * "how likely is this option to finish in the money?"
 * The cdf() method IS the N() function from the Black-Scholes formula.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.optionsdashboard.util;

public class NormalDistribution {

    /**
     * Private constructor — this class should never be instantiated.
     * All methods are static, so you call them directly on the class.
     */
    private NormalDistribution() {}

    /**
     * Cumulative Distribution Function (CDF) — written as N(x) in Black-Scholes.
     *
     * Returns the probability that a standard normal random variable
     * is less than or equal to x.
     *
     * Key reference points:
     *   N(0)    = 0.5    (50% probability at the mean)
     *   N(1.96) = 0.975  (97.5% — this is where the "95% confidence interval" comes from)
     *   N(-1)   = 0.159  (used for put option pricing)
     *
     * Uses the Abramowitz & Stegun polynomial approximation (26.2.17),
     * accurate to ±7.5e-8 — more than sufficient for options pricing.
     *
     * @param  x input value (d1 or d2 from Black-Scholes)
     * @return   probability P(Z ≤ x) where Z is a standard normal variable, between 0 and 1
     */
    public static double cdf(double x) {
        // Polynomial approximation constants (Abramowitz & Stegun)
        final double a1 =  0.254829592;
        final double a2 = -0.284496736;
        final double a3 =  1.421413741;
        final double a4 = -1.453152027;
        final double a5 =  1.061405429;
        final double p  =  0.3275911;

        // The CDF is symmetric around 0: N(-x) = 1 - N(x)
        // So we work with the absolute value and flip the sign at the end
        int sign = (x < 0) ? -1 : 1;
        x = Math.abs(x) / Math.sqrt(2.0);

        // Horner's method — evaluates the polynomial efficiently
        // This approximates the error function (erf)
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1)
                * t * Math.exp(-x * x);

        // Convert erf result to CDF: N(x) = 0.5 * (1 + sign * erf(x/√2))
        return 0.5 * (1.0 + sign * y);
    }

    /**
     * Probability Density Function (PDF) — written as N'(x) in Black-Scholes.
     *
     * Returns the height of the standard normal bell curve at point x.
     * Used when calculating Gamma, Vega, and Theta — the Greeks that
     * involve the rate of change of the distribution, not just its value.
     *
     * Formula: N'(x) = (1 / √2π) * e^(-x²/2)
     *
     * @param  x input value (typically d1 from Black-Scholes)
     * @return   the density value N'(x) at point x, always positive
     */
    public static double pdf(double x) {
        // Standard normal PDF: bell curve height at point x
        return Math.exp(-0.5 * x * x) / Math.sqrt(2.0 * Math.PI);
    }
}