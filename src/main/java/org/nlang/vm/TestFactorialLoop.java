package org.nlang.vm;

import static org.nlang.vm.ByteCode.*;

public class TestFactorialLoop {
    
    /**
     *   int i = 2;
     *   int n = 7;
     *   int result = 1;
     *   while (i<n){
     *       result = result * i;
     *       i = i + 1;
     *   }
     *   print result;
     * 
     * */

    static int[] factorial_with_loop = {
            // RESULT = 1
            ICONST, 1,      // 0000
            GSTORE, 2,      // 0002
            // N = 5
            ICONST, 7,      // 0004
            GSTORE, 0,      // 0006
            // I = 2
            ICONST, 2,      // 0008
            GSTORE, 1,      // 0010
            // I<5
            GLOAD, 1,        // 0012
            GLOAD, 0,        // 0014
            ILT,            // 0016
            
            BRF, 35,      // 0017
            
            // I * RESULT
            GLOAD, 1,       // 0019
            GLOAD, 2,       // 0021
            IMUL,           // 0023
            GSTORE, 2,      // 0024
            // I = I + 1
            ICONST, 1,      // 0026
            GLOAD, 1,       // 0028
            IADD,           // 0030
            GSTORE, 1,      // 0031
            
            BR, 12,      // 0033
            
            // PRINT RESULT
            GLOAD, 2,       // 0035
            PRINT,          // 0037
            HALT            // 0038
            
            
            
            
            
            
    };

    public static void main(String[] args) {
        VM vm = new VM(factorial_with_loop, 0, 3);
        vm.exec();
        
    }
}
