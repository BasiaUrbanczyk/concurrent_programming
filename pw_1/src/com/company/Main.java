package com.company;

import java.util.Arrays;
import java.util.Random;

public class Main {

    private static final int length = 102;
    private static final int fragmentLength = 10;

    private static class HelperSum implements Runnable {
        private final int position;
        private final Vector vector1;
        private final Vector vector2;
        private final Vector vectorSum;

        public HelperSum(int position, Vector vector1, Vector vector2, Vector vectorSum) {
            this.position = position;
            this.vector1 = vector1;
            this.vector2 = vector2;
            this.vectorSum = vectorSum;
        }

        @Override
        public void run() {
            for (int i = 0; i < fragmentLength; i++) {
                if (position + i == length) {
                    break;
                }
                vectorSum.vector[position + i] = vector1.vector[position + i] + vector2.vector[position + i];
            }
        }
    }

    private static class HelperDot implements Runnable {
        private final int position;
        private final Vector vector1;
        private final Vector vector2;
        private final int[] partSums;

        public HelperDot(int position, Vector vector1, Vector vector2, int[] partSums) {
            this.position = position;
            this.vector1 = vector1;
            this.vector2 = vector2;
            this.partSums = partSums;
        }

        @Override
        public void run() {
            for (int i = 0; i < fragmentLength; i++) {
                if (position + i == length) {
                    break;
                }
                partSums[position] += vector1.vector[position + i] * vector2.vector[position + i];
            }
        }

    }

    private static class Vector {

        private final int[] vector = new int[length];

        public Vector(int[] digits) {
            System.arraycopy(digits, 0, vector, 0, digits.length);
        }

        public Vector() {
            for (int i = 0; i < length; i++) {
                vector[i] = 0;
            }
        }

        Vector sum(Vector other) throws InterruptedException {
            Vector vectorSum = new Vector();
            Thread[] threads = new Thread[length / fragmentLength + 1];
            int j = 0;
            for (int i = 0; i < length; i += fragmentLength) {
                threads[j] = new Thread(new HelperSum(i, this, other, vectorSum));
                threads[j].start();
                j++;
            }
            j = 0;
            for (int i = 0; i < length; i += fragmentLength) {
                threads[j].join();
                j++;
            }
            return vectorSum;
        }

        Vector sumSeq(Vector other) {
            Vector vectorSumSeq = new Vector();
            for (int i = 0; i < length; i++) {
                vectorSumSeq.vector[i] = this.vector[i] + other.vector[i];
            }
            return vectorSumSeq;
        }

        int dot(Vector other) throws InterruptedException {
            int sum = 0;
            Thread[] threads = new Thread[length / fragmentLength + 1];
            int[] partSums = new int[length];
            int j = 0;
            for (int i = 0; i < length; i += fragmentLength) {
                threads[j] = new Thread(new HelperDot(i, this, other, partSums));
                threads[j].start();
                j++;
            }
            j = 0;
            for (int i = 0; i < length; i += fragmentLength) {
                threads[j].join();
                j++;
            }
            for (int i = 0; i < length; i += fragmentLength) {
                sum += partSums[i];
            }
            return sum;
        }

        int dotSeq(Vector other) {
            int sum = 0;
            for (int i = 0; i < length; i++) {
                sum+= this.vector[i] * other.vector[i];
            }
            return sum;
        }
    }

    public static void main(String[] args) {
        Random rand = new Random();

        int[] arr1 = new int[length];
        int[] arr2 = new int[length];
        for (int i = 0; i < length; i++) {
            arr1[i] = rand.nextInt(20);
            arr2[i] = rand.nextInt(20);
        }
        Vector vector1 = new Vector(arr1);
        Vector vector2 = new Vector(arr2);

        System.out.println("Wektor pierwszy: " + Arrays.toString(arr1));
        System.out.println("Wektor pierwszy: " + Arrays.toString(arr2));
        try {
            System.out.println("Suma liczona współbieżnie: " + Arrays.toString(vector1.sum(vector2).vector));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sum interrupted");
        }
        System.out.println("Suma liczona sekwencyjnie: " + Arrays.toString(vector1.sumSeq(vector2).vector));
        try {
            System.out.println("Iloczyn skalarny liczony współbieżnie: " + vector1.dot(vector2));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Dot interrupted");
        }
        System.out.println("Iloczyn skalarny liczony sekwencyjnie: " + vector1.dotSeq(vector2));
    }
}
