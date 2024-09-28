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
import static org.nlang.lexer.IterableTests.sanitize;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class ObjTests {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private Environment environment;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errStream));
        environment = new Environment();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void run(String code) {
        Main.processInput(code, environment);
    }

    @Test
    void test_ObjEmpty() {
        String test = """
                make obj = { };
                print(obj);
                """;

        run(test);
        String expected = "{}".trim();
        assertEquals(sanitize(expected), sanitize(outputStream.toString().trim()));
    }


    @Test
    void test_ObjDotKeyReturnValue() {
        String test = """
                make obj = { "key1":"value1"};
                print(obj.key1);
                """;

        run(test);
        String expected = """
                value1
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));
    }

    @Test
    void test_ObjInsideObjectShouldBeAccessible() {
        String test = """
                make obj = 
                { 
                "key1":"value1",
                "inner": { "innerKey":"innerValue"} 
                };
                print(obj.inner);
                """;

        run(test);
        String expected = """
                { innerKey : innerValue }
                                
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));
    }

    @Test
    void test_ValuesShouldBeAccessibleLikeIndexOperation() {
        String test = """
                make obj = {
                "key1":"val1",
                "key2":"val2",
                "inner": { "last":"Veli", "first":"Ali" }
                };
                                
                print(obj["key2"]);
                print(obj.inner.last);
                print(obj["inner"]["last"]);
                print(obj["inner"].last);
                print(obj.inner["last"]);
                print(obj.inner);
                """;

        run(test);
        String expected = """
                val2
                Veli
                Veli
                Veli
                Veli
                {
                	last : Veli,
                	first : Ali,
                }
                                
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));
    }

    @Test
    void test_InvalidKey() {
        String test = """
                make obj = 
                { 
                "key1":"value1",
                "inner": { "innerKey":"innerValue"} 
                };
                print(obj.inner1);
                """;
        try {
            run(test);
        } catch (RuntimeException exception) {
            assertEquals("Error: Key doesn't exist at line 6:11", exception.getMessage());
            assertEquals("""
                    };
                    print(obj.inner1);
                                   ^
                    """, errStream.toString());
        }
    }


}