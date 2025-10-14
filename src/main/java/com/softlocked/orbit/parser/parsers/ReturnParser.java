package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.statement.controlflow.BreakASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class ReturnParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("return");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("return");

        if (tokens.peek() == null || tokens.peek().equals(";")) {
            tokens.consumeSemicolon();
            return new BreakASTNode(Breakpoint.Type.RETURN, null);
        }

        try {
            ExpressionParser exprParser = new ExpressionParser(tokens, context);
            ASTNode returnValue = exprParser.parse();
            tokens.consumeSemicolon();

            return new BreakASTNode(Breakpoint.Type.RETURN, returnValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse return expression: " + e.getMessage(), e);
        }
    }
}
