package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.AssignVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionAccessASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionSetASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.ExpressionParser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VariableAssignmentParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return token -> token != null && token.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(token);
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        String varName = tokens.next();
        String next = tokens.peek();

        if (next == null) {
            throw new RuntimeException("Unexpected end of file after identifier");
        }

        // Handle array/collection access and assignment: arr[i] = value
        if (next.equals("[")) {
            return parseCollectionAssignment(tokens, context, varName);
        }

        // Handle regular assignment operators
        if (isAssignmentOperator(next)) {
            return parseSimpleAssignment(tokens, context, varName, next);
        }

        // If it's not an assignment, it might be an expression starting with identifier
        // Put the token back and let expression parser handle it
        tokens.setPosition(tokens.getPosition() - 1);
        return null;
    }

    private boolean isAssignmentOperator(String token) {
        return token.equals("=") || token.equals("+=") || token.equals("-=") ||
               token.equals("*=") || token.equals("/=") || token.equals("%=") ||
               token.equals("++") || token.equals("--");
    }

    private ASTNode parseSimpleAssignment(TokenStream tokens, GlobalContext context, String varName, String operator) {
        tokens.next(); // consume operator

        ASTNode value;

        try {
            switch (operator) {
                case "++" -> {
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        new ValueASTNode(1),
                        OperationType.ADD
                    );
                }
                case "--" -> {
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        new ValueASTNode(1),
                        OperationType.SUBTRACT
                    );
                }
                case "=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = exprParser.parse();
                }
                case "+=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        exprParser.parse(),
                        OperationType.ADD
                    );
                }
                case "-=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        exprParser.parse(),
                        OperationType.SUBTRACT
                    );
                }
                case "*=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        exprParser.parse(),
                        OperationType.MULTIPLY
                    );
                }
                case "/=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        exprParser.parse(),
                        OperationType.DIVIDE
                    );
                }
                case "%=" -> {
                    ExpressionParser exprParser = new ExpressionParser(tokens, context);
                    value = new OperationASTNode(
                        new VariableASTNode(varName, varName.hashCode()),
                        exprParser.parse(),
                        OperationType.MODULO
                    );
                }
                default -> throw new RuntimeException("Unknown assignment operator: " + operator);
            }

            tokens.consumeSemicolon();
            return new AssignVarASTNode(varName, varName.hashCode(), value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse assignment: " + e.getMessage(), e);
        }
    }

    private ASTNode parseCollectionAssignment(TokenStream tokens, GlobalContext context, String varName) {
        List<ASTNode> indices = new ArrayList<>();

        try {
            // Parse all bracket indices: arr[i][j][k]
            while (tokens.peek() != null && tokens.peek().equals("[")) {
                tokens.next(); // consume '['

                ExpressionParser exprParser = new ExpressionParser(tokens, context);
                ASTNode index = exprParser.parse();
                indices.add(index);

                tokens.expect("]");
            }

            // Now expect an assignment operator
            String operator = tokens.next();
            if (operator == null) {
                throw new RuntimeException("Expected assignment operator after array index");
            }

            VariableASTNode arrayVar = new VariableASTNode(varName, varName.hashCode());
            CollectionAccessASTNode accessNode = new CollectionAccessASTNode(arrayVar, indices);

            ASTNode value;
            ExpressionParser exprParser = new ExpressionParser(tokens, context);

            switch (operator) {
                case "=" -> {
                    value = exprParser.parse();
                }
                case "+=" -> {
                    value = new OperationASTNode(accessNode, exprParser.parse(), OperationType.ADD);
                }
                case "-=" -> {
                    value = new OperationASTNode(accessNode, exprParser.parse(), OperationType.SUBTRACT);
                }
                case "*=" -> {
                    value = new OperationASTNode(accessNode, exprParser.parse(), OperationType.MULTIPLY);
                }
                case "/=" -> {
                    value = new OperationASTNode(accessNode, exprParser.parse(), OperationType.DIVIDE);
                }
                case "%=" -> {
                    value = new OperationASTNode(accessNode, exprParser.parse(), OperationType.MODULO);
                }
                default -> throw new RuntimeException("Invalid assignment operator: " + operator);
            }

            tokens.consumeSemicolon();
            return new CollectionSetASTNode(arrayVar, indices, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse collection assignment: " + e.getMessage(), e);
        }
    }
}
