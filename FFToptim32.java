/**
 * @author Orlando Selenu Originally written in the Summer of 2008 Based on the
 *         algorithms originally published by E. Oran Brigham "The Fast Fourier
 *         Transform" 1973, in ALGOL60 and FORTRAN
 *
 * Released in the Public Domain.
 */
public class FFToptim32 {
  /**
   * The Fast Fourier Transform (generic version, with NO optimizations).
   *
   * @param inputReal an array of length n, the real part
   * @param inputImag an array of length n, the imaginary part
   * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
   * @return a new array of length 2n
   */
  public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
    // - n is the dimension of the problem
    // - nu is its logarithm in base e
    int n = inputReal.length;

    // If n is a power of 2, then ld is an integer (_without_ decimals)
    double ld = 5; // 2^5 = 32

    // Here I check if n is a power of 2. If exist decimals in ld, I quit
    // from the function returning null.
    if (n != 32) {
      System.out.println("The number of elements is not 32.");
      return null;
    }

    // Declaration and initialization of the variables
    // ld should be an integer, actually, so I don't lose any information in
    // the cast
    int nu = 5; // 5
    int n2 = 16; // 16
    int nu1 = 4; // 4
    double[] xReal = new double[32];
    double[] xImag = new double[32];
    double tReal, tImag, p, arg, c, s;

    // System.out.println("nu:" + nu + " ld:" + ld + " n2:" + n2 + " nu1:" + nu1);

    // Here I check if I'm going to do the direct transform or the inverse
    // transform.
    double constant;
    if (DIRECT)
      constant = -2 * Math.PI;
    else
      constant = 2 * Math.PI;

    // System.out.println("" + (2 * Math.PI));
    // System.out.println("" + (-2 * Math.PI));

    // I don't want to overwrite the input arrays, so here I copy them. This
    // choice adds \Theta(2n) to the complexity.
    for (int i = 0; i < 32; i++) {
      xReal[i] = inputReal[i];
      xImag[i] = inputImag[i];
    }

    // First phase - calculation
    int k = 0;
    for (int l = 1; l <= 5; l++) {
      while (k < n) {
        for (int i = 1; i <= n2; i++) {
          p = bitreverseReference(k >> nu1, 5);
          // direct FFT or inverse FFT
          arg = constant * p / n;
          c = Math.cos(arg);
          s = Math.sin(arg);
          tReal = xReal[k + n2] * c + xImag[k + n2] * s;
          tImag = xImag[k + n2] * c - xReal[k + n2] * s;
          xReal[k + n2] = xReal[k] - tReal;
          xImag[k + n2] = xImag[k] - tImag;
          xReal[k] += tReal;
          xImag[k] += tImag;
          k++;
        }
        k += n2;
      }
      k = 0;
      nu1--;
      n2 /= 2;
    }

    // Second phase - recombination
    k = 0;
    int r;
    while (k < n) {
      r = bitreverseReference(k, 5);
      if (r > k) {
        tReal = xReal[k];
        tImag = xImag[k];
        xReal[k] = xReal[r];
        xImag[k] = xImag[r];
        xReal[r] = tReal;
        xImag[r] = tImag;
      }
      k++;
    }

    // Here I have to mix xReal and xImag to have an array (yes, it should
    // be possible to do this stuff in the earlier parts of the code, but
    // it's here to readibility).
    double[] newArray = new double[xReal.length * 2];
    double radice = 1 / Math.sqrt(n);
    for (int i = 0; i < newArray.length; i += 2) {
      int i2 = i / 2;
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
  private static int bitreverseReference(int j, int nu) {
    int j2;
    int j1 = j;
    int k = 0;
    for (int i = 1; i <= 5; i++) {
      j2 = j1 / 2;
      k = 2 * k + j1 - 2 * j2;
      j1 = j2;
    }
    return k;
  }
}