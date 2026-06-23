import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FutureReciprocalArraySum {

    private static class SumTask extends RecursiveTask<Double> {
        private final double[] arr;
        private final int start;
        private final int end;

        private static final int THRESHOLD = 10_000;

        public SumTask(double[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Double compute() {
            if (end - start <= THRESHOLD) {
                double sum = 0;

                for (int i = start; i < end; i++) {
                    sum += 1.0 / arr[i];
                }

                return sum;
            }
            int mid = (start + end) / 2;
            SumTask leftTask = new SumTask(arr, start, mid);
            SumTask rightTask = new SumTask(arr, mid, end);
            rightTask.fork(); //future
            double left = leftTask.compute();
            double right = rightTask.join(); //wait for the right task to finish
            return left + right;
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
        return sum1 + sum2;
    }

    public static double parArraySum(double[] arr) {
        long startTime = System.nanoTime();

        SumTask task = new SumTask(arr, 0, arr.length - 1);
        double sum = ForkJoinPool.commonPool().invoke(task);
        long timeInNanos = System.nanoTime() - startTime;
        printResults("parArraySum", timeInNanos, sum);
        return sum;
    }

    private static void printResults(String name, long timeInNanos, double result) {
        System.out.printf("  %s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, result);
    }

    static void main() {
        double[] arr = new double[100000000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;
        }

        for (int i = 0; i < 4; i++) {
            System.out.println("Run #" + (i + 1) + ":");
            seqArraySum(arr);
            parArraySum(arr);
            System.out.println("===========");
        }
    }
}
