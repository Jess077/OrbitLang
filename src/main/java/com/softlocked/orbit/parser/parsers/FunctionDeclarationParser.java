package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.function.coroutine.CoroutineFunction;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FunctionDeclarationParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return token -> {
            if (token == null) return false;
            if (token.equals("fun")) return true;

            Class<?> primitiveType = GlobalContext.getPrimitiveType(token);
            return primitiveType != null;
        };
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        String typeToken = tokens.next();

        Variable.Type returnType;
        if (typeToken.equals("fun")) {
            returnType = Variable.Type.ANY;
        } else {
            Class<?> primitiveType = GlobalContext.getPrimitiveType(typeToken);
            if (primitiveType != null) {
                returnType = Variable.Type.fromJavaClass(primitiveType);
            } else {
                returnType = Variable.Type.CLASS;
            }
        }

        // Get function name
        String funcName = tokens.next();
        if (funcName == null) {
            throw new RuntimeException("Expected function name");
        }

        if (!funcName.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(funcName)) {
            throw new RuntimeException("Invalid function name: " + funcName);
        }

        if (tokens.peek() == null || !tokens.peek().equals("(")) {
            // Not a function declaration, put tokens back
            tokens.setPosition(tokens.getPosition() - 2);
            return null;
        }

        // Parse parameters
        tokens.expect("(");
        List<Pair<String, Variable.Type>> parameters = parseParameters(tokens, context);
        tokens.expect(")");

        // Check for function body or just declaration
        String next = tokens.peek();
        if (next == null || next.equals(";")) {
            tokens.consumeSemicolon();
            if (typeToken.equals("coroutine")) {
                return new CoroutineFunction(funcName, parameters.size(), parameters, new BodyASTNode());
            } else {
                return new OrbitFunction(funcName, parameters.size(), parameters, new BodyASTNode(), returnType);
            }
        }

        // Parse body
        ASTNode body = parseFunctionBody(tokens, context);

        if (!(body instanceof BodyASTNode)) {
            body = new BodyASTNode(List.of(body));
        }

        if (typeToken.equals("coroutine")) {
            return new CoroutineFunction(funcName, parameters.size(), parameters, body);
        } else {
            return new OrbitFunction(funcName, parameters.size(), parameters, body, returnType);
        }
    }

    private List<Pair<String, Variable.Type>> parseParameters(TokenStream tokens, GlobalContext context) {
        List<Pair<String, Variable.Type>> parameters = new ArrayList<>();

        while (tokens.peek() != null && !tokens.peek().equals(")")) {
            String token = tokens.next();

            if (token.equals(",")) {
                continue;
            }

            // Check if it's a type declaration
            Class<?> primitiveType = GlobalContext.getPrimitiveType(token);
            if (primitiveType != null) {
                // Typed parameter: int x
                String paramName = tokens.next();
                if (paramName == null || paramName.equals(",") || paramName.equals(")")) {
                    throw new RuntimeException("Expected parameter name after type");
                }
                Variable.Type type = Variable.Type.fromJavaClass(primitiveType);
                parameters.add(new Pair<>(paramName, type));
            } else {
                // Untyped parameter: x
                parameters.add(new Pair<>(token, Variable.Type.ANY));
            }

            // Handle comma
            if (tokens.peek() != null && tokens.peek().equals(",")) {
                tokens.next();
            }
        }

        return parameters;
    }

    private ASTNode parseFunctionBody(TokenStream tokens, GlobalContext context) {
        String bodyMarker = tokens.next();
        if (bodyMarker == null) {
            throw new RuntimeException("Expected function body");
        }

        if (bodyMarker.equals("{")) {
            // C-style body
            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in function body");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            ASTNode body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(bodyEnd + 1);
            return body;
        } else if (bodyMarker.equals("does") || bodyMarker.equals("do")) {
            // Lua-style body
            int depth = 1;
            int endPos = tokens.getPosition();

            while (endPos < tokens.size() && depth > 0) {
                String token = tokens.peek(endPos - tokens.getPosition());
                if (token == null) break;

                if (token.equals("func") || token.equals("coroutine") ||
                        token.equals("if") || token.equals("while") || token.equals("for")) {
                    depth++;
                } else if (token.equals("end")) {
                    depth--;
                    if (depth == 0) break;
                }
                endPos++;
            }

            if (depth != 0) {
                throw new RuntimeException("Unclosed function body - missing 'end'");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), endPos);
            ASTNode body = Parser.parseBody(bodyTokens, context);
            tokens.setPosition(endPos + 1); // skip 'end'
            return body;
        } else {
            throw new RuntimeException("Expected '{' or 'does' after function declaration, got: " + bodyMarker);
        }
    }
}

