package edu.coursera.parallel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicReference;

public class ForkJoinReciprocalArraySum {

    /**
     * a ForkJoin task T1
     */
    private static class ReciprocalSumTask extends RecursiveTask<Double>{
        private final double[] arr;
        private final int start;
        private final int end;

        public ReciprocalSumTask(double[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        /**
         * Task body S1
         * @return
         */
        @Override
        protected Double compute() {
            double sum = 0.0;
            for (int i = 0; i < end; i++) {
                sum += 1.0/arr[i];
            }
            return sum;
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

        // async s1 - create a child task to compute the reciprocal sum of the left half
        ReciprocalSumTask leftTask = new ReciprocalSumTask(arr, 0, arr.length / 2);

        // fork - create a child task for s1
        leftTask.fork();

        // s2 - parent task continues immediately
        double rightSum = 0;
        for (int i = arr.length / 2; i < arr.length; i++) {
            rightSum += 1.0 / arr[i];
        }

        // Finish / join - wait for the child task since the parent depends on it
        double leftSum = leftTask.join();
        long timeInNanos = System.nanoTime() - startTime;

        // s3 combine results after both computations have completed
        double sum = leftSum + rightSum;
        printResults("parArraySum", timeInNanos, sum);
        return sum;
    }

//    public static double parArraySumNoFinish(double[] arr) {
//        long startTime = System.nanoTime();
//        AtomicReference<Double> leftSum = new AtomicReference<>(0.0);
//
//        // async s1 - create a child task to compute the reciprocal sum of the left half
//        ReciprocalSumTask leftTask = new ReciprocalSumTask(arr, 0, arr.length / 2);
//
//        // fork - create a child task for s1
//        leftTask.fork();
//
//        // s2 - parent task continues immediately
//        double rightSum = 0;
//        for (int i = arr.length / 2; i < arr.length; i++) {
//            rightSum += 1.0 / arr[i];
//        }
//
//        // no Finish / join - wait for the child task since the parent depends on it
//        long timeInNanos = System.nanoTime() - startTime;
//
//        // s3 combine results after both computations have completed
//        double sum = leftSum.get() + rightSum;
//        printResults("parArraySumNoFinish", timeInNanos, sum);
//        return sum;
//    }
//
//    public static double parArraySumNoFinishIntermediateValues(double[] arr) {
//        long startTime = System.nanoTime();
//        AtomicReference<Double> leftSum = new AtomicReference<>(0.0);
//        // async s1 - create a child task to compute the reciprocal sum of the left half
//        CompletableFuture<Double> left = CompletableFuture.supplyAsync(() -> {
//            double sum = 0;
//
//            for (int i = 0; i < arr.length / 2; i++) {
//                sum += 1.0 / arr[i];
//                leftSum.set(sum);
//            }
//
//
//            return sum;
//        });
//
//        // s2 - parent task continues immediately
//        double rightSum = 0;
//        for (int i = arr.length / 2; i < arr.length; i++) {
//            rightSum += 1.0 / arr[i];
//        }
//
//        // no Finish / join - wait for the child task since the parent depends on it
//        long timeInNanos = System.nanoTime() - startTime;
//
//        // s3 combine results after both computations have completed
//        double sum = leftSum.get() + rightSum;
//        printResults("parArraySumNoFinishIntermediateValues", timeInNanos, sum);
//        return sum;
//    }

    private static void printResults(String name, long timeInNanos, double result) {
        System.out.printf("  %s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, result);
    }

    public static void main(String[] args) {
        double[] arr = new double[100000000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;
        }


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
