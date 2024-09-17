package org.nlang.parser;

import java.util.ArrayList;
import java.util.List;

public class NLangArray extends ASTNode {

    final List<ASTNode> elements;

    public NLangArray(List<ASTNode> elements) {
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
