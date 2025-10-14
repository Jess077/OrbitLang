package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.parsers.*;

import java.util.List;

public class Parser implements TokenParser {
    private static final List<TokenParser> PARSERS = List.of(
        // Control flow
        new ReturnParser(),
        new YieldParser(),
        new ThrowParser(),
        new BreakParser(),
        new ContinueParser(),

        // Imports and cleanup
        new ImportParser(),
        new DeleteParser(),

        // Loops and conditionals
        new WhileParser(),
        new ForParser(),
        new IfParser(),
        new TryCatchParser(),

        // Declarations
        new ClassDeclarationParser(),
        new EnumParser(),
        new FunctionDeclarationParser(),
        new VariableDeclarationParser(),

        // Assignments (should be after declarations)
        new VariableAssignmentParser(),

        // Expressions (fallback)
        new ExpressionTokenParser()
    );

    @Override
    public java.util.function.Predicate<String> predicate() {
        // The main parser accepts any token - it will delegate to sub-parsers
        return token -> true;
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        return parseStatement(tokens, context);
    }

    /**
     * Parse a single statement using the first matching parser
     */
    private static ASTNode parseStatement(TokenStream tokens, GlobalContext context) {
        if (!tokens.hasNext()) {
            return null;
        }

        String currentToken = tokens.peek();

        // Try each parser in order
        for (TokenParser parser : PARSERS) {
            if (parser.predicate().test(currentToken)) {
                ASTNode result = parser.parse(tokens, context);
                if (result != null) {
                    return result;
                }
                // If parser returned null, try next parser
            }
        }

        throw new RuntimeException("No parser found for token: " + currentToken);
    }

    /**
     * Parse a body of statements (multiple statements)
     */
    public static ASTNode parseBody(TokenStream tokens, GlobalContext context) {
        return parseBody(tokens, context, "");
    }

    /**
     * Parse a body of statements with optional class context
     */
    public static ASTNode parseBody(TokenStream tokens, GlobalContext context, String className) {
        BodyASTNode body = new BodyASTNode();

        while (tokens.hasNext()) {
            String token = tokens.peek();

            // Skip empty statements and delimiters
            if (token == null || token.equals(";") || token.equals("\n") ||
                token.equals("\r") || token.equals("\t")) {
                tokens.next();
                continue;
            }

            // Parse the next statement
            ASTNode statement = parseStatement(tokens, context);
            if (statement != null) {
                body.addNode(statement);
            }
        }

        return body;
    }

    /**
     * Main entry point for parsing a list of tokens
     */
    public static ASTNode parse(List<String> tokens, GlobalContext context) {
        TokenStream tokenStream = new TokenStream(tokens);
        return parseBody(tokenStream, context);
    }

    /**
     * Parse with class context
     */
    public static ASTNode parse(List<String> tokens, GlobalContext context, String className) {
        TokenStream tokenStream = new TokenStream(tokens);
        return parseBody(tokenStream, context, className);
    }
}
