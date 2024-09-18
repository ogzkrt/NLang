package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class IfNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode blockExpr;

    public IfNode(ASTNode condition, ASTNode blockExpr) {
        this.condition = condition;
        this.blockExpr = blockExpr;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        boolean result = (boolean) this.condition.evaluate(env).result;
        if (result) {
            return this.blockExpr.evaluate(env);
        }
        return null;
    }
}