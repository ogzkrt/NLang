package org.nlang.parser.astnodes;

import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;
import java.util.List;

public class ArrayIndexSetNode extends ASTNode {
    final ASTNode arrayVariable;
    final ASTNode index;
    final ASTNode value;

    public ArrayIndexSetNode(final ASTNode arrayVariable, ASTNode index, ASTNode value) {
        this.arrayVariable = arrayVariable;
        this.index = index;
        this.value = value;
    }

    @Override
    public EvalResult evaluate(Environment env) {
        Double indexDouble = (Double) this.index.evaluate(env).result;
        List<Object> array = (List<Object>) arrayVariable.evaluate(env).result;
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
