package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.generic.TryCatchASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;

import java.util.function.Predicate;

public class TryCatchParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("try");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("try");

        String next = tokens.peek();
        if (next == null) {
            throw new RuntimeException("Unexpected end of file after 'try'");
        }

        boolean isCSyntax = next.equals("{");
        ASTNode tryBody;

        if (isCSyntax) {
            // C-style: try { ... } catch (e) { ... }
            tokens.next(); // consume '{'
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in try block");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            tryBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
        } else {
            // Lua-style: try ... catch ... end
            int depth = 1;
            int catchPos = tokens.getPosition();

            while (catchPos < tokens.size() && depth > 0) {
                String token = tokens.peek(catchPos - tokens.getPosition());
                if (token == null) break;

                if (token.equals("try")) {
                    depth++;
                } else if (token.equals("catch") && depth == 1) {
                    break;
                }
                catchPos++;
            }

            if (tokens.peek(catchPos - tokens.getPosition()) == null ||
                !tokens.peek(catchPos - tokens.getPosition()).equals("catch")) {
                throw new RuntimeException("Missing 'catch' block for 'try' statement");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), catchPos);
            tryBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(catchPos);
        }

        // Parse catch block
        tokens.expect("catch");

        // Parse exception variable name
        String exceptionName = "e"; // default
        if (tokens.peek() != null && tokens.peek().equals("(")) {
            tokens.next(); // consume '('
            String varName = tokens.next();
            if (varName == null || varName.equals(")")) {
                throw new RuntimeException("Invalid catch block - expected exception variable name");
            }
            exceptionName = varName;
            tokens.expect(")");
        }

        // Parse catch body
        ASTNode catchBody;
        String catchMarker = tokens.peek();

        if (isCSyntax) {
            if (catchMarker == null || !catchMarker.equals("{")) {
                throw new RuntimeException("Expected '{' after catch");
            }
            tokens.next(); // consume '{'
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in catch block");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            catchBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
        } else {
            // Lua-style: find 'end'
            // Skip optional 'do', 'does', or 'then'
            if (catchMarker != null && (catchMarker.equals("do") || catchMarker.equals("does") || catchMarker.equals("then"))) {
                tokens.next();
            }

            int depth = 1;
            int endPos = tokens.getPosition();

            while (endPos < tokens.size() && depth > 0) {
                String token = tokens.peek(endPos - tokens.getPosition());
                if (token == null) break;

                if (token.equals("try")) {
                    depth++;
                } else if (token.equals("end")) {
                    depth--;
                    if (depth == 0) break;
                }
                endPos++;
            }

            if (depth != 0) {
                throw new RuntimeException("Unclosed 'try' statement - missing 'end'");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
            catchBody = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(endPos + 1); // skip 'end'
        }

        return new TryCatchASTNode(tryBody, catchBody, exceptionName);
    }
}