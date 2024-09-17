package org.nlang.parser;

import org.nlang.lexer.Token;

import java.util.List;

public class NLangFunction extends ASTNode {
    private final List<Token> parameters;
    private final List<ASTNode> body;
    final Token name;

    public NLangFunction(Token name, List<Token> parameters, List<ASTNode> body) {
        this.parameters = parameters;
        this.name = name;
        this.body = body;
    }

    @Override
    public Object evaluate(Environment env) {
        throw new UnsupportedOperationException("functions can't be evaluated in NLang");
    }

    Object call(List<Object> arguments, Environment environment) {
//        if (arguments.size() != parameters.size()) {
//            throw new RuntimeException("Argument count mismatch in function call: " + name);
//        }

        final Environment localEnvironment = new Environment(environment);
        for (int i = 0; i < parameters.size(); i++) {
            localEnvironment.setVariable(parameters.get(i), arguments.get(i));
        }
        Object result;
        for (ASTNode expr : body) {
            result = expr.evaluate(localEnvironment);
            if (expr instanceof Return) {
                return result;
            }
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
