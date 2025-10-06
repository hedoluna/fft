@echo off
REM JMH Benchmark Runner for FFT Library
REM
REM This script runs JMH benchmarks using direct java -cp instead of exec-maven-plugin
REM which has issues finding the META-INF/BenchmarkList resource in test-classes.
REM
REM Usage:
REM   run-jmh-benchmarks.bat                    # Run all benchmarks
REM   run-jmh-benchmarks.bat FFTBaseProfiling  # Run specific benchmark

REM Compile test classes and generate JMH metadata
echo Compiling test classes and generating JMH metadata...
call mvn clean test-compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    exit /b 1
)

REM Build classpath
set CP=target\test-classes;target\classes
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjdk\jmh\jmh-core\1.37\jmh-core-1.37.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjdk\jmh\jmh-generator-annprocess\1.37\jmh-generator-annprocess-1.37.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\net\sf\jopt-simple\jopt-simple\5.0.4\jopt-simple-5.0.4.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\apache\commons\commons-math3\3.6.1\commons-math3-3.6.1.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\2.0.9\slf4j-api-2.0.9.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.5.19\logback-classic-1.5.19.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.5.19\logback-core-1.5.19.jar

REM Set benchmark pattern (empty means run all)
set BENCHMARK_PATTERN=%1

echo.
if "%BENCHMARK_PATTERN%"=="" (
    echo Running ALL JMH benchmarks...
) else (
    echo Running JMH benchmarks matching pattern: %BENCHMARK_PATTERN%
)
echo.

REM Run JMH with the specified benchmark pattern (or all if empty)
java -cp "%CP%" org.openjdk.jmh.Main %BENCHMARK_PATTERN% %2 %3 %4 %5 %6 %7 %8 %9

echo.
echo Benchmark run complete!
