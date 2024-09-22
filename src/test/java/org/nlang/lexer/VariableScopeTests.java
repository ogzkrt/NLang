package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.Main;
import org.nlang.err.Err;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.FunctionDefinitionNode;
import org.nlang.parser.Parser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class VariableScopeTests {

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
    void test_ScopeTestBasic() {
        String test = """
                make a = 2;
                print(a);
                {
                    make a = 12;
                    print(a);
                }
                print(a);
                """;

        run(test);
        assertEquals("""
                2.0
                12.0
                2.0
                """, outputStream.toString());
    }

    @Test
    void test_GlobalVariableCanAssignedInsightScope() {
        String test = """
                make a = 2;
                print(a);
                {
                    a = 12;
                    print(a);
                }
                print(a);
                """;

        run(test);
        assertEquals("""
                2.0
                12.0
                12.0
                """, outputStream.toString());
    }

    @Test
    void test_UndefinedVarCanNotBeAssigned() {
        String test = """
                make a = 2;
                {
                    b = 12;
                }
                """;
        try {
            run(test);
        } catch (RuntimeException exception) {
            assertEquals("""
                    {
                        b = 12;
                        ^
                    }
                    """, errStream.toString());
            assertEquals("Undefined variable: b at line 3:5", exception.getMessage());
        }

    }

}
