package com.fft.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ArrayAllocationBenchmark {

    private Map<String, Double> algorithmWeights;
    private Map<String, Integer> successCounts;
    private Map<String, Integer> totalCounts;

    private static final String[] ALGORITHMS = {"exact", "partial", "variation", "dtw"};

    @Setup
    public void setup() {
        algorithmWeights = new HashMap<>();
        successCounts = new HashMap<>();
        totalCounts = new HashMap<>();

        algorithmWeights.put("exact", 0.3);
        algorithmWeights.put("partial", 0.25);
        algorithmWeights.put("variation", 0.2);
        algorithmWeights.put("dtw", 0.15);
        algorithmWeights.put("length", 0.1);

        // Pre-populate some data
        for (String alg : ALGORITHMS) {
            String key = "algorithm_" + alg;
            successCounts.put(key, 10);
            totalCounts.put(key, 20);
        }
    }

    @Benchmark
    public void baseline() {
        updateWeightsBaseline();
    }

    @Benchmark
    public void optimized() {
        updateWeightsOptimized();
    }

    private void updateWeightsBaseline() {
        for (String algorithm : new String[]{"exact", "partial", "variation", "dtw"}) {
            String key = "algorithm_" + algorithm;
            int successes = successCounts.getOrDefault(key, 0);
            int total = totalCounts.getOrDefault(key, 1); // Avoid division by zero
            double successRate = (double) successes / total;

            // Adjust weight based on success rate (reinforcement learning)
            double currentWeight = algorithmWeights.get(algorithm);
            double adjustment = (successRate - 0.5) * 0.01; // Small adjustments
            double newWeight = Math.max(0.05, Math.min(0.5, currentWeight + adjustment));

            algorithmWeights.put(algorithm, newWeight);
        }

        // Normalize weights to sum to 1.0 (excluding length bonus)
        double totalWeight = algorithmWeights.values().stream()
            .mapToDouble(Double::doubleValue).sum() - algorithmWeights.get("length");
        for (String algorithm : new String[]{"exact", "partial", "variation", "dtw"}) {
            algorithmWeights.put(algorithm, algorithmWeights.get(algorithm) / totalWeight * 0.9);
        }
    }

    private void updateWeightsOptimized() {
        for (String algorithm : ALGORITHMS) {
            String key = "algorithm_" + algorithm;
            int successes = successCounts.getOrDefault(key, 0);
            int total = totalCounts.getOrDefault(key, 1); // Avoid division by zero
            double successRate = (double) successes / total;

            // Adjust weight based on success rate (reinforcement learning)
            double currentWeight = algorithmWeights.get(algorithm);
            double adjustment = (successRate - 0.5) * 0.01; // Small adjustments
            double newWeight = Math.max(0.05, Math.min(0.5, currentWeight + adjustment));

            algorithmWeights.put(algorithm, newWeight);
        }

        // Normalize weights to sum to 1.0 (excluding length bonus)
        double totalWeight = algorithmWeights.values().stream()
            .mapToDouble(Double::doubleValue).sum() - algorithmWeights.get("length");
        for (String algorithm : ALGORITHMS) {
            algorithmWeights.put(algorithm, algorithmWeights.get(algorithm) / totalWeight * 0.9);
        }
    }
}
