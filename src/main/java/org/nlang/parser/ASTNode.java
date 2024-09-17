package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.lexer.Token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ASTNode {

    Optional<Token> token;

    public abstract Object evaluate(Environment env);
}

class FunctionCall extends ASTNode {
    final ASTNode callee;
    final Token token;
    final List<ASTNode> arguments;

    FunctionCall(ASTNode callee, final Token token, List<ASTNode> arguments) {
        this.callee = callee;
        this.token = token;
        this.arguments = arguments;
    }

    @Override
    public Object evaluate(Environment env) {
        
        if(callee!=null){
            Object evaluated = callee.evaluate(env);
            
            return null;
        }
        
        NLangFunction func = (NLangFunction) env.getFunction(token);
        List<Object> evaluatedArguments = new ArrayList<>();
        for (ASTNode argument : arguments) {
            evaluatedArguments.add(argument.evaluate(env));
        }

        return func.call(evaluatedArguments, env);
    }
}


class Return extends ASTNode {
    final ASTNode value;

    Return(ASTNode value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {

        return value.evaluate(env);
    }
}

class ArrayOperations extends ASTNode {
    final ASTNode array;
    final FunctionCall arrayFunction;

    ArrayOperations(ASTNode array, FunctionCall arrayFunction) {
        this.array = array;
        this.arrayFunction = arrayFunction;
    }


    @Override
    public Object evaluate(Environment env) {
        if (array instanceof Variable vr) {
            if (!(env.getVariable(vr.token) instanceof Iterable<?>)) {
                throw Err.err(String.format("Can not make array operation '%s'", arrayFunction.token.value), vr.token);
            }
        }
        Object result = array.evaluate(env);
        if (result != null) {
            List<Object> test = (List<Object>) result;
            String value = arrayFunction.token.value;
            if (value.equals("add")) {
                arrayFunction.arguments.forEach(a -> {
                    test.add(a.evaluate(env));
                });
            } else if (value.equals("remove")) {
                arrayFunction.arguments.forEach(a -> {
                    test.remove(a.evaluate(env));
                });
            } else if (value.equals("reverse")) {
                return test.reversed();
            } else if (value.equals("last")) {
                return test.getLast();
            }
        }
        return null;
    }
}

class ArrayIndexSet extends ASTNode {
    final Token arrayName;
    final ASTNode index;
    final ASTNode value;

    ArrayIndexSet(final Token arrayName, ASTNode index, ASTNode value) {
        this.arrayName = arrayName;
        this.index = index;
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        Double indexDouble = (Double) this.index.evaluate(env);
        List<Object> array = (List<Object>) env.getVariable(arrayName);
        if (array.size() > indexDouble.intValue()) {
            array.set(indexDouble.intValue(), value.evaluate(env));
        } else {
            array.add(indexDouble.intValue(), value.evaluate(env));
        }
//        if(indexDouble.intValue()>=array.size()){
//            List<Object> newArray = new ArrayList<>(indexDouble.intValue()*2);
//            newArray.addAll(array);
//            newArray.add(indexDouble.intValue(),value.evaluate(env));
//        }else{
//            array.add(indexDouble.intValue(),value.evaluate(env));
//        }
        return null;
    }
}

class ArrayIndexAccess extends ASTNode {
    final ASTNode index;
    final Token token;

    ArrayIndexAccess(final Token token, final ASTNode index) {
        this.token = token;
        this.index = index;
    }

    @Override
    public Object evaluate(Environment env) {
        List<Double> variable = (List<Double>) env.getVariable(token);
        Double evaluated = (Double) index.evaluate(env);
        return variable.get(evaluated.intValue());
    }
}

class Number extends ASTNode {
    private final double value;

    Number(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}

class Binary extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final Token operatorToken;
    private final TokenType operator;

    Binary(ASTNode left, ASTNode right, Token operatorToken) {
        this.left = left;
        this.right = right;
        this.operatorToken = operatorToken;
        this.operator = operatorToken.type;
    }

