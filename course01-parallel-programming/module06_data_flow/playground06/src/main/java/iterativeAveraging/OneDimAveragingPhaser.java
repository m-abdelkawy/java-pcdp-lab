package iterativeAveraging;

import java.util.concurrent.Phaser;

public class OneDimAveragingPhaser {
    private double[] newVals;
    private double[] oldVals;
    private final int n;

    String arrFromSequential;
    String arrFromBarrier;
    String arrFromFuzzyBarrier;

    public OneDimAveragingPhaser(int n) {
        this.n = n;

        oldVals = new double[n + 2];
        newVals = new double[n + 2];

        oldVals[0] = 0;
        oldVals[n + 1] = 1;

        newVals[0] = 0;
        newVals[n + 1] = 1;
    }

    private void reset() {
        oldVals = new double[n + 2];
        newVals = new double[n + 2];

        oldVals[0] = 0;
        oldVals[n + 1] = 1;

        newVals[0] = 0;
        newVals[n + 1] = 1;
    }

    public void runSequential(int iterations) {
        long startTime = System.nanoTime();

        for (int itr = 0; itr < iterations; itr++) {
            for (int i = 1; i < n + 1; i++) {
                double left = oldVals[i - 1];
                double right = oldVals[i + 1];
                newVals[i] = (left + right) / 2.0;
            }
            //after each iteration, swap the arrays
            double[] temp = oldVals;
            oldVals = newVals;
            newVals = temp;
        }

        long timeInNanos = System.nanoTime() - startTime;
        this.arrFromSequential = this.getArrayString(oldVals);
        printResults("runSequential", timeInNanos, this.arrFromSequential);
    }

    public void runForallBarrier(final int iterations, final int tasks) {
        this.reset();
        long startTime = System.nanoTime();

        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;
            threads[ii] = new Thread(() -> {
                double[] oldVals = this.oldVals;
                double[] newVals = this.newVals;

                // iterate over chunks, each of size (n/tasks)
                for (int itr = 0; itr < iterations; itr++) {
                    final int chunkSize = (n + tasks - 1) / tasks;
                    //chunk the array into groups of size n/tasks
                    int left = i * chunkSize + 1;

                    int right = left + chunkSize - 1;
                    if (right > n) right = n;

                    //iterate through elements of a group
                    for (int j = left; j <= right; j++) {
                        newVals[j] = (oldVals[j - 1] + oldVals[j + 1]) / 2.0;
                    }

                    //after each thread calculated a value on one iteration
                    //it reaches the barrier before switching the arrays
                    //so that the rest of the threads could see the same old values
                    //and there are no race conditions
                    //barrier
                    ph.arriveAndAwaitAdvance();

                    double[] temp = oldVals;
                    oldVals = newVals;
                    newVals = temp;
                }
            });
            threads[ii].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long timeInNanos = System.nanoTime() - startTime;
        this.arrFromBarrier = this.getArrayString(oldVals);
        printResults("runForallBarrier", timeInNanos, this.arrFromBarrier);
    }

    public void runForallFuzzyBarrier(final int iterations, final int tasks) {
        this.reset();
        long startTime = System.nanoTime();

        Phaser[] phs = new Phaser[tasks];
        for (int i = 0; i < phs.length; i++) {
            phs[i] = new Phaser(1);
        }
        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;
            threads[ii] = new Thread(() -> {
                double[] oldVals = this.oldVals;
                double[] newVals = this.newVals;

                // iterate over chunks, each of size (n/tasks)
                for (int itr = 0; itr < iterations; itr++) {
                    final int chunkSize = (n + tasks - 1) / tasks;
                    //chunk the array into groups of size n/tasks
                    int left = i * chunkSize + 1;
                    newVals[left] = (oldVals[left - 1] + oldVals[left + 1]) / 2.0;

                    int right = left + chunkSize - 1;
                    if (right > n) right = n;
                    newVals[right] = (oldVals[right - 1] + oldVals[right + 1]) / 2.0;


                    //iterate through elements of a group
                    for (int j = left + 1; j <= right; j++) {
                        newVals[j] = (oldVals[j - 1] + oldVals[j + 1]) / 2.0;
                    }
                    int currentPhase = phs[i].arrive();

                    // let this thread waits on the neighbouring threads as it needs their boundary values
                    if(i - 1 >= 0){
                        phs[i-1].awaitAdvance(currentPhase);
                    }

                    if(i + 1 < tasks){
                        phs[i+1].awaitAdvance(currentPhase);
                    }

                    double[] temp = oldVals;
                    oldVals = newVals;
                    newVals = temp;
                }
            });
            threads[ii].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long timeInNanos = System.nanoTime() - startTime;
        this.arrFromFuzzyBarrier = this.getArrayString(oldVals);
        printResults("runForallFuzzyBarrier", timeInNanos, this.arrFromFuzzyBarrier);
    }

    private static void printResults(String name, long timeInNanos, String result) {
        if (result.length() > 100000) {
//            System.out.printf("middle = %.6f, last = %.6f%n", oldVals[n / 2], oldVals[n]);
//            return;
            result = result.substring(0, 10);
        }
        System.out.printf("  %s completed in %8.3f milliseconds, with result = [%s] \n", name, timeInNanos / 1e6, result);
    }

    private boolean resutltsEqual() {
        return this.arrFromSequential.equals(this.arrFromBarrier)
                && this.arrFromSequential.equals(this.arrFromFuzzyBarrier);
    }

    private String getArrayString(double[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length - 1; i++) {
            sb.append(arr[i]).append(", ");
        }
        sb.append(arr[arr.length - 1]);
        return sb.toString();
    }

    static void main(String[] args) {
        OneDimAveragingPhaser avgIterative = new OneDimAveragingPhaser(1_00_000);
        avgIterative.runSequential(10_000);
        avgIterative.runForallBarrier(10_000, 12);
        avgIterative.runForallFuzzyBarrier(10_000, 12);
        System.out.println("are results equal? " + avgIterative.resutltsEqual());
    }
}
