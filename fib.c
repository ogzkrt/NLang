#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>

int fibonacci(int n) {
    if (n < 2) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

int main() {

    struct timespec start, end;

 
    clock_gettime(CLOCK_MONOTONIC, &start);
    int result = fibonacci(35);
    printf("Result %d \n",result);
    clock_gettime(CLOCK_MONOTONIC, &end);
    
    long total_time = (end.tv_sec - start.tv_sec) * 1000 + (end.tv_nsec - start.tv_nsec) / 1000000;
    printf("Time passed (ms): %ld\n", total_time);
    
    return 0;
}