package org.nlang.vm;

import java.util.Stack;

public class ExVM {
    private int[] registers = new int[10];
    private Stack<Integer> stack = new Stack<>();
    private Stack<Integer> callStack = new Stack<>();
    private int[] program;
    private int ip = 0; // Instruction pointer

    public ExVM(int[] program) {
        this.program = program;
    }

    public void run() {
        while (ip < program.length) {
            int opcode = program[ip++];
            switch (opcode) {
                case 1: // PUSH
                    stack.push(program[ip++]);
                    break;
                case 2: // LOAD
                    stack.push(registers[program[ip++]]);
                    break;
                case 3: // STORE
                    registers[program[ip++]] = stack.pop();
                    break;
                case 4: // ADD
                    stack.push(stack.pop() + stack.pop());
                    break;
                case 5: // SUB
                    stack.push(stack.pop() - stack.pop());
                    break;
                case 6: // CALL
                    callStack.push(ip + 1);
                    ip = program[ip];
                    break;
                case 7: // RET
                    ip = callStack.pop();
                    break;
                case 8: // JZ
                    int addr1 = program[ip++];
                    if (stack.pop() == 0) {
                        ip = addr1;
                    }
                    break;
                case 9: // JNZ
                    int addr2 = program[ip++];
                    if (stack.pop() != 0) {
                        ip = addr2;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown opcode: " + opcode);
            }
        }
    }

    public int getResult() {
        return stack.pop();
    }

    public static void main(String[] args) {
        // Fibonacci bytecode program (computes Fib(5))
        int[] program = {
                1, 5,       // PUSH 5
                3, 0,       // STORE 0 (n)
                2, 0,       // LOAD 0
                1, 1,       // PUSH 1
                5,          // SUB
                9, 14,      // JNZ L1
                1, 1,       // PUSH 1
                7,          // RET
                2, 0,       // L1: LOAD 0
                1, 1,       // PUSH 1
                5,          // SUB
                6, 0,       // CALL Fib
                2, 0,       // LOAD 0
                1, 2,       // PUSH 2
                5,          // SUB
                6, 0,       // CALL Fib
                4,          // ADD
                7           // RET
        };

        ExVM vm = new ExVM(program);
        vm.run();
        System.out.println("Fib(5) = " + vm.getResult()); // Output should be Fib(5) = 5
    }
}
