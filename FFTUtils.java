/**
 * Backward compatibility wrapper for FFTUtils.
 * 
 * @deprecated Use com.fft.utils.FFTUtils instead. This class will be removed in version 3.0.
 * @author Engine AI Assistant
 * @since 1.0
 */
@Deprecated
public class FFTUtils {
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.fft(double[], double[], boolean).getInterleavedResult()
     */
    @Deprecated
    public static double[] fft(double[] real, double[] imaginary, boolean direct) {
        return com.fft.utils.FFTUtils.fftLegacy(real, imaginary, direct);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.fft(double[], boolean).getInterleavedResult()
     */
    @Deprecated
    public static double[] fft(double[] inputReal, boolean direct) {
        return com.fft.utils.FFTUtils.fft(inputReal, direct).getInterleavedResult();
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.fft(double[]).getInterleavedResult()
     */
    @Deprecated
    public static double[] fft(double[] inputReal) {
        return com.fft.utils.FFTUtils.fft(inputReal).getInterleavedResult();
    }
    
    /**
     * @deprecated Use FFTResult.getRealParts() instead
     */
    @Deprecated
    public static double[] getRealParts(double[] fftResult) {
        return com.fft.utils.FFTUtils.getRealParts(fftResult);
    }
    
    /**
     * @deprecated Use FFTResult.getImaginaryParts() instead
     */
    @Deprecated
    public static double[] getImagParts(double[] fftResult) {
        return com.fft.utils.FFTUtils.getImagParts(fftResult);
    }
    
    /**
     * @deprecated Use FFTResult.getMagnitudes() instead
     */
    @Deprecated
    public static double[] getMagnitudes(double[] fftResult) {
        return com.fft.utils.FFTUtils.getMagnitudes(fftResult);
    }
    
    /**
     * @deprecated Use FFTResult.getPhases() instead
     */
    @Deprecated
    public static double[] getPhases(double[] fftResult) {
        return com.fft.utils.FFTUtils.getPhases(fftResult);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.generateTestSignal()
     */
    @Deprecated
    public static double[] generateTestSignal(int size, double sampleRate, 
                                            double[] frequencies, double[] amplitudes) {
        return com.fft.utils.FFTUtils.generateTestSignal(size, sampleRate, frequencies, amplitudes);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.isPowerOfTwo()
     */
    @Deprecated
    public static boolean isPowerOfTwo(int n) {
        return com.fft.utils.FFTUtils.isPowerOfTwo(n);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.nextPowerOfTwo()
     */
    @Deprecated
    public static int nextPowerOfTwo(int n) {
        return com.fft.utils.FFTUtils.nextPowerOfTwo(n);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.zeroPadToPowerOfTwo()
     */
    @Deprecated
    public static double[] zeroPadToPowerOfTwo(double[] input) {
        return com.fft.utils.FFTUtils.zeroPadToPowerOfTwo(input);
    }
    
    /**
     * @deprecated Use com.fft.utils.FFTUtils.getImplementationInfo()
     */
    @Deprecated
    public static String getImplementationInfo(int size) {
        return com.fft.utils.FFTUtils.getImplementationInfo(size);
    }
}