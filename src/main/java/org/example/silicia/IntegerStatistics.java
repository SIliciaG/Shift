
package org.example.silicia;

import java.math.BigInteger;

public class IntegerStatistics implements Statistics {
    private int count = 0;
    private BigInteger min = null;
    private BigInteger max = null;
    private BigInteger sum = BigInteger.ZERO;

    @Override
    public void add(String value) {
        try {
            BigInteger val = new BigInteger(value);
            count++;
            if (min == null || val.compareTo(min) < 0) min = val;
            if (max == null || val.compareTo(max) > 0) max = val;
            sum = sum.add(val);
        } catch (NumberFormatException ignored) {
            // Игнорируем некорректные значения (хотя по логике их не будет)
        }
    }

    @Override
    public void print(boolean full) {
        System.out.println("Integers:");
        System.out.println("Count: " + count);
        if (full && count > 0) {
            System.out.println("Min: " + min);
            System.out.println("Max: " + max);
            System.out.println("Sum: " + sum);
            // Среднее как BigDecimal для точности:
            double avg = sum.doubleValue() / count;
            System.out.printf("Average: %.6f%n", avg);
        }
        System.out.println();
    }

    public int getCount() {
        return count;
    }
}




