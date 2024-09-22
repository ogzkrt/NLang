package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import java.util.List;

public class FunctionDefinitionNode extends ASTNode {
    private final List<Token> parameters;
    private final List<ASTNode> body;
    public final Token name;

    public FunctionDefinitionNode(Token name, List<Token> parameters, List<ASTNode> body) {
        this.parameters = parameters;
        this.name = name;
        this.body = body;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        throw new UnsupportedOperationException("functions can't be evaluated in NLang");
    }

    public EvalResult call(List<Object> arguments, Environment environment) {
        if (arguments.size() != parameters.size()) {
            throw Err.err("Argument count mismatch in function call: ", name);
        }

        final Environment localEnvironment = new Environment(environment);
        for (int i = 0; i < parameters.size(); i++) {
            localEnvironment.defineVariable(parameters.get(i), arguments.get(i));
        }
        EvalResult result;
        for (ASTNode expr : body) {
            result = expr.evaluate(localEnvironment);
            if (expr instanceof ReturnNode) {
                return result;
            }
            if (result != null && result.isReturn) {
                return result;
            }
        }
        return null;
    }

}