    @Override
    public Object evaluate(Environment env) {
        double leftVal = (double) left.evaluate(env);
        double rightVal = (double) right.evaluate(env);
        return switch (operator) {
            case PLUS -> leftVal + rightVal;
            case MINUS -> leftVal - rightVal;
            case MULTIPLY -> leftVal * rightVal;
            case DIVIDE -> leftVal / rightVal;
            case GREATER -> leftVal > rightVal;
            case SMALLER -> leftVal < rightVal;
            default -> Err.err("Unknown operator: " + operatorToken.value, operatorToken);
        };
    }
}

class Variable extends ASTNode {
    final Token token;

    Variable(Token token) {
        this.token = token;
    }

    @Override
    public Object evaluate(Environment env) {
        return env.getVariable(token);
    }
}

class VarDeclaration extends ASTNode {
    final Token token;
    private final ASTNode value;

    VarDeclaration(Token token, ASTNode value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        if (value instanceof NLangArray) {
            Object evaluated = value.evaluate(env);
            env.setVariable(token, evaluated);
        } else {
            env.setVariable(token, value.evaluate(env));
        }
        return null;
    }

}

class Assignment extends ASTNode {
    final Token token;
    private final ASTNode value;

    Assignment(Token token, ASTNode value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        env.assignVariable(token, value.evaluate(env));
        return null;
    }
}

class ForInLoop extends ASTNode {
    private final ASTNode iterable;
    private final ASTNode body;
    private final Token loopVarName;
    private final Token indexVarName;

    ForInLoop(final ASTNode iterable, final ASTNode body, final Token loopVarName,
              final Token indexVarName) {
        this.iterable = iterable;
        this.body = body;
        this.loopVarName = loopVarName;
        this.indexVarName = indexVarName;
    }

    @Override
    public Object evaluate(Environment env) {

        Object potentialIterable = iterable.evaluate(env);
        if (!(potentialIterable instanceof Iterable<?>)) {
            iterable.token.ifPresent(t -> {
                throw Err.err("variable " + t.value + " is not iterable.", t);
            });
        }
        List<Object> variable = (List<Object>) potentialIterable;

        for (int i = 0; i < variable.size(); i++) {
            Environment loopEnv = new Environment(env);
            loopEnv.setVariable(loopVarName, variable.get(i));
            loopEnv.setVariable(indexVarName, (double) i);
            body.evaluate(loopEnv);
        }
        return null;
    }
}

class ForNodeExperimental extends ASTNode {

    private final ASTNode start;
    private final ASTNode end;
    private final ASTNode body;

    ForNodeExperimental(ASTNode start, ASTNode end, ASTNode body) {
        this.start = start;
        this.end = end;
        this.body = body;
    }

    @Override
    public Object evaluate(Environment env) {
        Double startValue = (Double) (start.evaluate(env));
        Double endValue = (Double) end.evaluate(env);
        for (int i = startValue.intValue(); i < endValue.intValue(); i++) {
            Environment loopEnv = new Environment(env);
            loopEnv.setVariable(new Token(TokenType.IDENTIFIER, "i", 0, 0, 0), (double) i);
            body.evaluate(loopEnv);

        }
        return null;
    }
}

class If extends ASTNode {
    private final ASTNode condition;
    private final ASTNode blockExpr;

    If(ASTNode condition, ASTNode blockExpr) {
        this.condition = condition;
        this.blockExpr = blockExpr;
    }

    @Override
    public Object evaluate(Environment env) {
        boolean result = (boolean) this.condition.evaluate(env);
        if (result) {
            return this.blockExpr.evaluate(env);
        }
        return null;
    }
}

class Print extends ASTNode {
    private final List<ASTNode> expressions;

    Print(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public Object evaluate(Environment env) {
        for (ASTNode e : expressions) {
            System.out.print(e.evaluate(env));
        }
        System.out.println();
        return null;
    }
}

class Block extends ASTNode {
    private final List<ASTNode> expressions;

    Block(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public Object evaluate(Environment env) {
        Environment localEnv = new Environment(env);
        Object result = 0;
        for (ASTNode expr : expressions) {
            if (expr instanceof NLangFunction) {
                localEnv.addFunction((NLangFunction) expr);
            } else {
                result = expr.evaluate(localEnv);
            }
        }
        return result;
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }
}

class StringExpr extends ASTNode {
    private final String value;

    StringExpr(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}
