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

class IterableTests {

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
                a.reverse();
                """;
        try {
            run(test);
        } catch (RuntimeException e) {
            assertEquals("Can not make array operation 'reverse' at line 2:1", e.getMessage());
            assertEquals("""
                            a.reverse();
                            ^
                            """,
                    errStream.toString());
        }

    }
    @Test
    void test_shouldReverse_When_ReverseCalled() {
        String test = """
                make a = [1,2,3];
                make b = a.reverse();
                print(b);
                """;
       
        run(test);
        assertEquals("[3.0, 2.0, 1.0]"+System.lineSeparator(),outputStream.toString());
        
    }

    @Test
    void test_shouldAdd_When_AddIsCalled() {
        String test = """
                make a = [1,2,3];
                a.add(12);
                print(a);
                """;

        run(test);
        assertEquals("[1.0, 2.0, 3.0, 12.0]"+System.lineSeparator(),outputStream.toString());

    }

    @Test
    void test_shouldRemove_When_RemoveIsCalled() {
        String test = """
                make a = [1,2,3];
                a.remove(2);
                print(a);
                """;

        run(test);
        assertEquals("[1.0, 3.0]"+System.lineSeparator(),outputStream.toString());

    }

    @Test
    void test_shouldReturnLastItem() {
        String test = """
                make a = [1,2,3];
                print(a.last());
                """;

        run(test);
        assertEquals("3.0"+System.lineSeparator(),outputStream.toString());

    }

    @Test
    void test_AssignmentViaIndexShouldWork() {
        String test = """
                make a = [1,2,3];
                a[2] = 12;
                print(a);
                """;

        run(test);
        assertEquals("[1.0, 2.0, 12.0]"+System.lineSeparator(),outputStream.toString());

    }
   
    @Test
    void test_DefaultIterableFunctionsShouldBeChainedFine() {
        String test = """
                make a = [1,2,3];
                make b = a.reverse().reverse().reverse();
                print(b);
                """;

        run(test);
        assertEquals("[3.0, 2.0, 1.0]"+System.lineSeparator(),outputStream.toString());

    }

    @Test
    void test_NestedArrayWorks() {
        String test = """
                make a = [[1,2,3],[4,5,6],[7,8,9]];
                for x in a {
                    print(a[i][1]);
                }
                """;

        run(test);
        assertEquals("""
                2.0
                5.0
                8.0
                """,outputStream.toString());

    }

    @Test
    void test_SettingElementInNestedArrayWorks() {
        String test = """
                make a = [[1,2,3],[4,5,6],[7,8,9]];
                for x in a {
                    a[i][1]=5;
                }
                for x in a {
                    print(a[i][1]);
                }
                """;

        run(test);
        assertEquals("""
                5.0
                5.0
                5.0
                """,outputStream.toString());

    }
}