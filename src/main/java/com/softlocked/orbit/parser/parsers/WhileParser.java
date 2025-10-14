package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.loops.WhileASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class WhileParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("while");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("while");

        // Parse condition
        int startPos = tokens.getPosition();
        int bodyStart = -1;

        // Find where the body starts (either { or "do")
        for (int i = startPos; i < tokens.size(); i++) {
            String token = tokens.peek(i - startPos);
            if (token == null) break;
            if (token.equals("{") || token.equals("do")) {
                bodyStart = i;
                break;
            }
        }

        if (bodyStart == -1) {
            throw new RuntimeException("Expected 'do' or '{' after while condition");
        }

        // Parse the condition expression
        ExpressionParser exprParser = new ExpressionParser(tokens, context);
        ASTNode condition;
        try {
            condition = exprParser.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse while loop condition: " + e.getMessage());
        }

        // Consume the 'do' or '{'
        String bodyMarker = tokens.next();

        // Parse the body
        ASTNode body;
        if (bodyMarker.equals("{")) {
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in while loop");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
        } else {
            // Lua-style: while condition do ... end
            int depth = 1;
            int endPos = tokens.getPosition();

            while (endPos < tokens.size() && depth > 0) {
                String token = tokens.peek(endPos - tokens.getPosition());
                if (token == null) break;
                if (token.equals("while") || token.equals("for") || token.equals("if")) {
                    depth++;
                } else if (token.equals("end")) {
                    depth--;
                }
                if (depth > 0) endPos++;
            }

            if (depth != 0) {
                throw new RuntimeException("Unclosed 'while' statement - missing 'end'");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
            body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(endPos + 1);
        }

        return new WhileASTNode(condition, body);
    }
}

