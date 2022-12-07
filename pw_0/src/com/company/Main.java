package com.company;

public class Main {
    private static class Writer implements Runnable {
        private static final int THREADS_COUNT = 20;

        private final int nr;

        public Writer(int nr) {
            this.nr = nr;
        }

        @Override
        public void run() {
            if (nr < THREADS_COUNT) {
                Thread watek = new Thread(new Writer( nr + 1));
                watek.start();
                if (nr == 15) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(nr + " ");
                while (watek.isAlive()) {
                    // pusta
                }
            }
            else {
                System.out.print(nr + " ");
            }
        }

    }

    public static void main(String args[]) {
        Thread watek = new Thread(new Writer( 1));
        System.out.println("Początek");
        watek.start();
        while (watek.isAlive()) {
            // pusta
        }
        System.out.println();
        System.out.println("Koniec");
    }

}
