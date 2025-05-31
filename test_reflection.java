public class test_reflection {
    public static void main(String[] args) {
        try {
            Class<?> fftClass = Class.forName("FFToptim64");
            System.out.println("FFToptim64 class found: " + fftClass);
            
            java.lang.reflect.Method fftMethod = fftClass.getMethod("fft", double[].class, double[].class, boolean.class);
            System.out.println("FFT method found: " + fftMethod);
            
            // Test calling it
            double[] real = new double[64];
            double[] imag = new double[64];
            real[0] = 1.0;
            
            double[] result = (double[]) fftMethod.invoke(null, real, imag, true);
            System.out.println("Method call successful, result length: " + result.length);
            System.out.println("First few values: " + result[0] + ", " + result[1] + ", " + result[2] + ", " + result[3]);
            
        } catch (Exception e) {
            System.out.println("Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}