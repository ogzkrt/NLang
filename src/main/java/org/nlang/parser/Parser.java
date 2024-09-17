package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.lexer.Token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode> parse() {
        return parseStatements();
    }

    private List<ASTNode> parseStatements() {
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return statements;
    }

    private ASTNode parseStatement() {
        if (match(TokenType.FUNC)) return parseFuncStatement();
        if (match(TokenType.MAKE)) return parseVarDeclaration();
        if (match(TokenType.IF)) return parseIfStatement();
        if (match(TokenType.FOR)) return parseForSecondVersion();
        if (match(TokenType.PRINT)) return parsePrint();
        if (match(TokenType.RETURN)) return parseReturnStatement();
        if (match(TokenType.LBRACE)) return parseBlockV2();
        return parseExpressionStatement();
    }

    private ASTNode parseArrayExpression(Token name) {
        consume(TokenType.LEFT_BRACKET, "Expecting [");
        List<ASTNode> elements = new ArrayList<>();
        if (!check(TokenType.RIGHT_BRACKET)) {
            elements.add(parseExpression());
            while (match(TokenType.COMMA)) {
                elements.add(parseExpression());
            }
            consume(TokenType.RIGHT_BRACKET, "Expecting ] for array");
        } else {
            consume(TokenType.RIGHT_BRACKET, "Expecting ] for array");
        }
        return new NLangArray(name, elements);
    }

    private ASTNode parseForSecondVersion() {
        ASTNode startNode;
        Token startVarName;
        Token indexVarName = new Token(TokenType.IDENTIFIER, "i", 0, 0, 0);

        if (check(TokenType.NUMBER)) {
            Token start = consume(TokenType.NUMBER, "error while parsing for second-version");
            startNode = new Number(Double.parseDouble(start.value));
            consume(TokenType.DOT, "expecting . after identifier");
            consume(TokenType.DOT, "expecting ..");
            ASTNode end = parseExpression();
            ASTNode block = parseBlock();
            return new ForNodeExperimental(startNode, end, block);
        }
        startVarName = consume(TokenType.IDENTIFIER, "expecting identifier like 'i'");
        if (check(TokenType.COMMA)) {
            consume(TokenType.COMMA, "expecting ,");
            Token second = consume(TokenType.IDENTIFIER, "expecting identifier like 'i'");
            consume(TokenType.IN, "for a in array:");
            Token iterableToken = peek();
            ASTNode iterable = parseExpression();
            iterable.token = Optional.of(iterableToken);
            ASTNode body = parseBlock();
            return new ForInLoop(iterable, body, second, startVarName);
        } else {
            consume(TokenType.IN, "for a in array:");
            Token iterableToken = peek();
            ASTNode iterable = parseExpression();
            iterable.token = Optional.of(iterableToken);
            ASTNode body = parseBlock();
            return new ForInLoop(iterable, body, startVarName, indexVarName);
        }

    }

    private ASTNode parseBlockV2() {
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        consume(TokenType.RBRACE, "Expecting }");
        return new Block(statements);
    }

    private Block parseBlock() {
        consume(TokenType.LBRACE, "Expecting {");
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        consume(TokenType.RBRACE, "Expecting }");
        return new Block(statements);
    }

    private ASTNode parseReturnStatement() {
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new Return(value);
    }

    private ASTNode parseIfStatement() {
        consume(TokenType.LPAREN, "Expecting ( after if");
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN, "Expecting )");
        return new If(condition, parseBlock());

    }

    private ASTNode parseFuncStatement() {
        Token name = consume(TokenType.IDENTIFIER, "function  should have a name");
        consume(TokenType.LPAREN, "Expecting (");
        List<Token> parameters = parseParameters();
        Block astNode = parseBlock();
        return new NLangFunction(name, parameters, astNode.getExpressions());
    }

    private List<Token> parseParameters() {
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            parameters.add(consume(TokenType.IDENTIFIER, "error while reading func parameter"));
            while (match(TokenType.COMMA)) {
                parameters.add(consume(TokenType.IDENTIFIER, "error while reading func parameter"));
            }
            consume(TokenType.RPAREN, "Expecting ) for function def");
        } else {
            consume(TokenType.RPAREN, "Expecting ) for function def");
        }
        return parameters;
    }

    private ASTNode parseVarDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        consume(TokenType.ASSIGN, "Expected '=' after variable name.");
        ASTNode value;
        value = parseExpression();
        consume(TokenType.SEMICOLON,
                "Expected ';'");
        return new VarDeclaration(name, value);
    }

    private ASTNode parsePrint() {
        consume(TokenType.LPAREN, "Expected '(' after print statement");
        List<ASTNode> expressions = new ArrayList<>();
        expressions.add(parseExpression());
        while (check(TokenType.COMMA)) {
            consume(TokenType.COMMA, "expected , between parameters");
            expressions.add(parseExpression());
        }
        consume(TokenType.RPAREN, "Expected ')' after print statement");
        consume(TokenType.SEMICOLON, "Expected ';' after print statement");
        return new Print(expressions);
    }

    private ASTNode parseExpressionStatement() {
        ASTNode expr = parseAssignStatement();
        consume(TokenType.SEMICOLON, "; after expression");
        return expr;
    }

    private ASTNode parseAssignStatement() {
        ASTNode expr = parseExpression();
        if (expr instanceof ArrayOperations) {
            // a.add(1);
            // here we should return before expecting =
            return expr;
        }
        consume(TokenType.ASSIGN, "This is not statement = expected");
        ASTNode value = parseExpression();
        if (expr instanceof ArrayIndexAccess v) {
            return new ArrayIndexSet(v.token, v.index, value);
        }
        if (!(expr instanceof Variable v)) {
            throw Err.err("can only assign to the variable", previous());
        }
        return new Assignment(v.token, value);
    }

    private ASTNode parseExpression() {
        ASTNode expr = parseAddition();
        while (match(TokenType.GREATER, TokenType.SMALLER)) {
            Token operator = previous();
            ASTNode right = parseAddition();
            expr = new Binary(expr, right, operator);
        }
        return expr;
    }


    private ASTNode parseAddition() {
        ASTNode expr = parseFactor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ASTNode right = parseFactor();
            expr = new Binary(expr, right, operator);
        }
        return expr;
    }

    private ASTNode parseFactor() {
        ASTNode expr = parseMemberCall();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            Token operator = previous();
            ASTNode right = parseMemberCall();
            expr = new Binary(expr, right, operator);
        }
        return expr;
    }
    
    private ASTNode parseCall(){
        ASTNode expr = parsePrimary();
        Token name = previous();
        if(match(TokenType.LPAREN)){
            return parseFunctionCall(name,null);
        }
        if(match(TokenType.LEFT_BRACKET)){
            return parseIndexAccess(name);
        }
        return expr;
    }

    private ASTNode parseFunctionCall(Token token, ASTNode callee) {
        List<ASTNode> arguments = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(parseExpression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after arguments.");
        return new FunctionCall(callee,token, arguments);
    }

    private ASTNode parseIndexAccess(final Token token) {
        ASTNode index = parseExpression();
        consume(TokenType.RIGHT_BRACKET, "Expected ]");
        return new ArrayIndexAccess(token, index);
    }
    
    /**
     * make array = [1,2,3];
     * array.reverse()
     * array.add(1);
     * array.remove(1);
     * */
    private ASTNode parseMemberCall() {
        ASTNode expr = parseCall();
        while (match(TokenType.DOT)) {
            ASTNode right = parseCall();
            expr = new ArrayOperations(expr, (FunctionCall) right);
        }
        return expr;
    }

    private ASTNode parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new Number(Integer.parseInt(previous().value));
        }
        if (match(TokenType.STRING)) {
            return new StringExpr(previous().value);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Variable(previous());
        }
        if (match(TokenType.LPAREN)) {
            ASTNode expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }
        if (check(TokenType.LEFT_BRACKET)) {
            return parseArrayExpression(null);
        }
        throw Err.err("Unexpected token: ", peek());
    }
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) pos++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw Err.err(message, previous());
    }

}