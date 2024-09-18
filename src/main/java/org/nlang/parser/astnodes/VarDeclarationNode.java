package org.nlang.parser.astnodes;

import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

public class VarDeclarationNode extends ASTNode {
    final Token token;
    private final ASTNode value;

    public VarDeclarationNode(Token token, ASTNode value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        env.setVariable(token, value.evaluate(env).result);
        return null;
    }

}
