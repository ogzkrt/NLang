package org.nlang.err;

import org.nlang.lexer.Token;

public class Err {
    
    public static String[] lines;

    public static RuntimeException err(String message, Token token) {
        System.err.println(lines[token.line - 1]);
        System.err.println(" ".repeat(token.end-1) + "^");
        return new RuntimeException(String.format("%s at line %s:%s",
                message, token.line, token.start + 1));
    }

    public static RuntimeException err(final String message,final int line,
                                final int columnEnd) {
        System.err.println(lines[line-1]);
        System.err.println(" ".repeat(columnEnd-1) + "^");
        return new RuntimeException(String.format("%s at line %s:%s",
                message, line, columnEnd));
    }
}
