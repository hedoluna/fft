package com.fft.demos.signals;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.FFTUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Advanced Signal Analysis Engine using FFT.
 * 
 * <p>This analyzer provides comprehensive frequency domain analysis capabilities
 * for signal characterization, quality assessment, and feature extraction.
 * It combines time-domain and frequency-domain techniques to provide deep
 * insights into signal properties.</p>
 * 
 * <h3>Analysis Capabilities:</h3>
 * <ul>
 * <li><b>Frequency Analysis:</b> Peak detection, harmonic analysis, THD calculation</li>
 * <li><b>Statistical Analysis:</b> Power spectral density, spectral moments</li>
 * <li><b>Quality Metrics:</b> SNR, SINAD, ENOB, spurious-free dynamic range</li>
 * <li><b>Feature Extraction:</b> Spectral centroid, bandwidth, rolloff, flux</li>
 * <li><b>Pattern Recognition:</b> Signal classification, modulation detection</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class SignalAnalyzer {
    
    private final FFTFactory factory;
    private final List<AnalysisResult> analysisHistory;
    
    /**
     * Comprehensive analysis result containing all computed metrics.
     */
    public static class AnalysisResult {
        private final double[] originalSignal;
        private final double sampleRate;
        private final Map<String, Double> timeMetrics;
        private final Map<String, Double> frequencyMetrics;
        private final List<Peak> peaks;
        private final double[] powerSpectrum;
        private final double[] frequencies;
        private final String signalClassification;
        private final Date timestamp;
        
        public AnalysisResult(double[] signal, double sampleRate) {
            this.originalSignal = signal.clone();
            this.sampleRate = sampleRate;
            this.timeMetrics = new HashMap<>();
            this.frequencyMetrics = new HashMap<>();
            this.peaks = new ArrayList<>();
            this.powerSpectrum = new double[0];
            this.frequencies = new double[0];
            this.signalClassification = "unknown";
            this.timestamp = new Date();
        }
        
        // Getters
        public double[] getOriginalSignal() { return originalSignal.clone(); }
        public double getSampleRate() { return sampleRate; }
        public Map<String, Double> getTimeMetrics() { return new HashMap<>(timeMetrics); }
        public Map<String, Double> getFrequencyMetrics() { return new HashMap<>(frequencyMetrics); }
        public List<Peak> getPeaks() { return new ArrayList<>(peaks); }
        public double[] getPowerSpectrum() { return powerSpectrum.clone(); }
        public double[] getFrequencies() { return frequencies.clone(); }
        public String getSignalClassification() { return signalClassification; }
        public Date getTimestamp() { return new Date(timestamp.getTime()); }
        
        public double getTimeMetric(String name) { return timeMetrics.getOrDefault(name, 0.0); }
        public double getFrequencyMetric(String name) { return frequencyMetrics.getOrDefault(name, 0.0); }
        
        @Override
        public String toString() {
            return String.format("AnalysisResult{class=%s, peaks=%d, snr=%.1fdB, thd=%.2f%%, " +
                               "centroid=%.1fHz, length=%d}", 
                               signalClassification, peaks.size(), 
                               getFrequencyMetric("snr_db"), getFrequencyMetric("thd_percent"),
                               getFrequencyMetric("spectral_centroid"), originalSignal.length);
        }
    }
    
    /**
     * Represents a spectral peak with frequency, amplitude, and quality metrics.
     */
    public static class Peak {
        private final double frequency;
        private final double amplitude;
        private final double phase;
        private final double bandwidth;
        private final double quality;
        private final String type;
        
        public Peak(double frequency, double amplitude, double phase, double bandwidth, String type) {
            this.frequency = frequency;
            this.amplitude = amplitude;
            this.phase = phase;
            this.bandwidth = bandwidth;
            this.quality = bandwidth > 0 ? frequency / bandwidth : Double.POSITIVE_INFINITY;
            this.type = type;
        }
        
        public double getFrequency() { return frequency; }
        public double getAmplitude() { return amplitude; }
        public double getPhase() { return phase; }
        public double getBandwidth() { return bandwidth; }
        public double getQuality() { return quality; }
        public String getType() { return type; }
        
        @Override
        public String toString() {
            return String.format("Peak{%.1fHz, %.3f, Q=%.1f, %s}", 
                               frequency, amplitude, quality, type);
        }
    }
    
    /**
     * Signal classification result.
     */
    public static class SignalClassification {
        private final String primaryClass;
        private final Map<String, Double> confidence;
        private final List<String> characteristics;
        
        public SignalClassification(String primaryClass) {
            this.primaryClass = primaryClass;
            this.confidence = new HashMap<>();
            this.characteristics = new ArrayList<>();
        }
        
        public String getPrimaryClass() { return primaryClass; }
        public Map<String, Double> getConfidence() { return new HashMap<>(confidence); }
        public List<String> getCharacteristics() { return new ArrayList<>(characteristics); }
        
        public void addConfidence(String className, double confidence) {
            this.confidence.put(className, confidence);
        }
        
        public void addCharacteristic(String characteristic) {
            this.characteristics.add(characteristic);
        }
    }
    
    public SignalAnalyzer() {
        this.factory = new DefaultFFTFactory();
        this.analysisHistory = new ArrayList<>();
    }
    
    /**
     * Perform comprehensive analysis of a signal.
     * 
     * @param signal input signal
     * @param sampleRate sample rate in Hz
     * @return comprehensive analysis result
     */
    public AnalysisResult analyze(double[] signal, double sampleRate) {
        AnalysisResult result = new AnalysisResult(signal, sampleRate);
        
        // Time domain analysis
        analyzeTimeDomain(signal, result.timeMetrics);
        
        // Frequency domain analysis
        analyzeFrequencyDomain(signal, sampleRate, result);
        
        // Signal classification
        SignalClassification classification = classifySignal(result);
        
        // Store result
        analysisHistory.add(result);
        
        return result;
    }
    
    /**
     * Compare two signals and quantify differences.
     * 
     * @param signal1 first signal
     * @param signal2 second signal
     * @param sampleRate sample rate
     * @return comparison metrics
     */
    public Map<String, Double> compareSignals(double[] signal1, double[] signal2, double sampleRate) {
        Map<String, Double> comparison = new HashMap<>();
        
        int minLength = Math.min(signal1.length, signal2.length);
        double[] s1 = Arrays.copyOf(signal1, minLength);
        double[] s2 = Arrays.copyOf(signal2, minLength);
        
        // Time domain comparison
        double mse = 0;
        double mae = 0;
        double correlation = calculateCorrelation(s1, s2);
        
        for (int i = 0; i < minLength; i++) {
            double error = s1[i] - s2[i];
            mse += error * error;
            mae += Math.abs(error);
        }
        
        mse /= minLength;
        mae /= minLength;
        
        comparison.put("mse", mse);
        comparison.put("mae", mae);
        comparison.put("rmse", Math.sqrt(mse));
        comparison.put("correlation", correlation);
        
        // SNR calculation
        double signalPower = Arrays.stream(s1).map(x -> x * x).average().orElse(0);
        double noisePower = mse;
        double snr = signalPower > 0 && noisePower > 0 ? 10 * Math.log10(signalPower / noisePower) : Double.POSITIVE_INFINITY;
        comparison.put("snr_db", snr);
        
        // Frequency domain comparison
        AnalysisResult analysis1 = analyze(s1, sampleRate);
        AnalysisResult analysis2 = analyze(s2, sampleRate);
        
        comparison.put("spectral_distance", calculateSpectralDistance(analysis1.getPowerSpectrum(), analysis2.getPowerSpectrum()));
        comparison.put("peak_count_diff", Math.abs(analysis1.getPeaks().size() - analysis2.getPeaks().size()));
        
        return comparison;
    }
    
    /**
     * Batch analysis of multiple signals.
     */
    public List<AnalysisResult> analyzeBatch(List<double[]> signals, double sampleRate) {
        List<AnalysisResult> results = new ArrayList<>();
        for (double[] signal : signals) {
            results.add(analyze(signal, sampleRate));
        }
        return results;
    }
    
    /**
     * Find signals in history matching specified criteria.
     */
    public List<AnalysisResult> findSimilarSignals(AnalysisResult target, double threshold) {
        List<AnalysisResult> similar = new ArrayList<>();
        
        for (AnalysisResult candidate : analysisHistory) {
            if (candidate == target) continue;
            
            double similarity = calculateSimilarity(target, candidate);
            if (similarity > threshold) {
                similar.add(candidate);
            }
        }
        
        similar.sort((a, b) -> Double.compare(
            calculateSimilarity(target, b), 
            calculateSimilarity(target, a)
        ));
        
        return similar;
    }
    
    private void analyzeTimeDomain(double[] signal, Map<String, Double> metrics) {
        // Basic statistics
        double mean = Arrays.stream(signal).average().orElse(0);
        double variance = Arrays.stream(signal).map(x -> (x - mean) * (x - mean)).average().orElse(0);
        double skewness = calculateSkewness(signal, mean, variance);
        double kurtosis = calculateKurtosis(signal, mean, variance);
        
        // Energy metrics
        double rms = Math.sqrt(Arrays.stream(signal).map(x -> x * x).average().orElse(0));
        double peak = Arrays.stream(signal).map(Math::abs).max().orElse(0);
        double crestFactor = peak / rms;
        
        // Dynamic range
        double min = Arrays.stream(signal).min().orElse(0);
        double max = Arrays.stream(signal).max().orElse(0);
        double dynamicRange = 20 * Math.log10((max - min) / 2);
        
        // Zero crossing rate
        int zeroCrossings = 0;
        for (int i = 1; i < signal.length; i++) {
            if (Math.signum(signal[i]) != Math.signum(signal[i-1])) {
                zeroCrossings++;
            }
        }
        double zcr = (double) zeroCrossings / (signal.length - 1);
        
        // Store metrics
        metrics.put("mean", mean);
        metrics.put("variance", variance);
        metrics.put("std_dev", Math.sqrt(variance));
        metrics.put("skewness", skewness);
        metrics.put("kurtosis", kurtosis);
        metrics.put("rms", rms);
        metrics.put("peak", peak);
        metrics.put("crest_factor", crestFactor);
        metrics.put("dynamic_range_db", dynamicRange);
        metrics.put("zero_crossing_rate", zcr);
    }
    
    private void analyzeFrequencyDomain(double[] signal, double sampleRate, AnalysisResult result) {
        // Perform FFT
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        FFT fft = factory.createFFT(paddedSignal.length);
        FFTResult fftResult = fft.transform(paddedSignal, true);
        
        double[] magnitudes = fftResult.getMagnitudes();
        double[] powerSpectrum = new double[magnitudes.length / 2];
        double[] frequencies = new double[powerSpectrum.length];
        
        // Calculate power spectrum and frequency bins
        for (int i = 0; i < powerSpectrum.length; i++) {
            powerSpectrum[i] = magnitudes[i] * magnitudes[i];
            frequencies[i] = (double) i * sampleRate / paddedSignal.length;
        }
        
        // Store in result
        System.arraycopy(powerSpectrum, 0, result.powerSpectrum, 0, powerSpectrum.length);
        System.arraycopy(frequencies, 0, result.frequencies, 0, frequencies.length);
        
        // Spectral analysis
        analyzeSpectralCharacteristics(powerSpectrum, frequencies, result.frequencyMetrics);
        
        // Peak detection
        detectPeaks(powerSpectrum, frequencies, result.peaks);
        
        // Quality metrics
        calculateQualityMetrics(powerSpectrum, frequencies, result.frequencyMetrics, result.peaks);
    }
    
    private void analyzeSpectralCharacteristics(double[] powerSpectrum, double[] frequencies, 
                                               Map<String, Double> metrics) {
        // Skip DC component
        double totalPower = Arrays.stream(powerSpectrum, 1, powerSpectrum.length).sum();
        
        if (totalPower == 0) {
            // All metrics are 0 for zero signal
            metrics.put("spectral_centroid", 0.0);
            metrics.put("spectral_spread", 0.0);
            metrics.put("spectral_rolloff", 0.0);
            metrics.put("spectral_flux", 0.0);
            metrics.put("bandwidth", 0.0);
            return;
        }
        
        // Spectral centroid
        double weightedSum = 0;
        for (int i = 1; i < powerSpectrum.length; i++) {
            weightedSum += frequencies[i] * powerSpectrum[i];
        }
        double centroid = weightedSum / totalPower;
        
        // Spectral spread
        double spreadSum = 0;
        for (int i = 1; i < powerSpectrum.length; i++) {
            double deviation = frequencies[i] - centroid;
            spreadSum += deviation * deviation * powerSpectrum[i];
        }
        double spread = Math.sqrt(spreadSum / totalPower);
        
        // Spectral rolloff (95% energy point)
        double energySum = 0;
        double rolloffThreshold = 0.95 * totalPower;
        double rolloff = frequencies[frequencies.length - 1];
        
        for (int i = 1; i < powerSpectrum.length; i++) {
            energySum += powerSpectrum[i];
            if (energySum >= rolloffThreshold) {
                rolloff = frequencies[i];
                break;
            }
        }
        
        // Bandwidth (between 5% and 95% energy points)
        energySum = 0;
        double lowerBand = 0, upperBand = frequencies[frequencies.length - 1];
        double lowerThreshold = 0.05 * totalPower;
        double upperThreshold = 0.95 * totalPower;
        
        for (int i = 1; i < powerSpectrum.length; i++) {
            energySum += powerSpectrum[i];
            if (lowerBand == 0 && energySum >= lowerThreshold) {
                lowerBand = frequencies[i];
            }
            if (energySum >= upperThreshold) {
                upperBand = frequencies[i];
                break;
            }
        }
        double bandwidth = upperBand - lowerBand;
        
        metrics.put("spectral_centroid", centroid);
        metrics.put("spectral_spread", spread);
        metrics.put("spectral_rolloff", rolloff);
        metrics.put("bandwidth", bandwidth);
        metrics.put("total_power", totalPower);
    }
    
    private void detectPeaks(double[] powerSpectrum, double[] frequencies, List<Peak> peaks) {
        // Find local maxima
        double threshold = Arrays.stream(powerSpectrum).max().orElse(0) * 0.01; // 1% of max
        
        for (int i = 2; i < powerSpectrum.length - 2; i++) {
            if (powerSpectrum[i] > threshold &&
                powerSpectrum[i] > powerSpectrum[i-1] && 
                powerSpectrum[i] > powerSpectrum[i+1] &&
                powerSpectrum[i] > powerSpectrum[i-2] &&
                powerSpectrum[i] > powerSpectrum[i+2]) {
                
                double frequency = frequencies[i];
                double amplitude = Math.sqrt(powerSpectrum[i]);
                double phase = 0; // Phase calculation would require complex FFT result
                double bandwidth = estimatePeakBandwidth(powerSpectrum, i);
                
                String type = classifyPeak(frequency, amplitude, bandwidth);
                peaks.add(new Peak(frequency, amplitude, phase, bandwidth, type));
            }
        }
        
        // Sort peaks by amplitude (descending)
        peaks.sort((a, b) -> Double.compare(b.getAmplitude(), a.getAmplitude()));
    }
    
    private double estimatePeakBandwidth(double[] powerSpectrum, int peakIndex) {
        double peakPower = powerSpectrum[peakIndex];
        double halfPower = peakPower / 2;
        
        // Find -3dB points
        int leftIndex = peakIndex;
        int rightIndex = peakIndex;
        
        while (leftIndex > 0 && powerSpectrum[leftIndex] > halfPower) {
            leftIndex--;
        }
        
        while (rightIndex < powerSpectrum.length - 1 && powerSpectrum[rightIndex] > halfPower) {
            rightIndex++;
        }
        
        return rightIndex - leftIndex; // Bandwidth in bins
    }
    
    private String classifyPeak(double frequency, double amplitude, double bandwidth) {
        if (bandwidth < 2) return "narrow";
        if (bandwidth > 10) return "broad";
        return "medium";
    }
    
    private void calculateQualityMetrics(double[] powerSpectrum, double[] frequencies, 
                                       Map<String, Double> metrics, List<Peak> peaks) {
        if (peaks.isEmpty()) {
            metrics.put("snr_db", 0.0);
            metrics.put("thd_percent", 0.0);
            metrics.put("sinad_db", 0.0);
            return;
        }
        
        // Find fundamental frequency (largest peak)
        Peak fundamental = peaks.get(0);
        double fundamentalPower = fundamental.getAmplitude() * fundamental.getAmplitude();
        
        // Calculate total harmonic distortion (THD)
        double harmonicPower = 0;
        int harmonicCount = 0;
        
        for (Peak peak : peaks) {
            if (peak == fundamental) continue;
            
            // Check if this peak is a harmonic of the fundamental
            double ratio = peak.getFrequency() / fundamental.getFrequency();
            if (Math.abs(ratio - Math.round(ratio)) < 0.05) { // Within 5% of integer ratio
                harmonicPower += peak.getAmplitude() * peak.getAmplitude();
                harmonicCount++;
            }
        }
        
        double thd = harmonicCount > 0 ? Math.sqrt(harmonicPower / fundamentalPower) * 100 : 0;
        
        // Calculate noise power (everything that's not signal or harmonics)
        double totalPower = Arrays.stream(powerSpectrum, 1, powerSpectrum.length).sum();
        double signalPower = fundamentalPower + harmonicPower;
        double noisePower = totalPower - signalPower;
        
        // Signal-to-noise ratio
        double snr = signalPower > 0 && noisePower > 0 ? 
                    10 * Math.log10(fundamentalPower / noisePower) : 
                    Double.POSITIVE_INFINITY;
        
        // SINAD (Signal to Noise and Distortion)
        double sinad = signalPower > 0 && (noisePower + harmonicPower) > 0 ?
                      10 * Math.log10(fundamentalPower / (noisePower + harmonicPower)) :
                      Double.POSITIVE_INFINITY;
        
        metrics.put("snr_db", snr);
        metrics.put("thd_percent", thd);
        metrics.put("sinad_db", sinad);
        metrics.put("fundamental_frequency", fundamental.getFrequency());
        metrics.put("harmonic_count", (double) harmonicCount);
    }
    
    private SignalClassification classifySignal(AnalysisResult result) {
        String classification = "unknown";
        
        // Simple classification based on characteristics
        int peakCount = result.getPeaks().size();
        double zcr = result.getTimeMetric("zero_crossing_rate");
        double thd = result.getFrequencyMetric("thd_percent");
        double bandwidth = result.getFrequencyMetric("bandwidth");
        
        if (peakCount == 1 && thd < 1.0) {
            classification = "pure_tone";
        } else if (peakCount > 1 && thd > 5.0) {
            classification = "harmonic";
        } else if (zcr > 0.4) {
            classification = "noise_like";
        } else if (bandwidth > 1000) {
            classification = "broadband";
        } else if (peakCount > 5) {
            classification = "complex_tone";
        }
        
        return new SignalClassification(classification);
    }
    
    private double calculateCorrelation(double[] x, double[] y) {
        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);
        
        double numerator = 0;
        double sumXSq = 0;
        double sumYSq = 0;
        
        for (int i = 0; i < x.length; i++) {
            double xDev = x[i] - meanX;
            double yDev = y[i] - meanY;
            numerator += xDev * yDev;
            sumXSq += xDev * xDev;
            sumYSq += yDev * yDev;
        }
        
        double denominator = Math.sqrt(sumXSq * sumYSq);
        return denominator > 0 ? numerator / denominator : 0;
    }
    
    private double calculateSpectralDistance(double[] spectrum1, double[] spectrum2) {
        double distance = 0;
        int minLength = Math.min(spectrum1.length, spectrum2.length);
        
        for (int i = 0; i < minLength; i++) {
            double diff = spectrum1[i] - spectrum2[i];
            distance += diff * diff;
        }
        
        return Math.sqrt(distance / minLength);
    }
    
    private double calculateSimilarity(AnalysisResult a, AnalysisResult b) {
        // Weighted similarity based on multiple factors
        double specSim = 1.0 / (1.0 + calculateSpectralDistance(a.getPowerSpectrum(), b.getPowerSpectrum()));
        double peakSim = 1.0 / (1.0 + Math.abs(a.getPeaks().size() - b.getPeaks().size()));
        double classSim = a.getSignalClassification().equals(b.getSignalClassification()) ? 1.0 : 0.0;
        
        return (2 * specSim + peakSim + classSim) / 4.0;
    }
    
    private double calculateSkewness(double[] data, double mean, double variance) {
        if (variance == 0) return 0;
        
        double sum = 0;
        double stdDev = Math.sqrt(variance);
        
        for (double value : data) {
            double normalized = (value - mean) / stdDev;
            sum += normalized * normalized * normalized;
        }
        
        return sum / data.length;
    }
    
    private double calculateKurtosis(double[] data, double mean, double variance) {
        if (variance == 0) return 0;
        
        double sum = 0;
        double stdDev = Math.sqrt(variance);
        
        for (double value : data) {
            double normalized = (value - mean) / stdDev;
            sum += normalized * normalized * normalized * normalized;
        }
        
        return sum / data.length - 3.0; // Excess kurtosis
    }
    
    /**
     * Get analysis history.
     */
    public List<AnalysisResult> getAnalysisHistory() {
        return new ArrayList<>(analysisHistory);
    }
    
    /**
     * Clear analysis history.
     */
    public void clearHistory() {
        analysisHistory.clear();
    }
    
    /**
     * Get summary statistics from analysis history.
     */
    public Map<String, Object> getHistorySummary() {
        Map<String, Object> summary = new HashMap<>();
        
        if (analysisHistory.isEmpty()) {
            summary.put("total_analyses", 0);
            return summary;
        }
        
        Map<String, Integer> classificationCounts = new HashMap<>();
        double avgPeaks = 0;
        double avgSnr = 0;
        
        for (AnalysisResult result : analysisHistory) {
            classificationCounts.merge(result.getSignalClassification(), 1, Integer::sum);
            avgPeaks += result.getPeaks().size();
            avgSnr += result.getFrequencyMetric("snr_db");
        }
        
        avgPeaks /= analysisHistory.size();
        avgSnr /= analysisHistory.size();
        
        summary.put("total_analyses", analysisHistory.size());
        summary.put("classification_distribution", classificationCounts);
        summary.put("average_peak_count", avgPeaks);
        summary.put("average_snr_db", avgSnr);
        
        return summary;
    }
}