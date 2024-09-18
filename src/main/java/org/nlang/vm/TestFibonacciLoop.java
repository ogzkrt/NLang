package org.nlang.vm;

import static org.nlang.vm.ByteCode.*;

public class TestFibonacciLoop {
    
    
    /**
     *  fib1 = 0
     *  fib2 = 1;
     *  i = 0;
     *  n = 5;
     *  while(i<n){
     *      temp = fib2;
     *      fib2 = fib1 + fib2;
     *      fib1 = temp;
     *      i = i + 1;
     *  }
     *  print fib2;
     * 
     * */
    
    static  int[] fibonacci_with_loop = {
            
            // FIB 1
            ICONST, 0,  // 0000
            GSTORE, 0,  // 0002
            // FIB 2
            ICONST, 1,  // 0004
            GSTORE, 1,  // 0006
            // I = 0
            ICONST, 1,  // 0008
            GSTORE, 2,  // 0010
            // N = VALUE
            ICONST, 30,  // 0012
            GSTORE, 3,  // 0014
            // WHILE 
            GLOAD, 2,   // 0016
            GLOAD, 3,   // 0018
            ILT,        // 0020

            JZ,  47, // 0021
            // TEMP = FIB2
            GLOAD, 1,   // 0023
            GSTORE, 4,  // 0025
            
            // FIB2 = FIB2 + FIB1
            GLOAD, 0,   // 0027
            GLOAD, 1,   // 0029
            IADD,       // 0031
            GSTORE, 1,  // 0032
            // FIB1 = TEMP
            GLOAD,4,    // 0034
            GSTORE,0,   // 0036
            
            GLOAD, 2,   // 0038
            ICONST, 1,  // 0040
            IADD,       // 0042
            GSTORE, 2,  // 0043

            JMP, 16,     // 0045

            GLOAD,1,    // 0047
            PRINT,      // 0049
            HALT        // 0050
            
            
    };

    public static void main(String[] args) {
        VM vm = new VM(fibonacci_with_loop, 0, 10);
        long start = System.currentTimeMillis();
        vm.exec();
        System.out.println("Time (ms): "+(double)(System.currentTimeMillis()-start));
    }
}
