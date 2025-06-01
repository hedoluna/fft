package com.fft.demos.audio;

import com.fft.utils.FFTUtils;

/**
 * Interactive Audio Processing Demo Application.
 * 
 * <p>This demonstration application showcases the audio signal processing capabilities
 * of the FFT library through practical examples and interactive scenarios.</p>
 * 
 * <h3>Demo Scenarios:</h3>
 * <ul>
 * <li><b>Scenario 1:</b> Music Enhancement - Remove noise and enhance vocals</li>
 * <li><b>Scenario 2:</b> Audio Analysis - Analyze harmonic content of musical signals</li>
 * <li><b>Scenario 3:</b> Effects Processing - Add reverb and apply filters</li>
 * <li><b>Scenario 4:</b> Real-time Simulation - Process continuous audio streams</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class AudioDemo {
    
    private static final double SAMPLE_RATE = 44100.0; // CD quality
    private static final AudioSignalProcessor processor = new AudioSignalProcessor();
    
    public static void main(String[] args) {
        System.out.println("=== Audio Signal Processing Demo ===\n");
        
        runScenario1_MusicEnhancement();
        runScenario2_AudioAnalysis();
        runScenario3_EffectsProcessing();
        runScenario4_RealtimeSimulation();
        
        printPerformanceSummary();
    }
    
    /**
     * Scenario 1: Music Enhancement
     * Demonstrates noise reduction and vocal enhancement on a simulated music signal.
     */
    private static void runScenario1_MusicEnhancement() {
        System.out.println("=== Scenario 1: Music Enhancement ===");
        
        // Generate a complex music-like signal with noise
        double[] musicSignal = generateMusicSignal(4.0); // 4 seconds
        double[] noiseProfile = generateNoise(0.5); // 0.5 seconds noise profile
        double[] noisyMusic = addNoise(musicSignal, 0.15); // 15% noise level
        
        System.out.printf("Original signal: %.0f samples (%.1fs)\n", 
                         (double) musicSignal.length, musicSignal.length / SAMPLE_RATE);
        
        // Apply noise reduction
        double[] denoisedMusic = processor.reduceNoise(noisyMusic, noiseProfile, SAMPLE_RATE, 2.0);
        
        // Apply vocal enhancement filter
        double[] enhancedMusic = processor.applyFilter(denoisedMusic, "vocal", null, SAMPLE_RATE);
        
        // Analyze results
        AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(musicSignal, SAMPLE_RATE);
        AudioSignalProcessor.AudioMetrics noisyMetrics = processor.analyzeSignal(noisyMusic, SAMPLE_RATE);
        AudioSignalProcessor.AudioMetrics enhancedMetrics = processor.analyzeSignal(enhancedMusic, SAMPLE_RATE);
        
        System.out.println("\nSignal Analysis Results:");
        System.out.printf("  Original:  %s\n", originalMetrics);
        System.out.printf("  Noisy:     %s\n", noisyMetrics);
        System.out.printf("  Enhanced:  %s\n", enhancedMetrics);
        
        // Calculate improvement metrics
        double noiseReduction = (noisyMetrics.getRmsEnergy() - enhancedMetrics.getRmsEnergy()) / 
                               noisyMetrics.getRmsEnergy() * 100;
        double spectralRecovery = 1.0 - Math.abs(enhancedMetrics.getSpectralCentroid() - 
                                                originalMetrics.getSpectralCentroid()) / 
                                       originalMetrics.getSpectralCentroid();
        
        System.out.printf("\nEnhancement Results:\n");
        System.out.printf("  Noise Reduction: %.1f%%\n", noiseReduction);
        System.out.printf("  Spectral Recovery: %.1f%%\n", spectralRecovery * 100);
        System.out.println();
    }
    
    /**
     * Scenario 2: Audio Analysis
     * Demonstrates comprehensive audio analysis on various signal types.
     */
    private static void runScenario2_AudioAnalysis() {
        System.out.println("=== Scenario 2: Audio Analysis ===");
        
        // Generate different types of audio signals
        double[] pianoNote = generatePianoNote(440.0, 2.0); // A4 for 2 seconds
        double[] vocalSound = generateVocalSound(200.0, 1.5); // ~200Hz fundamental
        double[] drumHit = generateDrumHit(0.2); // 200ms drum hit
        
        System.out.println("Analyzing different audio signal types:\n");
        
        // Analyze each signal type
        analyzeAndPrint("Piano Note (A4)", pianoNote);
        analyzeAndPrint("Vocal Sound", vocalSound);
        analyzeAndPrint("Drum Hit", drumHit);
        
        // Demonstrate harmonic analysis
        System.out.println("=== Harmonic Analysis ===");
        AudioSignalProcessor.AudioMetrics pianoMetrics = processor.analyzeSignal(pianoNote, SAMPLE_RATE);
        System.out.printf("Piano harmonics: %s\n", pianoMetrics.getHarmonicPeaks());
        System.out.printf("Harmonic intervals: ");
        
        if (pianoMetrics.getHarmonicPeaks().size() > 1) {
            double fundamental = pianoMetrics.getHarmonicPeaks().get(0);
            for (int i = 1; i < Math.min(5, pianoMetrics.getHarmonicPeaks().size()); i++) {
                double ratio = pianoMetrics.getHarmonicPeaks().get(i) / fundamental;
                System.out.printf("%.1fx ", ratio);
            }
        }
        System.out.println("\n");
    }
    
    /**
     * Scenario 3: Effects Processing
     * Demonstrates audio effects using FFT-based processing.
     */
    private static void runScenario3_EffectsProcessing() {
        System.out.println("=== Scenario 3: Effects Processing ===");
        
        // Generate a clean guitar-like signal
        double[] guitarSignal = generateGuitarSound(330.0, 3.0); // E4 for 3 seconds
        
        System.out.printf("Processing guitar signal: %.0f samples\n", (double) guitarSignal.length);
        
        // Apply different filters
        System.out.println("\nApplying filters:");
        double[] lowPassFiltered = processor.applyFilter(guitarSignal, "lowpass", null, SAMPLE_RATE);
        double[] highPassFiltered = processor.applyFilter(guitarSignal, "highpass", null, SAMPLE_RATE);
        double[] notchFiltered = processor.applyFilter(guitarSignal, "notch60", null, SAMPLE_RATE);
        
        // Add reverb effects
        System.out.println("Adding reverb effects:");
        double[] shortReverb = processor.addReverb(guitarSignal, 0.5, SAMPLE_RATE); // 0.5s reverb
        double[] longReverb = processor.addReverb(guitarSignal, 2.0, SAMPLE_RATE);  // 2.0s reverb
        
        // Analyze the effects
        analyzeEffects("Original", guitarSignal);
        analyzeEffects("Low-pass", lowPassFiltered);
        analyzeEffects("High-pass", highPassFiltered);
        analyzeEffects("60Hz Notch", notchFiltered);
        analyzeEffects("Short Reverb", shortReverb);
        analyzeEffects("Long Reverb", longReverb);
        
        System.out.println();
    }
    
    /**
     * Scenario 4: Real-time Processing Simulation
     * Simulates real-time audio processing with performance monitoring.
     */
    private static void runScenario4_RealtimeSimulation() {
        System.out.println("=== Scenario 4: Real-time Processing Simulation ===");
        
        int bufferSize = 1024; // Typical audio buffer size
        int numBuffers = 50;   // Simulate 50 buffers
        double bufferDurationMs = bufferSize * 1000.0 / SAMPLE_RATE;
        
        System.out.printf("Simulating real-time processing:\n");
        System.out.printf("  Buffer size: %d samples (%.1fms)\n", bufferSize, bufferDurationMs);
        System.out.printf("  Processing %d buffers...\n\n", numBuffers);
        
        long totalProcessingTime = 0;
        double maxProcessingTime = 0;
        int droppedBuffers = 0;
        
        for (int i = 0; i < numBuffers; i++) {
            // Generate buffer of audio data
            double[] buffer = generateRandomAudioBuffer(bufferSize);
            
            long startTime = System.nanoTime();
            
            // Simulate typical real-time processing chain
            double[] filtered = processor.applyFilter(buffer, "vocal", null, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(filtered, SAMPLE_RATE);
            
            long endTime = System.nanoTime();
            double processingTimeMs = (endTime - startTime) / 1_000_000.0;
            
            totalProcessingTime += (endTime - startTime);
            maxProcessingTime = Math.max(maxProcessingTime, processingTimeMs);
            
            // Check if processing exceeds buffer duration (would cause dropout)
            if (processingTimeMs > bufferDurationMs) {
                droppedBuffers++;
            }
            
            if (i % 10 == 0) {
                System.out.printf("  Buffer %2d: %.2fms processing (%.1f%% CPU)\n", 
                                 i, processingTimeMs, processingTimeMs / bufferDurationMs * 100);
            }
        }
        
        double avgProcessingTimeMs = totalProcessingTime / 1_000_000.0 / numBuffers;
        double avgCpuUsage = avgProcessingTimeMs / bufferDurationMs * 100;
        
        System.out.printf("\nReal-time Performance Summary:\n");
        System.out.printf("  Average processing time: %.2fms (%.1f%% CPU)\n", 
                         avgProcessingTimeMs, avgCpuUsage);
        System.out.printf("  Maximum processing time: %.2fms\n", maxProcessingTime);
        System.out.printf("  Dropped buffers: %d/%d (%.1f%%)\n", 
                         droppedBuffers, numBuffers, (double) droppedBuffers / numBuffers * 100);
        System.out.printf("  Real-time capable: %s\n", 
                         droppedBuffers == 0 ? "YES" : "NO");
        System.out.println();
    }
    
    private static void analyzeAndPrint(String name, double[] signal) {
        AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(signal, SAMPLE_RATE);
        System.out.printf("  %s: %s\n", name, metrics);
    }
    
    private static void analyzeEffects(String effectName, double[] signal) {
        AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(signal, SAMPLE_RATE);
        System.out.printf("  %-12s: Centroid=%.0fHz, RMS=%.3f, ZCR=%.3f\n", 
                         effectName, metrics.getSpectralCentroid(), 
                         metrics.getRmsEnergy(), metrics.getZeroCrossingRate());
    }
    
    private static void printPerformanceSummary() {
        System.out.println("=== Performance Summary ===");
        
        var stats = processor.getPerformanceStats();
        for (var entry : stats.entrySet()) {
            if (entry.getKey().endsWith("_avg_ms")) {
                String operation = entry.getKey().replace("_avg_ms", "");
                double avgTime = entry.getValue();
                double count = stats.getOrDefault(operation + "_count", 0.0);
                System.out.printf("  %-20s: %.2fms average (%.0f operations)\n", 
                                 operation, avgTime, count);
            }
        }
        
        System.out.printf("\nTotal operations processed: %d\n", processor.getProcessingHistory().size());
    }
    
    // Signal generation utilities
    
    private static double[] generateMusicSignal(double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] signal = new double[length];
        
        // Fundamental frequency (guitar E string)
        double fundamental = 82.4; // Low E
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            
            // Add harmonics with decay
            signal[i] = 0.5 * Math.sin(2 * Math.PI * fundamental * t) * Math.exp(-t * 0.3);
            signal[i] += 0.3 * Math.sin(2 * Math.PI * fundamental * 2 * t) * Math.exp(-t * 0.5);
            signal[i] += 0.2 * Math.sin(2 * Math.PI * fundamental * 3 * t) * Math.exp(-t * 0.7);
            signal[i] += 0.1 * Math.sin(2 * Math.PI * fundamental * 5 * t) * Math.exp(-t * 1.0);
            
            // Add some vibrato
            double vibrato = 1.0 + 0.02 * Math.sin(2 * Math.PI * 5.0 * t);
            signal[i] *= vibrato;
        }
        
        return signal;
    }
    
    private static double[] generatePianoNote(double frequency, double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] signal = new double[length];
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = Math.exp(-t * 1.5); // Piano-like decay
            
            // Piano harmonics
            signal[i] = envelope * (
                0.6 * Math.sin(2 * Math.PI * frequency * t) +
                0.3 * Math.sin(2 * Math.PI * frequency * 2 * t) +
                0.2 * Math.sin(2 * Math.PI * frequency * 3 * t) +
                0.1 * Math.sin(2 * Math.PI * frequency * 4 * t) +
                0.05 * Math.sin(2 * Math.PI * frequency * 5 * t)
            );
        }
        
        return signal;
    }
    
    private static double[] generateVocalSound(double fundamentalFreq, double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] signal = new double[length];
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            
            // Vocal formants (simplified)
            double formant1 = fundamentalFreq * 3;  // First formant
            double formant2 = fundamentalFreq * 7;  // Second formant
            
            signal[i] = 
                0.4 * Math.sin(2 * Math.PI * fundamentalFreq * t) +
                0.3 * Math.sin(2 * Math.PI * fundamentalFreq * 2 * t) +
                0.2 * Math.sin(2 * Math.PI * formant1 * t) * Math.exp(-Math.abs(t - durationSeconds/2) * 2) +
                0.1 * Math.sin(2 * Math.PI * formant2 * t) * Math.exp(-Math.abs(t - durationSeconds/2) * 3);
        }
        
        return signal;
    }
    
    private static double[] generateDrumHit(double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] signal = new double[length];
        
        java.util.Random random = new java.util.Random(123);
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = Math.exp(-t * 15); // Very fast decay
            
            // Mix of pitched and noise components
            double pitched = Math.sin(2 * Math.PI * 60 * t * Math.exp(-t * 10)); // Pitch sweep down
            double noise = random.nextGaussian();
            
            signal[i] = envelope * (0.3 * pitched + 0.7 * noise);
        }
        
        return signal;
    }
    
    private static double[] generateGuitarSound(double frequency, double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] signal = new double[length];
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = Math.exp(-t * 0.8); // Guitar-like sustain
            
            // Guitar-like harmonics with some inharmonicity
            signal[i] = envelope * (
                0.5 * Math.sin(2 * Math.PI * frequency * t) +
                0.4 * Math.sin(2 * Math.PI * frequency * 2.02 * t) + // Slightly inharmonic
                0.3 * Math.sin(2 * Math.PI * frequency * 3.01 * t) +
                0.2 * Math.sin(2 * Math.PI * frequency * 4.03 * t) +
                0.1 * Math.sin(2 * Math.PI * frequency * 5.02 * t)
            );
        }
        
        return signal;
    }
    
    private static double[] generateNoise(double durationSeconds) {
        int length = (int) (durationSeconds * SAMPLE_RATE);
        double[] noise = new double[length];
        java.util.Random random = new java.util.Random(456);
        
        for (int i = 0; i < length; i++) {
            noise[i] = random.nextGaussian() * 0.1;
        }
        
        return noise;
    }
    
    private static double[] addNoise(double[] signal, double noiseLevel) {
        double[] noisy = signal.clone();
        java.util.Random random = new java.util.Random(789);
        
        for (int i = 0; i < noisy.length; i++) {
            noisy[i] += random.nextGaussian() * noiseLevel;
        }
        
        return noisy;
    }
    
    private static double[] generateRandomAudioBuffer(int size) {
        double[] buffer = new double[size];
        java.util.Random random = new java.util.Random();
        
        // Generate audio-like signal (band-limited noise + tone)
        double frequency = 200 + random.nextDouble() * 800; // 200-1000 Hz
        
        for (int i = 0; i < size; i++) {
            double t = (double) i / SAMPLE_RATE;
            buffer[i] = 0.3 * Math.sin(2 * Math.PI * frequency * t) + 
                       0.1 * random.nextGaussian();
        }
        
        return buffer;
    }
}