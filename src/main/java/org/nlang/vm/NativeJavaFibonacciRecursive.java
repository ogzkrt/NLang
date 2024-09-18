package org.nlang.vm;

public class NativeJavaFibonacciRecursive {

    static int fibonacci(int n) {
        if (n < 2)
            return n;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }
    

    
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int n = 35;
        int result = fibonacci(n);
        System.out.println("Fibonacci of "+n+" :"+result);
        System.out.println("Time (ms): "+(System.currentTimeMillis()-start));
    }
}
