package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForDowntoASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForInASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForToASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class ForParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("for");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("for");

        // Check if starts with parenthesis (C-style)
        boolean hasParens = tokens.peek() != null && tokens.peek().equals("(");
        if (hasParens) {
            tokens.next(); // consume '('
        }

        // Save position to analyze the loop type
        int startPos = tokens.getPosition();

        // Look ahead to determine loop type
        boolean hasTo = false;
        boolean hasDownto = false;
        boolean hasIn = false;
        int semicolonCount = 0;
        int depth = hasParens ? 1 : 0;

        for (int i = 0; i < 100 && tokens.peek(i) != null; i++) {
            String token = tokens.peek(i);
            if (hasParens) {
                if (token.equals("(")) depth++;
                if (token.equals(")")) {
                    depth--;
                    if (depth == 0) break;
                }
            } else {
                if (token.equals("{") || token.equals("do")) break;
            }

            if (token.equals(";")) semicolonCount++;
            if (token.equals("to")) hasTo = true;
            if (token.equals("downto")) hasDownto = true;
            if (token.equals("in")) hasIn = true;
        }

        // Parse based on loop type
        if (hasTo) {
            return parseForTo(tokens, context, hasParens);
        } else if (hasDownto) {
            return parseForDownto(tokens, context, hasParens);
        } else if (hasIn) {
            return parseForIn(tokens, context, hasParens);
        } else if (semicolonCount >= 2) {
            throw new RuntimeException("C-style loops unsupported");
        } else {
            throw new RuntimeException("Invalid for loop syntax");
        }
    }

    private ASTNode parseForTo(TokenStream tokens, GlobalContext context, boolean hasParens) {
        // Parse: for i = start to end
        String varName = tokens.next();
        if (varName == null) {
            throw new RuntimeException("Expected variable name in for loop");
        }

        ASTNode initNode;

        // Check if there's an assignment
        if (tokens.peek() != null && tokens.peek().equals("=")) {
            tokens.next(); // consume '='
            ExpressionParser exprParser = new ExpressionParser(tokens, context);
            ASTNode initValue;
            try {
                initValue = exprParser.parse();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse initial value in for-to loop: " + e.getMessage(), e);
            }
            initNode = new DecVarASTNode(varName, varName.hashCode(), initValue, Variable.Type.LONG);
        } else {
            initNode = new DecVarASTNode(varName, varName.hashCode(), new ValueASTNode(0), Variable.Type.LONG);
        }

        // Expect 'to'
        tokens.expect("to");

        // Parse end condition
        ExpressionParser exprParser = new ExpressionParser(tokens, context);
        ASTNode endCondition;
        try {
            endCondition = exprParser.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse end condition in for-to loop: " + e.getMessage(), e);
        }

        if (hasParens) {
            tokens.expect(")");
        }

        // Parse body
        ASTNode body = parseLoopBody(tokens, context);

        return new ForToASTNode(initNode, endCondition, body);
    }

    private ASTNode parseForDownto(TokenStream tokens, GlobalContext context, boolean hasParens) {
        // Parse: for i = start downto end
        String varName = tokens.next();
        if (varName == null) {
            throw new RuntimeException("Expected variable name in for loop");
        }

        ASTNode initNode;

        // Check if there's an assignment
        if (tokens.peek() != null && tokens.peek().equals("=")) {
            tokens.next(); // consume '='
            ExpressionParser exprParser = new ExpressionParser(tokens, context);
            ASTNode initValue;
            try {
                initValue = exprParser.parse();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse initial value in for-downto loop: " + e.getMessage(), e);
            }
            initNode = new DecVarASTNode(varName, varName.hashCode(), initValue, Variable.Type.INT);
        } else {
            initNode = new DecVarASTNode(varName, varName.hashCode(), new ValueASTNode(0), Variable.Type.INT);
        }

        // Expect 'downto'
        tokens.expect("downto");

        // Parse end condition
        ExpressionParser exprParser = new ExpressionParser(tokens, context);
        ASTNode endCondition;
        try {
            endCondition = exprParser.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse end condition in for-downto loop: " + e.getMessage(), e);
        }

        if (hasParens) {
            tokens.expect(")");
        }

        // Parse body
        ASTNode body = parseLoopBody(tokens, context);

        return new ForDowntoASTNode(initNode, endCondition, body);
    }

    private ASTNode parseForIn(TokenStream tokens, GlobalContext context, boolean hasParens) {
        // Parse: for item in collection
        String varName = tokens.next();
        if (varName == null) {
            throw new RuntimeException("Expected variable name in for loop");
        }

        ASTNode initNode = new DecVarASTNode(varName, varName.hashCode(), new ValueASTNode(null), Variable.Type.ANY);

        // Expect 'in'
        tokens.expect("in");

        // Parse collection expression
        ExpressionParser exprParser = new ExpressionParser(tokens, context);
        ASTNode collection;
        try {
            collection = exprParser.parse();
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to parse collection in for-in loop: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parsing interrupted", e);
        }

        if (hasParens) {
            tokens.expect(")");
        }

        // Parse body
        ASTNode body = parseLoopBody(tokens, context);

        return new ForInASTNode(initNode, collection, body);
    }

    private ASTNode parseLoopBody(TokenStream tokens, GlobalContext context) {
        String bodyMarker = tokens.next();
        if (bodyMarker == null) {
            throw new RuntimeException("Expected 'do' or '{' after for condition");
        }

        if (bodyMarker.equals("{")) {
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in for loop");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            ASTNode body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
            return body;
        } else if (bodyMarker.equals("do")) {
            // Lua-style
            int depth = 1;
            int endPos = tokens.getPosition();

            while (endPos < tokens.size() && depth > 0) {
                String token = tokens.peek(endPos - tokens.getPosition());
                if (token == null) break;
                if (token.equals("for") || token.equals("while") || token.equals("if")) {
                    depth++;
                } else if (token.equals("end")) {
                    depth--;
                }
                if (depth > 0) endPos++;
            }

            if (depth != 0) {
                throw new RuntimeException("Unclosed 'for' statement - missing 'end'");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
            ASTNode body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(endPos + 1); // skip 'end'
            return body;
        } else {
            throw new RuntimeException("Expected 'do' or '{' after for condition, got: " + bodyMarker);
        }
    }
}

