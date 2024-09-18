package org.nlang.parser.astnodes;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import java.util.List;

public class ForInLoopNode extends ASTNode {
    private final Token loopVar;
    private final ASTNode endNode;
    private final ASTNode body;
    private final Token indexVar;

    public ForInLoopNode(Token loopVar, ASTNode endNode, ASTNode body, Token indexVar) {
        this.loopVar = loopVar;
        this.endNode = endNode;
        this.body = body;
        this.indexVar = indexVar;
    }

    @Override
    public EvalResult evaluate(Environment env) {

        Object potentialIterable = endNode.evaluate(env).result;
        if (!(potentialIterable instanceof Iterable<?>)) {
            endNode.token.ifPresent(t -> {
                throw Err.err("variable " + t.value + " is not iterable.", t);
            });
        }
        List<Object> variable = (List<Object>) potentialIterable;

        final Environment loopEnv = new Environment(env);
        loopEnv.setVariable(indexVar, 0);
        for (int i = 0; i < variable.size(); i++) {
            loopEnv.setVariable(loopVar, variable.get(i));
            loopEnv.setVariable(indexVar, (double) i);
            body.evaluate(loopEnv);
        }
        return null;
    }
}

