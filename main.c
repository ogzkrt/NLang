#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>

#define DEFAULT_STACK_SIZE 1000
#define FALSE 0
#define TRUE 1

// Opcodes
typedef enum {
    HALT, IADD, ISUB, IMUL, ILT, IEQ, BR, BRT, BRF, ICONST, LOAD, GLOAD,
    STORE, GSTORE, PRINT, POP, CALL, RET
} ByteCode;

typedef struct {
    const int* code;
    int* stack;
    int* globals;
    int ip;
    int sp;
    int fp;
} VM;

// Inline function to create a VM instance
static inline VM* createVM(const int* code, int startip, int nglobals) {
    VM* vm = (VM*)malloc(sizeof(VM));
    vm->code = code;
    vm->ip = startip;
    vm->sp = -1;
    vm->fp = -1;
    vm->stack = (int*)malloc(DEFAULT_STACK_SIZE * sizeof(int));
    vm->globals = (int*)calloc(nglobals, sizeof(int));
    return vm;
}

// Inline function to free VM instance
static inline void freeVM(VM* vm) {
    free(vm->stack);
    free(vm->globals);
    free(vm);
}

void cpu(VM* vm) {
    register const int* code = vm->code;
    register int* stack = vm->stack;
    register int* globals = vm->globals;
    register int ip = vm->ip;
    register int sp = vm->sp;
    register int fp = vm->fp;

    int opcode;
    while (1) {
        opcode = code[ip++];
        switch (opcode) {
            case HALT:
                goto end_execution;
            case IADD:
                stack[sp - 1] += stack[sp];
                sp--;
                break;
            case ISUB:
                stack[sp - 1] -= stack[sp];
                sp--;
                break;
            case IMUL:
                stack[sp - 1] *= stack[sp];
                sp--;
                break;
            case ILT:
                stack[sp - 1] = (stack[sp - 1] < stack[sp]) ? TRUE : FALSE;
                sp--;
                break;
            case IEQ:
                stack[sp - 1] = (stack[sp - 1] == stack[sp]) ? TRUE : FALSE;
                sp--;
                break;
            case BR:
                ip = code[ip];
                break;
            case BRT:
                ip = (stack[sp--] == TRUE) ? code[ip] : ip + 1;
                break;
            case BRF:
                ip = (stack[sp--] == FALSE) ? code[ip] : ip + 1;
                break;
            case ICONST:
                stack[++sp] = code[ip++];
                break;
            case LOAD:
                stack[++sp] = stack[fp + code[ip++]];
                break;
            case GLOAD:
                stack[++sp] = globals[code[ip++]];
                break;
            case STORE:
                stack[fp + code[ip++]] = stack[sp--];
                break;
            case GSTORE:
                globals[code[ip++]] = stack[sp--];
                break;
            case PRINT:
                printf("%d\n", stack[sp--]);
                break;
            case POP:
                sp--;
                break;
            case CALL: {
                int addr = code[ip++];
                int nargs = code[ip++];
                stack[++sp] = nargs;
                stack[++sp] = fp;
                stack[++sp] = ip;
                fp = sp;
                ip = addr;
                break;
            }
            case RET: {
                int rvalue = stack[sp--];
                sp = fp;
                ip = stack[sp--];
                fp = stack[sp--];
                int nargs = stack[sp--];
                sp -= nargs;
                stack[++sp] = rvalue;
                break;
            }
            default:
                fprintf(stderr, "Error: Invalid opcode %d at ip=%d\n", opcode, ip - 1);
                exit(1);
        }
    }

end_execution:
    // Write back registers to VM state
    vm->ip = ip;
    vm->sp = sp;
    vm->fp = fp;
}

int main() {
    // Example usage
    const int code[] = {
        LOAD, -3,     // 0000
        ICONST, 2,    // 0002
        ILT,          // 0004
        BRF, 10,      // 0005
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
    
    struct timespec start, end;

    VM* vm = createVM(code, 28, 10);
    clock_gettime(CLOCK_MONOTONIC, &start);
    cpu(vm);
    clock_gettime(CLOCK_MONOTONIC, &end);
    
    long total_time = (end.tv_sec - start.tv_sec) * 1000 + (end.tv_nsec - start.tv_nsec) / 1000000;
    printf("Time passed (ms): %ld\n", total_time);
    
    freeVM(vm);
    
    return 0;
}