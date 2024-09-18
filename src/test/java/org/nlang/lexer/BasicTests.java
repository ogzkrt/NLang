package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.err.Err;
import org.nlang.parser.astnodes.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.NLangFunction;
import org.nlang.parser.Parser;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class BasicTests {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    private void run(String code){
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
    void test1() {

        String test = """
                make a = 10;
                make b = 12;
                print(a+b);
                """;
        run(test);
        assertEquals("22.0" + System.lineSeparator(), outputStream.toString());
    }
    @Test
    void test2() {

        String test = """
                make a = "string1";
                make b = "string2";
                print(a,b);
                """;
        run(test);
        assertEquals("string1string2" + System.lineSeparator(), outputStream.toString());
    }
    
    @Test
    void test3() {

        String test = """
                make a = [2,4,6,8];
                for x in a{
                    print(x*x);
                }
                """;
        run(test);
        String expected = """
                4.0
                16.0
                36.0
                64.0
                """;
        assertEquals(expected, outputStream.toString());
        
    }

    @Test
    void test4() {

        String test = """
                make a = [2,4,6,8];
                for x in a{
                    print("index: ",i," value: ",x*x);
                }
                """;
        run(test);
        String expected = """
                index: 0.0 value: 4.0
                index: 1.0 value: 16.0
                index: 2.0 value: 36.0
                index: 3.0 value: 64.0
                """;
        assertEquals(expected, outputStream.toString());

    }

    @Test
    void test5() {

        String test = """
                make names = ["Ahmet","Mehmet","Osman"];
                for n in names{
                    print(n);
                }
                print(names[0]);
                print(names[1]);
                print(names[2]);
                """;
        run(test);
        String expected = """
                Ahmet
                Mehmet
                Osman
                Ahmet
                Mehmet
                Osman
                """;
        assertEquals(expected, outputStream.toString());

    }

    @Test
    void test6() {

        String test = """
                make elements = ["Ahmet","Veli",12/4,(3*5)/2];
                for e in elements{
                    print(e);
                }
                """;
        run(test);
        String expected = """
                Ahmet
                Veli
                3.0
                7.5
                """;
        assertEquals(expected, outputStream.toString());

    }

    @Test
    void test7() {

        String test = """
                make elements = ["Ahmet","Veli",12/4,(3*5)/2];
                for 1..4 {
                  print("Element at index: ",i," is ", elements[i]);
                }
                """;
        run(test);
        String expected = """
                Element at index: 1.0 is Veli
                Element at index: 2.0 is 3.0
                Element at index: 3.0 is 7.5
                """;
        assertEquals(expected, outputStream.toString());

    }

    @Test
    void testCommentWorking() {

        String test = """
                make a = 10;
                //a = 12;
                print(a);
                """;
        run(test);
        String expected = """
                10.0
                """;
        assertEquals(expected, outputStream.toString());

    }
    

}