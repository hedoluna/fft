import java.util.Arrays;

/**
 * Unit tests for FFT implementations.
 * Tests correctness and basic functionality of all FFT classes.
 * 
 * @author Engine AI Assistant
 */
public class FFTTest {
    
    private static final double EPSILON = 1e-10;
    
    /**
     * Test data for size 8 arrays
     */
    private static final double[] TEST_REAL_8 = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final double[] TEST_IMAG_8 = {0, 0, 0, 0, 0, 0, 0, 0};
    
    /**
     * Test data for size 32 arrays
     */
    private static final double[] TEST_REAL_32 = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    private static final double[] TEST_IMAG_32 = new double[32]; // All zeros
    
    /**
     * Test data for size 64 arrays
     */
    private static final double[] TEST_REAL_64 = generateTestData(64);
    private static final double[] TEST_IMAG_64 = new double[64]; // All zeros
    
    /**
     * Test data for size 128 arrays
     */
    private static final double[] TEST_REAL_128 = generateTestData(128);
    private static final double[] TEST_IMAG_128 = new double[128]; // All zeros
    
    /**
     * Test data for size 256 arrays
     */
    private static final double[] TEST_REAL_256 = generateTestData(256);
    private static final double[] TEST_IMAG_256 = new double[256]; // All zeros
    
    /**
     * Test data for size 512 arrays
     */
    private static final double[] TEST_REAL_512 = generateTestData(512);
    private static final double[] TEST_IMAG_512 = new double[512]; // All zeros
    
    /**
     * Test data for size 1024 arrays
     */
    private static final double[] TEST_REAL_1024 = generateTestData(1024);
    private static final double[] TEST_IMAG_1024 = new double[1024]; // All zeros
    
    /**
     * Test data for size 2048 arrays
     */
    private static final double[] TEST_REAL_2048 = generateTestData(2048);
    private static final double[] TEST_IMAG_2048 = new double[2048]; // All zeros
    
    /**
     * Test data for size 4096 arrays
     */
    private static final double[] TEST_REAL_4096 = generateTestData(4096);
    private static final double[] TEST_IMAG_4096 = new double[4096]; // All zeros
    
    /**
     * Test data for size 8192 arrays
     */
    private static final double[] TEST_REAL_8192 = generateTestData(8192);
    private static final double[] TEST_IMAG_8192 = new double[8192]; // All zeros
    
    /**
     * Generate test data for arrays of given size
     */
    private static double[] generateTestData(int size) {
        double[] data = new double[size];
        for (int i = 0; i < size; i++) {
            data[i] = Math.sin(2 * Math.PI * i / size) + 0.5 * Math.cos(4 * Math.PI * i / size);
        }
        return data;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting FFT Unit Tests...");
        
        testFFTBase();
        testFFTOptim8();
        testFFTOptim32();
        testFFTOptim64();
        testFFTOptim128();
        testFFTOptim256();
        testFFTOptim512();
        testFFTOptim1024();
        testFFTOptim2048();
        testFFTOptim4096();
        testFFTOptim8192();
        testConsistencyBetweenImplementations();
        testInvalidSizes();
        testInverseTransform();
        
        System.out.println("All tests completed!");
    }
    
