package edu.coursera.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinReciprocalArraySum2 {
    private static class SumArray extends RecursiveAction {
        static int SEQUENTIAL_THRESHOLD = 5;
        double[] arr;
        int start;
        int end;
        double ans = 0.0;

        SumArray(double[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
            // if we keep it 5, we recursively create tasks with 5 elements
            // and that can be enormous amount of tasks depending on input value
            // SEQUENTIAL_THRESHOLD = arr.length / 2;
            SEQUENTIAL_THRESHOLD = 1000;
        }

        @Override
        protected void compute() {
            if (end - start <= SEQUENTIAL_THRESHOLD) {
                for (int i = start; i < end; i++) {
                    ans += 1.0 / arr[i];
                }
            } else {
                SumArray left = new SumArray(arr, start, (start + end) / 2);
                SumArray right = new SumArray(arr, (start + end) / 2, end);
                left.fork();
                // right.fork();
                right.compute();
                left.join();
                // right.join();
                ans = left.ans + right.ans;
            }
        }
    }

    public static double seqArraySum(double[] arr) {
        long startTime = System.nanoTime();
        double sum1 = 0, sum2 = 0;
        for (int i = 0; i < arr.length / 2; i++) {
            sum1 += 1.0 / arr[i];
        }
        System.out.println("seqArraySum: leftSum = " + sum1);
        for (int i = arr.length / 2; i < arr.length; i++) {
            sum2 += 1.0 / arr[i];
        }
        System.out.println("seqArraySum: rightSum = " + sum2);
        double sum = sum1 + sum2;
        long timeInNanos = System.nanoTime() - startTime;
        printResults("seqArraySum", timeInNanos, sum);
        return sum;
    }

    public static double parArraySum(double[] arr) {
        long startTime = System.nanoTime();

        SumArray t = new SumArray(arr, 0, arr.length);
        ForkJoinPool.commonPool().invoke(t);
        double sum = t.ans;

        long timeInNanos = System.nanoTime() - startTime;
        printResults("parArraySum", timeInNanos, sum);
        return sum;
    }

    private static void printResults(String name, long timeInNanos, double result) {
        System.out.printf("  %s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, result);
    }

    public static void main(String[] args) {
        double[] arr = new double[100000000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;
        }

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8");


        for (int i = 0; i < 4; i++) {
            System.out.println("Run #" + (i + 1) + ":");
            seqArraySum(arr);
            parArraySum(arr);
//            parArraySumNoFinish(arr);
//            parArraySumNoFinishIntermediateValues(arr);
            System.out.println("===========");
        }
    }

}
