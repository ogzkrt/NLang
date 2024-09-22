package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.err.Err;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.ASTNode.*;
import org.nlang.parser.Parser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class FunctionTests {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void run(String code) {
        Err.lines = code.split("\n");
        Environment env = new Environment();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> statements = parser.parse();
        for (ASTNode stmt : statements) {
            if (stmt instanceof FunctionDefinitionNode) {
                env.addFunction((FunctionDefinitionNode) stmt);
                continue;
            }
            stmt.evaluate(env);
        }
    }


    @Test
    void test_functionShouldWorkFineForBasicCase() {
        String test = """
                func add(x,y){
                    return x+y;
                }
                make result = add(2,3);
                print(result);
                """;
       
        run(test);
        assertEquals("5.0"+System.lineSeparator(),outputStream.toString());
    }

    @Test
    void test_SumOfFunctionShouldJustReturnResult() {
        String test = """
                func add(x,y){
                    return x+y;
                }
                make result = add(2,3) + add(5,7);
                print(result);
                """;

        run(test);
        assertEquals("17.0"+System.lineSeparator(),outputStream.toString());
    }

    @Test
    void test_FunctionCanReturnIterable() {
        String test = """
                func square(nums){
                    make result = [];
                    for 0..len(nums){
                        result.add(nums[i]*nums[i]);
                    }
                    return result;
                }
                make param = [1,2,3];
                make result = square(param);
                print(result);
                """;

        run(test);
        assertEquals("[1.0, 4.0, 9.0]"+System.lineSeparator(),outputStream.toString());
    }

    @Test
    void test_FunctionCanReturnIterableCanTakeBracketAsParameter() {
        String test = """
                func square_in_reverse_order(nums){
                    make result = [];
                    for 0..len(nums){
                        result.add(nums[i]*nums[i]);
                    }
                    return result.reverse();
                }
                make param = [1,2,3];
                make result = square_in_reverse_order([1,2,3]);
                print(result);
                """;

        run(test);
        assertEquals("[9.0, 4.0, 1.0]"+System.lineSeparator(),outputStream.toString());
    }

    @Test
    void test_RecursiveFunctionWorks() {
        String test = """
                func fib(n){
                    if(n<2){
                        return n;
                    }
                    return fib(n-1) + fib(n-2);
                }
                make result = fib(20);
                print(result);
                """;

        run(test);
        assertEquals("6765.0"+System.lineSeparator(),outputStream.toString());
    }

    @Test
    void test_IterativeFibWorks() {
        String test = """
                func fib(n){
                    make result = [0,1];
                    for 2..n+1{
                        result.add(result[i-1]+result[i-2]);
                    }
                    return result.last();
                }
                make result = fib(20);
                print(result);
                """;

        run(test);
        assertEquals("6765.0"+System.lineSeparator(),outputStream.toString());
    }
    
}