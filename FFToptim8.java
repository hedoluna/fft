/**
 * @author Orlando Selenu Originally written in the Summer of 2008 Based on the
 *         algorithms originally published by E. Oran Brigham "The Fast Fourier
 *         Transform" 1973, in ALGOL60 and FORTRAN
 *
 * Released in the Public Domain.
 */
public class FFToptim8 {
  /**
   * The Fast Fourier Transform (optimized version for arrays of size 8).
   * 
   * This implementation is highly optimized for 8-element arrays with
   * completely unrolled loops and precomputed trigonometric values.
   * Note: Currently only supports direct transform (DIRECT parameter is ignored).
   *
   * @param inputReal an array of length 8, the real part
   * @param inputImag an array of length 8, the imaginary part
   * @param DIRECT    currently unused, always performs direct transform
   * @return a new array of length 16 (interleaved real and imaginary parts)
   */
  public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
    // - n is the dimension of the problem
    // - nu is its logarithm in base e
    int n = inputReal.length;

    if (n != 8) {
      System.out.println("ERROR: The number of input elements is not 8.");
      return new double[0];
    }

    // double ld = 3;

    // Declaration and initialization of the variables
    // ld should be an integer, actually, so I don't lose any information in
    // the cast
    // int nu = 3;
    // int n2 = 4;
    // int nu1 = 2;
    double[] xReal = new double[8];
    double[] xImag = new double[8];
    double tReal;
    double tImag;

    // System.out.println("nu:" + nu + " n2:" + n2 + " nu1:" + nu1);

    // System.out.println("" + (2 * Math.PI));
    // System.out.println("" + (-2 * Math.PI));

    // I don't want to overwrite the input arrays, so here I copy them. This
    // choice adds \Theta(2n) to the complexity.
    xReal[0] = inputReal[0];
    xImag[0] = inputImag[0];
    xReal[1] = inputReal[1];
    xImag[1] = inputImag[1];
    xReal[2] = inputReal[2];
    xImag[2] = inputImag[2];
    xReal[3] = inputReal[3];
    xImag[3] = inputImag[3];
    xReal[4] = inputReal[4];
    xImag[4] = inputImag[4];
    xReal[5] = inputReal[5];
    xImag[5] = inputImag[5];
    xReal[6] = inputReal[6];
    xImag[6] = inputImag[6];
    xReal[7] = inputReal[7];
    xImag[7] = inputImag[7];

    // First phase - calculation

    // nu = 3, nu1 = 2, l = 1, n2 = 4,

    // i = 1
    tReal = xReal[4];
    tImag = xImag[4];
    xReal[4] = xReal[0] - tReal;
    xImag[4] = xImag[0] - tImag;
    xReal[0] += tReal;
    xImag[0] += tImag;

    // i = 2
    tReal = xReal[5];
    tImag = xImag[5];
    xReal[5] = xReal[1] - tReal;
    xImag[5] = xImag[1] - tImag;
    xReal[1] += tReal;
    xImag[1] += tImag;

    // i = 3
    tReal = xReal[6];
    tImag = xImag[6];
    xReal[6] = xReal[2] - tReal;
    xImag[6] = xImag[2] - tImag;
    xReal[2] += tReal;
    xImag[2] += tImag;

    // i = 4
    tReal = xReal[7];
    tImag = xImag[7];
    xReal[7] = xReal[3] - tReal;
    xImag[7] = xImag[3] - tImag;
    xReal[3] += tReal;
    xImag[3] += tImag;

    // n2 = 2;
    // nu = 3, nu1 = 1, l = 2, n2 = 2
    // while (k < 8) {

    // k = 0
    tReal = xReal[2];
    tImag = xImag[2];
    xReal[2] = xReal[0] - tReal;
    xImag[2] = xImag[0] - tImag;
    xReal[0] += tReal;
    xImag[0] += tImag;

