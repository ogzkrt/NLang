package org.nlang.parser.builtins;

import org.nlang.lexer.Token;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import org.nlang.parser.FunctionDefinitionNode;
import java.util.ArrayList;
import java.util.List;

public class TimeFunction {

    private final List<ASTNode> body = new ArrayList<>();
    private final FunctionDefinitionNode nLangFunction;

    public TimeFunction() {
        body.add(new ASTNode() {
            @Override
            public EvalResult evaluate(Environment env) {

                return new EvalResult((double) System.currentTimeMillis(),true);
            }
        });
        nLangFunction = new FunctionDefinitionNode(
                new Token(Token.TokenType.IDENTIFIER, "time", 0, 0, 0),
                new ArrayList<>(), body
        );
    }

    public FunctionDefinitionNode getnLangFunction() {
        return nLangFunction;
    }

}
