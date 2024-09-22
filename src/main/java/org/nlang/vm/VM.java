package org.nlang.vm;

import static org.nlang.vm.ByteCode.*;


public class VM {
    public static final int DEFAULT_STACK_SIZE = 1000;
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    
    int ip; 
    int sp = -1;
    int fp = -1;

    int startip = 0;
    
    int[] code;
    int[] globals;
    int[] stack;
    

    public VM(int[] code, int startip, int nglobals) {
        this.code = code;
        this.startip = startip;
        globals = new int[nglobals];
        stack = new int[DEFAULT_STACK_SIZE];
    }

    public void exec() {
        ip = startip;
        cpu();
    }

    /** Simulate the fetch-decode execute cycle */
    protected void cpu() {
        int opcode = code[ip];
        int a,b,addr,offset;
        while (opcode!= HALT) {
            ip++;
            switch (opcode) {
                case IADD:
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = a + b;
                    break;
                case ISUB:
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = a - b;
                    break;
                case IMUL:
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = a * b;
                    break;
                case ILT :
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = (a < b) ? TRUE : FALSE;
                    break;
                case IEQ :
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = (a == b) ? TRUE : FALSE;
                    break;
                case BR :
                    ip = code[ip++];
                    break;
                case BRT :
                    addr = code[ip++];
                    if ( stack[sp--]==TRUE ) ip = addr;
                    break;
                case BRF :
                    addr = code[ip++];
                    if ( stack[sp--]==FALSE ) ip = addr;
                    break;
                case ICONST:
                    stack[++sp] = code[ip++]; // push operand
                    break;
                case LOAD :
                    offset = code[ip++];
                    stack[++sp] = stack[fp+offset];
                    break;
                case GLOAD :// load from global memory
                    addr = code[ip++];
                    stack[++sp] = globals[addr];
                    break;
                case STORE :
                    offset = code[ip++];
                    stack[fp+offset] = stack[sp--];
                    break;
                case GSTORE :
                    addr = code[ip++];
                    globals[addr] = stack[sp--];
                    break;
                case PRINT :
                    System.out.println(stack[sp--]);
                    break;
                case POP:
                    --sp;
                    break;
                case CALL :
                    addr = code[ip++];
                    int nargs = code[ip++];
                    stack[++sp] = nargs;
                    stack[++sp] = fp;
                    stack[++sp] = ip;
                    fp = sp;
                    ip = addr;					
                    break;
                case RET:
                    int rvalue = stack[sp--];	
                    sp = fp;					
                    ip = stack[sp--];			
                    fp = stack[sp--];			
                    nargs = stack[sp--];		
                    sp -= nargs;				
                    stack[++sp] = rvalue;	  	
                    break;
                default :
                    throw new Error("invalid opcode: "+opcode+" at ip="+(ip-1));
            }
            opcode = code[ip];
        }
    }
}