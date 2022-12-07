import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.function.IntBinaryOperator;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Main {
    private static final int MAX_AVAILABLE = 100;
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
    private static final int ROWS = 10;
    private static final int COLUMNS = 100;

    private static class Matrix {

        private final int rows;
        private final int columns;
        private final IntBinaryOperator definition;
        static int[] row = new int[COLUMNS];
        static int which_row = 0;
        static int[] row_sums_conc = new int[ROWS];

        private static void count_row_sum(){
            row_sums_conc[which_row] = Arrays.stream(row).sum();
            which_row++;
        }

        private static final CyclicBarrier barrier = new CyclicBarrier(COLUMNS, Matrix::count_row_sum);

        public Matrix(int rows, int columns, IntBinaryOperator definition) {
            this.rows = rows;
            this.columns = columns;
            this.definition = definition;
        }

        public int[] rowSums() {
            int[] rowSums = new int[rows];
            for (int row = 0; row < rows; ++row) {
                int sum = 0;
                for (int column = 0; column < columns; ++column) {
                    sum += definition.applyAsInt(row, column);
                }
                rowSums[row] = sum;
            }
            return rowSums;
        }

        private static class Help implements Runnable{
            private final int column;
            private final IntBinaryOperator definition;

            private Help(int column, IntBinaryOperator definition) {
                this.column = column;
                this.definition = definition;
            }

            @Override
            public void run() {
                try {
                    for (int i = 0; i < ROWS; i++) {
                        row[column] = definition.applyAsInt(i, column);
                        barrier.await();
                    }

                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread t = Thread.currentThread();
                    t.interrupt();
                    System.err.println(t.getName() + " interrupted");
                }
            }
        }


        public int[] rowSumsConcurrent(){
            Thread[] threads = new Thread[COLUMNS];

            for (int i = 0; i < COLUMNS; ++i) {
                threads[i] = new Thread(new Help(i, definition), "column_" + i);
            }
            for (int i = 0; i < COLUMNS; ++i) {
                threads[i].start();
            }

            try {
                for (int i = 0; i < COLUMNS; ++i) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                System.err.println("rowSumsConcurrent interrupted");
                Thread.currentThread().interrupt();
            }
            return row_sums_conc;
        }
    }

    public static void main(String[] args) {
        Matrix matrix = new Matrix(ROWS, COLUMNS, (row, column) -> {
            int a = 2 * column + 1;
            return (row + 1) * (a % 4 - 2) * a;
        });

        int[] rowSums = matrix.rowSums();
        System.out.println("Sekwencyjne:");
        for (int i = 0; i < rowSums.length; i++) {
            System.out.println(i + " -> " + rowSums[i]);
        }

        int[] rowSumsConcurrent = matrix.rowSumsConcurrent();
        System.out.println("Współbieżne:");
        for (int i = 0; i < rowSums.length; i++) {
            System.out.println(i + " -> " + rowSumsConcurrent[i]);
        }
    }
}