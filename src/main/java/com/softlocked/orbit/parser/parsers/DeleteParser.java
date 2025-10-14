package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DeleteVarASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class DeleteParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("delete");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("delete");

        String varName = tokens.next();
        if (varName == null) {
            throw new RuntimeException("Unexpected end of file after delete");
        }

        // Remove quotes if present
        if (varName.startsWith("\"") && varName.endsWith("\"")) {
            varName = varName.substring(1, varName.length() - 1);
        }

        tokens.consumeSemicolon();
        return new DeleteVarASTNode(varName, varName.hashCode());
    }
}