    /**
     * Test the base FFT implementation
     */
    private static void testFFTBase() {
        System.out.println("Testing FFTbase...");
        
        // Test with size 8
        double[] result8 = FFTbase.fft(TEST_REAL_8, TEST_IMAG_8, true);
        assert result8.length == 16 : "Result length should be 2*input length";
        
        // Test with size 32
        double[] result32 = FFTbase.fft(TEST_REAL_32, TEST_IMAG_32, true);
        assert result32.length == 64 : "Result length should be 2*input length";
        
        // Test invalid size (not power of 2)
        double[] invalidReal = {1, 2, 3};
        double[] invalidImag = {0, 0, 0};
        double[] resultInvalid = FFTbase.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFTbase tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 8
     */
    private static void testFFTOptim8() {
        System.out.println("Testing FFToptim8...");
        
        double[] result = FFToptim8.fft(TEST_REAL_8, TEST_IMAG_8, true);
        assert result.length == 16 : "Result length should be 16";
        
        // Test invalid size
        double[] invalidReal = {1, 2, 3, 4};
        double[] invalidImag = {0, 0, 0, 0};
        double[] resultInvalid = FFToptim8.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim8 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 32
     */
    private static void testFFTOptim32() {
        System.out.println("Testing FFToptim32...");
        
        double[] result = FFToptim32.fft(TEST_REAL_32, TEST_IMAG_32, true);
        assert result.length == 64 : "Result length should be 64";
        
        // Test invalid size
        double[] invalidReal = new double[16];
        double[] invalidImag = new double[16];
        double[] resultInvalid = FFToptim32.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim32 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 64
     */
    private static void testFFTOptim64() {
        System.out.println("Testing FFToptim64...");
        
        double[] result = FFToptim64.fft(TEST_REAL_64, TEST_IMAG_64, true);
        assert result.length == 128 : "Result length should be 128";
        
        // Test invalid size
        double[] invalidReal = new double[16];
        double[] invalidImag = new double[16];
        double[] resultInvalid = FFToptim64.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim64 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 128
     */
    private static void testFFTOptim128() {
        System.out.println("Testing FFToptim128...");
        
        double[] result = FFToptim128.fft(TEST_REAL_128, TEST_IMAG_128, true);
        assert result.length == 256 : "Result length should be 256";
        
        // Test invalid size
        double[] invalidReal = new double[64];
        double[] invalidImag = new double[64];
        double[] resultInvalid = FFToptim128.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim128 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 256
     */
    private static void testFFTOptim256() {
        System.out.println("Testing FFToptim256...");
        
        double[] result = FFToptim256.fft(TEST_REAL_256, TEST_IMAG_256, true);
        assert result.length == 512 : "Result length should be 512";
        
        // Test invalid size
        double[] invalidReal = new double[128];
        double[] invalidImag = new double[128];
        double[] resultInvalid = FFToptim256.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim256 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 512
     */
    private static void testFFTOptim512() {
        System.out.println("Testing FFToptim512...");
        
        double[] result = FFToptim512.fft(TEST_REAL_512, TEST_IMAG_512, true);
        assert result.length == 1024 : "Result length should be 1024";
        
        // Test invalid size
        double[] invalidReal = new double[256];
        double[] invalidImag = new double[256];
        double[] resultInvalid = FFToptim512.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim512 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 1024
     */
    private static void testFFTOptim1024() {
        System.out.println("Testing FFToptim1024...");
        
        double[] result = FFToptim1024.fft(TEST_REAL_1024, TEST_IMAG_1024, true);
        assert result.length == 2048 : "Result length should be 2048";
        
        // Test invalid size
        double[] invalidReal = new double[512];
        double[] invalidImag = new double[512];
        double[] resultInvalid = FFToptim1024.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim1024 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 2048
     */
    private static void testFFTOptim2048() {
        System.out.println("Testing FFToptim2048...");
        
        double[] result = FFToptim2048.fft(TEST_REAL_2048, TEST_IMAG_2048, true);
        assert result.length == 4096 : "Result length should be 4096";
        
        // Test invalid size
        double[] invalidReal = new double[1024];
        double[] invalidImag = new double[1024];
        double[] resultInvalid = FFToptim2048.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim2048 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 4096
     */
    private static void testFFTOptim4096() {
        System.out.println("Testing FFToptim4096...");
        
        double[] result = FFToptim4096.fft(TEST_REAL_4096, TEST_IMAG_4096, true);
        assert result.length == 8192 : "Result length should be 8192";
        
        // Test invalid size
        double[] invalidReal = new double[2048];
        double[] invalidImag = new double[2048];
        double[] resultInvalid = FFToptim4096.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim4096 tests passed");
    }
    
    /**
     * Test the optimized FFT implementation for size 8192
     */
    private static void testFFTOptim8192() {
        System.out.println("Testing FFToptim8192...");
        
        double[] result = FFToptim8192.fft(TEST_REAL_8192, TEST_IMAG_8192, true);
        assert result.length == 16384 : "Result length should be 16384";
        
        // Test invalid size
        double[] invalidReal = new double[4096];
        double[] invalidImag = new double[4096];
        double[] resultInvalid = FFToptim8192.fft(invalidReal, invalidImag, true);
        assert resultInvalid.length == 0 : "Invalid size should return empty array";
        
        System.out.println("✓ FFToptim8192 tests passed");
    }
    
    /**
     * Test consistency between different implementations
     */
    private static void testConsistencyBetweenImplementations() {
        System.out.println("Testing consistency between implementations...");
        
        // Test size 8 consistency
        double[] baseResult8 = FFTbase.fft(TEST_REAL_8, TEST_IMAG_8, true);
        double[] optimResult8 = FFToptim8.fft(TEST_REAL_8, TEST_IMAG_8, true);
        
        assertArraysEqual(baseResult8, optimResult8, "Base and Optim8 should produce same results");
        
        // Test size 32 consistency  
        double[] baseResult32 = FFTbase.fft(TEST_REAL_32, TEST_IMAG_32, true);
        double[] optimResult32 = FFToptim32.fft(TEST_REAL_32, TEST_IMAG_32, true);
        
        assertArraysEqual(baseResult32, optimResult32, "Base and Optim32 should produce same results");
        
        // Test size 64 consistency
        double[] baseResult64 = FFTbase.fft(TEST_REAL_64, TEST_IMAG_64, true);
        double[] optimResult64 = FFToptim64.fft(TEST_REAL_64, TEST_IMAG_64, true);
        
        assertArraysEqual(baseResult64, optimResult64, "Base and Optim64 should produce same results");
        
        // Test size 128 consistency
        double[] baseResult128 = FFTbase.fft(TEST_REAL_128, TEST_IMAG_128, true);
        double[] optimResult128 = FFToptim128.fft(TEST_REAL_128, TEST_IMAG_128, true);
        
        assertArraysEqual(baseResult128, optimResult128, "Base and Optim128 should produce same results");
        
        // Test size 256 consistency
        double[] baseResult256 = FFTbase.fft(TEST_REAL_256, TEST_IMAG_256, true);
        double[] optimResult256 = FFToptim256.fft(TEST_REAL_256, TEST_IMAG_256, true);
        
        assertArraysEqual(baseResult256, optimResult256, "Base and Optim256 should produce same results");
        
        // Test size 512 consistency
        double[] baseResult512 = FFTbase.fft(TEST_REAL_512, TEST_IMAG_512, true);
        double[] optimResult512 = FFToptim512.fft(TEST_REAL_512, TEST_IMAG_512, true);
        
        assertArraysEqual(baseResult512, optimResult512, "Base and Optim512 should produce same results");
        
        // Test size 1024 consistency
        double[] baseResult1024 = FFTbase.fft(TEST_REAL_1024, TEST_IMAG_1024, true);
        double[] optimResult1024 = FFToptim1024.fft(TEST_REAL_1024, TEST_IMAG_1024, true);
        
        assertArraysEqual(baseResult1024, optimResult1024, "Base and Optim1024 should produce same results");
        
        // Test size 2048 consistency
        double[] baseResult2048 = FFTbase.fft(TEST_REAL_2048, TEST_IMAG_2048, true);
        double[] optimResult2048 = FFToptim2048.fft(TEST_REAL_2048, TEST_IMAG_2048, true);
        
        assertArraysEqual(baseResult2048, optimResult2048, "Base and Optim2048 should produce same results");
        
        // Test size 4096 consistency
        double[] baseResult4096 = FFTbase.fft(TEST_REAL_4096, TEST_IMAG_4096, true);
        double[] optimResult4096 = FFToptim4096.fft(TEST_REAL_4096, TEST_IMAG_4096, true);
        
        assertArraysEqual(baseResult4096, optimResult4096, "Base and Optim4096 should produce same results");
        
        // Test size 8192 consistency
        double[] baseResult8192 = FFTbase.fft(TEST_REAL_8192, TEST_IMAG_8192, true);
        double[] optimResult8192 = FFToptim8192.fft(TEST_REAL_8192, TEST_IMAG_8192, true);
        
        assertArraysEqual(baseResult8192, optimResult8192, "Base and Optim8192 should produce same results");
        
        System.out.println("✓ Consistency tests passed");
    }
    
    /**
     * Test invalid input sizes
     */
    private static void testInvalidSizes() {
        System.out.println("Testing invalid input sizes...");
        
        // Test empty arrays
        double[] empty = {};
        double[] resultEmpty = FFTbase.fft(empty, empty, true);
        assert resultEmpty.length == 0 : "Empty input should return empty array";
        
        System.out.println("✓ Invalid size tests passed");
    }
    
    /**
     * Test inverse transform functionality
     */
    private static void testInverseTransform() {
        System.out.println("Testing inverse transforms...");
        
        // Test forward then inverse should recover original (approximately)
        double[] forward = FFTbase.fft(TEST_REAL_8, TEST_IMAG_8, true);
        
        // Extract real and imaginary parts from forward transform
        double[] forwardReal = new double[8];
        double[] forwardImag = new double[8];
        for (int i = 0; i < 8; i++) {
            forwardReal[i] = forward[2*i];
            forwardImag[i] = forward[2*i + 1];
        }
        
        // Apply inverse transform
        double[] inverse = FFTbase.fft(forwardReal, forwardImag, false);
        
        // Check if we recovered the original (within numerical precision)
        for (int i = 0; i < 8; i++) {
            double recoveredReal = inverse[2*i];
            double originalReal = TEST_REAL_8[i];
            assert Math.abs(recoveredReal - originalReal) < EPSILON : 
                String.format("Failed to recover original at index %d: expected %f, got %f", 
                    i, originalReal, recoveredReal);
        }
        
        System.out.println("✓ Inverse transform tests passed");
    }
    
    /**
     * Assert that two arrays are equal within epsilon tolerance
     */
    private static void assertArraysEqual(double[] expected, double[] actual, String message) {
        assert expected.length == actual.length : message + " - different lengths";
        
        for (int i = 0; i < expected.length; i++) {
            assert Math.abs(expected[i] - actual[i]) < EPSILON : 
                String.format("%s - difference at index %d: expected %f, got %f", 
                    message, i, expected[i], actual[i]);
        }
    }
}