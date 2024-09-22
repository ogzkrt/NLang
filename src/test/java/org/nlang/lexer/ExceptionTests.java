package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.Main;
import org.nlang.err.Err;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.ASTNode.*;
import org.nlang.parser.FunctionDefinitionNode;
import org.nlang.parser.Parser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class ExceptionTests {

    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    private Environment environment;

    @BeforeEach
    public void setUp() {
        environment = new Environment();
        System.setErr(new PrintStream(errStream));
    }

    @AfterEach
    public void tearDown() {
        System.setErr(originalErr);
    }

    private void run(String code) {
        Main.processInput(code, environment);
    }

    @Test
    void test1() {

        String test = """
                make a = 10
                make b = 12;
                print(a+b);
                """;
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("Expected ';' at line 1:10", e.getMessage());
            assertEquals("""
                    make a = 10
                              ^
                    make b = 12;
                    """, errStream.toString());
        }
    }

    @Test
    void test2() {

        String test = """
                a = 12;
                """;
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("Undefined variable: a at line 1:1", e.getMessage());
            assertEquals("""
                    a = 12;
                    ^
                    """, errStream.toString());
        }
    }

    @Test
    void test3() {

        String test = """
                make numbers = [1,2,3,4,5;
                """;
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("Expecting ] for array at line 1:25", e.getMessage());
            assertEquals("""
                    make numbers = [1,2,3,4,5;
                                            ^
                    """, errStream.toString());
        }
    }

    @Test
    void test4() {

        String test = """
                make name = "java;""";
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("Unterminated string. at line 1:18", e.getMessage());
            String actual = errStream.toString();
            String expected = """
                    make name = "java;
                                     ^
                    """;
            assertEquals(expected, actual);
        }
    }
}