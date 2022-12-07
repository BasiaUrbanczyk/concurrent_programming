package com.company;

public class Main {

    private static volatile boolean found = false;

    private static class Writer implements Runnable {

        private final int start;
        private final int number;

        public Writer(int start, int number) {
            this.start = start;
            this.number = number;
        }

        @Override
        public void run() {
            for (int i = start; i < number; i+=30) {
                if (number % i == 0 || found) {
                    found = true;
                    break;
                }
            }
        }
    }

    static boolean isPrime(int n) {
        int[] tab = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29 };
        for (int j : tab) {
            if (n % j == 0 && n != j) {
                return false;
            }
        }

        int[] tab2 = { 31, 37, 41, 43, 47, 49, 53, 59 };
        Thread[] threads = new Thread[8];
        int i = 0;
        for (int j : tab2) {
            threads[i] = new Thread(new Writer(j, n));
            threads[i].start();
            i++;
        }
        for (int k = 0; k < 8; k++) {
            while (threads[k].isAlive()) {
                // pusta
            }
        }
        if (found) {
            found = false;
            return false;
        }

        return true;
    }

    public static void main(String args[]) {

        int count = 0;
        for (int i = 2; i <= 10000; i++){
            if (isPrime(i)) {
                count++;
            }
        }

        System.out.println(count);
    }
}
