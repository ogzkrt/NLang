package org.nlang.vm;


import static org.nlang.vm.ByteCode.*;

public class TestVM {
    static int[] hello = {
            ICONST, 1,
            ICONST, 2,
            IADD,
            PRINT,
            HALT
    };
    

    public static void main(String[] args) {
        VM vm = new VM(hello, 0, 2);
        vm.exec();
    }
}