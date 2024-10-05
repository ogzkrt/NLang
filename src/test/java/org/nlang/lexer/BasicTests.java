package org.nlang.lexer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nlang.Main;
import org.nlang.parser.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.nlang.lexer.IterableTests.sanitize;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


class BasicTests {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private Environment environment;

    @BeforeEach
    public void setUp() {
        environment = new Environment();
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errStream));
    }

    @AfterEach
    public void tearDown() {
        System.setErr(originalErr);
        System.setOut(originalOut);
    }

    private void run(String code) {
        Main.processInput(code, environment);
    }

    @Test
    void test1() {

        String test = """
                make a = 10;
                make b = 12;
                print(a+b);
                """;
        run(test);
        assertEquals("22" + System.lineSeparator(), outputStream.toString());
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
                4
                16
                36
                64
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

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
                index: 0 value: 4
                index: 1 value: 16
                index: 2 value: 36
                index: 3 value: 64
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

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
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

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
                3
                7.5
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

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
                Element at index: 1 is Veli
                Element at index: 2 is 3
                Element at index: 3 is 7.5
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

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
                10
                """;
        assertEquals(sanitize(expected), sanitize(outputStream.toString()));

    }

    @Test
    void testStringEqualsWorks() {

        String test = """
                make s1 = "test";
                make s2 = "test";
                make s3 = "test2";
                assert s1==s2;
                assert s2!=s3;
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());

    }

    @Test
    void testNumberEqualsWorks() {

        String test = """
                make n1 = 100001;
                make n2 = 100001;
                make n3 = 111111;
                assert n1==n2, "should be equal";
                assert n2!=n3, "not equal";
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());

    }

    @Test
    void testUnaryWorks() {

        String test = """
                assert -1/2+5*3==14.5 , "14.5";
                assert --2 == 2, "2";
                assert --3/2*5+(6*5) == 37.5, "37.5";
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());

    }

    @Test
    void testUnaryForBooleanWorks() {

        String test = """
                assert !(1==1) == false, "should be false";
                assert !(5==3) == true, "should be true";
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());
    }

    @Test
    void testBooleanWorks() {

        String test = """
                make a = !(5==3);
                assert a, "a should be true";
                make b = false;
                assert a!=b , "a and b is not equal";
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());
    }

    @Test
    void testLogicalAndWorks() {

        String test = """
                make a = 10;
                make b = 50;
                make c = false;
                make d = true;
                assert a<20 and b>20 ;
                assert c or d ;
                """;
        run(test);
        assertTrue(errStream.toString().isEmpty());
    }


}