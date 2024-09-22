package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ASTNode {

    public Optional<Token> token;

    public abstract EvalResult evaluate(Environment env);

    static class AssignmentNode extends ASTNode {
        final Token token;
        private final ASTNode value;

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
    static class BinaryNode extends ASTNode {
        private final ASTNode left;
        private final ASTNode right;
        private final Token operatorToken;
        private final Token.TokenType operator;

        public BinaryNode(ASTNode left, ASTNode right, Token operatorToken) {
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

    static class BlockNode extends ASTNode {
        private final List<ASTNode> expressions;

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

    static class CallNode extends ASTNode {
        final ASTNode callee;
        public final Token token;
        public final List<ASTNode> arguments;

        public CallNode(ASTNode callee, final Token token, List<ASTNode> arguments) {
            this.callee = callee;
            this.token = token;
            this.arguments = arguments;
        }

        @Override
        public EvalResult evaluate(Environment env) {

            if (callee != null) {
                Object evaluated = callee.evaluate(env).result;
                if (evaluated instanceof Iterable<?> v) {
                    List<Object> test = (List<Object>) evaluated;
                    String value = token.value;
                    if (value.equals("add")) {
                        arguments.forEach(a -> {
                            test.add(a.evaluate(env).result);
                        });
                    } else if (value.equals("remove")) {
                        arguments.forEach(a -> {
                            test.remove(a.evaluate(env).result);
                        });
                    } else if (value.equals("reverse")) {
                        return new EvalResult(test.reversed());
                    } else if (value.equals("last")) {
                        return new EvalResult(test.getLast());
                    } else if (value.equals("first")) {
                        return new EvalResult(test.getFirst());
                    }

                } else if (evaluated instanceof NInstance n) {
                    return new EvalResult(n.getField(token));
                }
                return null;
            } else {

                FunctionDefinitionNode func = (FunctionDefinitionNode) env.getFunction(token);
                List<Object> evaluatedArguments = new ArrayList<>();
                for (ASTNode argument : arguments) {
                    evaluatedArguments.add(argument.evaluate(env).result);
                }
                return func.call(evaluatedArguments, env);
            }
        }
    }

    static class ChildAccessNode extends ASTNode {

        private final Token parentToken;
        public final ASTNode index;
        public final ASTNode parent;
        private final Token indexToken;

        public ChildAccessNode(final Token parentToken, final ASTNode parent, final ASTNode index, final Token indexToken) {
            this.parentToken = parentToken;
            this.parent = parent;
            this.index = index;
            this.indexToken = indexToken;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            NInstance evaluated = (NInstance) parent.evaluate(env).result;
            //EvalResult indexEval = index.evaluate(env);
            return new EvalResult(evaluated.getField(indexToken));
        }
    }

    static class ContainerNode extends ASTNode {

        private final Map<ASTNode, ASTNode> fields;
        private NInstance instance;

        public ContainerNode(Map<ASTNode, ASTNode> fields) {
            this.fields = fields;
        }

        private void init(Environment environment) {

            instance = new NInstance();
            fields.forEach((key, value) ->
            {
                if (key instanceof VariableNode node) {
                    instance.defineField(node.token.value, value.evaluate(environment).result);
                } else {
                    instance.defineField(key.evaluate(environment).result, value.evaluate(environment).result);
                }
            });

        }

        @Override
        public EvalResult evaluate(Environment env) {
            init(env);
            return new EvalResult(instance, false);
        }
    }

    static class ForInLoopNode extends ASTNode {
        private final Token loopVar;
        private final ASTNode endNode;
        private final ASTNode body;
        private final Token indexVar;

        public ForInLoopNode(Token loopVar, ASTNode endNode, ASTNode body, Token indexVar) {
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
            loopEnv.defineVariable(indexVar, 0);
            for (int i = 0; i < variable.size(); i++) {
                loopEnv.defineVariable(loopVar, variable.get(i));
                loopEnv.defineVariable(indexVar, (double) i);
                body.evaluate(loopEnv);
            }
            return null;
        }
    }

    static class ForLoopNode extends ASTNode {

        private final ASTNode start;
        private final ASTNode end;
        private final Token indexVariable;
        private final ASTNode body;
        private final boolean isEqual;

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
    public static class FunctionDefinitionNode extends ASTNode {
        private final List<Token> parameters;
        private final List<ASTNode> body;
        public final Token name;

        public FunctionDefinitionNode(Token name, List<Token> parameters, List<ASTNode> body) {
            this.parameters = parameters;
            this.name = name;
            this.body = body;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            throw new UnsupportedOperationException("functions can't be evaluated in NLang");
        }

        public EvalResult call(List<Object> arguments, Environment environment) {
            if (arguments.size() != parameters.size()) {
                throw Err.err("Argument count mismatch in function call: ", name);
            }

            final Environment localEnvironment = new Environment(environment);
            for (int i = 0; i < parameters.size(); i++) {
                localEnvironment.defineVariable(parameters.get(i), arguments.get(i));
            }
            EvalResult result;
            for (ASTNode expr : body) {
                result = expr.evaluate(localEnvironment);
                if (expr instanceof ReturnNode) {
                    return result;
                }
                if (result != null && result.isReturn) {
                    return result;
                }
            }
            return null;
        }

    }

    static class IfNode extends ASTNode {
        private final ASTNode condition;
        private final ASTNode blockExpr;

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
    static class IndexAccessNode extends ASTNode {
        private final Token parentToken;
        public final ASTNode index;
        public final ASTNode parent;
        private final Token indexToken;

        public IndexAccessNode(final Token parentToken, final ASTNode parent, final ASTNode index, final Token indexToken) {
            this.parentToken = parentToken;
            this.parent = parent;
            this.index = index;
            this.indexToken = indexToken;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            Object evaluated = parent.evaluate(env).result;
            if (evaluated instanceof Iterable v) {
                List<Object> variable = (List<Object>) v;
                Double indexValue = (Double) index.evaluate(env).result;
                return new EvalResult(variable.get(indexValue.intValue()));
            } else if (evaluated instanceof NInstance nInstance) {
                return new EvalResult(nInstance.getField(indexToken));
            }
            throw Err.err("Parent is not iterable", parentToken);
        }
    }

    static class IndexSetNode extends ASTNode {
        final ASTNode arrayVariable;
        final ASTNode index;
        final ASTNode value;

        public IndexSetNode(final ASTNode arrayVariable, ASTNode index, ASTNode value) {
            this.arrayVariable = arrayVariable;
            this.index = index;
            this.value = value;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            Object indexValue = index.evaluate(env).result;
            if (indexValue instanceof String index){
                Object result = arrayVariable.evaluate(env).result;
                if (result instanceof NInstance node){
                    node.defineField(index,value.evaluate(env).result);
                }

            }else if (indexValue instanceof Double index){

                List<Object> array = (List<Object>) arrayVariable.evaluate(env).result;
                if (array.size() > index.intValue()) {
                    array.set(index.intValue(), value.evaluate(env).result);
                } else {
                    array.add(index.intValue(), value.evaluate(env).result);
                }
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

    static class NumberNode extends ASTNode {
        private final double value;

        public NumberNode(double value) {
            this.value = value;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            return new EvalResult(value);
        }
    }

    static class PrintNode extends ASTNode {
        private final List<ASTNode> expressions;

        public PrintNode(List<ASTNode> expressions) {
            this.expressions = expressions;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            for (ASTNode e : expressions) {
                Object result = e.evaluate(env).result;
                if(result instanceof NInstance sp){
                    System.out.println(sp.formattedView(0));
                }else{
                    System.out.print(result);
                }

            }
            System.out.println();
            return null;
        }
    }

    static class ReturnNode extends ASTNode {
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

    static class StringNode extends ASTNode {
        private final String value;

        public StringNode(String value) {
            this.value = value;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            return new EvalResult(value);
        }
    }
    static class VarDeclarationNode extends ASTNode {
        final Token token;
        private final ASTNode value;

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

    static class VariableNode extends ASTNode {
        public final Token token;

        public VariableNode(Token token) {
            this.token = token;
        }

        @Override
        public EvalResult evaluate(Environment env) {
            return new EvalResult(env.getVariable(token));
        }
    }


}

