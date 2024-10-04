package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ASTNode {

    public abstract EvalResult evaluate(Environment env);

}

class BinaryNode extends ASTNode {
    final ASTNode left;
    final ASTNode right;
    final Token operatorToken;
    final Token.TokenType operator;

    public BinaryNode(ASTNode left, ASTNode right, Token operatorToken) {
        this.left = left;
        this.right = right;
        this.operatorToken = operatorToken;
        this.operator = operatorToken.type;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Object leftVal = left.evaluate(env).result;
        Object rightVal = right.evaluate(env).result;

        if (operator == Token.TokenType.EQUAL) {
            return new EvalResult(leftVal.equals(rightVal));
        }
        if (operator == Token.TokenType.NOT_EQUAL) {
            return new EvalResult(!leftVal.equals(rightVal));
        }
        return processNumberOperations((double) leftVal, (double) rightVal, operator);
    }

    private EvalResult processNumberOperations(double leftVal, double rightVal, Token.TokenType operator) {
        return switch (operator) {
            case PLUS -> new EvalResult(leftVal + rightVal);
            case MINUS -> new EvalResult(leftVal - rightVal);
            case MULTIPLY -> new EvalResult(leftVal * rightVal);
            case DIVIDE -> new EvalResult(leftVal / rightVal);
            case GREATER -> new EvalResult(leftVal > rightVal);
            case SMALLER -> new EvalResult(leftVal < rightVal);
            default -> throw Err.err("Unknown operator: " + operatorToken.value, operatorToken);
        };
    }

}

class BlockNode extends ASTNode {
    final List<ASTNode> expressions;

