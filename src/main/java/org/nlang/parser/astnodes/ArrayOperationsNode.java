package org.nlang.parser.astnodes;

import org.nlang.err.Err;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import java.util.List;

public class ArrayOperationsNode extends ASTNode {
    final ASTNode array;
    final FunctionCallNode arrayFunction;

    public ArrayOperationsNode(ASTNode array, FunctionCallNode arrayFunction) {
        this.array = array;
        this.arrayFunction = arrayFunction;
    }


    @Override
    public EvalResult evaluate(Environment env) {
        if (array instanceof VariableNode vr) {
            if (!(env.getVariable(vr.token) instanceof Iterable<?>)) {
                throw Err.err(String.format("Can not make array operation '%s'", arrayFunction.token.value), vr.token);
            }
        }
        Object result = array.evaluate(env).result;
        if (result != null) {
            List<Object> test = (List<Object>) result;
            String value = arrayFunction.token.value;
            if (value.equals("add")) {
                arrayFunction.arguments.forEach(a -> {
                    test.add(a.evaluate(env).result);
                });
            } else if (value.equals("remove")) {
                arrayFunction.arguments.forEach(a -> {
                    test.remove(a.evaluate(env).result);
                });
            } else if (value.equals("reverse")) {
                return new EvalResult(test.reversed());
            } else if (value.equals("last")) {
                return new EvalResult(test.getLast());
            } else if (value.equals("first")) {
                return new EvalResult(test.getFirst());
            }
        }
        return null;
    }
}

