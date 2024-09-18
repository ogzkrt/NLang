package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class ReturnNode extends ASTNode {
    final ASTNode value;

    public ReturnNode(ASTNode value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        EvalResult evaluate = value.evaluate(env);
        evaluate.isReturn = true;
        return evaluate;
    }
}