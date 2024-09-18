package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import java.util.List;

public class PrintNode extends ASTNode {
    private final List<ASTNode> expressions;

    public PrintNode(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        for (ASTNode e : expressions) {
            System.out.print(e.evaluate(env).result);
        }
        System.out.println();
        return null;
    }
}