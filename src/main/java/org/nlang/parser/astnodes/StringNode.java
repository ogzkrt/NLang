package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class StringNode extends ASTNode {
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value);
    }
}