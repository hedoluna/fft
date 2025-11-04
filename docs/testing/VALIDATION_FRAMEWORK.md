# FFT Validation Framework - Usage Guide

**Purpose**: Stage-by-stage validation of optimized FFT implementations against FFTBase reference

**Created**: October 6, 2025 (P1 Implementation)
**Context**: Addresses CONSENSUS_ANALYSIS.md P1 recommendation for automated validation infrastructure

---

## Problem Solved

During FASE 2 FFT16 optimization, implementation errors were difficult to debug because tests only validated the final output. When errors occurred, it was unclear which stage (butterfly operations, bit-reversal, etc.) caused the problem.

**This framework solves that by:**
- Capturing intermediate values after each FFT stage
- Comparing against FFTBase at each stage
- Pinpointing the exact stage where errors occur
- Providing detailed error diagnostics

---

## Quick Start

### Basic Usage

```java
import com.fft.validation.FFTValidationFramework;

// 1. Create validator for your FFT size
FFTValidationFramework validator = new FFTValidationFramework(8, 1e-10);

// 2. Set input data
double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
double[] imag = new double[8];
validator.setInput(real, imag, true);  // true = forward transform

// 3. Capture stages during your implementation
validator.captureStage("STAGE 1", realAfterStage1, imagAfterStage1);
validator.captureStage("STAGE 2", realAfterStage2, imagAfterStage2);
validator.captureStage("BIT-REVERSAL", finalReal, finalImag);

// 4. Validate against FFTBase
ValidationReport report = validator.validate();

// 5. Check results
if (report.isValid()) {
    System.out.println("✅ " + report.getMessage());
} else {
    System.out.println("❌ " + report.getMessage());
    System.out.println(report.getDetailedReport());
}
```

---

## Integration Examples

### Example 1: Validating New FFT16 Optimization

```java
public class FFTOptimized16WithValidation implements FFT {
    private static final int SIZE = 16;

    @Override
    public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
        // Optional: Enable validation in debug mode
        FFTValidationFramework validator = new FFTValidationFramework(SIZE, 1e-10);
        validator.setInput(real, imaginary, forward);

        // Copy arrays for in-place computation
        double[] re = real.clone();
        double[] im = imaginary.clone();

        // STAGE 1: Butterflies with span 8
        performButterflySpan8(re, im, forward);
        validator.captureStage("STAGE 1 (span 8)", re, im);

        // STAGE 2: Butterflies with span 4
        performButterflySpan4(re, im, forward);
        validator.captureStage("STAGE 2 (span 4)", re, im);

        // STAGE 3: Butterflies with span 2
        performButterflySpan2(re, im, forward);
        validator.captureStage("STAGE 3 (span 2)", re, im);

        // STAGE 4: Butterflies with span 1
        performButterflySpan1(re, im, forward);
        validator.captureStage("STAGE 4 (span 1)", re, im);

        // BIT-REVERSAL
        performBitReversal(re, im);
        validator.captureStage("BIT-REVERSAL", re, im);

        // Validate
        ValidationReport report = validator.validate();
        if (!report.isValid()) {
            throw new IllegalStateException("Validation failed:\n" + report.getDetailedReport());
        }

        // Return result
        // ... (normalization and FFTResult creation)
    }
}
```

**Benefits:**
- Catches errors immediately during development
- Identifies exact stage where algorithm breaks
- Can be disabled in production by removing validator calls

### Example 2: Test-Driven Optimization

```java
@Test
@DisplayName("FFT32 optimization matches reference at each stage")
void testFFT32StageByStage() {
    double[] real = generateTestSignal(32);
    double[] imag = new double[32];

    FFTValidationFramework validator = new FFTValidationFramework(32, 1e-8);
    validator.setInput(real, imag, true);

    FFTOptimized32 optimized = new FFTOptimized32();

    // Assuming we modify FFTOptimized32 to expose intermediate stages
    double[][] stages = optimized.transformWithStages(real, imag, true);

    validator.captureStage("STAGE 1", stages[0], stages[1]);
    validator.captureStage("STAGE 2", stages[2], stages[3]);
    validator.captureStage("STAGE 3", stages[4], stages[5]);
    validator.captureStage("FINAL", stages[6], stages[7]);

    ValidationReport report = validator.validate();
    assertThat(report.isValid()).isTrue();
}
```

---

## API Reference

### Constructor

```java
FFTValidationFramework(int size, double tolerance)
```

**Parameters:**
- `size`: FFT size (must be power of 2: 8, 16, 32, ...)
- `tolerance`: Maximum allowed error (e.g., `1e-10`)

**Throws:** `IllegalArgumentException` if size is not power of 2

### Methods

#### setInput()
```java
void setInput(double[] real, double[] imag, boolean forward)
```

Set input data for validation.

**Parameters:**
- `real`: Real part of input signal
- `imag`: Imaginary part of input signal
- `forward`: `true` for forward FFT, `false` for inverse

#### captureStage()
```java
void captureStage(String stageName, double[] real, double[] imag)
```

Capture intermediate values after a specific algorithm stage.

**Parameters:**
- `stageName`: Descriptive name (e.g., "STAGE 1", "BIT-REVERSAL")
- `real`: Real values after this stage
- `imag`: Imaginary values after this stage

