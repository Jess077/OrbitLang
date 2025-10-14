package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.statement.ConditionalASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class IfParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("if");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("if");

        // Parse condition
        ExpressionParser exprParser = new ExpressionParser(tokens, context);
        ASTNode condition;
        try {
            condition = exprParser.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse if condition: " + e.getMessage());
        }

        // Check for 'then' or '{'
        String bodyMarker = tokens.next();
        if (bodyMarker == null) {
            throw new RuntimeException("Expected 'then' or '{' after if condition");
        }

        if (!bodyMarker.equals("then") && !bodyMarker.equals("{")) {
            throw new RuntimeException("Expected 'then' or '{' after if condition, got: " + bodyMarker);
        }

        // Parse if body
        ASTNode ifBody;
        boolean isCSyntax = bodyMarker.equals("{");

        if (isCSyntax) {
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in if statement");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            ifBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
        } else {
            // Lua-style: find the matching else/end
            int depth = 1;
            int endPos = tokens.getPosition();

            while (endPos < tokens.size() && depth > 0) {
                String token = tokens.peek(endPos - tokens.getPosition());
                if (token == null) break;

                if (token.equals("if")) {
                    depth++;
                } else if (token.equals("end")) {
                    depth--;
                    if (depth == 0) break;
                } else if (token.equals("else") && depth == 1) {
                    break;
                }
                endPos++;
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
            ifBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(endPos);
        }

        // Parse else-if and else branches
        ASTNode elseBranch = null;

        if (tokens.peek() != null && tokens.peek().equals("else")) {
            tokens.next(); // consume 'else'

            if (tokens.peek() != null && tokens.peek().equals("if")) {
                // else if - recursively parse as a new if statement
                elseBranch = parse(tokens, context);
            } else {
                // else
                String elseMarker = tokens.peek();

                if (isCSyntax && elseMarker != null && elseMarker.equals("{")) {
                    tokens.next(); // consume '{'
                    int bodyEnd = tokens.findPair("{", "}");
                    if (bodyEnd == -1) {
                        throw new RuntimeException("Unclosed '{' in else statement");
                    }

                    TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
                    elseBranch = Parser.parseBody(bodyTokens, context);
                    tokens.setPosition(bodyEnd + 1);
                } else {
                    // Lua-style
                    int depth = 1;
                    int endPos = tokens.getPosition();

                    while (endPos < tokens.size() && depth > 0) {
                        String token = tokens.peek(endPos - tokens.getPosition());
                        if (token == null) break;

                        if (token.equals("if")) {
                            depth++;
                        } else if (token.equals("end")) {
                            depth--;
                            if (depth == 0) break;
                        }
                        endPos++;
                    }

                    TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
                    elseBranch = Parser.parseBody(bodyTokens, context);
                    tokens.setPosition(endPos);
                }
            }
        }

        // Consume 'end' if Lua-style
        if (!isCSyntax && tokens.peek() != null && tokens.peek().equals("end")) {
            tokens.next();
        }

        return new ConditionalASTNode(condition, ifBody, elseBranch);
    }
}