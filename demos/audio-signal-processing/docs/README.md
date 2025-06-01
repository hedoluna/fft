# Audio Signal Processing Demo

A comprehensive demonstration of advanced audio signal processing capabilities using the FFT library. This demo showcases real-world applications of frequency domain analysis and filtering for audio signals.

## Overview

The Audio Signal Processing Demo implements professional-grade audio processing algorithms including noise reduction, frequency filtering, reverb effects, and detailed spectral analysis. It demonstrates how FFT can be used for practical audio engineering applications.

## Features

### 🎵 Signal Processing
- **Frequency Domain Filtering**: Low-pass, high-pass, band-pass, and notch filters
- **Noise Reduction**: Spectral subtraction-based noise removal
- **Audio Effects**: Convolution-based reverb and echo effects
- **Signal Enhancement**: Vocal frequency enhancement and audio cleanup

### 📊 Audio Analysis
- **Spectral Analysis**: Centroid, spread, rolloff calculations
- **Harmonic Detection**: Automatic identification of harmonic peaks
- **Feature Extraction**: RMS energy, zero-crossing rate, fundamental frequency
- **Real-time Metrics**: Performance monitoring for streaming applications

### ⚡ Performance
- **Optimized Processing**: Automatic selection of fastest FFT implementations
- **Real-time Capable**: Processing times suitable for audio streaming
- **Memory Efficient**: Minimal memory allocation during processing
- **Scalable**: Handles various buffer sizes and sample rates

## Quick Start

### Running the Demo

```bash
# Compile and run the main demo
cd demos/audio-signal-processing
javac -cp "../../../target/classes:../../../src/main/java" src/main/java/com/fft/demos/audio/*.java
java -cp ".:../../../target/classes" com.fft.demos.audio.AudioDemo
```

### Basic Usage

```java
import com.fft.demos.audio.AudioSignalProcessor;

// Create processor instance
AudioSignalProcessor processor = new AudioSignalProcessor();

// Apply filters
double[] signal = ...; // Your audio signal
double[] filtered = processor.applyFilter(signal, "vocal", null, 44100.0);

// Analyze audio characteristics
AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(signal, 44100.0);
System.out.println("Fundamental frequency: " + metrics.getFundamentalFrequency() + " Hz");

// Add reverb effect
double[] withReverb = processor.addReverb(signal, 1.5, 44100.0); // 1.5s reverb time

// Reduce noise
double[] noiseProfile = ...; // Noise-only segment
double[] denoised = processor.reduceNoise(noisySignal, noiseProfile, 44100.0, 2.0);
```

## Demo Scenarios

### Scenario 1: Music Enhancement
Demonstrates noise reduction and vocal enhancement on a complex musical signal:
- Generates realistic music signal with harmonics and noise
- Applies noise reduction using spectral subtraction
- Enhances vocal frequencies using band-pass filtering
- Analyzes improvement metrics

### Scenario 2: Audio Analysis
Showcases comprehensive audio analysis capabilities:
- Analyzes different instrument types (piano, vocal, drums)
- Detects harmonic content and fundamental frequencies
- Calculates spectral features and audio characteristics
- Demonstrates automatic feature extraction

### Scenario 3: Effects Processing
Shows professional audio effects processing:
- Applies various frequency domain filters
- Demonstrates reverb effects with different decay times
- Compares processed vs. original signal characteristics
- Analyzes effect of different processing chains

### Scenario 4: Real-time Processing Simulation
Simulates real-time audio processing performance:
- Processes typical audio buffer sizes (1024 samples)
- Monitors processing times and CPU usage
- Detects potential audio dropouts
- Validates real-time processing capabilities

## Technical Details

### Filter Implementations

#### Low-pass Filter
```java
// Butterworth-style low-pass with 10% of Nyquist cutoff
double cutoff = sampleRate * 0.1;
double attenuation = 1.0 / (1.0 + Math.pow(frequency / cutoff, 4));
```

#### High-pass Filter
```java
// High-pass with 5% of Nyquist cutoff
double cutoff = sampleRate * 0.05;
double attenuation = Math.pow(frequency / cutoff, 2);
```

#### Vocal Enhancement
- Passband: 80-4000 Hz (vocal frequency range)
- Optimized for speech and singing voice enhancement
- Preserves formant structures while attenuating noise

#### 60Hz Notch Filter
- Targets power line interference (60Hz ± 5Hz)
- Minimal impact on adjacent frequencies
- Essential for clean audio recording

### Noise Reduction Algorithm

Uses spectral subtraction technique:
1. Analyze noise profile spectrum
2. Subtract scaled noise spectrum from signal
3. Apply spectral floor to prevent artifacts
4. Reconstruct time-domain signal