**Best Practices:**
- Use clear, consistent naming conventions
- Capture after each major algorithmic step
- Final stage should represent complete transform

#### validate()
```java
ValidationReport validate()
```

Validate captured stages against FFTBase reference.

**Returns:** `ValidationReport` with detailed results

**Throws:** `IllegalStateException` if input not set

#### reset()
```java
void reset()
```

Clear all captured stages for new validation run.

---

## ValidationReport API

### Properties

```java
boolean isValid()           // True if validation passed
String getMessage()         // Summary message
List<StageCapture> getStages()  // All captured stages
String getDetailedReport()  // Multi-line detailed analysis
```

### Detailed Report Format

```
=== FFT Validation Report ===
Status: FAIL
Message: Final stage 'STAGE 3' FAILED: max error 1.23e-05 > tolerance 1.00e-10

Captured Stages: 3

--- Stage 1: STAGE 1 (span 4) ---
Max Error Real: 0.00e+00 (at index 0)
Max Error Imag: 0.00e+00

--- Stage 2: STAGE 2 (span 2) ---
Max Error Real: 0.00e+00 (at index 0)
Max Error Imag: 0.00e+00

--- Stage 3: STAGE 3 (span 1) ---
Max Error Real: 1.23e-05 (at index 7)
Max Error Imag: 8.45e-06

First discrepancy at index 7:
  Actual:    (0.123456, -0.234567)
  Reference: (0.123468, -0.234576)
```

**Interpretation:**
- Stages 1 and 2 are perfect (no error)
- Stage 3 has errors → bug is in span-1 butterfly implementation
- Index 7 shows largest error → check butterfly operation for index 7

---

## Recommended Tolerances

| Precision Level | Tolerance | Use Case |
|----------------|-----------|----------|
| Strict | `1e-12` | Production code, critical accuracy |
| Standard | `1e-10` | General development |
| Relaxed | `1e-8` | Early prototyping |
| Loose | `1e-6` | Debugging only (not for validation) |

**Note:** FFTOptimized8 achieves `2.22e-16` error (machine precision), so `1e-10` is a safe standard.

---

## Troubleshooting

### Issue: "max error 3.14e-08 > tolerance 1.00e-10"

**Cause:** Your optimization has precision loss

**Solution:**
1. Check twiddle factor precision (use `static final` constants)
2. Verify butterfly operation order
3. Ensure normalization applied correctly
4. Check for intermediate rounding errors

### Issue: Validation fails at "BIT-REVERSAL" stage

**Cause:** Incorrect bit-reversal permutation

**Solution:**
1. Verify bit-reversal swap pairs for your size
2. Use lookup table from FFTBase for reference:
   - FFT8: swap (1,4) and (3,6)
   - FFT16: swap (1,8), (2,4), (3,12), (5,10), (7,14), (11,13)
3. Test bit-reversal function independently

### Issue: All stages show same error pattern

**Cause:** Error in initial array copying or data setup

**Solution:**
1. Verify input arrays cloned correctly
2. Check array indices in butterfly operations
3. Ensure no aliasing between input/output arrays

---

## Advanced Usage

### Conditional Validation (Debug Mode)

```java
public class FFTOptimized32 implements FFT {
    private static final boolean DEBUG_VALIDATION =
        Boolean.getBoolean("fft.debug.validate");

    @Override
    public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
        FFTValidationFramework validator = null;

        if (DEBUG_VALIDATION) {
            validator = new FFTValidationFramework(32, 1e-10);
            validator.setInput(real, imaginary, forward);
        }

        // ... implementation with optional validation ...

        if (DEBUG_VALIDATION) {
            ValidationReport report = validator.validate();
            if (!report.isValid()) {
                System.err.println(report.getDetailedReport());
            }
        }

        return result;
    }
}
```

**Enable with:**
```bash
mvn test -Dfft.debug.validate=true -Dtest=FFTOptimized32Test
```

### Performance Impact

- **Zero overhead in production** (if validation code removed)
- **~10-20% overhead during validation** (array cloning + comparison)
- **Recommended:** Use only in tests or debug builds

---

## Related Documentation

- [CONSENSUS_ANALYSIS.md](CONSENSUS_ANALYSIS.md) - P1 recommendation for validation framework
- [OPTIMIZATION_LESSONS_LEARNED.md](OPTIMIZATION_LESSONS_LEARNED.md) - Why FFT16 failed without validation
- [P1_IMPLEMENTATION_SUMMARY.md](P1_IMPLEMENTATION_SUMMARY.md) - Complete P1 implementation report

---

## Future Enhancements

**Planned Features:**
1. **Graphical error visualization** - Plot error vs stage
2. **Automated tolerance recommendation** - Analyze error patterns
3. **Stage comparison mode** - Compare two optimized implementations
4. **JUnit integration** - Parameterized validation tests

**Contribution:**
To add features, modify `src/test/java/com/fft/validation/FFTValidationFramework.java`

---

**Status**: ✅ Production Ready (as of October 6, 2025)
**Test Coverage**: 10/10 tests passing (`FFTValidationFrameworkTest`)
**Validated Against**: FFTOptimized8 (max error 2.22e-16)
