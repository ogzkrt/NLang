package org.nlang.vm;

import static org.nlang.vm.ByteCode.*;

public class TestFibonacciRecursive {

    /**
     * fibonacci(n){
     * if n < 2
     * return n
     * else
     * return fibonacci(n-1) + fibonacci(n-2)
     * }
     */

    static int[] fibonacci_with_recursive = {

            LOAD, -3,     // 0000
            ICONST, 2,    // 0002
            ILT,          // 0004
            JZ, 10,      // 0005
            LOAD, -3,     // 0007
            RET,          // 0009

            LOAD, -3,     // 0010
            ICONST, 1,    // 0012
            ISUB,         // 0014
            CALL, 0, 1,   // 0015

            LOAD, -3,     // 0018
            ICONST, 2,    // 0020
            ISUB,         // 0022
            CALL, 0, 1,   // 0023
            IADD,         // 0026
            RET,          // 0027
            
            ICONST, 35,   // 0028 <---- MAIN  print(fib(30));
            CALL, 0, 1,   // 0030
            PRINT,        // 0033
            HALT          // 0034
    };

    public static void main(String[] args) {
        

        VM vm = new  VM(fibonacci_with_recursive, 28, 10);
        long start = System.currentTimeMillis();
        vm.exec();
        System.out.println("Time (ms): "+(System.currentTimeMillis()-start));

    }
}
