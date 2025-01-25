package com.microsoft.demo;

/**
 * This class represents a simple calculator that can perform basic arithmetic operations.
 * <p>
 * The calculator supports addition, subtraction, multiplication, and division operations.
 * <p>
 * Usage example:
 * <pre>
 *     Calculator calculator = new Calculator();
 *     double sum = calculator.add(5.0, 3.0);  // 8.0
 *     double quotient = calculator.divide(10.0, 2.0);  // 5.0
 * </pre>
 *
 * @author John Doe
 * @version 1.0
 */
public class Calculator {

    /**
     * Adds two numbers together.
     * <p>
     * This method takes two numeric values and returns their sum.
     *
     * @param num1 The first number to add.
     * @param num2 The second number to add.
     * @return The sum of num1 and num2.
     * @throws IllegalArgumentException If either of the numbers is negative.
     * @since 1.0
     */
    public double add(double num1, double num2) throws IllegalArgumentException {
        if (num1 < 0 || num2 < 0) {
            throw new IllegalArgumentException("Numbers must be non-negative");
        }
        return num1 + num2;
    }

    /**
     * Divides two numbers.
     * <p>
     * This method divides the first number by the second number.
     * If the second number is zero, it throws an ArithmeticException.
     *
     * @param num1 The numerator (dividend).
     * @param num2 The denominator (divisor).
     * @return The result of num1 divided by num2.
     * @throws ArithmeticException If num2 is zero.
     * @since 1.0
     */
    public double divide(double num1, double num2) throws ArithmeticException {
        if (num2 == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return num1 / num2;
    }
}