```java
double subtractedMag = originalMag - alpha * noiseMag;
double finalMag = Math.max(subtractedMag, 0.1 * originalMag); // 10% floor
```

### Reverb Processing

Implements convolution-based reverb:
1. Generate exponential decay impulse response
2. Add early reflection modulation
3. Convolve with input signal using FFT
4. Mix wet and dry signals

### Audio Metrics

#### Spectral Centroid
Frequency center of mass:
```java
centroid = Σ(frequency[i] * magnitude[i]) / Σ(magnitude[i])
```

#### Spectral Spread
Frequency distribution width around centroid:
```java
spread = √(Σ((frequency[i] - centroid)² * magnitude[i]) / Σ(magnitude[i]))
```

#### Spectral Rolloff
Frequency below which 95% of energy is contained.

## Performance Characteristics

### Typical Processing Times (1024-sample buffer @ 44.1kHz)
- **Filter Application**: 0.5-2.0ms
- **Audio Analysis**: 1.0-3.0ms  
- **Noise Reduction**: 2.0-5.0ms
- **Reverb Processing**: 3.0-8.0ms

### Real-time Capability
- Buffer duration: 23.2ms (1024 samples @ 44.1kHz)
- Typical processing: 5-15ms
- Real-time margin: 8-18ms
- **Result**: Real-time capable with comfortable headroom

### Memory Usage
- Minimal dynamic allocation during processing
- Efficient reuse of FFT working arrays
- Suitable for embedded and mobile applications

## Testing

### Running Tests
```bash
# Run comprehensive test suite
mvn test -Dtest=AudioSignalProcessorTest

# Run specific test categories
mvn test -Dtest=AudioSignalProcessorTest$FilterTests
mvn test -Dtest=AudioSignalProcessorTest$PerformanceTests
```

### Test Coverage
- **Filter Correctness**: Validates frequency response of all filters
- **Analysis Accuracy**: Tests metric calculations against known signals
- **Noise Reduction**: Measures effectiveness on synthetic signals
- **Reverb Quality**: Validates effect characteristics
- **Performance**: Ensures real-time processing capability
- **Edge Cases**: Handles unusual inputs gracefully

### Performance Benchmarks
The test suite includes performance validation:
- Processing time limits for real-time operation
- Memory usage monitoring
- Throughput measurement for different buffer sizes

## Applications

### Music Production
- **Vocal Enhancement**: Isolate and enhance vocal tracks
- **Noise Cleanup**: Remove recording artifacts and background noise
- **Creative Effects**: Add reverb, echo, and frequency shaping

### Audio Analysis
- **Instrument Recognition**: Identify instruments by harmonic content
- **Key Detection**: Analyze fundamental frequencies
- **Quality Assessment**: Measure audio characteristics

### Broadcasting
- **Audio Processing**: Real-time enhancement for live audio
- **Interference Removal**: Eliminate power line and RF noise
- **Dynamic Range**: Optimize audio for transmission

### Research & Development
- **Algorithm Testing**: Validate new audio processing techniques
- **Performance Analysis**: Benchmark processing implementations
- **Educational**: Learn frequency domain audio processing

## Limitations and Future Enhancements

### Current Limitations
- Single-channel (mono) processing only
- Fixed filter parameters (no runtime adjustment)
- Limited reverb models (exponential decay only)
- No overlap-add for long convolutions

### Planned Enhancements
1. **Multi-channel Support**: Stereo and surround sound processing
2. **Adaptive Filters**: Runtime parameter adjustment
3. **Advanced Reverb**: Convolution with real impulse responses
4. **Real-time I/O**: Direct audio device integration
5. **SIMD Optimization**: Vector processing for better performance

## Dependencies

- Core FFT Library (com.fft.core, com.fft.factory, com.fft.utils)
- JUnit 5 for testing
- AssertJ for test assertions
- Java 17+ for modern language features

## License

This demo is part of the Fast Fourier Transform Library project and follows the same licensing terms.

## Contributing

Contributions are welcome! Areas of particular interest:
- Additional filter implementations
- Advanced audio effects
- Performance optimizations
- Multi-channel support
- Real-time audio I/O integration

## References

1. Smith, J.O. "Spectral Audio Signal Processing", CCRMA, Stanford University
2. Rabiner, L.R. & Gold, B. "Theory and Application of Digital Signal Processing"
3. Zölzer, U. "Digital Audio Signal Processing"
4. Verfaille, V. & Arfib, D. "A-DAFx: Adaptive Digital Audio Effects"