package org.example.silicia;

public class FloatStatistics implements Statistics {
    private int count = 0;
    private Double min = null;
    private Double max = null;
    private double sum = 0.0;

    @Override
    public void add(String value) {
        try {
            double val = Double.parseDouble(value);
            count++;
            if (min == null || val < min) min = val;
            if (max == null || val > max) max = val;
            sum += val;
        } catch (NumberFormatException ignored) {
            // Игнор. некорректных значений
        }
    }

    @Override
    public void print(boolean full) {
        System.out.println("Floats:");
        System.out.println("Count: " + count);
        if (full && count > 0) {
            System.out.println("Min: " + min);
            System.out.println("Max: " + max);
            System.out.println("Sum: " + sum);
            double avg = sum / count;
            System.out.printf("Average: %.6f%n", avg);
        }
        System.out.println();
    }

    public int getCount() {
        return count;
    }
}







