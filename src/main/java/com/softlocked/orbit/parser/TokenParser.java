package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;

import java.util.function.Predicate;

public interface TokenParser {
    Predicate<String> predicate();
    ASTNode parse(TokenStream tokens, GlobalContext context);
}
