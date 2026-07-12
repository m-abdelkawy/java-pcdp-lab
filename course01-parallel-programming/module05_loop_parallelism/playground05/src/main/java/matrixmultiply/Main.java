package matrixmultiply;

import java.util.Random;

public class Main {

    static void main(String[] args) {
        int runs = 5;
        for (int i = 0; i < runs; i++) {
            int n = 500;

            double[][] A = randomMatrix(n);
            double[][] B = randomMatrix(n);

            double[][] C1 = new double[n][n];
            double[][] C2 = new double[n][n];

            MatrixMultiply.seqMatrixMultiply1(A, B, C1, n);

            MatrixMultiply.seqMatrixMultiply_FP(A, B, C2, n);

            MatrixMultiply.parMatrixMultiply(A, B, C2, n);

            System.out.println("Results equal? " + matricesEqual(C1, C2, n));
            System.out.println();

        }
    }

    private static double[][] randomMatrix(int n) {
        Random random = new Random();

        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }

        return matrix;
    }

    private static boolean matricesEqual(double[][] A, double[][] B, int n) {
        double EPSILON = 1e-9;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(A[i][j]) - B[i][j] > EPSILON) return false;
            }
        }
        return true;
    }
}
