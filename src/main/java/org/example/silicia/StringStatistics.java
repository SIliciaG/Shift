package org.example.silicia;

public class StringStatistics implements Statistics {
    private int count = 0;
    private int minLength = Integer.MAX_VALUE;
    private int maxLength = Integer.MIN_VALUE;

    @Override
    public void add(String value) {
        if (value == null) return;
        count++;
        int len = value.length();
        if (len < minLength) minLength = len;
        if (len > maxLength) maxLength = len;
    }

    @Override
    public void print(boolean full) {
        System.out.println("Strings:");
        System.out.println("Count: " + count);
        if (full && count > 0) {
            System.out.println("Min length: " + minLength);
            System.out.println("Max length: " + maxLength);
        }
        System.out.println();
    }

    public int getCount() {
        return count;
    }
}
