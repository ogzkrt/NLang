package org.nlang.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nlang.err.Err;
import org.nlang.lexer.Token.TokenType;

public class Lexer {
    private final String source;
    private final String[] lines;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int columnStart;
    private int columnEnd;

    private final Map<String, TokenType> keywords = new HashMap<>();

    {
        keywords.put("make", TokenType.MAKE);
        keywords.put("print", TokenType.PRINT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("func", TokenType.FUNC);
        keywords.put("return", TokenType.RETURN);
        keywords.put("in", TokenType.IN);
        keywords.put("out", TokenType.OUT);
    }


    public Lexer(String source) {
        this.source = source;
        this.lines = source.split("\n");
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            columnStart = columnEnd;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, start, current));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;
            case '{':
                addToken(TokenType.LBRACE);
                break;
            case '}':
                addToken(TokenType.RBRACE);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '=':
                addToken(TokenType.ASSIGN);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.MULTIPLY);
                break;
            case '/':
                if(peek()=='/'){
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }else{
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '>':
                addToken(TokenType.GREATER);
                break;
            case '<':
                addToken(TokenType.SMALLER);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '[':
                addToken(TokenType.LEFT_BRACKET);
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ':':
                addToken(TokenType.COLUMN);
                break;
            case '"':
                string();
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                columnEnd = 0;
                line += 1;
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw Err.err("Unexpected character: " + c, line, columnEnd);
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            advance();
        }
        if (isAtEnd()) {
            throw Err.err("Unterminated string.", line, columnEnd);
        }
        advance(); // The closing ".
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();
        addToken(TokenType.NUMBER, source.substring(start, current));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        columnEnd++;
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(final TokenType type) {
        addToken(type, source.substring(start, current));
    }

    private void addToken(final TokenType type, final String literal) {
        tokens.add(new Token(type, literal, line, columnStart, columnEnd));
    }

}
