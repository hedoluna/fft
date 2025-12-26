#!/bin/bash

# JMH Benchmark Runner for FFT Library
#
# This script runs JMH benchmarks using direct java -cp instead of exec-maven-plugin
# which has issues finding the META-INF/BenchmarkList resource in test-classes.
#
# Usage:
#   ./run-jmh-benchmarks.sh                    # Run all benchmarks
#   ./run-jmh-benchmarks.sh FFTBaseProfiling  # Run specific benchmark
#   ./run-jmh-benchmarks.sh FFT8 -f 3         # Run with custom JMH options

# Compile test classes and generate JMH metadata
echo "Compiling test classes and generating JMH metadata..."
mvn clean test-compile
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

# Build classpath
CP="target/test-classes:target/classes"
CP="$CP:$HOME/.m2/repository/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar"
CP="$CP:$HOME/.m2/repository/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar"
CP="$CP:$HOME/.m2/repository/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar"
CP="$CP:$HOME/.m2/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar"
CP="$CP:$HOME/.m2/repository/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
CP="$CP:$HOME/.m2/repository/ch/qos/logback/logback-classic/1.5.19/logback-classic-1.5.19.jar"
CP="$CP:$HOME/.m2/repository/ch/qos/logback/logback-core/1.5.19/logback-core-1.5.19.jar"

# Set benchmark pattern (empty means run all)
BENCHMARK_PATTERN=$1

echo ""
if [ -z "$BENCHMARK_PATTERN" ]; then
    echo "Running ALL JMH benchmarks..."
else
    echo "Running JMH benchmarks matching pattern: $BENCHMARK_PATTERN"
    shift  # Remove first argument (benchmark pattern) so remaining args are passed to JMH
fi
echo ""

# Run JMH with the specified benchmark pattern (or all if empty) and any additional arguments
java -cp "$CP" org.openjdk.jmh.Main $BENCHMARK_PATTERN "$@"

echo ""
echo "Benchmark run complete!"
