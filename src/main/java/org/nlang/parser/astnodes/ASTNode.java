package org.nlang.parser.astnodes;

import org.nlang.lexer.Token;
import org.nlang.parser.Environment;
import org.nlang.parser.EvalResult;

import java.util.Optional;

public abstract class ASTNode {

    public Optional<Token> token;

    public abstract EvalResult evaluate(Environment env);
}