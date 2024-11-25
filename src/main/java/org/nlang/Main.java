package org.nlang;

import org.nlang.err.Err;
import org.nlang.lexer.Lexer;
import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.ASTNode;
import org.nlang.parser.FunctionDefinitionNode;
import org.nlang.parser.Parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static Environment env;

    public static void main(String[] args) {

        env = new Environment();
        if (args.length == 0) {
            interactiveMode();
        } else if (args.length == 1) {
            String filename = args[0];
            fileMode(filename);
        } else {
            System.out.println("Usage: java -jar NLang [filename.o]");
        }
    }

    private static void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("NLang Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner
                    .nextLine()
                    .trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            if (input.isEmpty()) {
                continue;
            }

            processInput(input, env);
        }

        scanner.close();
    }

    private static void fileMode(String filename) {

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            processInput(new String(bytes, Charset.defaultCharset()), env);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static void processInput(String input, Environment env) {
        try {

            Err.lines = input.split("\n");
            Lexer lexer = new Lexer(input.stripTrailing());
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            List<ASTNode> statements = parser.parse();
            for (ASTNode stmt : statements) {
                if (stmt instanceof FunctionDefinitionNode node) {
                    env.addFunction(node);
                    continue;
                }
                stmt.evaluate(env);

            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}