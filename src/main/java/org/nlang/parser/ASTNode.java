package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import org.nlang.lexer.Token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ASTNode {

    Optional<Token> token;

    public abstract EvalResult evaluate(Environment env);
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
    public EvalResult evaluate(Environment env) {

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


class Return extends ASTNode {
    final ASTNode value;

    Return(ASTNode value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        EvalResult evaluate = value.evaluate(env);
        evaluate.isReturn = true;
        return evaluate;
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
    public EvalResult evaluate(Environment env) {
        if (array instanceof Variable vr) {
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
                return new EvalResult(test.reversed(),false);
            } else if (value.equals("last")) {
                return new EvalResult(test.getLast(),false);
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
    public EvalResult evaluate(Environment env) {
        Double indexDouble = (Double) this.index.evaluate(env).result;
        List<Object> array = (List<Object>) env.getVariable(arrayName);
        if (array.size() > indexDouble.intValue()) {
            array.set(indexDouble.intValue(), value.evaluate(env).result);
        } else {
            array.add(indexDouble.intValue(), value.evaluate(env).result);
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
    public EvalResult evaluate(Environment env) {
        List<Double> variable = (List<Double>) env.getVariable(token);
        Double evaluated = (Double) index.evaluate(env).result;
        return new EvalResult(variable.get(evaluated.intValue()),false);
    }
}

class Number extends ASTNode {
    private final double value;

    Number(double value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value,false);
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
    public EvalResult evaluate(Environment env) {
        double leftVal = (double) left.evaluate(env).result;
        double rightVal = (double) right.evaluate(env).result;
        return switch (operator) {
            case PLUS -> new EvalResult(leftVal + rightVal,false);
            case MINUS -> new EvalResult(leftVal - rightVal,false);
            case MULTIPLY -> new EvalResult(leftVal * rightVal,false);
            case DIVIDE -> new EvalResult(leftVal / rightVal,false);
            case GREATER -> new EvalResult(leftVal > rightVal,false);
            case SMALLER -> new EvalResult(leftVal < rightVal,false);
            default -> throw Err.err("Unknown operator: " + operatorToken.value, operatorToken);
        };
    }
}

class Variable extends ASTNode {
    final Token token;

    Variable(Token token) {
        this.token = token;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(env.getVariable(token),false);
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
    public EvalResult evaluate(Environment env) {
        if (value instanceof NLangArray) {
            EvalResult evaluated = value.evaluate(env);
            env.setVariable(token, evaluated.result);
        } else {
            env.setVariable(token, value.evaluate(env).result);
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
    public EvalResult evaluate(Environment env) {
        env.assignVariable(token, value.evaluate(env).result);
        return null;
    }
}

class ForInLoop extends ASTNode {
    private final Token loopVar;
    private final ASTNode endNode;
    private final ASTNode body;
    private final Token indexVar;

    ForInLoop(Token loopVar, ASTNode endNode, ASTNode body, Token indexVar) {
        this.loopVar = loopVar;
        this.endNode = endNode;
        this.body = body;
        this.indexVar = indexVar;
    }

    @Override
    public EvalResult evaluate(Environment env) {

        Object potentialIterable = endNode.evaluate(env).result;
        if (!(potentialIterable instanceof Iterable<?>)) {
            endNode.token.ifPresent(t -> {
                throw Err.err("variable " + t.value + " is not iterable.", t);
            });
        }
        List<Object> variable = (List<Object>) potentialIterable;

        final Environment loopEnv = new Environment(env);
        loopEnv.setVariable(indexVar, 0);
        for (int i = 0; i < variable.size(); i++) {
            loopEnv.setVariable(loopVar, variable.get(i));
            loopEnv.setVariable(indexVar, (double) i);
            body.evaluate(loopEnv);
        }
        return null;
    }
}

class ForNodeExperimental extends ASTNode {

    private final ASTNode start;
    private final ASTNode end;
    private final Token indexVariable;
    private final ASTNode body;
    private final boolean isEqual;

    ForNodeExperimental(ASTNode start, ASTNode end, Token indexVariable, ASTNode body, boolean isEqual) {
        this.start = start;
        this.end = end;
        this.indexVariable = indexVariable;
        this.body = body;
        this.isEqual = isEqual;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        int startValue = ((Double) (start.evaluate(env).result)).intValue();
        int endValue = ((Double) end.evaluate(env).result).intValue();
        endValue = isEqual ? endValue + 1 : endValue;
        Environment loopEnv = new Environment(env);
        for (int i = startValue; i < endValue; i++) {
            loopEnv.setVariable(indexVariable, (double) i);
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
    public EvalResult evaluate(Environment env) {
        boolean result = (boolean) this.condition.evaluate(env).result;
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
    public EvalResult evaluate(Environment env) {
        for (ASTNode e : expressions) {
            System.out.print(e.evaluate(env).result);
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
    public EvalResult evaluate(Environment env) {
        Environment localEnv = new Environment(env);
        EvalResult result = null;
        for (ASTNode expr : expressions) {
            if (expr instanceof NLangFunction) {
                localEnv.addFunction((NLangFunction) expr);
            } else {
                result = expr.evaluate(localEnv);
                if(result!=null && result.isReturn){
                    return result;
                }
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
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value,false);
    }
}
