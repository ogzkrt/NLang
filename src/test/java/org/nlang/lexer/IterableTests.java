package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.Main;

import org.nlang.parser.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class IterableTests {

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
                            make a = 2;
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
        assertEquals("[3, 2, 1]" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_shouldAdd_When_AddIsCalled() {
        String test = """
                make a = [1,2,3];
                a.add(12);
                print(a);
                """;

        run(test);
        assertEquals("[1, 2, 3, 12]" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_shouldRemove_When_RemoveIsCalled() {
        String test = """
                make a = [1,2,3];
                a.remove(2);
                print(a);
                """;

        run(test);
        assertEquals("[1, 3]" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_shouldReturnLastItem() {
        String test = """
                make a = [1,2,3];
                print(a.last());
                """;

        run(test);
        assertEquals("3" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_shouldReturnFirstItem() {
        String test = """
                make a = [1,2,3];
                print(a.first());
                """;

        run(test);
        assertEquals("1" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_AssignmentViaIndexShouldWork() {
        String test = """
                make a = [1,2,3];
                a[2] = 12;
                print(a);
                """;

        run(test);
        assertEquals("[1, 2, 12]" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_DefaultIterableFunctionsShouldBeChainedFine() {
        String test = """
                make a = [1,2,3];
                make b = a.reverse().reverse().reverse();
                print(b);
                """;

        run(test);
        assertEquals("[3, 2, 1]" + System.lineSeparator(), outputStream.toString());

    }

    @Test
    void test_ObjectsCanBePartOfIterable() {
        String test = """
                make one = { "name":"Ali" , "job":"cifti"};
                make two = { "name":"Veli", "job":"tuccar" };
                make three = { "name":"Konya", "job":"donerci"};
                make people = [one,two,three];
                for p in people{
                    print(p);
                }
                """;

        run(test);
        assertEquals(sanitize("""
                {
                	name : Ali,
                	job : cifti,
                }
                
                {
                	name : Veli,
                	job : tuccar,
                }
                
                {
                	name : Konya,
                	job : donerci,
                }
                
                """), sanitize(outputStream.toString()));

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
        assertEquals(sanitize("""
                2
                5
                8
                """), sanitize(outputStream.toString()));

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
        assertEquals(sanitize("""
                5
                5
                5
                """), sanitize(outputStream.toString()));

    }
    @Test
    void test_SettingElementInEmptyArrayFillValuesWithNull() {
        String test = """
                make a = [];
                a[4] = 101;
                print(a);
                """;

        run(test);
        assertEquals(sanitize("""
                [null, null, null, null, 101]
                """), sanitize(outputStream.toString()));

    }

    @Test
    void test_String_shouldRemove_When_RemoveIsCalled() {
        String test = """
                make a = "123";
                a.remove("2");
                print(a);
                """;

        run(test);
        assertEquals("13" + System.lineSeparator(), outputStream.toString());

    }


    public static String sanitize(String s){
        return s.replace("\r\n", "\n").replace("\r", "\n");
    }

}