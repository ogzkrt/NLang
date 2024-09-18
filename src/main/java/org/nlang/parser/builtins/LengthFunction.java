package org.nlang.parser.builtins;


import org.nlang.lexer.Token;
import org.nlang.parser.ASTNode;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import org.nlang.parser.NLangFunction;
import java.util.ArrayList;
import java.util.List;

public class LengthFunction {

    private final List<ASTNode> lengthBody = getAstNodes();
    private final List<Token> parameters = new ArrayList<>();
    private final NLangFunction nLangFunction;
    
    public LengthFunction(){
        parameters.add(new Token(Token.TokenType.IDENTIFIER, "len",0,0,0));
        nLangFunction = new NLangFunction(
                new Token(Token.TokenType.IDENTIFIER, "len",0,0,0),
                parameters,lengthBody
        );
    }

    public NLangFunction getnLangFunction() {
        return nLangFunction;
    }

    private static List<ASTNode> getAstNodes() {
        final ASTNode lengthNode = new ASTNode() {
            @Override
            public EvalResult evaluate(Environment env) {
                Object strLen = env.getVariable(new Token(Token.TokenType.IDENTIFIER,"len",0,0,0));
                if (strLen instanceof String) return new EvalResult((double)((String) strLen).length(),true);
                if (strLen instanceof List) return new EvalResult((double)((List<?>) strLen).size(),true);
                throw new UnsupportedOperationException("Can not call len() other than strings");
            }
        };

        final List<ASTNode> lengthBody = new ArrayList<>();
        lengthBody.add(lengthNode);
        return lengthBody;
    }
    
}
