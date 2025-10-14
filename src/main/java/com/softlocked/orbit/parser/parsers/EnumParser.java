package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class EnumParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("enum");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("enum");

        // Get enum name
        String enumName = tokens.next();
        if (enumName == null) {
            throw new RuntimeException("Expected enum name after 'enum'");
        }

        if (!enumName.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(enumName)) {
            throw new RuntimeException("Invalid enum name: " + enumName);
        }

        // Expect '{'
        tokens.expect("{");

        // Parse enum values
        Map<String, Integer> enumValuesMap = new HashMap<>();
        int index = 0;

        while (tokens.peek() != null && !tokens.peek().equals("}")) {
            String value = tokens.next();

            if (value.equals(",") || value.equals(";")) {
                continue;
            }

            if (value.equals("}")) {
                break;
            }

            if (!value.matches(Utils.IDENTIFIER_REGEX)) {
                throw new RuntimeException("Invalid enum value: " + value);
            }

            enumValuesMap.put(value, index++);

            // Handle optional comma
            if (tokens.peek() != null && tokens.peek().equals(",")) {
                tokens.next();
            }
        }

        tokens.expect("}");

        return new DecVarASTNode(
            enumName,
            enumName.hashCode(),
            new ValueASTNode(enumValuesMap),
            Variable.Type.MAP
        );
    }
}