    public BlockNode(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Environment localEnv = new Environment(env);
        EvalResult result = null;
        for (ASTNode expr : expressions) {
            if (expr instanceof FunctionDefinitionNode node) {
                localEnv.addFunction(node);
            } else {
                result = expr.evaluate(localEnv);
                if (result != null && result.isReturn) {
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

class CallNode extends ASTNode {
    ASTNode callee;
    final Token name;
    final List<ASTNode> arguments;

    public CallNode(ASTNode callee, final Token token, List<ASTNode> arguments) {
        this.callee = callee;
        this.name = token;
        this.arguments = arguments;
    }

    @Override
    public EvalResult evaluate(Environment env) {

        if (callee == null) {
            FunctionDefinitionNode func = (FunctionDefinitionNode) env.getFunction(name);
            List<Object> evaluatedArguments = new ArrayList<>();
            for (ASTNode argument : arguments) {
                evaluatedArguments.add(argument.evaluate(env).result);
            }
            return func.call(evaluatedArguments, env);
        }
        Object evaluated = callee.evaluate(env).result;

        if (evaluated instanceof Iterable<?>) {
            List<Object> test = new ArrayList<>((List<Object>) evaluated);
            String value = name.value;
            switch (value) {
                case "add" -> {
                    arguments.forEach(a -> test.add(a.evaluate(env).result));
                    assignResult(env, test);
                    return new EvalResult(new ArrayList<>(test));
                }
                case "remove" -> {
                    arguments.forEach(a -> test.remove(a.evaluate(env).result));
                    assignResult(env, test);
                    return new EvalResult(new ArrayList<>(test));
                }
                case "reverse" -> {
                    return new EvalResult(test.reversed());
                }
                case "last" -> {
                    return new EvalResult(test.getLast());
                }
                case "first" -> {
                    return new EvalResult(test.getFirst());
                }
                default -> throw Err.err(String.format("%s is not a valid method on iterable", name.value), name);
            }


        }
        if (evaluated instanceof String str) {
            List<String> newValue = str.chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.toCollection(ArrayList::new));

            String value = name.value;
            switch (value) {
                case "add" -> {
                    arguments.forEach(a -> newValue.add((String) a.evaluate(env).result));
                    String result = String.join("", newValue);
                    assignResult(env, result);
                    return new EvalResult(result);
                }
                case "remove" -> {
                    arguments.forEach(a -> newValue.remove(a.evaluate(env).result));
                    String result = String.join("", newValue);
                    assignResult(env, result);
                    return new EvalResult(result);
                }
                case "reverse" -> {
                    return new EvalResult(String.join("", newValue.reversed()));
                }
                case "last" -> {
                    return new EvalResult(newValue.getLast());
                }
                case "first" -> {
                    return new EvalResult(newValue.getFirst());
                }
                default -> throw Err.err(String.format("%s is not a valid method on iterable", name.value), name);
            }

        }
        if (evaluated instanceof NObjectInstance n) {
            return new EvalResult(n.getField(name));
        }
        throw Err.err("Wrong usage", name);

    }

    private void assignResult(Environment env, Object result) {
        while (callee instanceof CallNode v) {
            callee = v.callee;
        }
        if (callee instanceof VariableNode calV) {
            env.assignVariable(calV.token, result);
        }
    }
}

class ChildAccessNode extends ASTNode {

    final ASTNode index;
    final ASTNode parent;
    final Token indexToken;

    public ChildAccessNode(final ASTNode parent, final ASTNode index, final Token indexToken) {
        this.parent = parent;
        this.index = index;
        this.indexToken = indexToken;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        NObjectInstance evaluated = (NObjectInstance) parent.evaluate(env).result;
        return new EvalResult(evaluated.getField(indexToken));
    }
}

class ContainerNode extends ASTNode {

    final Map<ASTNode, ASTNode> fields;
    private NObjectInstance instance;

    public ContainerNode(Map<ASTNode, ASTNode> fields) {
        this.fields = fields;
    }

    private void init(Environment environment) {

        instance = new NObjectInstance();
        fields.forEach((key, value) ->
                instance.defineField(key.evaluate(environment).result, value.evaluate(environment).result));

    }

    @Override
    public EvalResult evaluate(Environment env) {
        init(env);
        return new EvalResult(instance, false);
    }
}

class ForInLoopNode extends ASTNode {
    final Token loopVar;
    final ASTNode end;
    final ASTNode body;
    final Token indexVar;
    final Token endToken;

    public ForInLoopNode(Token loopVar, ASTNode endNode, ASTNode body, Token indexVar, Token endToken) {
        this.loopVar = loopVar;
        this.end = endNode;
        this.body = body;
        this.endToken = endToken;
        this.indexVar = indexVar;
    }

    @Override
    public EvalResult evaluate(Environment env) {

        Object potentialIterable = end.evaluate(env).result;
        if (potentialIterable instanceof String str) {
            potentialIterable = str.chars().mapToObj(c -> String.valueOf((char) c)).toList();
        }
        if (!(potentialIterable instanceof Iterable<?>)) {
            throw Err.err("variable " + endToken.value + " is not iterable.", endToken);
        }
        List<Object> variable = (List<Object>) potentialIterable;

        final Environment loopEnv = new Environment(env);
        loopEnv.defineVariable(indexVar, 0);
        for (int i = 0; i < variable.size(); i++) {
            loopEnv.defineVariable(loopVar, variable.get(i));
            loopEnv.defineVariable(indexVar, (double) i);
            body.evaluate(loopEnv);
        }
        return null;
    }
}

class ForLoopNode extends ASTNode {

    final ASTNode start;
    final ASTNode end;
    final Token indexVariable;
    final ASTNode body;
    final boolean isEqual;

    public ForLoopNode(ASTNode start, ASTNode end, Token indexVariable, ASTNode body, boolean isEqual) {
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
            loopEnv.defineVariable(indexVariable, (double) i);
            body.evaluate(loopEnv);

        }
        return null;
    }
}

class IfNode extends ASTNode {
    final ASTNode condition;
    final ASTNode blockExpr;

    public IfNode(ASTNode condition, ASTNode blockExpr) {
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

class IndexAccessNode extends ASTNode {
    final ASTNode index;
    final ASTNode parent;
    final Token indexValue;

    public IndexAccessNode(final ASTNode parent, final ASTNode index, final Token indexToken) {
        this.parent = parent;
        this.index = index;
        this.indexValue = indexToken;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Object evaluated = parent.evaluate(env).result;
        if (evaluated instanceof Iterable v) {
            List<Object> variable = (List<Object>) v;
            Double indexValue = (Double) index.evaluate(env).result;
            return new EvalResult(variable.get(indexValue.intValue()));
        } else if (evaluated instanceof NObjectInstance nInstance) {
            return new EvalResult(nInstance.getField(indexValue));
        }
        throw Err.err("[\"value\"] operation only supported for iterables and objects", indexValue);
    }
}

class IndexSetNode extends ASTNode {
    final ASTNode arrayVariable;
    final ASTNode index;
    final ASTNode value;
    final Token arrayName;

    public IndexSetNode(Token arrayName, ASTNode arrayVariable, ASTNode index, ASTNode value) {
        this.arrayName = arrayName;
        this.arrayVariable = arrayVariable;
        this.index = index;
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Object indexValue = index.evaluate(env).result;
        if (indexValue instanceof String index) {
            Object result = arrayVariable.evaluate(env).result;
            if (result instanceof NObjectInstance node) {
                node.defineField(index, value.evaluate(env).result);
            }

        } else if (indexValue instanceof Double index) {

            final List<Object> array = (List<Object>) arrayVariable.evaluate(env).result;
            int requiredSize = index.intValue();
            if (requiredSize > array.size() - 1) {
                IntStream.range(array.size(), requiredSize + 1).forEach(_ -> array.add(null));
            }
            array.set(index.intValue(), value.evaluate(env).result);
        }

        return null;
    }
}

class NumberNode extends ASTNode {
    final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value);
    }
}

class PrintNode extends ASTNode {
    final List<ASTNode> expressions;

    public PrintNode(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        for (ASTNode e : expressions) {
            Object result = e.evaluate(env).result;
            if (result instanceof NObjectInstance sp) {
                System.out.println(sp.formattedView(0));
            } else {
                System.out.print(result);
            }

        }
        System.out.println();
        return null;
    }
}

class ReturnNode extends ASTNode {
    final ASTNode value;

    public ReturnNode(ASTNode value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        EvalResult evaluate = value.evaluate(env);
        evaluate.isReturn = true;
        return evaluate;
    }
}

class StringNode extends ASTNode {
    final String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(value);
    }
}

class VarDeclarationNode extends ASTNode {
    final Token token;
    final ASTNode value;

    public VarDeclarationNode(Token token, ASTNode value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        env.defineVariable(token, value.evaluate(env).result);
        return null;
    }
}

class VariableNode extends ASTNode {
    final Token token;

    public VariableNode(Token token) {
        this.token = token;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        return new EvalResult(env.getVariable(token));
    }
}

class BooleanNode extends ASTNode {
    final Token token;

    public BooleanNode(Token token) {
        this.token = token;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        switch (token.type) {
            case TRUE -> {
                return new EvalResult(true);
            }
            case FALSE -> {
                return new EvalResult(false);
            }
        }
        throw Err.err("Unexpected token type for boolean", token);
    }
}


class AssignmentNode extends ASTNode {
    final Token token;
    final ASTNode value;

    public AssignmentNode(Token token, ASTNode value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        env.assignVariable(token, value.evaluate(env).result);
        return null;
    }
}

class UnaryNode extends ASTNode {
    final Token operator;
    final ASTNode expr;

    public UnaryNode(Token token, ASTNode value) {
        this.operator = token;
        this.expr = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Object result = expr.evaluate(env).result;
        if (operator.type == Token.TokenType.MINUS) {
            return new EvalResult(-(double) result);
        }
        if (operator.type == Token.TokenType.NOT) {
            return new EvalResult(!(boolean) result);
        }
        throw Err.err("Token type is not suitable for unary", operator);
    }
}
