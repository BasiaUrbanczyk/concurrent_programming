import java.util.function.IntBinaryOperator;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int ROWS = 10;
    private static final int COLUMNS = 100;

    private static class Matrix {

        private final int rows;
        private final int columns;
        private final IntBinaryOperator definition;
        private static final ConcurrentHashMap<Integer, Integer> map_sums = new ConcurrentHashMap<>(); // nr_wiersza -> suma
        private static final ConcurrentHashMap<Integer, Integer> map_count = new ConcurrentHashMap<>(); // nr_wiersza -> liczba_wątków

        public Matrix(int rows, int columns, IntBinaryOperator definition) {
            this.rows = rows;
            this.columns = columns;
            this.definition = definition;
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
                for (int i = 0; i < ROWS; i++) {
                    final int help = i ;
                    if (map_sums.putIfAbsent(i, definition.applyAsInt(i, column)) != null) {
                        map_sums.compute(i, (key, val)
                                -> val + definition.applyAsInt(help, column));
                    }
                    if (map_count.putIfAbsent(i, 1) != null) {
                        map_count.compute(help, (key, val)
                                -> val + 1);
                    }
                    if (map_count.remove(i, COLUMNS)) {
                        System.out.println(i + " -> " + map_sums.get(i));
                        map_sums.remove(i);
                    }
                }
            }
        }

        public void rowSumsConcurrent(){
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
        }
    }

    public static void main(String[] args) {
        Matrix matrix = new Matrix(ROWS, COLUMNS, (row, column) -> {
            int a = 2 * column + 1;
            return (row + 1) * (a % 4 - 2) * a;
        });
        matrix.rowSumsConcurrent();
    }
}