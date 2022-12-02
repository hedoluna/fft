import static java.lang.String.format;

class Main {
    public static void main(String[] args) {

        // double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT)

        // These numbers are to run the FFTs and to check the accuracy of the optimized
        // versions, length = 8
        double[] inputReal = {1, 22, 33, 44, 15, 16, 17, 18};
        double[] inputImag = {0, 0, 0, 0, 0, 0, 0, 0};

        double[] result_base = FFTbase.fft(inputReal, inputImag, true);
        double[] result_optim8 = FFToptim8.fft(inputReal, inputImag, true);

        // These numbers are to run the FFTs and to check the accuracy of the optimized
        // versions, length = 32
        double[] inputReal32 = {1, 22, 33, 44, 15, 16, 17, 18, 1, 22, 33, 44, 15, 16, 17, 18, 1, 22, 33, 44, 15, 16, 17,
                18, 1, 22, 33, 44, 15, 16, 17, 18};
        double[] inputImag32 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0};

        double[] result_base32 = FFTbase.fft(inputReal32, inputImag32, true);
        double[] result_optim32 = FFToptim32.fft(inputReal32, inputImag32, true);

        int howManyTimes = 1000 * 1000 * 1000 * 10 /* * 1000 * 1000 */;
        boolean printDebug = howManyTimes <= 1;

        // confronto versione base con versione ottimizzata 8 elementi
        long startTime1_base = System.nanoTime();
        // base
        if (printDebug)
            System.out.print("{");
        int i = 0;
        for (int ct = 0; ct < howManyTimes; ct++) {
            for (; i < result_base.length - 1; i++) {
                if (printDebug) {
                    System.out.print(result_base[i]);
                    System.out.print(", ");
                }
            }
        }
        if (printDebug) {
            System.out.print(result_base[i]);
            System.out.println("}");
        }
        long estimatedTime1_base = System.nanoTime() - startTime1_base;
        long startTime2_ottimizzato = System.nanoTime();
        // optim8
        if (printDebug)
            System.out.print("{");
        i = 0;
        for (int ct = 0; ct < howManyTimes; ct++) {
            for (; i < result_optim8.length - 1; i++) {
                if (printDebug) {
                    System.out.print(result_optim8[i]);
                    System.out.print(", ");
                }
            }
        }
        if (printDebug) {
            System.out.print(result_optim8[i]);
            System.out.println("}");
        }
        // ... the code being measured ...
        long estimatedTime2_ottimizzato = System.nanoTime() - startTime2_ottimizzato;

        // delta
        boolean flag = false;
        double delta;
        if (printDebug)
            System.out.print("{");
        i = 0;
        for (; i < result_optim8.length - 1; i++) {
            delta = result_optim8[i] - result_base[i];
            if (delta != 0)
                flag = true;
            if (printDebug) {
                System.out.print(delta);
                System.out.print(", ");
            }
        }
        if (printDebug)
            System.out.print(result_optim8[i] - result_base[i]);
        if (result_optim8[i] - result_base[i] != 0)
            flag = true;
        if (printDebug)
            System.out.println("}");

        System.out.println("estimatedTime1-8 base: " + estimatedTime1_base);
        System.out.println("estimatedTime2-8 ottimizzato: " + estimatedTime2_ottimizzato);
        System.out.printf("boost-8: %s%% of improvement%n", format("%.2f", (1 - ((double) estimatedTime2_ottimizzato / (double) estimatedTime1_base)) * 100));

        if (!flag)
            System.out.println("OK, same output in the two versions"); // non ho trovato discrepanze tra le 2 versioni
        else
            System.out.println("!!! KO !!!"); // ATTENZIONE! Ci sono differenze nelle 2 versioni

        // confronto versione base con versione ottimizzata 32 elementi
        startTime1_base = System.nanoTime();
        // base
        if (printDebug)
            System.out.print("{");
        for (int ct = 0; ct < howManyTimes; ct++) {
            for (i = 0; i < result_base32.length - 1; i++) {
                if (printDebug) {
                    System.out.print(result_base32[i]);
                    System.out.print(", ");
                }
            }
        }
        if (printDebug) {
            System.out.print(result_base32[i]);
            System.out.println("}");
        }
        estimatedTime1_base = System.nanoTime() - startTime1_base;

        startTime2_ottimizzato = System.nanoTime();
        // optim32
        if (printDebug)
            System.out.print("{");
        for (int ct = 0; ct < howManyTimes; ct++) {
            for (i = 0; i < result_optim32.length - 1; i++) {
                if (printDebug) {
                    System.out.print(result_optim32[i]);
                    System.out.print(", ");
                }
            }
        }
        if (printDebug) {
            System.out.print(result_optim32[i]);
            System.out.println("}");
        }
        // ... the code being measured ...
        estimatedTime2_ottimizzato = System.nanoTime() - startTime2_ottimizzato;

        // delta
        flag = false;
        if (printDebug)
            System.out.print("{");
        for (i = 0; i < result_optim32.length - 1; i++) {
            delta = result_optim32[i] - result_base32[i];
            if (delta != 0)
                flag = true;
            if (printDebug) {
                System.out.print(delta);
                System.out.print(", ");
            }
        }
        if (printDebug)
            System.out.print(result_optim32[i] - result_base32[i]);
        if (result_optim32[i] - result_base32[i] != 0)
            flag = true;
        if (printDebug)
            System.out.println("}");

        System.out.println("estimatedTime1-32: " + estimatedTime1_base);
        System.out.println("estimatedTime2-32: " + estimatedTime2_ottimizzato);
        System.out.printf("boost-32: %s%% of improvement%n", format("%.2f", (1 - ((double) estimatedTime2_ottimizzato / (double) estimatedTime1_base)) * 100));

        if (!flag)
            System.out.println("OK, same output in the two versions"); // non ho trovato discrepanze tra le 2 versioni
        else
            System.out.println("!!! KO !!!"); // ATTENZIONE! Ci sono differenze nelle 2 versioni

        // end all
    }
}