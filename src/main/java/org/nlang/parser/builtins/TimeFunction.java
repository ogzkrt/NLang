package org.nlang.parser.builtins;

import org.nlang.lexer.Token;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.NLangFunction;
import java.util.ArrayList;
import java.util.List;

public class TimeFunction {

    private final List<ASTNode> body = new ArrayList<>();
    private final NLangFunction nLangFunction;
    
    public TimeFunction(){
        body.add(new ASTNode() {
            @Override
            public Object evaluate(Environment env) {

                return (double) System.currentTimeMillis();
            }
        });
        nLangFunction = new NLangFunction(
                new Token(Token.TokenType.IDENTIFIER, "time", 0, 0, 0),
                new ArrayList<>(), body
        );
    }

    public NLangFunction getnLangFunction() {
        return nLangFunction;
    }
    
}
