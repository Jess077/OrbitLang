package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.statement.controlflow.BreakASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class ThrowParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("throw");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("throw");

        if (tokens.peek() == null || tokens.peek().equals(";")) {
            throw new RuntimeException("Invalid throw statement - expression required");
        }

        try {
            ExpressionParser exprParser = new ExpressionParser(tokens, context);
            ASTNode throwValue = exprParser.parse();
            tokens.consumeSemicolon();

            return new BreakASTNode(Breakpoint.Type.THROW, throwValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse throw expression: " + e.getMessage(), e);
        }
    }
}

