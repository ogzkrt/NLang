package org.nlang.parser.astnodes;


import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import org.nlang.parser.NLangFunction;
import java.util.List;

public class BlockNode extends ASTNode {
    private final List<ASTNode> expressions;

    public BlockNode(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Environment localEnv = new Environment(env);
        EvalResult result = null;
        for (ASTNode expr : expressions) {
            if (expr instanceof NLangFunction) {
                localEnv.addFunction((NLangFunction) expr);
            } else {
                result = expr.evaluate(localEnv);
                if (result != null && result.isReturn) {
                    return result;
                }
            }
        }
        return result;
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }
}


