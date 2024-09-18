package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import java.util.List;

public class ArrayIndexAccessNode extends ASTNode {
    public final ASTNode index;
    public final ASTNode parent;

    public ArrayIndexAccessNode(final ASTNode parent, final ASTNode index) {
        this.parent = parent;
        this.index = index;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        List<Object> variable = (List<Object>) parent.evaluate(env).result;
        Double evaluated = (Double) index.evaluate(env).result;
        return new EvalResult(variable.get(evaluated.intValue()));
    }
}