package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Utils;

import java.util.function.Predicate;

public class ExpressionTokenParser implements TokenParser {

    @Override
    public Predicate<String> predicate() {
        return token -> {
            if (token == null) return false;

            // Literals
            if (Lexer.isNumeric(token)) return true;
            if (token.startsWith("\"")) return true;
            if (token.startsWith("'")) return true;
            if (token.equals("true") || token.equals("false")) return true;
            if (token.equals("null")) return true;

            // Identifiers (variables, function names)
            if (token.matches(Utils.IDENTIFIER_REGEX)) return true;

            // Prefix operators and grouping
            if (token.equals("(")) return true;
            if (token.equals("[")) return true;
            if (token.equals("{")) return true;
            if (token.equals("!")) return true;
            if (token.equals("~")) return true;
            if (token.equals("@")) return true;
            if (token.equals("+")) return true;
            if (token.equals("-")) return true;

            return false;
        };
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        try {
            ExpressionParser parser = new ExpressionParser(tokens, context);
            return parser.parse();
        } catch (ParsingException e) {
            throw new RuntimeException("Expression parsing failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Expression parsing interrupted", e);
        }
    }
}
