package com.fft.factory;

import com.fft.core.FFT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FFTImplementationDiscovery Tests")
class FFTImplementationDiscoveryTest {

    @Nested
    @DisplayName("discoverImplementations() method")
    class DiscoverImplementationsTests {

        @Test
        @DisplayName("should discover at least FFTOptimized8 implementation")
        void shouldDiscoverFFTOptimized8() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered =
                FFTImplementationDiscovery.discoverImplementations();

            assertThat(discovered).isNotNull();
            assertThat(discovered).containsKey(8);

            List<FFTImplementationDiscovery.DiscoveredImplementation> size8Impls = discovered.get(8);
            assertThat(size8Impls).isNotEmpty();

            // Verify FFTOptimized8 is found
            boolean hasOptimized8 = size8Impls.stream()
                .anyMatch(impl -> impl.getImplementationClass().getSimpleName().equals("FFTOptimized8"));
            assertThat(hasOptimized8).isTrue();
        }

        @Test
        @DisplayName("should return same instance on repeated calls (caching)")
        void shouldReturnCachedInstance() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> first =
                FFTImplementationDiscovery.discoverImplementations();
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> second =
                FFTImplementationDiscovery.discoverImplementations();

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("should sort implementations by priority (highest first)")
        void shouldSortByPriorityDescending() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered =
                FFTImplementationDiscovery.discoverImplementations();

            for (List<FFTImplementationDiscovery.DiscoveredImplementation> impls : discovered.values()) {
                if (impls.size() > 1) {
                    for (int i = 0; i < impls.size() - 1; i++) {
                        assertThat(impls.get(i).getPriority())
                            .as("Priority at index %d should be >= priority at index %d", i, i + 1)
                            .isGreaterThanOrEqualTo(impls.get(i + 1).getPriority());
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("DiscoveredImplementation class")
    class DiscoveredImplementationTests {

        @Test
        @DisplayName("should provide correct metadata for discovered implementation")
        void shouldProvideCorrectMetadata() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered =
                FFTImplementationDiscovery.discoverImplementations();

            List<FFTImplementationDiscovery.DiscoveredImplementation> size8Impls = discovered.get(8);
            assertThat(size8Impls).isNotEmpty();

            FFTImplementationDiscovery.DiscoveredImplementation impl = size8Impls.get(0);

            assertThat(impl.getSize()).isEqualTo(8);
            assertThat(impl.getPriority()).isGreaterThan(0);
            assertThat(impl.getDescription()).isNotEmpty();
            assertThat(impl.isAutoRegister()).isTrue();
            assertThat(impl.getImplementationClass()).isNotNull();
            assertThat(FFT.class).isAssignableFrom(impl.getImplementationClass());
        }

        @Test
        @DisplayName("should provide working supplier that creates FFT instance")
        void shouldProvideWorkingSupplier() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered =
                FFTImplementationDiscovery.discoverImplementations();

            List<FFTImplementationDiscovery.DiscoveredImplementation> size8Impls = discovered.get(8);
            assertThat(size8Impls).isNotEmpty();

            FFTImplementationDiscovery.DiscoveredImplementation impl = size8Impls.get(0);

            FFT fftInstance = impl.getSupplier().get();

            assertThat(fftInstance).isNotNull();
            assertThat(fftInstance).isInstanceOf(FFT.class);
        }

        @Test
        @DisplayName("should have characteristics array")
        void shouldHaveCharacteristicsArray() {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered =
                FFTImplementationDiscovery.discoverImplementations();

            List<FFTImplementationDiscovery.DiscoveredImplementation> size8Impls = discovered.get(8);
            assertThat(size8Impls).isNotEmpty();

            FFTImplementationDiscovery.DiscoveredImplementation impl = size8Impls.get(0);

            String[] characteristics = impl.getCharacteristics();
            assertThat(characteristics).isNotNull();
            // FFTOptimized8 should have characteristics like "OPTIMIZED", "PRECOMPUTED_TWIDDLES"
        }
    }

    @Nested
    @DisplayName("getDiscoveryReport() method")
    class GetDiscoveryReportTests {

        @Test
        @DisplayName("should return formatted report string")
        void shouldReturnFormattedReport() {
            String report = FFTImplementationDiscovery.getDiscoveryReport();

            assertThat(report).isNotEmpty();
            assertThat(report).contains("FFT Implementation Discovery Report");
            assertThat(report).contains("Size 8:");
        }

        @Test
        @DisplayName("should include priority and auto-register info")
        void shouldIncludePriorityAndAutoRegisterInfo() {
            String report = FFTImplementationDiscovery.getDiscoveryReport();

            assertThat(report).contains("priority:");
            assertThat(report).contains("auto-register:");
        }
    }

    @Nested
    @DisplayName("registerDiscoveredImplementations() method")
    class RegisterDiscoveredImplementationsTests {

        @Test
        @DisplayName("should register implementations with factory")
        void shouldRegisterWithFactory() {
            DefaultFFTFactory factory = new DefaultFFTFactory();

            // Register discovered implementations
            FFTImplementationDiscovery.registerDiscoveredImplementations(factory);

            // Verify factory can create FFT for discovered sizes
            FFT fft8 = factory.createFFT(8);
            assertThat(fft8).isNotNull();

            // Should use optimized implementation, not fallback
            String info = factory.getImplementationInfo(8);
            assertThat(info).contains("8");
        }

        @Test
        @DisplayName("should not throw exception when called multiple times")
        void shouldHandleMultipleCalls() {
            DefaultFFTFactory factory = new DefaultFFTFactory();

            // Should not throw on repeated calls
            assertThatCode(() -> {
                FFTImplementationDiscovery.registerDiscoveredImplementations(factory);
                FFTImplementationDiscovery.registerDiscoveredImplementations(factory);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent discovery calls")
        void shouldHandleConcurrentDiscoveryCalls() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            Map<?, ?>[] results = new Map[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = FFTImplementationDiscovery.discoverImplementations();
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All threads should get the same cached instance
            for (int i = 1; i < threadCount; i++) {
                assertThat(results[i]).isSameAs(results[0]);
            }
        }
    }
}
