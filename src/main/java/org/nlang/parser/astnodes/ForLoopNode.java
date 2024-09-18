package org.nlang.parser.astnodes;

import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class ForLoopNode extends ASTNode {

    private final ASTNode start;
    private final ASTNode end;
    private final Token indexVariable;
    private final ASTNode body;
    private final boolean isEqual;

    public ForLoopNode(ASTNode start, ASTNode end, Token indexVariable, ASTNode body, boolean isEqual) {
        this.start = start;
        this.end = end;
        this.indexVariable = indexVariable;
        this.body = body;
        this.isEqual = isEqual;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        int startValue = ((Double) (start.evaluate(env).result)).intValue();
        int endValue = ((Double) end.evaluate(env).result).intValue();
        endValue = isEqual ? endValue + 1 : endValue;
        Environment loopEnv = new Environment(env);
        for (int i = startValue; i < endValue; i++) {
            loopEnv.setVariable(indexVariable, (double) i);
            body.evaluate(loopEnv);

        }
        return null;
    }
}
