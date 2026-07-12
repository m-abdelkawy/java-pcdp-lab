package matrixmultiply;

import java.util.stream.IntStream;

import static edu.rice.pcdp.PCDP.*;


public class MatrixMultiply {
    private MatrixMultiply(){}

    public static void seqMatrixMultiply(double[][] A, double[][] B, double[][] C, int n){
        long startTime = System.nanoTime();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        long timeInNanos = System.nanoTime() - startTime;
        printResults("seqMatrixMultiply", timeInNanos, C[n-1][n-1]);
    }

    public static void seqMatrixMultiply_FP(double[][] A, double[][] B, double[][] C, int n){
        long startTime = System.nanoTime();

        IntStream.range(0, n)
                .forEach(i-> IntStream.range(0, n).forEach(j->
                        C[i][j] = IntStream.range(0, n)
                                .mapToDouble(k -> A[i][k] * B[k][j])
                                .sum())
                );

        long timeInNanos = System.nanoTime() - startTime;
        printResults("seqMatrixMultiply_FP", timeInNanos, C[n-1][n-1]);
    }

    public static void seqMatrixMultiply1(double[][] A, double[][] B, double[][] C, int n){
        long startTime = System.nanoTime();

        forseq2d(0, n-1, 0, n-1, (i, j) -> {
            C[i][j] = 0;
            for (int k = 0; k < n; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });

        long timeInNanos = System.nanoTime() - startTime;
        printResults("seqMatrixMultiply1", timeInNanos, C[n-1][n-1]);
    }

    public static void parMatrixMultiply(double[][] A, double[][] B, double[][] C, int n){
        long startTime = System.nanoTime();

        forall2dChunked(0, n-1, 0, n-1, (i, j) -> {
            C[i][j] = 0;
            for (int k = 0; k < n; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });

        long timeInNanos = System.nanoTime() - startTime;
        printResults("parMatrixMultiply", timeInNanos, C[n-1][n-1]);
    }

    private static void printResults(String name, long timeInNanos, double result) {
        System.out.printf("  %s completed in %8.3f milliseconds, with last element = %8.5f \n", name, timeInNanos / 1e6, result);
    }
}
