package com.fft.validation;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.optimized.FFTOptimized8;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for FFTValidationFramework.
 *
 * <p>Validates the validation framework itself and demonstrates proper usage
 * for testing optimized FFT implementations.</p>
 *
 * @since 2.0.0
 */
@DisplayName("FFT Validation Framework Tests")
class FFTValidationFrameworkTest {

    private static final double TOLERANCE = 1e-10;
    private FFTValidationFramework validator;

    @BeforeEach
    void setUp() {
        validator = new FFTValidationFramework(8, TOLERANCE);
    }

    @Test
    @DisplayName("Should reject non-power-of-2 size")
    void testInvalidSize() {
        assertThatThrownBy(() -> new FFTValidationFramework(7, TOLERANCE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
    }

    @Test
    @DisplayName("Should require input before validation")
    void testRequiresInput() {
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Input must be set");
    }

    @Test
    @DisplayName("Should validate correct implementation (FFTBase vs FFTBase)")
    void testValidateCorrectImplementation() {
        // Input data
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        // Compute with FFTBase
        FFTBase fftBase = new FFTBase();
        FFTResult result = fftBase.transform(real.clone(), imag.clone(), true);

        // Validate
        validator.setInput(real, imag, true);
        validator.captureStage("FINAL", result.getRealParts(), result.getImaginaryParts());

        FFTValidationFramework.ValidationReport report = validator.validate();

        assertThat(report.isValid()).isTrue();
        assertThat(report.getMessage()).contains("successfully");
        assertThat(report.getStages()).hasSize(1);
    }

    @Test
    @DisplayName("Should detect incorrect implementation")
    void testDetectIncorrectImplementation() {
        // Input data
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        // Create intentionally wrong output
        double[] wrongReal = {0, 0, 0, 0, 0, 0, 0, 0};  // All zeros (wrong!)
        double[] wrongImag = new double[8];

        // Validate
        validator.setInput(real, imag, true);
        validator.captureStage("WRONG FINAL", wrongReal, wrongImag);

        FFTValidationFramework.ValidationReport report = validator.validate();

        assertThat(report.isValid()).isFalse();
        assertThat(report.getMessage()).contains("FAILED");
        assertThat(report.getMessage()).contains("max error");
    }

    @Test
    @DisplayName("Should provide detailed error report")
    void testDetailedErrorReport() {
        // Input data
        double[] real = {1, 0, 0, 0, 0, 0, 0, 0};
        double[] imag = new double[8];

        // Create slightly wrong output (off by 0.01)
        FFTBase fftBase = new FFTBase();
        FFTResult correctResult = fftBase.transform(real.clone(), imag.clone(), true);

        double[] slightlyWrong = correctResult.getRealParts();
        slightlyWrong[0] += 0.01;  // Introduce error

        // Validate
        validator.setInput(real, imag, true);
        validator.captureStage("SLIGHTLY WRONG", slightlyWrong, correctResult.getImaginaryParts());

        FFTValidationFramework.ValidationReport report = validator.validate();

        String detailedReport = report.getDetailedReport();

        assertThat(detailedReport).contains("FFT Validation Report");
        assertThat(detailedReport).contains("Status: FAIL");
        assertThat(detailedReport).contains("Max Error Real");
        assertThat(detailedReport).contains("Max Error Imag");
        assertThat(detailedReport).contains("Actual:");
        assertThat(detailedReport).contains("Reference:");
    }

    @Test
    @DisplayName("Should validate multiple stages")
    void testMultipleStages() {
        // Simulate FFT with multiple stages
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        // Stage 1 (after first butterfly operations)
        double[] stage1Real = real.clone();
        double[] stage1Imag = imag.clone();

        // Stage 2 (after second butterfly operations)
        double[] stage2Real = real.clone();
        double[] stage2Imag = imag.clone();

        // Final (correct result)
        FFTBase fftBase = new FFTBase();
        FFTResult result = fftBase.transform(real.clone(), imag.clone(), true);

        // Validate
        validator.setInput(real, imag, true);
        validator.captureStage("STAGE 1", stage1Real, stage1Imag);
        validator.captureStage("STAGE 2", stage2Real, stage2Imag);
        validator.captureStage("FINAL", result.getRealParts(), result.getImaginaryParts());

        FFTValidationFramework.ValidationReport report = validator.validate();

        assertThat(report.isValid()).isTrue();
        assertThat(report.getStages()).hasSize(3);
    }

    @Test
    @DisplayName("Should reset between validation runs")
    void testReset() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        // First validation
        validator.setInput(real, imag, true);
        validator.captureStage("STAGE 1", real, imag);

        // Reset
        validator.reset();

        // Should require input again
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should work with FFTOptimized8 (real example)")
    void testWithFFTOptimized8() {
        // Input data
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        // Use actual FFTOptimized8
        FFTOptimized8 optimized = new FFTOptimized8();
        FFTResult result = optimized.transform(real.clone(), imag.clone(), true);

        // Validate
        validator.setInput(real, imag, true);
        validator.captureStage("FFTOptimized8 FINAL", result.getRealParts(), result.getImaginaryParts());

        FFTValidationFramework.ValidationReport report = validator.validate();

        assertThat(report.isValid()).isTrue();
        assertThat(report.getMessage()).contains("successfully");

        // Print detailed report for documentation
        System.out.println("\n" + report.getDetailedReport());
    }

    @Test
    @DisplayName("Should handle inverse transforms")
    void testInverseTransform() {
        double[] real = {1, 0, 0, 0, 0, 0, 0, 0};
        double[] imag = new double[8];

        // Forward transform
        FFTBase fftBase = new FFTBase();
        FFTResult forward = fftBase.transform(real.clone(), imag.clone(), true);

        // Inverse transform
        FFTResult inverse = fftBase.transform(
            forward.getRealParts(),
            forward.getImaginaryParts(),
            false
        );

        // Validate inverse
        validator.setInput(forward.getRealParts(), forward.getImaginaryParts(), false);
        validator.captureStage("INVERSE FINAL", inverse.getRealParts(), inverse.getImaginaryParts());

        FFTValidationFramework.ValidationReport report = validator.validate();

        assertThat(report.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should reject wrong size arrays")
    void testWrongSizeArrays() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];

        validator.setInput(real, imag, true);

        double[] wrongSize = new double[16];

        assertThatThrownBy(() -> validator.captureStage("WRONG", wrongSize, imag))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must have size 8");
    }
}
