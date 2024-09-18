package org.nlang.parser.astnodes;


import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class BinaryNode extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final Token operatorToken;
    private final Token.TokenType operator;

    public BinaryNode(ASTNode left, ASTNode right, Token operatorToken) {
        this.left = left;
        this.right = right;
        this.operatorToken = operatorToken;
        this.operator = operatorToken.type;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        double leftVal = (double) left.evaluate(env).result;
        double rightVal = (double) right.evaluate(env).result;
        return switch (operator) {
            case PLUS -> new EvalResult(leftVal + rightVal);
            case MINUS -> new EvalResult(leftVal - rightVal);
            case MULTIPLY -> new EvalResult(leftVal * rightVal);
            case DIVIDE -> new EvalResult(leftVal / rightVal);
            case GREATER -> new EvalResult(leftVal > rightVal);
            case SMALLER -> new EvalResult(leftVal < rightVal);
            default -> throw Err.err("Unknown operator: " + operatorToken.value, operatorToken);
        };
    }
}
