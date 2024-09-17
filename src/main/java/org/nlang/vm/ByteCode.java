package org.nlang.vm;

public class ByteCode {
    public static class Instruction {
        String name; // E.g., "iadd", "call"
        int n = 0;
        public Instruction(String name) { this(name,0); }
        public Instruction(String name, int nargs) {
            this.name = name;
            this.n = nargs;
        }
    }

    // INSTRUCTION BYTECODES (byte is signed; use a byte to keep 0..255)
    public static final byte IADD = 1;     // int add
    public static final byte ISUB = 2;
    public static final byte IMUL = 3;
    public static final byte ILT  = 4;     // int less than
    public static final byte IEQ  = 5;     // int equal
    public static final byte JMP = 6;     // branch
    public static final byte JNZ = 7;     // branch if true
    public static final byte JZ = 8;     // branch if true
    public static final byte ICONST = 9;   // push constant integer
    public static final byte LOAD   = 10;  // load from local context
    public static final byte GLOAD  = 11;  // load from global memory
    public static final byte STORE  = 12;  // store in local context
    public static final byte GSTORE = 13;  // store in global memory
    public static final byte PRINT  = 14;  // print stack top
    public static final byte POP  = 15;    // throw away top of stack
    public static final byte CALL = 16;
    public static final byte RET  = 17;    // return with/without value
    public static final byte FADD = 18;     // int add
    public static final byte FSUB = 19;
    public static final byte FMUL = 20;
    public static final byte HALT = 18;

    public static Instruction[] instructions = new Instruction[] {
            null, // <INVALID>
            new Instruction("iadd"), // index is the opcode
            new Instruction("isub"),
            new Instruction("imul"),
            new Instruction("ilt"),
            new Instruction("ieq"),
            new Instruction("br", 1),
            new Instruction("brt", 1),
            new Instruction("brf", 1),
            new Instruction("iconst", 1),
            new Instruction("load", 1),
            new Instruction("gload", 1),
            new Instruction("store", 1),
            new Instruction("gstore", 1),
            new Instruction("print"),
            new Instruction("pop"),
            new Instruction("call", 2), // call addr, nargs
            new Instruction("ret"),
            new Instruction("halt")
    };
}