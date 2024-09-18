package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class NumberNode extends ASTNode {
    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value);
    }
}