package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.statement.controlflow.BreakASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class BreakParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("break");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("break");
        tokens.consumeSemicolon();
        return new BreakASTNode(Breakpoint.Type.BREAK, null);
    }
}

