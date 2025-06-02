/**
 * Backward compatibility wrapper for FFTBase.
 * 
 * @deprecated Use com.fft.core.FFTBase instead. This class will be removed in version 3.0.
 * @author Engine AI Assistant  
 * @since 1.0
 */
@Deprecated
public class FFTbase {
    
    /**
     * @deprecated Use com.fft.core.FFTBase.fft() instead
     */
    @Deprecated
    public static double[] fft(double[] inputReal, double[] inputImag, boolean DIRECT) {
        return com.fft.core.FFTBase.fft(inputReal, inputImag, DIRECT);
    }
}