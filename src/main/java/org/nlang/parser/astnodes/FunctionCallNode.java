package org.nlang.parser.astnodes;

import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import org.nlang.parser.NLangFunction;
import java.util.ArrayList;
import java.util.List;

public class FunctionCallNode extends ASTNode {
    final ASTNode callee;
    public final Token token;
    public final List<ASTNode> arguments;

    public FunctionCallNode(ASTNode callee, final Token token, List<ASTNode> arguments) {
        this.callee = callee;
        this.token = token;
        this.arguments = arguments;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        // ????
        if (callee != null) {
            Object evaluated = callee.evaluate(env);

            return null;
        }

        NLangFunction func = (NLangFunction) env.getFunction(token);
        List<Object> evaluatedArguments = new ArrayList<>();
        for (ASTNode argument : arguments) {
            evaluatedArguments.add(argument.evaluate(env).result);
        }
        return func.call(evaluatedArguments, env);
    }
}
