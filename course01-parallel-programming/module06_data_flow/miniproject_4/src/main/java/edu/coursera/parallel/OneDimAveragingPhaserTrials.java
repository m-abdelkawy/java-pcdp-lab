package edu.coursera.parallel;

import java.util.concurrent.Phaser;

public class OneDimAveragingPhaserTrials {
    /**
     * Default constructor.
     */
    private OneDimAveragingPhaserTrials() {
    }

    /**
     * Sequential implementation of one-dimensional iterative averaging.
     *
     * @param iterations The number of iterations to run
     * @param myNew      A double array that starts as the output array
     * @param myVal      A double array that contains the initial input to the
     *                   iterative averaging problem
     * @param n          The size of this problem
     */
    public static void runSequential(final int iterations, final double[] myNew,
                                     final double[] myVal, final int n) {
        double[] next = myNew;
        double[] curr = myVal;

        for (int iter = 0; iter < iterations; iter++) {
            for (int j = 1; j <= n; j++) {
                next[j] = (curr[j - 1] + curr[j + 1]) / 2.0;
            }
            double[] tmp = curr;
            curr = next;
            next = tmp;
        }
    }

    /**
     * An example parallel implementation of one-dimensional iterative averaging
     * that uses phasers as a simple barrier (arriveAndAwaitAdvance).
     *
     * @param iterations The number of iterations to run
     * @param myNew      A double array that starts as the output array
     * @param myVal      A double array that contains the initial input to the
     *                   iterative averaging problem
     * @param n          The size of this problem
     * @param tasks      The number of threads/tasks to use to compute the solution
     */
    public static void runParallelBarrier(final int iterations,
                                          final double[] myNew, final double[] myVal, final int n,
                                          final int tasks) {
        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;

            threads[ii] = new Thread(() -> {
                double[] threadPrivateMyVal = myVal;
                double[] threadPrivateMyNew = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = (i * chunkSize) + 1;
                int right = (left + chunkSize) - 1;
                if (right > n) right = n;

                for (int iter = 0; iter < iterations; iter++) {
                    for (int j = left; j <= right; j++) {
                        threadPrivateMyNew[j] = (threadPrivateMyVal[j - 1]
                                + threadPrivateMyVal[j + 1]) / 2.0;
                    }
                    ph.arriveAndAwaitAdvance();

                    double[] temp = threadPrivateMyNew;
                    threadPrivateMyNew = threadPrivateMyVal;
                    threadPrivateMyVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A parallel implementation of one-dimensional iterative averaging that
     * uses the Phaser.arrive and Phaser.awaitAdvance APIs to overlap
     * computation with barrier completion.
     * <p>
     * TODO Complete this method based on the provided runSequential and
     * runParallelBarrier methods.
     *
     * @param iterations The number of iterations to run
     * @param myNew      A double array that starts as the output array
     * @param myVal      A double array that contains the initial input to the
     *                   iterative averaging problem
     *                   achieved 1.060763x speedup locally
     *                   achieved 0.904972x speedup on 4 cores on coursera autograder
     * @param n          The size of this problem
     * @param tasks      The number of threads/tasks to use to compute the solution
     */
    public static void runParallelFuzzyBarrier1(final int iterations,
                                                final double[] myNew, final double[] myVal, final int n,
                                                final int tasks) {

        Phaser[] phs = new Phaser[tasks];

        for (int i = 0; i < phs.length; i++) {
            phs[i] = new Phaser(1);
        }

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;



            threads[ii] = new Thread(() -> {
                double[] threadMyVal = myVal;
                double[] threadNewVal = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = i * chunkSize + 1;
                int right = left + chunkSize - 1;
                if (right > n) right = n;

                for (int itr = 0; itr < iterations; itr++) {
                    // calculation of average
                    for (int j = left+1; j <= right-1; j++) {
                        threadNewVal[j] = (threadMyVal[j - 1] + threadMyVal[j + 1]) / 2.0;
                    }

                    threadNewVal[left] =
                            (threadMyVal[left - 1] + threadMyVal[left + 1]) / 2.0;

                    if (right > left) {
                        threadNewVal[right] =
                                (threadMyVal[right - 1] + threadMyVal[right + 1]) / 2.0;
                    }

                    //before swapping, we need the phaser
                    int currentPhase = phs[i].arrive();


                    if (i - 1 >= 0) phs[i - 1].awaitAdvance(currentPhase);

                    if (i + 1 < tasks) phs[i + 1].awaitAdvance(currentPhase);

                    //swap
                    double[] temp = threadMyVal;
                    threadMyVal = threadNewVal;
                    threadNewVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * achieves 1.059556x speedup locally
     * achieves 0.784896x speedup on 4 cores on coursera autograder
     * @param iterations
     * @param myNew
     * @param myVal
     * @param n
     * @param tasks
     */
    public static void runParallelFuzzyBarrier0(final int iterations,
                                                final double[] myNew, final double[] myVal, final int n,
                                                final int tasks) {

        Phaser[] phs = new Phaser[tasks];

        for (int i = 0; i < phs.length; i++) {
            phs[i] = new Phaser(1);
        }

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;



            threads[ii] = new Thread(() -> {
                double[] threadMyVal = myVal;
                double[] threadNewVal = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = i * chunkSize + 1;
                int right = left + chunkSize - 1;
                if (right > n) right = n;

                for (int itr = 0; itr < iterations; itr++) {


                    threadNewVal[left] =
                            (threadMyVal[left - 1] + threadMyVal[left + 1]) / 2.0;

                    if (right > left) {
                        threadNewVal[right] =
                                (threadMyVal[right - 1] + threadMyVal[right + 1]) / 2.0;
                    }

                    //before swapping, we need the phaser
                    int currentPhase = phs[i].arrive();

                    // calculation of average in the inner loop
                    for (int j = left+1; j <= right-1; j++) {
                        threadNewVal[j] = (threadMyVal[j - 1] + threadMyVal[j + 1]) / 2.0;
                    }


                    if (i - 1 >= 0) phs[i - 1].awaitAdvance(currentPhase);

                    if (i + 1 < tasks) phs[i + 1].awaitAdvance(currentPhase);

                    //swap
                    double[] temp = threadMyVal;
                    threadMyVal = threadNewVal;
                    threadNewVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * achieves 1.059556x speedup locally
     * achieves 0.784896x speedup on 4 cores on coursera autograder
     * @param iterations
     * @param myNew
     * @param myVal
     * @param n
     * @param tasks
     */
    public static void runParallelFuzzyBarrier2(final int iterations,
                                                final double[] myNew, final double[] myVal, final int n,
                                                final int tasks) {

        Phaser[] phs = new Phaser[tasks];

        for (int i = 0; i < phs.length; i++) {
            phs[i] = new Phaser(1);
        }

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;



            threads[ii] = new Thread(() -> {
                double[] threadMyVal = myVal;
                double[] threadNewVal = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = i * chunkSize + 1;
                int right = left + chunkSize - 1;
                if (right > n) right = n;

                for (int itr = 0; itr < iterations; itr++) {

                    // calculation of average in the inner loop
                    for (int j = left+1; j <= right-1; j++) {
                        threadNewVal[j] = (threadMyVal[j - 1] + threadMyVal[j + 1]) / 2.0;
                    }

                    //before swapping, we need the phaser
                    int currentPhase = phs[i].arrive();

                    threadNewVal[left] =
                            (threadMyVal[left - 1] + threadMyVal[left + 1]) / 2.0;

                    if (right > left) {
                        threadNewVal[right] =
                                (threadMyVal[right - 1] + threadMyVal[right + 1]) / 2.0;
                    }

                    if (i - 1 >= 0) phs[i - 1].awaitAdvance(currentPhase);

                    if (i + 1 < tasks) phs[i + 1].awaitAdvance(currentPhase);

                    //swap
                    double[] temp = threadMyVal;
                    threadMyVal = threadNewVal;
                    threadNewVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * achieves 1.059556x speedup locally
     * achieves 0.784896x speedup on 4 cores on coursera autograder
     * @param iterations
     * @param myNew
     * @param myVal
     * @param n
     * @param tasks
     */
    public static void runParallelFuzzyBarrier(final int iterations,
                                               final double[] myNew, final double[] myVal, final int n,
                                               final int tasks) {

        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;

            threads[ii] = new Thread(() -> {
                double[] threadMyVal = myVal;
                double[] threadNewVal = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = i * chunkSize + 1;
                int right = left + chunkSize - 1;
                if (right > n) right = n;

                for (int itr = 0; itr < iterations; itr++) {

                    //calculate boundary elements for the chunk
                    threadNewVal[left] =
                            (threadMyVal[left - 1] + threadMyVal[left + 1]) / 2.0;

                    if (right > left) {
                        threadNewVal[right] =
                                (threadMyVal[right - 1] + threadMyVal[right + 1]) / 2.0;
                    }

                    //before swapping, we need the phaser
                    int currentPhase = ph.arrive();

                    // calculation of average in the inner loop
                    for (int j = left+1; j <= right-1; j++) {
                        threadNewVal[j] = (threadMyVal[j - 1] + threadMyVal[j + 1]) / 2.0;
                    }

                    ph.awaitAdvance(currentPhase);

                    //swap
                    double[] temp = threadMyVal;
                    threadMyVal = threadNewVal;
                    threadNewVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
