package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Utils;

import java.util.function.Predicate;

public class VariableDeclarationParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return token -> GlobalContext.getPrimitiveType(token) != null;
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        String typeToken = tokens.next();
        Class<?> primitiveType = GlobalContext.getPrimitiveType(typeToken);

        if (primitiveType == null) {
            throw new RuntimeException("Unknown type: " + typeToken);
        }

        Variable.Type varType = Variable.Type.fromJavaClass(primitiveType);

        // Get variable name
        String varName = tokens.next();
        if (varName == null) {
            throw new RuntimeException("Expected variable name after type");
        }

        if (!varName.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(varName)) {
            throw new RuntimeException("Invalid variable name: " + varName);
        }

        // Check what comes next
        String next = tokens.peek();

        if (next == null || next.equals(";")) {
            // Simple declaration without initialization
            tokens.consumeSemicolon();
            return new DecVarASTNode(
                    varName,
                    varName.hashCode(),
                    new ValueASTNode(Utils.newObject(primitiveType)),
                    varType
            );
        }

        if (next.equals("=") || next.equals("be")) {
            // Declaration with initialization
            tokens.next(); // consume '=' or 'be'

            try {
                ExpressionParser exprParser = new ExpressionParser(tokens, context);
                ASTNode initValue = exprParser.parse();
                tokens.consumeSemicolon();

                return new DecVarASTNode(
                        varName,
                        varName.hashCode(),
                        initValue,
                        varType
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse variable initialization: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Unexpected token after variable name: " + next);
    }
}
