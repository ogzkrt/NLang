package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.err.Err;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.NLangFunction;
import org.nlang.parser.Parser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class ForLoopTests {

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
            if (stmt instanceof NLangFunction) {
                env.addFunction((NLangFunction) stmt);
                continue;
            }
            stmt.evaluate(env);
        }
    }

    @Test
    void test_shouldFail_When_VariableIsNotIterable() {
        String test = """
                make a = 2;
                for x in a {
                    print(x);
                }
                """;
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("variable a is not iterable. at line 2:10", e.getMessage());
            assertEquals("""
                            for x in a {
                                     ^
                            """,
                    errStream.toString());
        }
    }
    @Test
    void test_ForLoopWithRangeEqualsWorks() {
        String test = """
                for 1..=3 {
                    print(i);
                }
                """;
        
        run(test);
        String expected = """
                1.0
                2.0
                3.0
                """;
        assertEquals(expected,outputStream.toString());
        
    }
    @Test
    void test_ForLoopWithRangeLessThanWorks() {
        String test = """
                for 1..3 {
                    print(i);
                }
                """;

        run(test);
        String expected = """
                1.0
                2.0
                """;
        assertEquals(expected,outputStream.toString());

    }

    @Test
    void test_ForInLoop() {
        String test = """
                make a = ["Omer","Ali","Osman"];
                for x in a {
                    print(x);
                }
                """;

        run(test);
        String expected = """
                Omer
                Ali
                Osman
                """;
        assertEquals(expected,outputStream.toString());

    }

    @Test
    void test_ForInLoopWithDefaultIndex() {
        String test = """
                make a = ["Omer","Ali","Osman"];
                for x in a {
                    print(a[i]);
                }
                """;

        run(test);
        String expected = """
                Omer
                Ali
                Osman
                """;
        assertEquals(expected,outputStream.toString());

    }

    @Test
    void test_ForInLoopWithSpecialIndex() {
        String test = """
                make a = ["Omer","Ali","Osman"];
                for x in a : special {
                    print(a[special]);
                }
                """;

        run(test);
        String expected = """
                Omer
                Ali
                Osman
                """;
        assertEquals(expected,outputStream.toString());

    }

    @Test
    void test_matrixWithNestedLoop() {
        String test = """
                make a = [1,2,3];
                make b = ["a","b","c"];
                
                for 0..len(a) : x {
                    for x..len(b) : y{
                        print(a[x],b[y]);
                    }
                }
                """;

        run(test);
        String expected = """
                1.0a
                1.0b
                1.0c
                2.0b
                2.0c
                3.0c
                """;
        assertEquals(expected,outputStream.toString());

    }
}