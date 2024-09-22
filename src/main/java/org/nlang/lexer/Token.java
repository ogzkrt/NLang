package org.nlang.lexer;

public class Token {

    public enum TokenType {
        LPAREN, RPAREN, SEMICOLON, ASSIGN,
        LBRACE, RBRACE,
        PLUS, MINUS, MULTIPLY, DIVIDE,
        NUMBER, IDENTIFIER, STRING,
        MAKE, PRINT,
        GREATER, SMALLER,
        COMMA, FUNC, RETURN,
        IN, DOT_DOT,
        DOT, COLUMN, CLASS,
        IF, ELSE, FOR, WHILE, OUT, LEFT_BRACKET, RIGHT_BRACKET, EOF
    }

    public final TokenType type;
    public final String value;
    public final int line;
    public final int start;
    public final int end;

    public Token(final TokenType type, final String value, final int line, final int start, final int end) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}