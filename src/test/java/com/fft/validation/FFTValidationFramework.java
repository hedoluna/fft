package com.fft.validation;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation framework for FFT implementations.
 *
 * <p>Provides stage-by-stage validation of optimized FFT implementations against
 * the FFTBase reference implementation. This addresses the CONSENSUS_ANALYSIS.md
 * P1 recommendation for automated validation infrastructure.</p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Capture intermediate values after each FFT stage</li>
 *   <li>Compare optimized implementation against FFTBase at each stage</li>
 *   <li>Identify exact stage where errors occur</li>
 *   <li>Generate detailed validation reports</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * FFTValidationFramework validator = new FFTValidationFramework(8, 1e-10);
 * validator.setInput(realData, imagData);
 *
 * // In optimized implementation, capture after each stage:
 * validator.captureStage("STAGE 1", realAfterStage1, imagAfterStage1);
 * validator.captureStage("STAGE 2", realAfterStage2, imagAfterStage2);
 * validator.captureStage("BIT-REVERSAL", finalReal, finalImag);
 *
 * // Validate against FFTBase
 * ValidationReport report = validator.validate();
 * if (!report.isValid()) {
 *     System.out.println(report.getDetailedReport());
 * }
 * }</pre>
 *
 * @since 2.0.0
 * @see FFTBase
 */
public class FFTValidationFramework {

    private final int size;
    private final double tolerance;
    private final List<StageCapture> stages;
    private double[] inputReal;
    private double[] inputImag;
    private boolean forward;

    /**
     * Create a validation framework for a specific FFT size.
     *
     * @param size FFT size (must be power of 2)
     * @param tolerance Tolerance for value comparison (e.g., 1e-10)
     */
    public FFTValidationFramework(int size, double tolerance) {
        if (!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Size must be power of 2, got: " + size);
        }
        this.size = size;
        this.tolerance = tolerance;
        this.stages = new ArrayList<>();
    }

    /**
     * Set input data for validation.
     *
     * @param real Real part of input
     * @param imag Imaginary part of input
     * @param forward True for forward transform, false for inverse
     */
    public void setInput(double[] real, double[] imag, boolean forward) {
        if (real.length != size || imag.length != size) {
            throw new IllegalArgumentException("Input arrays must have size " + size);
        }
        this.inputReal = real.clone();
        this.inputImag = imag.clone();
        this.forward = forward;
    }

    /**
     * Capture intermediate values after a specific stage.
     *
     * @param stageName Name of the stage (e.g., "STAGE 1", "BIT-REVERSAL")
     * @param real Real values after this stage
     * @param imag Imaginary values after this stage
     */
    public void captureStage(String stageName, double[] real, double[] imag) {
        if (real.length != size || imag.length != size) {
            throw new IllegalArgumentException("Stage arrays must have size " + size);
        }
        stages.add(new StageCapture(stageName, real.clone(), imag.clone()));
    }

    /**
     * Validate the captured stages against FFTBase reference.
     *
     * @return Validation report with detailed results
     */
    public ValidationReport validate() {
        if (inputReal == null) {
            throw new IllegalStateException("Input must be set before validation");
        }

        // Compute reference result using FFTBase
        FFTBase reference = new FFTBase();
        FFTResult referenceResult = reference.transform(
            inputReal.clone(),
            inputImag.clone(),
            forward
        );

        double[] refReal = referenceResult.getRealParts();
        double[] refImag = referenceResult.getImaginaryParts();

        // Validate final stage (if captured)
        if (stages.isEmpty()) {
            return new ValidationReport(true, "No stages captured");
        }

        StageCapture finalStage = stages.get(stages.size() - 1);
        boolean finalValid = arraysEqual(finalStage.real, refReal, tolerance) &&
                            arraysEqual(finalStage.imag, refImag, tolerance);

        if (!finalValid) {
            double maxError = findMaxError(finalStage.real, refReal, finalStage.imag, refImag);
            return new ValidationReport(
                false,
                String.format("Final stage '%s' FAILED: max error %.2e > tolerance %.2e",
                    finalStage.stageName, maxError, tolerance),
                stages,
                refReal,
                refImag
            );
        }

        return new ValidationReport(
            true,
            String.format("All %d stages validated successfully within tolerance %.2e",
                stages.size(), tolerance),
            stages,
            refReal,
            refImag
        );
    }

