package org.nlang.parser;

import org.nlang.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class NLangArray extends ASTNode{
    
    final List<ASTNode> elements;
    final Token name;
    
    public NLangArray(Token name, List<ASTNode> elements) {
        this.name = name;
        this.elements = elements;
    }
    
    @Override
    public Object evaluate(Environment env) {
        List<Object> result = new ArrayList<>();
        for (ASTNode element : elements) {
            result.add(element.evaluate(env));
        }
        return result;
    }
}
