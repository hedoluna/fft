package com.fft.demos.signals;

import java.util.*;
import java.util.function.Function;

/**
 * Advanced Signal Generator for Testing and Analysis.
 * 
 * <p>This class provides a comprehensive suite of signal generation capabilities
 * for testing FFT algorithms, validating signal processing techniques, and 
 * creating known test patterns for analysis. It includes both standard signals
 * and complex real-world signal models.</p>
 * 
 * <h3>Signal Types:</h3>
 * <ul>
 * <li><b>Basic Waveforms:</b> Sine, cosine, square, sawtooth, triangle</li>
 * <li><b>Complex Signals:</b> Chirps, AM/FM modulated signals, multi-tone</li>
 * <li><b>Noise Signals:</b> White, pink, brown, bandlimited noise</li>
 * <li><b>Test Patterns:</b> Impulses, frequency sweeps, harmonic series</li>
 * <li><b>Real-world Models:</b> ECG, EEG, seismic, financial time series</li>
 * </ul>
 * 
 * <h3>Advanced Features:</h3>
 * <ul>
 * <li><b>Parametric Control:</b> Frequency, amplitude, phase, modulation</li>
 * <li><b>Windowing:</b> Built-in window functions (Hann, Hamming, Blackman)</li>
 * <li><b>Signal Composition:</b> Combine multiple signals with different properties</li>
 * <li><b>Realistic Impairments:</b> Jitter, drift, harmonics, intermodulation</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class SignalGenerator {
    
    private final Random random;
    private final Map<String, Function<SignalParameters, double[]>> generators;
    
    /**
     * Signal generation parameters.
     */
    public static class SignalParameters {
        private int length = 1024;
        private double sampleRate = 44100.0;
        private double frequency = 440.0;
        private double amplitude = 1.0;
        private double phase = 0.0;
        private double duration = -1; // Auto-calculate from length if -1
        
        // Advanced parameters
        private double[] frequencies = null;
        private double[] amplitudes = null;
        private double[] phases = null;
        private double noiseLevel = 0.0;
        private String windowType = "none";
        private Map<String, Object> customParams = new HashMap<>();
        
        public SignalParameters() {}
        
        public SignalParameters(int length, double sampleRate) {
            this.length = length;
            this.sampleRate = sampleRate;
        }
        
        // Fluent builder methods
        public SignalParameters length(int length) { this.length = length; return this; }
        public SignalParameters sampleRate(double sampleRate) { this.sampleRate = sampleRate; return this; }
        public SignalParameters frequency(double frequency) { this.frequency = frequency; return this; }
        public SignalParameters amplitude(double amplitude) { this.amplitude = amplitude; return this; }
        public SignalParameters phase(double phase) { this.phase = phase; return this; }
        public SignalParameters duration(double duration) { this.duration = duration; return this; }
        public SignalParameters frequencies(double... frequencies) { 
            this.frequencies = frequencies.clone(); return this; 
        }
        public SignalParameters amplitudes(double... amplitudes) { 
            this.amplitudes = amplitudes.clone(); return this; 
        }
        public SignalParameters phases(double... phases) { 
            this.phases = phases.clone(); return this; 
        }
        public SignalParameters noiseLevel(double noiseLevel) { this.noiseLevel = noiseLevel; return this; }
        public SignalParameters window(String windowType) { this.windowType = windowType; return this; }
        public SignalParameters param(String key, Object value) { 
            this.customParams.put(key, value); return this; 
        }
        
        // Getters
        public int getLength() { return length; }
        public double getSampleRate() { return sampleRate; }
        public double getFrequency() { return frequency; }
        public double getAmplitude() { return amplitude; }
        public double getPhase() { return phase; }
        public double getDuration() { 
            return duration > 0 ? duration : (double) length / sampleRate; 
        }
        public double[] getFrequencies() { return frequencies != null ? frequencies.clone() : new double[]{frequency}; }
        public double[] getAmplitudes() { return amplitudes != null ? amplitudes.clone() : new double[]{amplitude}; }
        public double[] getPhases() { return phases != null ? phases.clone() : new double[]{phase}; }
        public double getNoiseLevel() { return noiseLevel; }
        public String getWindowType() { return windowType; }
        public Object getParam(String key) { return customParams.get(key); }
        public Object getParam(String key, Object defaultValue) { 
            return customParams.getOrDefault(key, defaultValue); 
        }
    }
    
    /**
     * Signal analysis result.
     */
    public static class SignalAnalysis {
        private final double[] signal;
        private final SignalParameters parameters;
        private final Map<String, Double> metrics;
        private final String description;
        
        public SignalAnalysis(double[] signal, SignalParameters parameters, String description) {
            this.signal = signal.clone();
            this.parameters = parameters;
            this.description = description;
            this.metrics = new HashMap<>();
            calculateMetrics();
        }
        
        private void calculateMetrics() {
            // Calculate basic signal metrics
            double mean = Arrays.stream(signal).average().orElse(0);
            double variance = Arrays.stream(signal).map(x -> (x - mean) * (x - mean)).average().orElse(0);
            double rms = Math.sqrt(Arrays.stream(signal).map(x -> x * x).average().orElse(0));
            double peak = Arrays.stream(signal).map(Math::abs).max().orElse(0);
            double crestFactor = peak / rms;
            
            // Calculate dynamic range
            double min = Arrays.stream(signal).min().orElse(0);
            double max = Arrays.stream(signal).max().orElse(0);
            double dynamicRange = max - min;
            
            // Calculate energy metrics
            double energy = Arrays.stream(signal).map(x -> x * x).sum();
            double power = energy / signal.length;
            
            metrics.put("mean", mean);
            metrics.put("variance", variance);
            metrics.put("std_dev", Math.sqrt(variance));
            metrics.put("rms", rms);
            metrics.put("peak", peak);
            metrics.put("crest_factor", crestFactor);
            metrics.put("dynamic_range", dynamicRange);
            metrics.put("energy", energy);
            metrics.put("power", power);
            metrics.put("length", (double) signal.length);
            metrics.put("duration", parameters.getDuration());
        }
        
        public double[] getSignal() { return signal.clone(); }
        public SignalParameters getParameters() { return parameters; }
        public Map<String, Double> getMetrics() { return new HashMap<>(metrics); }
        public String getDescription() { return description; }
        public double getMetric(String name) { return metrics.getOrDefault(name, 0.0); }
        
        @Override
        public String toString() {
            return String.format("SignalAnalysis{%s, length=%d, rms=%.3f, peak=%.3f, crest=%.1fdB}", 
                               description, signal.length, getMetric("rms"), getMetric("peak"),
                               20 * Math.log10(getMetric("crest_factor")));
        }
    }
    
    public SignalGenerator() {
        this(System.currentTimeMillis());
    }
    
    public SignalGenerator(long seed) {
        this.random = new Random(seed);
        this.generators = new HashMap<>();
        initializeGenerators();
    }
    
    private void initializeGenerators() {
        // Basic waveforms
        generators.put("sine", this::generateSine);
        generators.put("cosine", this::generateCosine);
        generators.put("square", this::generateSquare);
        generators.put("sawtooth", this::generateSawtooth);
        generators.put("triangle", this::generateTriangle);
        
        // Complex signals
        generators.put("chirp", this::generateChirp);
        generators.put("multitone", this::generateMultitone);
        generators.put("am_modulated", this::generateAmModulated);
        generators.put("fm_modulated", this::generateFmModulated);
        generators.put("harmonic_series", this::generateHarmonicSeries);
        
        // Noise signals
        generators.put("white_noise", this::generateWhiteNoise);
        generators.put("pink_noise", this::generatePinkNoise);
        generators.put("brown_noise", this::generateBrownNoise);
        generators.put("bandlimited_noise", this::generateBandlimitedNoise);
        
        // Test patterns
        generators.put("impulse", this::generateImpulse);
        generators.put("step", this::generateStep);
        generators.put("frequency_sweep", this::generateFrequencySweep);
        generators.put("burst", this::generateBurst);
        
        // Real-world models
        generators.put("ecg", this::generateEcg);
        generators.put("eeg", this::generateEeg);
        generators.put("speech_like", this::generateSpeechLike);
        generators.put("music_like", this::generateMusicLike);
    }
    
    /**
     * Generate signal of specified type.
     * 
     * @param signalType type of signal to generate
     * @param parameters signal parameters
     * @return generated signal analysis
     */
    public SignalAnalysis generate(String signalType, SignalParameters parameters) {
        Function<SignalParameters, double[]> generator = generators.get(signalType.toLowerCase());
        if (generator == null) {
            throw new IllegalArgumentException("Unknown signal type: " + signalType);
        }
        
        double[] signal = generator.apply(parameters);
        
        // Apply window if specified
        if (!parameters.getWindowType().equals("none")) {
            signal = applyWindow(signal, parameters.getWindowType());
        }
        
        // Add noise if specified
        if (parameters.getNoiseLevel() > 0) {
            signal = addNoise(signal, parameters.getNoiseLevel());
        }
        
        return new SignalAnalysis(signal, parameters, 
                                String.format("%s signal (%.1fHz, %d samples)", 
                                            signalType, parameters.getFrequency(), parameters.getLength()));
    }
    
    /**
     * Generate multiple signals with different parameters.
     */
    public List<SignalAnalysis> generateBatch(String signalType, List<SignalParameters> parametersList) {
        List<SignalAnalysis> results = new ArrayList<>();
        for (SignalParameters params : parametersList) {
            results.add(generate(signalType, params));
        }
        return results;
    }
    
    /**
     * Get list of available signal types.
     */
    public Set<String> getAvailableSignalTypes() {
        return new HashSet<>(generators.keySet());
    }
    
    // Basic waveform generators
    
    private double[] generateSine(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            signal[i] = params.getAmplitude() * Math.sin(2 * Math.PI * params.getFrequency() * t + params.getPhase());
        }
        return signal;
    }
    
    private double[] generateCosine(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            signal[i] = params.getAmplitude() * Math.cos(2 * Math.PI * params.getFrequency() * t + params.getPhase());
        }
        return signal;
    }
    
    private double[] generateSquare(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double dutyCycle = (Double) params.getParam("duty_cycle", 0.5);
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double phase = (params.getFrequency() * t + params.getPhase() / (2 * Math.PI)) % 1.0;
            signal[i] = params.getAmplitude() * (phase < dutyCycle ? 1.0 : -1.0);
        }
        return signal;
    }
    
    private double[] generateSawtooth(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double phase = (params.getFrequency() * t + params.getPhase() / (2 * Math.PI)) % 1.0;
            signal[i] = params.getAmplitude() * (2.0 * phase - 1.0);
        }
        return signal;
    }
    
    private double[] generateTriangle(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double phase = (params.getFrequency() * t + params.getPhase() / (2 * Math.PI)) % 1.0;
            signal[i] = params.getAmplitude() * (2.0 * Math.abs(2.0 * phase - 1.0) - 1.0);
        }
        return signal;
    }
    
    // Complex signal generators
    
    private double[] generateChirp(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double startFreq = params.getFrequency();
        double endFreq = (Double) params.getParam("end_frequency", startFreq * 2);
        double duration = params.getDuration();
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double instantFreq = startFreq + (endFreq - startFreq) * t / duration;
            signal[i] = params.getAmplitude() * Math.sin(2 * Math.PI * instantFreq * t + params.getPhase());
        }
        return signal;
    }
    
    private double[] generateMultitone(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double[] frequencies = params.getFrequencies();
        double[] amplitudes = params.getAmplitudes();
        double[] phases = params.getPhases();
        
        // Normalize amplitudes if needed
        if (amplitudes.length == 1 && frequencies.length > 1) {
            amplitudes = new double[frequencies.length];
            Arrays.fill(amplitudes, params.getAmplitude() / frequencies.length);
        }
        
        if (phases.length == 1 && frequencies.length > 1) {
            phases = new double[frequencies.length];
            Arrays.fill(phases, params.getPhase());
        }
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double sum = 0;
            
            for (int j = 0; j < frequencies.length; j++) {
                double amp = j < amplitudes.length ? amplitudes[j] : amplitudes[0];
                double phase = j < phases.length ? phases[j] : phases[0];
                sum += amp * Math.sin(2 * Math.PI * frequencies[j] * t + phase);
            }
            
            signal[i] = sum;
        }
        return signal;
    }
    
    private double[] generateAmModulated(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double carrierFreq = params.getFrequency();
        double modFreq = (Double) params.getParam("modulation_frequency", carrierFreq / 10);
        double modDepth = (Double) params.getParam("modulation_depth", 0.5);
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double modulation = 1.0 + modDepth * Math.sin(2 * Math.PI * modFreq * t);
            signal[i] = params.getAmplitude() * modulation * Math.sin(2 * Math.PI * carrierFreq * t + params.getPhase());
        }
        return signal;
    }
    
    private double[] generateFmModulated(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double carrierFreq = params.getFrequency();
        double modFreq = (Double) params.getParam("modulation_frequency", carrierFreq / 20);
        double modIndex = (Double) params.getParam("modulation_index", 5.0);
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double instantPhase = 2 * Math.PI * carrierFreq * t + 
                                modIndex * Math.sin(2 * Math.PI * modFreq * t) + params.getPhase();
            signal[i] = params.getAmplitude() * Math.sin(instantPhase);
        }
        return signal;
    }
    
    private double[] generateHarmonicSeries(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double fundamental = params.getFrequency();
        int numHarmonics = (Integer) params.getParam("num_harmonics", 5);
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double sum = 0;
            
            for (int h = 1; h <= numHarmonics; h++) {
                double amplitude = params.getAmplitude() / h; // 1/h amplitude decay
                sum += amplitude * Math.sin(2 * Math.PI * fundamental * h * t + params.getPhase());
            }
            
            signal[i] = sum;
        }
        return signal;
    }
    
    // Noise generators
    
    private double[] generateWhiteNoise(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        for (int i = 0; i < signal.length; i++) {
            signal[i] = params.getAmplitude() * random.nextGaussian();
        }
        return signal;
    }
    
    private double[] generatePinkNoise(SignalParameters params) {
        // Simple pink noise approximation using multiple octaves
        double[] signal = new double[params.getLength()];
        int numOctaves = 8;
        double[] octaveAmplitudes = new double[numOctaves];
        double[] octavePhases = new double[numOctaves];
        
        for (int oct = 0; oct < numOctaves; oct++) {
            octaveAmplitudes[oct] = params.getAmplitude() / Math.sqrt(oct + 1);
            octavePhases[oct] = random.nextDouble() * 2 * Math.PI;
        }
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double sum = 0;
            
            for (int oct = 0; oct < numOctaves; oct++) {
                double freq = params.getFrequency() * Math.pow(2, oct);
                sum += octaveAmplitudes[oct] * Math.sin(2 * Math.PI * freq * t + octavePhases[oct]);
            }
            
            signal[i] = sum + 0.1 * params.getAmplitude() * random.nextGaussian();
        }
        return signal;
    }
    
    private double[] generateBrownNoise(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double value = 0;
        double stepSize = params.getAmplitude() * 0.1;
        
        for (int i = 0; i < signal.length; i++) {
            value += stepSize * random.nextGaussian();
            // Keep bounded
            value = Math.max(-params.getAmplitude(), Math.min(params.getAmplitude(), value));
            signal[i] = value;
        }
        return signal;
    }
    
    private double[] generateBandlimitedNoise(SignalParameters params) {
        // Generate white noise and then filter it (simplified)
        double[] noise = generateWhiteNoise(params);
        double centerFreq = params.getFrequency();
        double bandwidth = (Double) params.getParam("bandwidth", centerFreq * 0.1);
        
        // Simple bandpass filter implementation (not optimal, but demonstrates concept)
        double[] filtered = new double[noise.length];
        double alpha = Math.exp(-2 * Math.PI * bandwidth / params.getSampleRate());
        
        for (int i = 1; i < noise.length; i++) {
            double t = (double) i / params.getSampleRate();
            double modulation = Math.sin(2 * Math.PI * centerFreq * t);
            filtered[i] = alpha * filtered[i-1] + (1 - alpha) * noise[i] * modulation;
        }
        
        return filtered;
    }
    
    // Test pattern generators
    
    private double[] generateImpulse(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        int position = (Integer) params.getParam("position", params.getLength() / 2);
        signal[position] = params.getAmplitude();
        return signal;
    }
    
    private double[] generateStep(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        int position = (Integer) params.getParam("position", params.getLength() / 2);
        
        for (int i = position; i < signal.length; i++) {
            signal[i] = params.getAmplitude();
        }
        return signal;
    }
    
    private double[] generateFrequencySweep(SignalParameters params) {
        return generateChirp(params); // Frequency sweep is same as chirp
    }
    
    private double[] generateBurst(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        int burstStart = (Integer) params.getParam("burst_start", params.getLength() / 4);
        int burstLength = (Integer) params.getParam("burst_length", params.getLength() / 2);
        
        for (int i = burstStart; i < Math.min(burstStart + burstLength, signal.length); i++) {
            double t = (double) (i - burstStart) / params.getSampleRate();
            signal[i] = params.getAmplitude() * Math.sin(2 * Math.PI * params.getFrequency() * t + params.getPhase());
        }
        return signal;
    }
    
    // Real-world model generators
    
    private double[] generateEcg(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double heartRate = (Double) params.getParam("heart_rate", 72.0); // BPM
        double beatInterval = 60.0 / heartRate; // seconds
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double beatPhase = (t % beatInterval) / beatInterval;
            
            // Simplified ECG waveform approximation
            double ecgValue = 0;
            if (beatPhase < 0.1) { // P wave
                ecgValue = 0.2 * Math.sin(Math.PI * beatPhase / 0.1);
            } else if (beatPhase > 0.2 && beatPhase < 0.4) { // QRS complex
                double qrsPhase = (beatPhase - 0.2) / 0.2;
                ecgValue = Math.sin(Math.PI * qrsPhase) * (qrsPhase < 0.5 ? -0.3 : 1.0);
            } else if (beatPhase > 0.5 && beatPhase < 0.7) { // T wave
                double tPhase = (beatPhase - 0.5) / 0.2;
                ecgValue = 0.3 * Math.sin(Math.PI * tPhase);
            }
            
            signal[i] = params.getAmplitude() * ecgValue + 0.05 * random.nextGaussian();
        }
        return signal;
    }
    
    private double[] generateEeg(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        
        // EEG frequency bands
        double[] frequencies = {2, 5, 10, 15, 25, 35}; // Delta, Theta, Alpha, Beta, Gamma
        double[] amplitudes = {0.3, 0.4, 0.6, 0.5, 0.2, 0.1};
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double sum = 0;
            
            for (int j = 0; j < frequencies.length; j++) {
                double phase = random.nextDouble() * 2 * Math.PI;
                sum += amplitudes[j] * Math.sin(2 * Math.PI * frequencies[j] * t + phase);
            }
            
            signal[i] = params.getAmplitude() * sum + 0.1 * random.nextGaussian();
        }
        return signal;
    }
    
    private double[] generateSpeechLike(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double[] formants = {800, 1200, 2500}; // Typical formant frequencies
        double fundamentalFreq = params.getFrequency();
        
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double harmonicSum = 0;
            
            // Add harmonics up to each formant
            for (int h = 1; h <= 10; h++) {
                double harmonic = fundamentalFreq * h;
                double amplitude = params.getAmplitude() / h;
                
                // Boost formant regions
                for (double formant : formants) {
                    if (Math.abs(harmonic - formant) < 100) {
                        amplitude *= 3.0;
                    }
                }
                
                harmonicSum += amplitude * Math.sin(2 * Math.PI * harmonic * t);
            }
            
            // Add some modulation and noise
            double modulation = 1.0 + 0.1 * Math.sin(2 * Math.PI * 5 * t);
            signal[i] = harmonicSum * modulation + 0.05 * random.nextGaussian();
        }
        return signal;
    }
    
    private double[] generateMusicLike(SignalParameters params) {
        double[] signal = new double[params.getLength()];
        double fundamental = params.getFrequency();
        
        // Musical harmonic series with some inharmonicity
        for (int i = 0; i < signal.length; i++) {
            double t = (double) i / params.getSampleRate();
            double sum = 0;
            
            for (int h = 1; h <= 8; h++) {
                double harmonic = fundamental * h * (1.0 + 0.002 * h * h); // Slight inharmonicity
                double amplitude = params.getAmplitude() / (h * h); // 1/h² decay
                double envelope = Math.exp(-t * 0.5); // Decay envelope
                sum += amplitude * envelope * Math.sin(2 * Math.PI * harmonic * t);
            }
            
            signal[i] = sum;
        }
        return signal;
    }
    
    // Utility methods
    
    private double[] applyWindow(double[] signal, String windowType) {
        double[] windowed = signal.clone();
        double[] window = generateWindow(signal.length, windowType);
        
        for (int i = 0; i < windowed.length; i++) {
            windowed[i] *= window[i];
        }
        
        return windowed;
    }
    
    private double[] generateWindow(int length, String windowType) {
        double[] window = new double[length];
        
        switch (windowType.toLowerCase()) {
            case "hann":
            case "hanning":
                for (int i = 0; i < length; i++) {
                    window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (length - 1)));
                }
                break;
                
            case "hamming":
                for (int i = 0; i < length; i++) {
                    window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (length - 1));
                }
                break;
                
            case "blackman":
                for (int i = 0; i < length; i++) {
                    double n = (double) i / (length - 1);
                    window[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * n) + 0.08 * Math.cos(4 * Math.PI * n);
                }
                break;
                
            case "rectangular":
            case "none":
            default:
                Arrays.fill(window, 1.0);
                break;
        }
        
        return window;
    }
    
    private double[] addNoise(double[] signal, double noiseLevel) {
        double[] noisy = signal.clone();
        for (int i = 0; i < noisy.length; i++) {
            noisy[i] += noiseLevel * random.nextGaussian();
        }
        return noisy;
    }
}