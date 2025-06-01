package com.fft.demos.audio;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.FFTUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Audio Signal Processing Demo - Advanced signal processing using FFT.
 * 
 * <p>This demo showcases practical audio signal processing applications including:</p>
 * <ul>
 * <li>Frequency domain filtering (low-pass, high-pass, band-pass, notch)</li>
 * <li>Noise reduction and signal enhancement</li>
 * <li>Spectral analysis and feature extraction</li>
 * <li>Audio effects (echo, reverb simulation)</li>
 * <li>Real-time frequency analysis</li>
 * </ul>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li><b>Filter Design:</b> Butterworth-style frequency domain filters</li>
 * <li><b>Spectral Analysis:</b> Peak detection, harmonic analysis, spectral centroid</li>
 * <li><b>Signal Enhancement:</b> Noise gating, spectral subtraction</li>
 * <li><b>Audio Effects:</b> Convolution-based effects using FFT</li>
 * <li><b>Performance Monitoring:</b> Processing time analysis for real-time applications</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class AudioSignalProcessor {
    
    private final FFTFactory factory;
    private final Map<String, FilterFunction> filters;
    private final List<ProcessingResult> processingHistory;
    
    /**
     * Represents the result of an audio processing operation.
     */
    public static class ProcessingResult {
        private final String operationType;
        private final double[] originalSignal;
        private final double[] processedSignal;
        private final long processingTimeNanos;
        private final Map<String, Double> metrics;
        private final Date timestamp;
        
        public ProcessingResult(String operationType, double[] originalSignal, 
                              double[] processedSignal, long processingTimeNanos) {
            this.operationType = operationType;
            this.originalSignal = originalSignal.clone();
            this.processedSignal = processedSignal.clone();
            this.processingTimeNanos = processingTimeNanos;
            this.metrics = new HashMap<>();
            this.timestamp = new Date();
        }
        
        public String getOperationType() { return operationType; }
        public double[] getOriginalSignal() { return originalSignal.clone(); }
        public double[] getProcessedSignal() { return processedSignal.clone(); }
        public long getProcessingTimeNanos() { return processingTimeNanos; }
        public double getProcessingTimeMs() { return processingTimeNanos / 1_000_000.0; }
        public Map<String, Double> getMetrics() { return new HashMap<>(metrics); }
        public Date getTimestamp() { return new Date(timestamp.getTime()); }
        
        public void addMetric(String name, double value) {
            metrics.put(name, value);
        }
        
        @Override
        public String toString() {
            return String.format("ProcessingResult{operation='%s', time=%.2fms, metrics=%s}", 
                               operationType, getProcessingTimeMs(), metrics);
        }
    }
    
    /**
     * Functional interface for frequency domain filtering.
     */
    @FunctionalInterface
    public interface FilterFunction {
        /**
         * Apply filter to frequency domain representation.
         * 
         * @param frequencies frequency bins (Hz)
         * @param real real parts of FFT
         * @param imaginary imaginary parts of FFT
         * @param sampleRate sample rate in Hz
         */
        void apply(double[] frequencies, double[] real, double[] imaginary, double sampleRate);
    }
    
    /**
     * Audio analysis metrics.
     */
    public static class AudioMetrics {
        private double spectralCentroid;
        private double spectralSpread;
        private double spectralRolloff;
        private double zeroCrossingRate;
        private double rmsEnergy;
        private List<Double> harmonicPeaks;
        private double fundamentalFrequency;
        
        // Getters and setters
        public double getSpectralCentroid() { return spectralCentroid; }
        public void setSpectralCentroid(double spectralCentroid) { this.spectralCentroid = spectralCentroid; }
        
        public double getSpectralSpread() { return spectralSpread; }
        public void setSpectralSpread(double spectralSpread) { this.spectralSpread = spectralSpread; }
        
        public double getSpectralRolloff() { return spectralRolloff; }
        public void setSpectralRolloff(double spectralRolloff) { this.spectralRolloff = spectralRolloff; }
        
        public double getZeroCrossingRate() { return zeroCrossingRate; }
        public void setZeroCrossingRate(double zeroCrossingRate) { this.zeroCrossingRate = zeroCrossingRate; }
        
        public double getRmsEnergy() { return rmsEnergy; }
        public void setRmsEnergy(double rmsEnergy) { this.rmsEnergy = rmsEnergy; }
        
        public List<Double> getHarmonicPeaks() { return new ArrayList<>(harmonicPeaks); }
        public void setHarmonicPeaks(List<Double> harmonicPeaks) { this.harmonicPeaks = new ArrayList<>(harmonicPeaks); }
        
        public double getFundamentalFrequency() { return fundamentalFrequency; }
        public void setFundamentalFrequency(double fundamentalFrequency) { this.fundamentalFrequency = fundamentalFrequency; }
        
        @Override
        public String toString() {
            return String.format("AudioMetrics{centroid=%.1fHz, spread=%.1f, rolloff=%.1fHz, " +
                               "zcr=%.3f, rms=%.3f, fundamental=%.1fHz, harmonics=%d}", 
                               spectralCentroid, spectralSpread, spectralRolloff,
                               zeroCrossingRate, rmsEnergy, fundamentalFrequency,
                               harmonicPeaks != null ? harmonicPeaks.size() : 0);
        }
    }
    
    public AudioSignalProcessor() {
        this.factory = new DefaultFFTFactory();
        this.filters = new HashMap<>();
        this.processingHistory = new ArrayList<>();
        initializeFilters();
    }
    
    private void initializeFilters() {
        // Low-pass filter
        filters.put("lowpass", (freq, real, imag, sampleRate) -> {
            double cutoff = sampleRate * 0.1; // 10% of Nyquist
            for (int i = 0; i < freq.length; i++) {
                if (Math.abs(freq[i]) > cutoff) {
                    double attenuation = 1.0 / (1.0 + Math.pow(Math.abs(freq[i]) / cutoff, 4));
                    real[i] *= attenuation;
                    imag[i] *= attenuation;
                }
            }
        });
        
        // High-pass filter
        filters.put("highpass", (freq, real, imag, sampleRate) -> {
            double cutoff = sampleRate * 0.05; // 5% of Nyquist
            for (int i = 0; i < freq.length; i++) {
                if (Math.abs(freq[i]) < cutoff) {
                    double attenuation = Math.pow(Math.abs(freq[i]) / cutoff, 2);
                    real[i] *= attenuation;
                    imag[i] *= attenuation;
                }
            }
        });
        
        // Band-pass filter (vocal range: 85-255 Hz fundamental)
        filters.put("vocal", (freq, real, imag, sampleRate) -> {
            double lowCutoff = 80.0;
            double highCutoff = 4000.0;
            for (int i = 0; i < freq.length; i++) {
                double f = Math.abs(freq[i]);
                if (f < lowCutoff || f > highCutoff) {
                    double attenuation = 0.1;
                    if (f < lowCutoff) {
                        attenuation = Math.max(0.1, Math.pow(f / lowCutoff, 2));
                    } else {
                        attenuation = Math.max(0.1, 1.0 / (1.0 + Math.pow(f / highCutoff, 3)));
                    }
                    real[i] *= attenuation;
                    imag[i] *= attenuation;
                }
            }
        });
        
        // Notch filter (60 Hz hum removal)
        filters.put("notch60", (freq, real, imag, sampleRate) -> {
            double notchFreq = 60.0;
            double bandwidth = 5.0;
            for (int i = 0; i < freq.length; i++) {
                double f = Math.abs(freq[i]);
                if (Math.abs(f - notchFreq) < bandwidth) {
                    double attenuation = Math.abs(f - notchFreq) / bandwidth;
                    attenuation = Math.max(0.01, attenuation);
                    real[i] *= attenuation;
                    imag[i] *= attenuation;
                }
            }
        });
    }
    
    /**
     * Apply frequency domain filter to audio signal.
     * 
     * @param signal input audio signal
     * @param filterName name of predefined filter or "custom"
     * @param customFilter custom filter function (if filterName is "custom")
     * @param sampleRate sample rate in Hz
     * @return filtered signal
     */
    public double[] applyFilter(double[] signal, String filterName, 
                               FilterFunction customFilter, double sampleRate) {
        long startTime = System.nanoTime();
        
        // Zero-pad to power of 2
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        
        // Forward FFT
        FFT fft = factory.createFFT(paddedSignal.length);
        FFTResult forwardResult = fft.transform(paddedSignal, true);
        
        // Get frequency bins
        double[] frequencies = new double[paddedSignal.length];
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = (double) i * sampleRate / paddedSignal.length;
            if (i > paddedSignal.length / 2) {
                frequencies[i] -= sampleRate; // Negative frequencies
            }
        }
        
        // Apply filter
        double[] real = forwardResult.getRealParts().clone();
        double[] imaginary = forwardResult.getImaginaryParts().clone();
        
        FilterFunction filter = filterName.equals("custom") ? customFilter : filters.get(filterName);
        if (filter == null) {
            throw new IllegalArgumentException("Unknown filter: " + filterName);
        }
        
        filter.apply(frequencies, real, imaginary, sampleRate);
        
        // Inverse FFT
        FFTResult inverseResult = fft.transform(real, imaginary, false);
        
        // Extract original length
        double[] filtered = new double[signal.length];
        System.arraycopy(inverseResult.getRealParts(), 0, filtered, 0, signal.length);
        
        long endTime = System.nanoTime();
        
        // Record processing result
        ProcessingResult result = new ProcessingResult("filter_" + filterName, 
                                                     signal, filtered, endTime - startTime);
        result.addMetric("sample_rate", sampleRate);
        result.addMetric("original_length", signal.length);
        result.addMetric("padded_length", paddedSignal.length);
        processingHistory.add(result);
        
        return filtered;
    }
    
    /**
     * Analyze audio signal and extract spectral features.
     * 
     * @param signal input audio signal
     * @param sampleRate sample rate in Hz
     * @return audio analysis metrics
     */
    public AudioMetrics analyzeSignal(double[] signal, double sampleRate) {
        long startTime = System.nanoTime();
        
        AudioMetrics metrics = new AudioMetrics();
        
        // Basic time domain metrics
        double rmsEnergy = 0;
        int zeroCrossings = 0;
        
        for (int i = 0; i < signal.length; i++) {
            rmsEnergy += signal[i] * signal[i];
            if (i > 0 && Math.signum(signal[i]) != Math.signum(signal[i-1])) {
                zeroCrossings++;
            }
        }
        
        metrics.setRmsEnergy(Math.sqrt(rmsEnergy / signal.length));
        metrics.setZeroCrossingRate((double) zeroCrossings / (signal.length - 1));
        
        // Frequency domain analysis
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        FFT fft = factory.createFFT(paddedSignal.length);
        FFTResult fftResult = fft.transform(paddedSignal, true);
        
        double[] magnitudes = fftResult.getMagnitudes();
        double[] frequencies = new double[magnitudes.length / 2]; // Only positive frequencies
        
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = (double) i * sampleRate / paddedSignal.length;
        }
        
        // Spectral centroid
        double weightedSum = 0;
        double magnitudeSum = 0;
        for (int i = 1; i < frequencies.length; i++) { // Skip DC
            weightedSum += frequencies[i] * magnitudes[i];
            magnitudeSum += magnitudes[i];
        }
        metrics.setSpectralCentroid(magnitudeSum > 0 ? weightedSum / magnitudeSum : 0);
        
        // Spectral spread
        double spreadSum = 0;
        for (int i = 1; i < frequencies.length; i++) {
            double deviation = frequencies[i] - metrics.getSpectralCentroid();
            spreadSum += deviation * deviation * magnitudes[i];
        }
        metrics.setSpectralSpread(magnitudeSum > 0 ? Math.sqrt(spreadSum / magnitudeSum) : 0);
        
        // Spectral rolloff (95% energy point)
        double energySum = 0;
        for (int i = 1; i < frequencies.length; i++) {
            energySum += magnitudes[i] * magnitudes[i];
        }
        
        double rollingSum = 0;
        double rolloffThreshold = 0.95 * energySum;
        for (int i = 1; i < frequencies.length; i++) {
            rollingSum += magnitudes[i] * magnitudes[i];
            if (rollingSum >= rolloffThreshold) {
                metrics.setSpectralRolloff(frequencies[i]);
                break;
            }
        }
        
        // Peak detection for harmonics
        List<Double> peaks = findSpectralPeaks(frequencies, magnitudes, 20.0); // Min 20Hz
        metrics.setHarmonicPeaks(peaks);
        
        if (!peaks.isEmpty()) {
            metrics.setFundamentalFrequency(peaks.get(0));
        }
        
        long endTime = System.nanoTime();
        
        // Record analysis result
        ProcessingResult result = new ProcessingResult("analysis", signal, signal, endTime - startTime);
        result.addMetric("spectral_centroid", metrics.getSpectralCentroid());
        result.addMetric("spectral_spread", metrics.getSpectralSpread());
        result.addMetric("rms_energy", metrics.getRmsEnergy());
        result.addMetric("fundamental_freq", metrics.getFundamentalFrequency());
        processingHistory.add(result);
        
        return metrics;
    }
    
    /**
     * Find spectral peaks in magnitude spectrum.
     */
    private List<Double> findSpectralPeaks(double[] frequencies, double[] magnitudes, double minFreq) {
        List<Double> peaks = new ArrayList<>();
        
        // Find local maxima above threshold
        double threshold = Arrays.stream(magnitudes).max().orElse(0) * 0.1; // 10% of max
        
        for (int i = 2; i < frequencies.length - 2; i++) {
            if (frequencies[i] >= minFreq && 
                magnitudes[i] > threshold &&
                magnitudes[i] > magnitudes[i-1] && 
                magnitudes[i] > magnitudes[i+1] &&
                magnitudes[i] > magnitudes[i-2] &&
                magnitudes[i] > magnitudes[i+2]) {
                peaks.add(frequencies[i]);
            }
        }
        
        return peaks;
    }
    
    /**
     * Apply noise reduction using spectral subtraction.
     * 
     * @param signal noisy signal
     * @param noiseProfile noise-only segment for profiling
     * @param sampleRate sample rate in Hz
     * @param alpha over-subtraction factor (1.0-3.0)
     * @return denoised signal
     */
    public double[] reduceNoise(double[] signal, double[] noiseProfile, 
                               double sampleRate, double alpha) {
        long startTime = System.nanoTime();
        
        // Analyze noise profile
        double[] paddedNoise = FFTUtils.zeroPadToPowerOfTwo(noiseProfile);
        FFT fft = factory.createFFT(paddedNoise.length);
        FFTResult noiseSpectrum = fft.transform(paddedNoise, true);
        double[] noiseMagnitudes = noiseSpectrum.getMagnitudes();
        
        // Process signal
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        if (paddedSignal.length != paddedNoise.length) {
            // Resize noise profile to match signal
            paddedNoise = Arrays.copyOf(paddedNoise, paddedSignal.length);
            FFT noiseFft = factory.createFFT(paddedNoise.length);
            noiseSpectrum = noiseFft.transform(paddedNoise, true);
            noiseMagnitudes = noiseSpectrum.getMagnitudes();
        }
        
        FFT signalFft = factory.createFFT(paddedSignal.length);
        FFTResult signalSpectrum = signalFft.transform(paddedSignal, true);
        double[] real = signalSpectrum.getRealParts().clone();
        double[] imaginary = signalSpectrum.getImaginaryParts().clone();
        double[] signalMagnitudes = signalSpectrum.getMagnitudes();
        
        // Spectral subtraction
        for (int i = 0; i < real.length; i++) {
            double originalMag = signalMagnitudes[i];
            double noiseMag = (i < noiseMagnitudes.length) ? noiseMagnitudes[i] : 0;
            
            double subtractedMag = originalMag - alpha * noiseMag;
            double finalMag = Math.max(subtractedMag, 0.1 * originalMag); // Floor to 10% of original
            
            if (originalMag > 0) {
                double scaleFactor = finalMag / originalMag;
                real[i] *= scaleFactor;
                imaginary[i] *= scaleFactor;
            }
        }
        
        // Inverse FFT
        FFTResult cleanResult = signalFft.transform(real, imaginary, false);
        double[] cleaned = new double[signal.length];
        System.arraycopy(cleanResult.getRealParts(), 0, cleaned, 0, signal.length);
        
        long endTime = System.nanoTime();
        
        ProcessingResult result = new ProcessingResult("noise_reduction", signal, cleaned, endTime - startTime);
        result.addMetric("alpha", alpha);
        result.addMetric("noise_profile_length", noiseProfile.length);
        processingHistory.add(result);
        
        return cleaned;
    }
    
    /**
     * Generate artificial reverb using convolution.
     * 
     * @param signal dry input signal
     * @param reverbTime decay time in seconds
     * @param sampleRate sample rate in Hz
     * @return signal with reverb effect
     */
    public double[] addReverb(double[] signal, double reverbTime, double sampleRate) {
        long startTime = System.nanoTime();
        
        // Generate impulse response (exponential decay with random modulation)
        int impulseLength = (int) (reverbTime * sampleRate);
        double[] impulseResponse = new double[impulseLength];
        Random random = new Random(42); // Deterministic for testing
        
        for (int i = 0; i < impulseLength; i++) {
            double t = (double) i / sampleRate;
            double decay = Math.exp(-3.0 * t / reverbTime); // 60dB decay
            double modulation = 1.0 + 0.3 * Math.sin(2 * Math.PI * 7.3 * t) * 
                               Math.exp(-t); // Early reflections
            impulseResponse[i] = decay * modulation * (random.nextGaussian() * 0.1 + 1.0);
        }
        
        // Convolve using FFT (overlap-add would be more efficient for long signals)
        int convolutionLength = signal.length + impulseLength - 1;
        int fftSize = Integer.highestOneBit(convolutionLength * 2 - 1) * 2; // Next power of 2
        
        FFT fft = factory.createFFT(fftSize);
        
        // Zero-pad both signals
        double[] paddedSignal = Arrays.copyOf(signal, fftSize);
        double[] paddedImpulse = Arrays.copyOf(impulseResponse, fftSize);
        
        // Transform both
        FFTResult signalFFT = fft.transform(paddedSignal, true);
        FFTResult impulseFFT = fft.transform(paddedImpulse, true);
        
        // Multiply in frequency domain (convolution)
        double[] real = new double[fftSize];
        double[] imaginary = new double[fftSize];
        
        double[] signalReal = signalFFT.getRealParts();
        double[] signalImag = signalFFT.getImaginaryParts();
        double[] impulseReal = impulseFFT.getRealParts();
        double[] impulseImag = impulseFFT.getImaginaryParts();
        
        for (int i = 0; i < fftSize; i++) {
            real[i] = signalReal[i] * impulseReal[i] - signalImag[i] * impulseImag[i];
            imaginary[i] = signalReal[i] * impulseImag[i] + signalImag[i] * impulseReal[i];
        }
        
        // Inverse transform
        FFTResult convolved = fft.transform(real, imaginary, false);
        
        // Extract result and mix with dry signal
        double[] wet = new double[signal.length];
        double wetLevel = 0.3; // 30% wet signal
        double dryLevel = 0.7; // 70% dry signal
        
        double[] convolvedReal = convolved.getRealParts();
        for (int i = 0; i < signal.length; i++) {
            wet[i] = dryLevel * signal[i] + wetLevel * convolvedReal[i];
        }
        
        long endTime = System.nanoTime();
        
        ProcessingResult result = new ProcessingResult("reverb", signal, wet, endTime - startTime);
        result.addMetric("reverb_time", reverbTime);
        result.addMetric("impulse_length", impulseLength);
        result.addMetric("wet_level", wetLevel);
        processingHistory.add(result);
        
        return wet;
    }
    
    /**
     * Get list of available filters.
     */
    public Set<String> getAvailableFilters() {
        return new HashSet<>(filters.keySet());
    }
    
    /**
     * Get processing history.
     */
    public List<ProcessingResult> getProcessingHistory() {
        return new ArrayList<>(processingHistory);
    }
    
    /**
     * Clear processing history.
     */
    public void clearHistory() {
        processingHistory.clear();
    }
    
    /**
     * Get performance statistics from processing history.
     */
    public Map<String, Double> getPerformanceStats() {
        Map<String, List<Double>> operationTimes = new HashMap<>();
        
        for (ProcessingResult result : processingHistory) {
            operationTimes.computeIfAbsent(result.getOperationType(), k -> new ArrayList<>())
                         .add(result.getProcessingTimeMs());
        }
        
        Map<String, Double> stats = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : operationTimes.entrySet()) {
            List<Double> times = entry.getValue();
            double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            stats.put(entry.getKey() + "_avg_ms", avgTime);
            stats.put(entry.getKey() + "_count", (double) times.size());
        }
        
        return stats;
    }
}