package org.nlang.vm;

import static org.nlang.vm.ByteCode.JZ;
import static org.nlang.vm.ByteCode.CALL;
import static org.nlang.vm.ByteCode.HALT;
import static org.nlang.vm.ByteCode.ICONST;
import static org.nlang.vm.ByteCode.ILT;
import static org.nlang.vm.ByteCode.IMUL;
import static org.nlang.vm.ByteCode.ISUB;
import static org.nlang.vm.ByteCode.LOAD;
import static org.nlang.vm.ByteCode.PRINT;
import static org.nlang.vm.ByteCode.RET;

public class TestFactorialRecursive {

    static int[] factorial = {
            LOAD, -3,                // 0
            ICONST, 2,               // 2
            ILT,                     // 4
            JZ, 10,                 // 5
            ICONST, 1,               // 7
            RET,                     // 9
            LOAD, -3,                // 10
            LOAD, -3,                // 12
            ICONST, 1,               // 14
            ISUB,                    // 16
            CALL, 0, 1,              // 17
            IMUL,                    // 20
            RET,                     // 21
            ICONST, 6,               // 22    <-- MAIN METHOD
            CALL, 0, 1,              // 24
            PRINT,                   // 27
            HALT                     // 28
    };

    public static void main(String[] args) {
        VM vm = new VM(factorial, 22, 0);
        vm.exec();
        
    }
}
