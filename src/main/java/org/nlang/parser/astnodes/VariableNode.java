package org.nlang.parser.astnodes;

import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class VariableNode extends ASTNode {
    public final Token token;

    public VariableNode(Token token) {
        this.token = token;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(env.getVariable(token));
    }
}
