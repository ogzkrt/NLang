package org.nlang.parser;

public class EvalResult {
    Object result;
    boolean isReturn;

    public EvalResult(Object result, boolean isReturn) {
        this.result = result;
        this.isReturn = isReturn;
    }
    
}
