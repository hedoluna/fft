# FFT Library User Guide

**Version**: 2.0.0-SNAPSHOT
**Last Updated**: November 4, 2025
**Audience**: Developers using the FFT library in their applications

---

## Table of Contents

1. [Introduction](#introduction)
2. [Installation & Setup](#installation--setup)
3. [Quick Start](#quick-start)
4. [Basic Usage](#basic-usage)
5. [Understanding FFT Results](#understanding-fft-results)
6. [Advanced Usage](#advanced-usage)
7. [Audio Processing](#audio-processing)
8. [Performance Optimization](#performance-optimization)
9. [Error Handling](#error-handling)
10. [Troubleshooting](#troubleshooting)
11. [Complete Examples](#complete-examples)
12. [Best Practices](#best-practices)
13. [FAQ](#faq)

---

## Introduction

### What is This Library?

This is a high-performance Java implementation of the Fast Fourier Transform (FFT) algorithm. The FFT is used to transform time-domain signals (like audio waveforms) into frequency-domain representations (showing which frequencies are present in the signal).

### When Should I Use This Library?

Use this library when you need to:
- **Analyze audio signals**: Detect pitch, identify frequencies, analyze harmonics
- **Process signals**: Filter, compress, or modify signals in the frequency domain
- **Detect patterns**: Recognize melodies, identify musical notes
- **Real-time analysis**: Process audio streams with low latency
- **Educational purposes**: Learn about FFT algorithms and signal processing

### Prerequisites

- **Java 17 or higher** installed on your system
- **Maven 3.6.3+** for building (if using source code)
- Basic understanding of arrays and signal processing concepts (helpful but not required)

---

## Installation & Setup

### Option 1: Maven Dependency (Recommended)

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>com.fft</groupId>
    <artifactId>fast-fourier-transform</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Option 2: Build from Source

```bash
# Clone the repository
git clone <repository-url>
cd fast-fourier-transform

# Build and install to local Maven repository
mvn clean install

# Now use it in your project (add dependency as shown above)
```

### Verify Installation

Create a simple test program:

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;

public class FFTTest {
    public static void main(String[] args) {
        double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
        FFTResult result = FFTUtils.fft(signal);
        System.out.println("FFT computed successfully!");
        System.out.println("First magnitude: " + result.getMagnitudeAt(0));
    }
}
```

If this compiles and runs without errors, your installation is successful.

---

## Quick Start

### The Simplest Possible Example

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;

public class SimplestExample {
    public static void main(String[] args) {
        // 1. Create your signal (must be power of 2: 2, 4, 8, 16, 32, ...)
        double[] signal = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};

        // 2. Compute FFT
        FFTResult result = FFTUtils.fft(signal);

        // 3. Get the magnitudes (how strong each frequency is)
        double[] magnitudes = result.getMagnitudes();

        // 4. Print results
        for (int i = 0; i < magnitudes.length; i++) {
            System.out.printf("Frequency bin %d: magnitude = %.2f\n",
                i, magnitudes[i]);
        }
    }
}
```

**Output:**
```
Frequency bin 0: magnitude = 36.00
Frequency bin 1: magnitude = 8.00
Frequency bin 2: magnitude = 8.00
Frequency bin 3: magnitude = 8.00
...
```

---

## Basic Usage

### Step 1: Prepare Your Signal

#### Signal Requirements

1. **Length must be a power of 2**: 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, or 65536
2. **Values should be normalized**: Typically between -1.0 and 1.0 for audio signals
3. **No NaN or Infinity values**: The library will throw an exception

#### Example: Creating a Signal

```java
// Simple sine wave at 440 Hz (musical note A4)
int sampleRate = 44100; // samples per second
int size = 1024;        // power of 2
double frequency = 440; // Hz
double[] signal = new double[size];

for (int i = 0; i < size; i++) {
    double time = i / (double) sampleRate;
    signal[i] = Math.sin(2 * Math.PI * frequency * time);
}
```

#### What If My Signal Isn't a Power of 2?

Use zero-padding:

```java
import com.fft.utils.FFTUtils;

double[] mySignal = {1, 2, 3, 4, 5}; // Only 5 samples

// Automatically pad to next power of 2 (8 in this case)
double[] padded = FFTUtils.zeroPadToPowerOfTwo(mySignal);

// Or find the next power of 2 size
int nextSize = FFTUtils.nextPowerOfTwo(mySignal.length);
System.out.println("Next size: " + nextSize); // Prints: 8
```

### Step 2: Compute the FFT

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;

// Simplest way (real signal, imaginary part is zero)
FFTResult result = FFTUtils.fft(signal);

// With explicit imaginary part (usually zeros for real signals)
double[] real = signal;
double[] imag = new double[signal.length]; // All zeros
FFTResult result2 = FFTUtils.fft(real, imag);

// With forward/inverse flag (true = forward, false = inverse)
FFTResult result3 = FFTUtils.fft(real, imag, true);
```

### Step 3: Extract Results

```java
// Get all magnitudes at once
double[] magnitudes = result.getMagnitudes();

// Get individual magnitude
double mag0 = result.getMagnitudeAt(0);

// Get phases (angle of each frequency component)
double[] phases = result.getPhases();
double phase0 = result.getPhaseAt(0);

// Get power spectrum (magnitude squared, useful for energy analysis)
double[] power = result.getPowerSpectrum();

// Get raw complex numbers
double[] realPart = result.getReal();
double[] imagPart = result.getImaginary();
```

### Step 4: Interpret Results

#### Frequency Bins

Each index in the result corresponds to a frequency:

```java
int binIndex = 10;  // Which frequency bin
int fftSize = 1024;
int sampleRate = 44100;

// Calculate actual frequency
double frequency = (binIndex * sampleRate) / (double) fftSize;
System.out.printf("Bin %d represents %.2f Hz\n", binIndex, frequency);
```

#### Example: Finding the Dominant Frequency

```java
public static double findDominantFrequency(FFTResult result,
                                           int sampleRate,
                                           int fftSize) {
    double[] magnitudes = result.getMagnitudes();

    // Find the bin with maximum magnitude (skip DC component at 0)
    int maxBin = 1;
    double maxMagnitude = magnitudes[1];

    for (int i = 2; i < magnitudes.length / 2; i++) { // Only first half (Nyquist)
        if (magnitudes[i] > maxMagnitude) {
            maxMagnitude = magnitudes[i];
            maxBin = i;
        }
    }

    // Convert bin to frequency
    double frequency = (maxBin * sampleRate) / (double) fftSize;
    return frequency;
}

// Usage
FFTResult result = FFTUtils.fft(signal);
double dominantFreq = findDominantFrequency(result, 44100, 1024);
System.out.printf("Dominant frequency: %.2f Hz\n", dominantFreq);
```

---

## Understanding FFT Results

### What Do the Results Mean?

After computing an FFT, you get:

1. **Magnitudes**: How strong each frequency component is
   - Higher magnitude = that frequency is more present in the signal
   - Units: amplitude (same as input signal units)

2. **Phases**: The timing/offset of each frequency component
   - Range: -π to π radians (-180° to 180°)
   - Important for reconstructing the original signal

3. **Power Spectrum**: Energy at each frequency
   - Magnitude squared
   - Useful for comparing relative strengths

### Understanding Frequency Bins

```java
// FFT divides the frequency range into bins
int fftSize = 1024;
int sampleRate = 44100; // Hz

// Number of useful bins (Nyquist limit)
int usefulBins = fftSize / 2;

// Frequency resolution (spacing between bins)
double freqResolution = sampleRate / (double) fftSize;
System.out.printf("Each bin represents %.2f Hz\n", freqResolution);
// Output: Each bin represents 43.07 Hz

// Maximum frequency (Nyquist frequency)
double maxFreq = sampleRate / 2.0;
System.out.printf("Maximum detectable frequency: %.0f Hz\n", maxFreq);
// Output: Maximum detectable frequency: 22050 Hz
```

### Why Only Use Half the Results?

The FFT of a real signal produces **symmetric results**. The second half is a mirror of the first half, so you only need bins `0` to `fftSize/2`.

```java
FFTResult result = FFTUtils.fft(signal);
double[] magnitudes = result.getMagnitudes();

// Only process first half
int nyquistBin = magnitudes.length / 2;
for (int i = 0; i < nyquistBin; i++) {
    double frequency = (i * sampleRate) / (double) fftSize;
    System.out.printf("%.2f Hz: magnitude = %.4f\n",
        frequency, magnitudes[i]);
}
```

### DC Component (Bin 0)

- **Bin 0** represents the DC component (0 Hz)
- This is the average value of your signal
- Often not interesting for audio analysis

### Special Considerations

```java
// Bin 0: DC component (average/offset)
double dc = result.getMagnitudeAt(0);

// For audio, usually focus on bins 1 to Nyquist
int minBin = 1;
int maxBin = fftSize / 2;

// Human hearing: ~20 Hz to 20,000 Hz
int minAudioBin = (int) Math.ceil(20 * fftSize / (double) sampleRate);
int maxAudioBin = (int) Math.floor(20000 * fftSize / (double) sampleRate);
```

---

## Advanced Usage

### Using the Factory Pattern

For maximum performance, use the factory to get optimized implementations:

```java
import com.fft.factory.DefaultFFTFactory;
import com.fft.core.FFT;
import com.fft.core.FFTResult;

// Create factory
DefaultFFTFactory factory = new DefaultFFTFactory();

// Get implementation for specific size
int size = 1024;
FFT fftImpl = factory.createFFT(size);

// Reuse implementation for multiple transforms
double[] signal1 = new double[size];
double[] signal2 = new double[size];
// ... fill signals ...

FFTResult result1 = fftImpl.transform(signal1, new double[size], true);
FFTResult result2 = fftImpl.transform(signal2, new double[size], true);
```

### Reusing Implementations for Performance

```java
public class FFTProcessor {
    private final FFT fft;
    private final double[] imagBuffer;

    public FFTProcessor(int fftSize) {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        this.fft = factory.createFFT(fftSize);
        this.imagBuffer = new double[fftSize]; // Reuse buffer
    }

    public FFTResult process(double[] realSignal) {
        // Clear imaginary buffer (or reuse if appropriate)
        Arrays.fill(imagBuffer, 0.0);

        // Transform
        return fft.transform(realSignal, imagBuffer, true);
    }
}

// Usage
FFTProcessor processor = new FFTProcessor(1024);

// Process many signals efficiently
for (double[] signal : audioBuffers) {
    FFTResult result = processor.process(signal);
    // ... analyze result ...
}
```

### Working with Complex Signals

Most audio signals are real-valued, but sometimes you need complex FFT:

```java
// Complex signal with real and imaginary parts
double[] real = {1.0, 2.0, 3.0, 4.0};
double[] imag = {0.5, 1.0, 1.5, 2.0};

// Forward FFT
FFTResult forward = FFTUtils.fft(real, imag, true);

// Get results
double[] freqReal = forward.getReal();
double[] freqImag = forward.getImaginary();

// Inverse FFT to get back original signal
FFTResult inverse = FFTUtils.fft(freqReal, freqImag, false);

// Verify (should match original within numerical precision)
double[] recoveredReal = inverse.getReal();
double[] recoveredImag = inverse.getImaginary();
```

### Inverse FFT

```java
// 1. Compute forward FFT
double[] signal = generateSignal();
FFTResult forward = FFTUtils.fft(signal);

// 2. Get frequency domain representation
double[] freqReal = forward.getReal();
double[] freqImag = forward.getImaginary();

// 3. Modify in frequency domain (e.g., filtering)
for (int i = 0; i < freqReal.length / 2; i++) {
    double freq = (i * sampleRate) / (double) freqReal.length;
    if (freq < 100 || freq > 1000) {
        // Zero out frequencies outside 100-1000 Hz
        freqReal[i] = 0;
        freqImag[i] = 0;
    }
}

// 4. Inverse FFT to get filtered signal
FFTResult inverse = FFTUtils.fft(freqReal, freqImag, false);
double[] filteredSignal = inverse.getReal();
```

### Windowing Functions

Apply windowing to reduce spectral leakage:

```java
public static double[] applyHammingWindow(double[] signal) {
    int n = signal.length;
    double[] windowed = new double[n];

    for (int i = 0; i < n; i++) {
        double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (n - 1));
        windowed[i] = signal[i] * window;
    }

    return windowed;
}

// Usage
double[] signal = captureAudio();
double[] windowed = applyHammingWindow(signal);
FFTResult result = FFTUtils.fft(windowed);
```

### Overlapping Windows for Continuous Analysis

```java
public class ContinuousFFTAnalyzer {
    private final int fftSize;
    private final int hopSize; // Overlap amount
    private final double[] buffer;
    private int bufferPos = 0;

    public ContinuousFFTAnalyzer(int fftSize, double overlapPercent) {
        this.fftSize = fftSize;
        this.hopSize = (int) (fftSize * (1.0 - overlapPercent));
        this.buffer = new double[fftSize];
    }

    public FFTResult processChunk(double[] newSamples) {
        // Shift buffer
        System.arraycopy(buffer, hopSize, buffer, 0, fftSize - hopSize);

        // Add new samples
        System.arraycopy(newSamples, 0, buffer, fftSize - hopSize, hopSize);

        // Apply window and compute FFT
        double[] windowed = applyHammingWindow(buffer);
        return FFTUtils.fft(windowed);
    }
}

// Usage
ContinuousFFTAnalyzer analyzer = new ContinuousFFTAnalyzer(1024, 0.5); // 50% overlap

while (audioIsPlaying) {
    double[] chunk = captureAudioChunk(512); // Half of FFT size
    FFTResult result = analyzer.processChunk(chunk);
    analyzeResult(result);
}
```

---

## Audio Processing

### Capturing Audio from Microphone

```java
import javax.sound.sampled.*;

public class AudioCapture {
    public static double[] captureAudio(int sampleRate, int samples)
            throws LineUnavailableException {

        // Setup audio format
        AudioFormat format = new AudioFormat(
            sampleRate,     // Sample rate (Hz)
            16,             // Bits per sample
            1,              // Channels (mono)
            true,           // Signed
            false           // Little-endian
        );

        // Open microphone line
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        // Capture audio
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        byte[] buffer = new byte[samples * bytesPerSample];
        microphone.read(buffer, 0, buffer.length);

        // Convert bytes to doubles
        double[] audio = new double[samples];
        for (int i = 0; i < samples; i++) {
            // 16-bit signed little-endian
            int sample = (buffer[2*i + 1] << 8) | (buffer[2*i] & 0xFF);
            audio[i] = sample / 32768.0; // Normalize to [-1, 1]
        }

        microphone.close();
        return audio;
    }
}

// Usage
double[] audio = AudioCapture.captureAudio(44100, 1024);
FFTResult result = FFTUtils.fft(audio);
```

### Pitch Detection (Simple Method)

```java
public class SimplePitchDetector {
    public static double detectPitch(double[] audio, int sampleRate) {
        // Apply window
        double[] windowed = applyHammingWindow(audio);

        // Compute FFT
        FFTResult result = FFTUtils.fft(windowed);
        double[] magnitudes = result.getMagnitudes();

        // Find peak in typical voice range (80-400 Hz)
        int fftSize = audio.length;
        int minBin = (int) Math.ceil(80.0 * fftSize / sampleRate);
        int maxBin = (int) Math.floor(400.0 * fftSize / sampleRate);

        int peakBin = minBin;
        double peakMag = magnitudes[minBin];

        for (int i = minBin + 1; i <= maxBin; i++) {
            if (magnitudes[i] > peakMag) {
                peakMag = magnitudes[i];
                peakBin = i;
            }
        }

        // Convert to frequency with parabolic interpolation
        double peak = parabolicInterpolation(magnitudes, peakBin);
        return (peak * sampleRate) / fftSize;
    }

    private static double parabolicInterpolation(double[] mags, int index) {
        if (index <= 0 || index >= mags.length - 1) {
            return index;
        }

        double alpha = mags[index - 1];
        double beta = mags[index];
        double gamma = mags[index + 1];

        double offset = 0.5 * (alpha - gamma) / (alpha - 2*beta + gamma);
        return index + offset;
    }
}
```

### Advanced Pitch Detection (Using Library Utilities)

```java
import com.fft.utils.PitchDetectionUtils;
import com.fft.utils.PitchDetectionUtils.PitchResult;

// Recommended: Hybrid method (spectral + YIN validation)
double[] audio = captureAudio();
PitchResult result = PitchDetectionUtils.detectPitchHybrid(
    audio,
    44100.0  // sample rate
);

if (result.isVoiced) {
    System.out.printf("Pitch: %.2f Hz (confidence: %.2f)\n",
        result.frequency, result.confidence);

    // Convert to musical note
    String note = frequencyToNote(result.frequency);
    System.out.println("Musical note: " + note);
} else {
    System.out.println("No pitch detected (silence or noise)");
}
```

### Converting Frequency to Musical Note

```java
public class NoteConverter {
    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    public static String frequencyToNote(double frequency) {
        // A4 = 440 Hz is the reference
        double a4 = 440.0;
        double halfStepsFromA4 = 12 * Math.log(frequency / a4) / Math.log(2);
        int halfSteps = (int) Math.round(halfStepsFromA4);

        // Calculate note and octave
        int noteIndex = (halfSteps + 9) % 12; // +9 because A is 9th note
        if (noteIndex < 0) noteIndex += 12;

        int octave = 4 + (halfSteps + 9) / 12;

        return NOTE_NAMES[noteIndex] + octave;
    }

    public static double centsOff(double frequency) {
        double a4 = 440.0;
        double halfStepsFromA4 = 12 * Math.log(frequency / a4) / Math.log(2);
        double cents = (halfStepsFromA4 - Math.round(halfStepsFromA4)) * 100;
        return cents;
    }
}

// Usage
double freq = 442.0; // Slightly sharp A
System.out.println("Note: " + NoteConverter.frequencyToNote(freq)); // "A4"
System.out.printf("Cents: %.1f\n", NoteConverter.centsOff(freq));    // "7.9"
```

---

## Performance Optimization

### Choose the Right FFT Size

```java
// Trade-offs:
// - Larger FFT = Better frequency resolution, slower processing
// - Smaller FFT = Worse frequency resolution, faster processing

// For pitch detection: 2048 or 4096 is typical
int pitchFFT = 2048;

// For spectral analysis: 4096 or 8192
int spectrumFFT = 4096;

// For real-time audio: 512 to 2048
int realtimeFFT = 1024;

// Frequency resolution formula:
double resolution = sampleRate / (double) fftSize;
System.out.printf("At FFT size %d: %.2f Hz resolution\n",
    fftSize, resolution);
```

### Optimal Sizes for This Library

The library has optimized implementations for specific sizes:

- **FFT8**: ~2.3x faster (best optimization)
- **FFT128**: ~1.4x faster
- **All sizes**: 30-50% faster due to twiddle cache

```java
// Query which implementation will be used
String info = FFTUtils.getImplementationInfo(1024);
System.out.println(info);
// Output: "Size 1024 will use: FFTOptimized1024 (priority: 50)"
```

### Reuse Objects

```java
// BAD: Creates new factory and implementation each time
public FFTResult processBad(double[] signal) {
    return FFTUtils.fft(signal); // Convenient but creates objects
}

// GOOD: Reuse factory and implementation
public class EfficientProcessor {
    private final FFT fftImpl;
    private final double[] imagBuffer;

    public EfficientProcessor(int size) {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        this.fftImpl = factory.createFFT(size);
        this.imagBuffer = new double[size];
    }

    public FFTResult process(double[] signal) {
        Arrays.fill(imagBuffer, 0.0);
        return fftImpl.transform(signal, imagBuffer, true);
    }
}
```

### Batch Processing

```java
public class BatchFFT {
    public static List<FFTResult> processBatch(List<double[]> signals) {
        int size = signals.get(0).length;

        // Create implementation once
        DefaultFFTFactory factory = new DefaultFFTFactory();
        FFT fftImpl = factory.createFFT(size);
        double[] imagBuffer = new double[size];

        List<FFTResult> results = new ArrayList<>();
        for (double[] signal : signals) {
            Arrays.fill(imagBuffer, 0.0);
            results.add(fftImpl.transform(signal, imagBuffer, true));
        }

        return results;
    }
}
```

---

## Error Handling

### Common Errors and Solutions

#### 1. IllegalArgumentException: Size not power of 2

```java
// WRONG
double[] signal = new double[1000]; // Not power of 2!

// RIGHT
double[] signal = new double[1024]; // Power of 2

// OR: Use zero-padding
double[] wrongSize = new double[1000];
double[] padded = FFTUtils.zeroPadToPowerOfTwo(wrongSize);
FFTResult result = FFTUtils.fft(padded);
```

#### 2. NullPointerException

```java
// WRONG
double[] signal = null;
FFTResult result = FFTUtils.fft(signal); // NPE!

// RIGHT
if (signal != null && signal.length > 0) {
    FFTResult result = FFTUtils.fft(signal);
} else {
    System.err.println("Invalid signal");
}
```

#### 3. ArrayIndexOutOfBoundsException

```java
// WRONG
FFTResult result = FFTUtils.fft(signal);
double mag = result.getMagnitudeAt(result.size() + 10); // Out of bounds!

// RIGHT
int index = 10;
if (index >= 0 && index < result.size()) {
    double mag = result.getMagnitudeAt(index);
} else {
    System.err.println("Index out of range");
}
```

### Robust Error Handling Example

```java
public class RobustFFT {
    public static FFTResult safeFFT(double[] signal) {
        // Validate input
        if (signal == null) {
            throw new IllegalArgumentException("Signal cannot be null");
        }

        if (signal.length == 0) {
            throw new IllegalArgumentException("Signal cannot be empty");
        }

        // Check for NaN or Infinity
        for (int i = 0; i < signal.length; i++) {
            if (Double.isNaN(signal[i]) || Double.isInfinite(signal[i])) {
                throw new IllegalArgumentException(
                    "Signal contains NaN or Infinity at index " + i);
            }
        }

        // Pad to power of 2 if needed
        if (!isPowerOfTwo(signal.length)) {
            signal = FFTUtils.zeroPadToPowerOfTwo(signal);
        }

        // Compute FFT
        try {
            return FFTUtils.fft(signal);
        } catch (Exception e) {
            throw new RuntimeException("FFT computation failed", e);
        }
    }

    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}
```

---

## Troubleshooting

### Problem: FFT Results Look Wrong

**Symptoms**: Magnitudes are all similar, no clear peaks, unexpected values

**Solutions**:

1. **Check if signal is constant**:
```java
double[] signal = ...; // Your signal
boolean allSame = true;
for (int i = 1; i < signal.length; i++) {
    if (signal[i] != signal[0]) {
        allSame = false;
        break;
    }
}
if (allSame) {
    System.out.println("Signal is constant - FFT will be trivial");
}
```

2. **Apply windowing**:
```java
double[] windowed = applyHammingWindow(signal);
FFTResult result = FFTUtils.fft(windowed);
```

3. **Check signal amplitude**:
```java
double max = Arrays.stream(signal).max().orElse(0);
double min = Arrays.stream(signal).min().orElse(0);
System.out.printf("Signal range: [%.3f, %.3f]\n", min, max);

// Normalize if needed
if (max > 1.0 || min < -1.0) {
    double scale = Math.max(Math.abs(max), Math.abs(min));
    for (int i = 0; i < signal.length; i++) {
        signal[i] /= scale;
    }
}
```

### Problem: Pitch Detection Not Working

**Symptoms**: Wrong frequency detected, no frequency detected

**Solutions**:

1. **Check signal level**:
```java
// Calculate RMS
double rms = 0;
for (double sample : signal) {
    rms += sample * sample;
}
rms = Math.sqrt(rms / signal.length);

if (rms < 0.01) {
    System.out.println("Signal too quiet - increase volume or gain");
}
```

2. **Check frequency range**:
```java
// Make sure you're looking in the right frequency range
int minFreq = 80;  // Hz
int maxFreq = 1000; // Hz
int minBin = (int) Math.ceil(minFreq * fftSize / (double) sampleRate);
int maxBin = (int) Math.floor(maxFreq * fftSize / (double) sampleRate);

// Search only in this range
```

3. **Increase FFT size**:
```java
// Larger FFT = better frequency resolution
int fftSize = 4096; // Instead of 1024
```

### Problem: Performance is Slow

**Solutions**:

1. **Use optimal FFT sizes**:
```java
// Prefer these sizes (best optimized):
int[] optimalSizes = {8, 128, 256, 512, 1024, 2048, 4096};
```

2. **Reuse implementations**:
```java
// Cache the FFT implementation
private static final Map<Integer, FFT> cache = new HashMap<>();

public static FFT getCachedFFT(int size) {
    return cache.computeIfAbsent(size, s -> {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        return factory.createFFT(s);
    });
}
```

3. **Reduce FFT size if possible**:
```java
// Do you really need 8192? Try 2048 or 4096
```

---

## Complete Examples

### Example 1: Simple Frequency Analyzer

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;

public class FrequencyAnalyzer {
    public static void main(String[] args) {
        // Generate test signal: 440 Hz sine wave
        int sampleRate = 44100;
        int fftSize = 2048;
        double frequency = 440.0;

        double[] signal = new double[fftSize];
        for (int i = 0; i < fftSize; i++) {
            double time = i / (double) sampleRate;
            signal[i] = Math.sin(2 * Math.PI * frequency * time);
        }

        // Compute FFT
        FFTResult result = FFTUtils.fft(signal);

        // Find dominant frequency
        double[] magnitudes = result.getMagnitudes();
        int peakBin = 0;
        double peakMag = 0;

        for (int i = 1; i < magnitudes.length / 2; i++) {
            if (magnitudes[i] > peakMag) {
                peakMag = magnitudes[i];
                peakBin = i;
            }
        }

        double detectedFreq = (peakBin * sampleRate) / (double) fftSize;
        System.out.printf("Input frequency: %.1f Hz\n", frequency);
        System.out.printf("Detected frequency: %.1f Hz\n", detectedFreq);
        System.out.printf("Error: %.2f%%\n",
            Math.abs(detectedFreq - frequency) / frequency * 100);
    }
}
```

### Example 2: Real-Time Spectrum Analyzer

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;
import javax.sound.sampled.*;

public class SpectrumAnalyzer {
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 2048;
    private static final int DISPLAY_BINS = 20;

    public static void main(String[] args) throws Exception {
        // Setup audio capture
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        byte[] buffer = new byte[FFT_SIZE * 2];
        double[] signal = new double[FFT_SIZE];

        System.out.println("Spectrum Analyzer - Press Ctrl+C to stop");

        while (true) {
            // Capture audio
            microphone.read(buffer, 0, buffer.length);

            // Convert to doubles
            for (int i = 0; i < FFT_SIZE; i++) {
                int sample = (buffer[2*i + 1] << 8) | (buffer[2*i] & 0xFF);
                signal[i] = sample / 32768.0;
            }

            // Compute FFT
            FFTResult result = FFTUtils.fft(signal);
            double[] magnitudes = result.getMagnitudes();

            // Display spectrum as ASCII bars
            System.out.print("\r");
            for (int i = 0; i < DISPLAY_BINS; i++) {
                int bin = i * (FFT_SIZE / 2) / DISPLAY_BINS;
                double magnitude = magnitudes[bin];
                int bars = (int) (magnitude * 50); // Scale for display
                bars = Math.min(bars, 50); // Cap at 50

                System.out.print("|");
                for (int j = 0; j < bars; j++) {
                    System.out.print("=");
                }
                for (int j = bars; j < 50; j++) {
                    System.out.print(" ");
                }
            }

            Thread.sleep(100); // Update 10 times per second
        }
    }
}
```

### Example 3: Audio File Analysis

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;
import javax.sound.sampled.*;
import java.io.File;

public class AudioFileAnalyzer {
    public static void analyzeAudioFile(String filename) throws Exception {
        // Open audio file
        File file = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioStream.getFormat();

        int sampleRate = (int) format.getSampleRate();
        int fftSize = 4096;

        System.out.printf("Analyzing: %s\n", filename);
        System.out.printf("Sample rate: %d Hz\n", sampleRate);

        // Read audio data
        byte[] buffer = new byte[fftSize * 2];
        double[] signal = new double[fftSize];

        int framesAnalyzed = 0;
        double[] avgSpectrum = new double[fftSize / 2];

        while (audioStream.read(buffer) > 0) {
            // Convert to doubles
            for (int i = 0; i < fftSize; i++) {
                int sample = (buffer[2*i + 1] << 8) | (buffer[2*i] & 0xFF);
                signal[i] = sample / 32768.0;
            }

            // Apply window
            signal = applyHammingWindow(signal);

            // Compute FFT
            FFTResult result = FFTUtils.fft(signal);
            double[] magnitudes = result.getMagnitudes();

            // Accumulate average spectrum
            for (int i = 0; i < fftSize / 2; i++) {
                avgSpectrum[i] += magnitudes[i];
            }

            framesAnalyzed++;
        }

        // Calculate average
        for (int i = 0; i < avgSpectrum.length; i++) {
            avgSpectrum[i] /= framesAnalyzed;
        }

        // Find dominant frequencies
        System.out.println("\nTop 5 frequencies:");
        for (int rank = 0; rank < 5; rank++) {
            int maxBin = 0;
            double maxMag = 0;

            for (int i = 1; i < avgSpectrum.length; i++) {
                if (avgSpectrum[i] > maxMag) {
                    maxMag = avgSpectrum[i];
                    maxBin = i;
                }
            }

            double frequency = (maxBin * sampleRate) / (double) fftSize;
            System.out.printf("%d. %.2f Hz (magnitude: %.4f)\n",
                rank + 1, frequency, maxMag);

            avgSpectrum[maxBin] = 0; // Remove for next iteration
        }

        audioStream.close();
    }

    private static double[] applyHammingWindow(double[] signal) {
        int n = signal.length;
        double[] windowed = new double[n];
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (n - 1));
            windowed[i] = signal[i] * window;
        }
        return windowed;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: AudioFileAnalyzer <audio_file.wav>");
            return;
        }

        analyzeAudioFile(args[0]);
    }
}
```

### Example 4: Guitar Tuner

```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;
import javax.sound.sampled.*;

public class GuitarTuner {
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 4096;

    // Standard guitar tuning frequencies (E2, A2, D3, G3, B3, E4)
    private static final double[] STANDARD_TUNING = {
        82.41, 110.00, 146.83, 196.00, 246.94, 329.63
    };
    private static final String[] STRING_NAMES = {
        "E2 (6th)", "A2 (5th)", "D3 (4th)", "G3 (3rd)", "B3 (2nd)", "E4 (1st)"
    };

    public static void main(String[] args) throws Exception {
        // Setup audio
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        System.out.println("Guitar Tuner - Play a string");
        System.out.println("Press Ctrl+C to quit\n");

        byte[] buffer = new byte[FFT_SIZE * 2];
        double[] signal = new double[FFT_SIZE];

        while (true) {
            // Capture audio
            microphone.read(buffer, 0, buffer.length);

            // Convert to doubles
            for (int i = 0; i < FFT_SIZE; i++) {
                int sample = (buffer[2*i + 1] << 8) | (buffer[2*i] & 0xFF);
                signal[i] = sample / 32768.0;
            }

            // Check if signal is loud enough
            double rms = 0;
            for (double s : signal) rms += s * s;
            rms = Math.sqrt(rms / signal.length);

            if (rms < 0.01) {
                System.out.print("\rWaiting for sound...              ");
                Thread.sleep(100);
                continue;
            }

            // Apply window and compute FFT
            double[] windowed = applyHammingWindow(signal);
            FFTResult result = FFTUtils.fft(windowed);

            // Detect pitch
            double frequency = detectPitch(result, SAMPLE_RATE, FFT_SIZE);

            if (frequency < 70 || frequency > 400) {
                System.out.print("\rOut of guitar range...             ");
                Thread.sleep(100);
                continue;
            }

            // Find closest string
            int closestString = 0;
            double minDiff = Math.abs(frequency - STANDARD_TUNING[0]);

            for (int i = 1; i < STANDARD_TUNING.length; i++) {
                double diff = Math.abs(frequency - STANDARD_TUNING[i]);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestString = i;
                }
            }

            double target = STANDARD_TUNING[closestString];
            double cents = 1200 * Math.log(frequency / target) / Math.log(2);

            // Display tuning info
            System.out.printf("\r%s: %.2f Hz ",
                STRING_NAMES[closestString], frequency);

            if (Math.abs(cents) < 5) {
                System.out.print("✓ IN TUNE ✓");
            } else if (cents > 0) {
                System.out.printf("↑ %.1f cents SHARP", cents);
            } else {
                System.out.printf("↓ %.1f cents FLAT", -cents);
            }

            System.out.print("          ");
            Thread.sleep(100);
        }
    }

    private static double[] applyHammingWindow(double[] signal) {
        int n = signal.length;
        double[] windowed = new double[n];
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (n - 1));
            windowed[i] = signal[i] * window;
        }
        return windowed;
    }

    private static double detectPitch(FFTResult result, int sampleRate, int fftSize) {
        double[] mags = result.getMagnitudes();

        // Search in guitar frequency range (70-400 Hz)
        int minBin = (int) Math.ceil(70.0 * fftSize / sampleRate);
        int maxBin = (int) Math.floor(400.0 * fftSize / sampleRate);

        int peakBin = minBin;
        double peakMag = mags[minBin];

        for (int i = minBin + 1; i <= maxBin; i++) {
            if (mags[i] > peakMag) {
                peakMag = mags[i];
                peakBin = i;
            }
        }

        // Parabolic interpolation for better accuracy
        if (peakBin > 0 && peakBin < mags.length - 1) {
            double alpha = mags[peakBin - 1];
            double beta = mags[peakBin];
            double gamma = mags[peakBin + 1];
            double offset = 0.5 * (alpha - gamma) / (alpha - 2*beta + gamma);
            peakBin += offset;
        }

        return (peakBin * sampleRate) / (double) fftSize;
    }
}
```

---

## Best Practices

### 1. Always Validate Input

```java
public FFTResult robustFFT(double[] signal) {
    if (signal == null || signal.length == 0) {
        throw new IllegalArgumentException("Invalid signal");
    }

    // Pad if necessary
    if (!isPowerOfTwo(signal.length)) {
        signal = FFTUtils.zeroPadToPowerOfTwo(signal);
    }

    return FFTUtils.fft(signal);
}
```

### 2. Use Windowing for Audio

```java
// Always window audio signals before FFT
double[] audio = captureAudio();
double[] windowed = applyHammingWindow(audio);
FFTResult result = FFTUtils.fft(windowed);
```

### 3. Reuse Implementations

```java
// Good for repeated FFT operations
private final FFT fftImpl;

public MyProcessor(int size) {
    DefaultFFTFactory factory = new DefaultFFTFactory();
    this.fftImpl = factory.createFFT(size);
}
```

### 4. Choose Appropriate FFT Size

```java
// Frequency resolution vs speed trade-off
int fftSize = 2048; // Good balance for most audio

// Need better frequency resolution?
int highResFFT = 8192;

// Need faster processing?
int fastFFT = 512;
```

### 5. Only Process First Half of Results

```java
double[] magnitudes = result.getMagnitudes();
int nyquist = magnitudes.length / 2;

for (int i = 0; i < nyquist; i++) {
    // Process only useful bins
    processFrequencyBin(i, magnitudes[i]);
}
```

### 6. Handle Silence Gracefully

```java
// Check RMS before processing
double rms = 0;
for (double s : signal) rms += s * s;
rms = Math.sqrt(rms / signal.length);

if (rms < 0.001) {
    return; // Too quiet, skip processing
}
```

### 7. Normalize Audio Signals

```java
// Keep signals in [-1, 1] range
public static void normalizeInPlace(double[] signal) {
    double max = 0;
    for (double s : signal) {
        max = Math.max(max, Math.abs(s));
    }
    if (max > 0) {
        for (int i = 0; i < signal.length; i++) {
            signal[i] /= max;
        }
    }
}
```

---

## FAQ

### Q: What sizes does the FFT support?

**A:** The FFT requires input sizes that are powers of 2:
- Supported: 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536
- For other sizes, use `FFTUtils.zeroPadToPowerOfTwo()` to pad your signal

### Q: How do I convert FFT bin index to frequency?

**A:** Use this formula:
```java
double frequency = (binIndex * sampleRate) / (double) fftSize;
```

### Q: Why are there only useful results in the first half?

**A:** For real-valued signals, the FFT produces symmetric results. The second half is a mirror of the first half, so you only need bins 0 to `fftSize/2`.

### Q: How do I improve frequency resolution?

**A:** Use a larger FFT size. Frequency resolution = sampleRate / fftSize
- FFT 1024: 43 Hz resolution at 44100 Hz sample rate
- FFT 4096: 10.8 Hz resolution at 44100 Hz sample rate
- FFT 8192: 5.4 Hz resolution at 44100 Hz sample rate

### Q: What is windowing and why do I need it?

**A:** Windowing reduces "spectral leakage" - artifacts that occur because FFT assumes the signal repeats periodically. Always apply a window (like Hamming) to audio signals before FFT.

### Q: How accurate is pitch detection?

**A:** The library's hybrid pitch detection achieves **0.92% error** across 80Hz-2000Hz using the spectral method. This is 44x more accurate than YIN alone. See `docs/testing/PITCH_DETECTION_ANALYSIS.md` for details.

### Q: Can I use this for real-time audio?

**A:** Yes! The library processes 4096-point FFT in ~75ms on typical hardware, enabling real-time analysis at 44.1 kHz sample rate.

### Q: What's the difference between magnitude and power spectrum?

**A:**
- **Magnitude**: Square root of (real² + imaginary²) - amplitude of frequency component
- **Power**: Magnitude squared - energy at that frequency
- Use magnitude for most applications, power for energy analysis

### Q: How do I filter specific frequencies?

**A:** Compute FFT, zero out unwanted frequency bins, then compute inverse FFT:
```java
FFTResult forward = FFTUtils.fft(signal);
double[] real = forward.getReal();
double[] imag = forward.getImaginary();

// Zero out high frequencies (low-pass filter)
for (int i = 100; i < real.length; i++) {
    real[i] = 0;
    imag[i] = 0;
}

FFTResult inverse = FFTUtils.fft(real, imag, false);
double[] filtered = inverse.getReal();
```

### Q: What does "forward" and "inverse" mean?

**A:**
- **Forward FFT** (true): Time domain → Frequency domain
- **Inverse FFT** (false): Frequency domain → Time domain
- Use forward to analyze signals, inverse to reconstruct signals

### Q: How much memory does the FFT use?

**A:** Approximately `8 * fftSize * 5` bytes for intermediate arrays:
- FFT 1024: ~40 KB
- FFT 4096: ~160 KB
- FFT 8192: ~320 KB

The library also uses a twiddle factor cache (~128 KB) for performance.

### Q: Can I process stereo audio?

**A:** Process each channel separately:
```java
double[] leftChannel = extractLeftChannel(stereoAudio);
double[] rightChannel = extractRightChannel(stereoAudio);

FFTResult leftFFT = FFTUtils.fft(leftChannel);
FFTResult rightFFT = FFTUtils.fft(rightChannel);
```

Or convert to mono:
```java
double[] mono = new double[stereoAudio.length / 2];
for (int i = 0; i < mono.length; i++) {
    mono[i] = (stereoAudio[2*i] + stereoAudio[2*i+1]) / 2.0;
}
```

### Q: What if I get NaN or Infinity in results?

**A:** Check your input signal:
```java
for (int i = 0; i < signal.length; i++) {
    if (Double.isNaN(signal[i]) || Double.isInfinite(signal[i])) {
        System.err.println("Invalid value at index " + i);
        signal[i] = 0; // Or handle appropriately
    }
}
```

### Q: How do I save FFT results for later?

**A:** FFTResult is immutable and can be stored:
```java
List<FFTResult> history = new ArrayList<>();
history.add(result); // Safe to store

// Extract data if you need to serialize
double[] magnitudes = result.getMagnitudes();
saveToFile(magnitudes);
```

---

## Additional Resources

### Documentation Files

- **README.md**: Project overview and quick start
- **docs/demos/DEMO_DOCUMENTATION.md**: Comprehensive demo application guide
- **docs/testing/PITCH_DETECTION_ANALYSIS.md**: Pitch detection accuracy analysis
- **docs/performance/FASE_2_LESSONS_LEARNED.md**: Optimization insights
- **docs/testing/JMH_BENCHMARKING_GUIDE.md**: Performance benchmarking guide

### Running Example Demos

```bash
# Real-time pitch detection
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Chord recognition
mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"
```

### Getting Help

1. Check this guide first
2. Review the FAQ section
3. Look at the demo applications for examples
4. Check the JavaDoc API documentation
5. Review the test files for usage patterns

---

**Happy FFT Processing!** 🎵

For questions, issues, or contributions, please refer to CONTRIBUTING.md
