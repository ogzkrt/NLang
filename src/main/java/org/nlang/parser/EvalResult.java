package org.nlang.parser;

public class EvalResult {
    public Object result;
    public boolean isReturn;

    public EvalResult(Object result) {
        this(result, false);
    }

    public EvalResult(Object result, boolean isReturn) {
        this.result = result;
        this.isReturn = isReturn;
    }

}
