package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.parser.builtins.LengthFunction;
import org.nlang.parser.builtins.TimeFunction;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, ASTNode> functions = new HashMap<>();
    private final Environment parent;

    public Environment() {
        this(null);

        addFunction(new TimeFunction().getnLangFunction());
        addFunction(new LengthFunction().getnLangFunction());
    }
    
    public Environment(final Environment parent) {
        this.parent = parent;
    }

    public Object getVariable(final Token token) {

        if (variables.containsKey(token.value)) {
            return variables.get(token.value);
        }
        if (parent != null) {
            return parent.getVariable(token);
        }
        throw Err.err("Undefined variable: " + token.value, token);
    }

    void assignVariable(final Token token, final Object value) {
        if (variables.containsKey(token.value)) {
            variables.put(token.value, value);
            return;
        }
        if (parent != null) {
            parent.assignVariable(token, value);
            return;
        }
        throw Err.err("Undefined variable: " + token.value, token);
    }

    void setVariable(final Token token, final Object value) {
        variables.put(token.value, value);
    }

    public void addFunction(final NLangFunction function) {
        functions.put(String.valueOf(function.name.value), function);
    }

    public ASTNode getFunction(final Token token) {
        if (functions.containsKey(token.value)) {
            return functions.get(token.value);
        }
        if (parent != null) {
            return parent.getFunction(token);
        }
        throw Err.err("function doesn't exist", token);
    }

}