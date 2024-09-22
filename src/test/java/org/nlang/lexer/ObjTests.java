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

class ObjTests {

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
    void test_ObjEmpty() {
        String test = """
                make obj = { };
                print(obj);
                """;

        run(test);
        assertEquals("""
                {}
                
                """,outputStream.toString());
    }


    @Test
    void test_ObjDotKeyReturnValue() {
        String test = """
                make obj = { "key1":"value1"};
                print(obj.key1);
                """;

        run(test);
        assertEquals("""
                value1
                """,outputStream.toString());
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
        assertEquals("""
                { innerKey : innerValue }
                
                """,outputStream.toString());
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
        assertEquals("""
                val2
                Veli
                Veli
                Veli
                Veli
                {
                	last : Veli,
                	first : Ali,
                }
                
                """,outputStream.toString());
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
        }catch (RuntimeException exception){
            assertEquals("Error: Key doesn't exist at line 6:11",exception.getMessage());
            assertEquals("""
                    };
                    print(obj.inner1);
                                   ^
                    """,errStream.toString());
        }
    }
    
    
    
}