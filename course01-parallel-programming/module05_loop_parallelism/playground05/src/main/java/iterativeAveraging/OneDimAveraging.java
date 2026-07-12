package iterativeAveraging;

import java.util.ArrayList;

import static edu.rice.pcdp.PCDP.forall;

public class OneDimAveraging {
    private double[] values;
    private double[] newValues;
    private final int n;

    public OneDimAveraging(int n) {
        this.n = n;

        values = new double[n + 2];
        newValues = new double[n + 2];

        values[0] = 0;
        values[n + 1] = 1;

        newValues[0] = 0;
        newValues[n + 1] = 1;
    }

    public void runSequential(int iterations) {
        long startTime = System.nanoTime();
        for (int iter = 0; iter < iterations; iter++) {
            for (int i = 1; i <= n; i++) {
                double left = values[i - 1];
                double right = values[i + 1];
                newValues[i] = (left + right) / 2.0;
            }
//            printIteration(iter, values, newValues);
            double[] temp = newValues;
            newValues = values;
            values = temp;
        }
        long timeInNanos = System.nanoTime() - startTime;
        printResults("runSequential", timeInNanos, this.getArrayString(values));
    }

    public void runForAll(final int iterations){
        long startTime = System.nanoTime();

        for (int iter = 0; iter < iterations; iter++) {
            forall(1, n, i->{
                newValues[i] = (values[i - 1] + values[i+1])/2.0;
            });
//            printIteration(iter, values, newValues);
            double[] temp = newValues;
            newValues = values;
            values = temp;
        }
        long timeInNanos = System.nanoTime() - startTime;
        printResults("runForAll", timeInNanos, this.getArrayString(values));
    }

    public void runForAllGrouped(final int iterations, final int tasks){
        long startTime = System.nanoTime();

        for (int iter = 0; iter < iterations; iter++) {
            forall(0, tasks - 1, i->{
                for (int j = i * (n/tasks) + 1; j < (i + 1) * (n/tasks); j++) {
                    newValues[j] = (values[j - 1] + values[j+1])/2.0;
                }
            });
//            printIteration(iter, values, newValues);
            double[] temp = newValues;
            newValues = values;
            values = temp;
        }
        long timeInNanos = System.nanoTime() - startTime;
        printResults("runForAllGrouped", timeInNanos, this.getArrayString(values));
    }

    public void runForAllBarrier(final int iterations, final int tasks){
//        long startTime = System.nanoTime();
//
//        forall(0, tasks - 1, i->{
//            for (int iter = 0; iter < iterations; iter++) {
//                for (int j = i * (n/tasks) + 1; j < (i + 1) * (n/tasks); j++) {
//                    newValues[j] = (values[j - 1] + values[j+1])/2.0;
//                }
//            }
//        });
//
//        for (int iter = 0; iter < iterations; iter++) {
//            forall(0, tasks - 1, i->{
//                for (int j = i * (n/tasks) + 1; j < (i + 1) * (n/tasks); j++) {
//                    newValues[j] = (values[j - 1] + values[j+1])/2.0;
//                }
//                next();
//            });
////            printIteration(iter, values, newValues);
//            double[] temp = newValues;
//            newValues = values;
//            values = temp;
//        }
//        long timeInNanos = System.nanoTime() - startTime;
//        printResults("runForAllGrouped", timeInNanos, this.getArrayString(values));
    }


    private void printIteration(int iteration, double[] oldValues, double[] newValues){
        StringBuilder sb = new StringBuilder();
        sb.append("iteration: ").append(iteration).append("\n");
        sb.append("old: ").append(getArrayString(oldValues)).append("\n");
        sb.append("new: ").append(getArrayString(newValues)).append("\n");
        System.out.println(sb.toString());
    }


    private String getArrayString(double[] arr){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length - 1; i++) {
            sb.append(arr[i]).append(", ");
        }
        sb.append(arr[arr.length - 1]);
        return sb.toString();
    }

    private static void printResults(String name, long timeInNanos, String result) {
        if(result.length() > 1000000){
            result = result.substring(0, 10);
        }
        System.out.printf("  %s completed in %8.3f milliseconds, with result = [%s] \n", name, timeInNanos / 1e6, result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length - 1; i++) {
            sb.append(values[i]).append(", ");
        }
        sb.append(values[values.length - 1]);
        return sb.toString();
    }

    static void main() {
        OneDimAveraging avgSequential = new OneDimAveraging(1_000_000);
        avgSequential.runSequential(50);
//        System.out.println(avgSequential);

        OneDimAveraging avgparallel = new OneDimAveraging(1_000_000);
        avgparallel.runForAll(50);
//        System.out.println(avgparallel);

        OneDimAveraging avgparallelGrouped = new OneDimAveraging(1_000_000);
        avgparallelGrouped.runForAllGrouped(50, 12);
    }
}