    // k = 1
    tReal = xReal[3];
    tImag = xImag[3];
    xReal[3] = xReal[1] - tReal;
    xImag[3] = xImag[1] - tImag;
    xReal[1] += tReal;
    xImag[1] += tImag;

    // k = 4
    tReal = xReal[6] * 6.123233995736766E-17 - xImag[6];
    tImag = xImag[6] * 6.123233995736766E-17 + xReal[6];
    xReal[6] = xReal[4] - tReal;
    xImag[6] = xImag[4] - tImag;
    xReal[4] += tReal;
    xImag[4] += tImag;

    // k = 5
    tReal = xReal[7] * 6.123233995736766E-17 - xImag[7];
    tImag = xImag[7] * 6.123233995736766E-17 + xReal[7];
    xReal[7] = xReal[5] - tReal;
    xImag[7] = xImag[5] - tImag;
    xReal[5] += tReal;
    xImag[5] += tImag;

    //////////////////////////////

    // n2 = 1;
    // nu = 3, nu1 = 0, l = 3, n2 = 1

    // k = 0
    tReal = xReal[1];
    tImag = xImag[1];
    xReal[1] = xReal[0] - tReal;
    xImag[1] = xImag[0] - tImag;
    xReal[0] += tReal;
    xImag[0] += tImag;

    // k = 2
    tReal = xReal[3] * 6.123233995736766E-17 - xImag[3];
    tImag = xImag[3] * 6.123233995736766E-17 + xReal[3];
    xReal[3] = xReal[2] - tReal;
    xImag[3] = xImag[2] - tImag;
    xReal[2] += tReal;
    xImag[2] += tImag;

    // k = 4
    tReal = xReal[5] * 0.7071067811865476 - xImag[5] * 0.7071067811865475; // c: 0.7071067811865476
    tImag = xImag[5] * 0.7071067811865476 + xReal[5] * 0.7071067811865475; // s: -0.7071067811865475
    xReal[5] = xReal[4] - tReal;
    xImag[5] = xImag[4] - tImag;
    xReal[4] += tReal;
    xImag[4] += tImag;

    // k = 6
    // p = 3;
    tReal = xReal[7] * -0.7071067811865475 - xImag[7] * 0.7071067811865476; // c: -0.7071067811865475
    tImag = xImag[7] * -0.7071067811865475 + xReal[7] * 0.7071067811865476; // s: -0.7071067811865476
    xReal[7] = xReal[6] - tReal;
    xImag[7] = xImag[6] - tImag;
    xReal[6] += tReal;
    xImag[6] += tImag;

    // Second phase - recombination

    // k = 1 r = 4
    tReal = xReal[1];
    tImag = xImag[1];
    xReal[1] = xReal[4];
    xImag[1] = xImag[4];
    xReal[4] = tReal;
    xImag[4] = tImag;
    // k = 3 r = 6
    tReal = xReal[3];
    tImag = xImag[3];
    xReal[3] = xReal[6];
    xImag[3] = xImag[6];
    xReal[6] = tReal;
    xImag[6] = tImag;

    // Here I have to mix xReal and xImag to have an array (yes, it should
    // be possible to do this stuff in the earlier parts of the code, but
    // it's here to readability).
    double[] newArray = new double[xReal.length << 1];
    double radice = 1 / Math.sqrt(n);
    for (int i = 0; i < newArray.length; i += 2) {
      int i2 = i >> 1;
      // I used Stephen Wolfram's Mathematica as a reference so I'm going
      // to normalize the output while I'm copying the elements.
      newArray[i] = xReal[i2] * radice;
      newArray[i + 1] = xImag[i2] * radice;
    }
    return newArray;
  }

  /**
   * The reference bit reverse function.
   */
  private static int bitreverseReference(int j) {

    return switch (j) {
      case 1 -> 4;
      case 2 -> 2;
      case 3 -> 6;
      case 4 -> 1;
      case 5 -> 5;
      case 6 -> 3;
      case 7 -> 7;
      default -> 0;
    };
  }
}