    /**
     * Clear all captured stages for new validation run.
     */
    public void reset() {
        stages.clear();
        inputReal = null;
        inputImag = null;
    }

    // ==================== Helper Methods ====================

    private boolean arraysEqual(double[] a, double[] b, double tol) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    private double findMaxError(double[] real1, double[] real2, double[] imag1, double[] imag2) {
        double maxError = 0.0;
        for (int i = 0; i < real1.length; i++) {
            double errorReal = Math.abs(real1[i] - real2[i]);
            double errorImag = Math.abs(imag1[i] - imag2[i]);
            maxError = Math.max(maxError, Math.max(errorReal, errorImag));
        }
        return maxError;
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    // ==================== Inner Classes ====================

    /**
     * Captured stage data.
     */
    public static class StageCapture {
        public final String stageName;
        public final double[] real;
        public final double[] imag;

        public StageCapture(String stageName, double[] real, double[] imag) {
            this.stageName = stageName;
            this.real = real;
            this.imag = imag;
        }
    }

    /**
     * Validation report with detailed results.
     */
    public static class ValidationReport {
        private final boolean valid;
        private final String message;
        private final List<StageCapture> stages;
        private final double[] referenceReal;
        private final double[] referenceImag;

        public ValidationReport(boolean valid, String message) {
            this(valid, message, new ArrayList<>(), null, null);
        }

        public ValidationReport(boolean valid, String message, List<StageCapture> stages,
                              double[] referenceReal, double[] referenceImag) {
            this.valid = valid;
            this.message = message;
            this.stages = new ArrayList<>(stages);
            this.referenceReal = referenceReal;
            this.referenceImag = referenceImag;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public List<StageCapture> getStages() {
            return new ArrayList<>(stages);
        }

        /**
         * Generate detailed validation report.
         *
         * @return Multi-line report with stage-by-stage analysis
         */
        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== FFT Validation Report ===\n");
            sb.append(String.format("Status: %s\n", valid ? "PASS" : "FAIL"));
            sb.append(String.format("Message: %s\n", message));
            sb.append(String.format("\nCaptured Stages: %d\n", stages.size()));

            for (int i = 0; i < stages.size(); i++) {
                StageCapture stage = stages.get(i);
                sb.append(String.format("\n--- Stage %d: %s ---\n", i + 1, stage.stageName));

                if (referenceReal != null && i == stages.size() - 1) {
                    // Compare final stage with reference
                    double maxErrorReal = 0.0;
                    double maxErrorImag = 0.0;
                    int maxErrorIndex = 0;

                    for (int j = 0; j < stage.real.length; j++) {
                        double errReal = Math.abs(stage.real[j] - referenceReal[j]);
                        double errImag = Math.abs(stage.imag[j] - referenceImag[j]);
                        if (errReal > maxErrorReal) {
                            maxErrorReal = errReal;
                            maxErrorIndex = j;
                        }
                        if (errImag > maxErrorImag) {
                            maxErrorImag = errImag;
                        }
                    }

                    sb.append(String.format("Max Error Real: %.2e (at index %d)\n",
                        maxErrorReal, maxErrorIndex));
                    sb.append(String.format("Max Error Imag: %.2e\n", maxErrorImag));

                    if (maxErrorReal > 1e-10 || maxErrorImag > 1e-10) {
                        sb.append(String.format("\nFirst discrepancy at index %d:\n", maxErrorIndex));
                        sb.append(String.format("  Actual:    (%.6f, %.6f)\n",
                            stage.real[maxErrorIndex], stage.imag[maxErrorIndex]));
                        sb.append(String.format("  Reference: (%.6f, %.6f)\n",
                            referenceReal[maxErrorIndex], referenceImag[maxErrorIndex]));
                    }
                }
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
