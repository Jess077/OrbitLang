package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.operation.ReferenceASTNode;
import com.softlocked.orbit.interpreter.ast.operation.TernaryASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionAccessASTNode;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pratt parser for expressions. Directly builds an AST from tokens using
 * operator precedence climbing.
 */
public class ExpressionParser {
    private final TokenStream stream;
    private final GlobalContext context;

    public ExpressionParser(TokenStream stream, GlobalContext context) {
        this.stream = stream;
        this.context = context;
    }

    /**
     * Parse an expression with a minimum precedence level.
     * This is the main entry point for expression parsing.
     */
    public ASTNode parse() throws ParsingException, InterruptedException {
        return parse(0);
    }

    /**
     * Parse an expression with the given minimum precedence.
     */
    public ASTNode parse(int minPrecedence) throws ParsingException, InterruptedException {
        // Parse prefix (left side)
        ASTNode left = parsePrefix();

        // Parse infix operators while their precedence is high enough
        while (stream.hasNext() && !isExpressionTerminator(stream.peek())) {
            String operator = stream.peek();
            int precedence = getPrecedence(operator);

            if (precedence < minPrecedence) {
                break;
            }

            left = parseInfix(left, operator, precedence);
        }

        return left;
    }

    /**
     * Parse prefix expressions (literals, variables, unary operators, parentheses, etc.)
     */
    private ASTNode parsePrefix() throws ParsingException, InterruptedException {
        String token = stream.next();

        if (token == null) {
            throw new ParsingException("Unexpected end of expression");
        }

        // Numeric literals
        if (Lexer.isNumeric(token)) {
            return parseNumericLiteral(token);
        }

        // String literals
        if (token.startsWith("\"")) {
            return new ValueASTNode(token.substring(1, token.length() - 1));
        }

        // Character literals
        if (token.startsWith("'")) {
            return new ValueASTNode(token.charAt(1));
        }

        // Boolean literals
        if (token.equals("true") || token.equals("false")) {
            return new ValueASTNode(Boolean.parseBoolean(token));
        }

        // Null literal
        if (token.equals("null")) {
            return new ValueASTNode(null);
        }

        // Unary prefix operators: !, ~, @, +, -
        if (token.equals("!") || token.equals("~") || token.equals("@")) {
            ASTNode operand = parse(getPrecedence(token));
            return new OperationASTNode(operand, null, OperationType.fromSymbol(token));
        }

        if (token.equals("+") || token.equals("-")) {
            // Unary + or -
            ASTNode operand = parse(getPrecedence("@"));
            return new OperationASTNode(new ValueASTNode(0), operand, OperationType.fromSymbol(token));
        }

        // Parentheses - could be grouping or lambda
        if (token.equals("(")) {
            return parseParentheses();
        }

        // Array literals
        if (token.equals("[")) {
            return parseArrayLiteral();
        }

        // Map literals
        if (token.equals("{")) {
            return parseMapLiteral();
        }

        // Identifiers - could be variable, or single-token lambda
        if (token.matches(Utils.IDENTIFIER_REGEX)) {
            // Check for single-token lambda: x -> { ... }
            if (stream.hasNext() && (stream.peek().equals("->") || stream.peek().equals("=>"))) {
                return parseSingleParamLambda(token);
            }
            return new VariableASTNode(token, token.hashCode());
        }

        throw new ParsingException("Unexpected token in expression: " + token);
    }

    /**
     * Parse infix expressions (binary operators, function calls, array access, ternary)
     */
    private ASTNode parseInfix(ASTNode left, String operator, int precedence) throws ParsingException, InterruptedException {
        // Function call: identifier(args)
        if (operator.equals("(") && left instanceof VariableASTNode) {
            return parseFunctionCall(left);
        }

        // Array/collection access: expr[index]
        if (operator.equals("[")) {
            return parseArrayAccess(left);
        }

        // Ternary operator: condition ? trueExpr : falseExpr
        if (operator.equals("?")) {
            return parseTernary(left);
        }

        // Binary operators
        OperationType opType = OperationType.fromSymbol(operator);
        if (opType != null) {
            stream.next(); // consume operator

            // Right-associative operators (like **)
            int nextPrecedence = isRightAssociative(operator) ? precedence : precedence + 1;
            ASTNode right = parse(nextPrecedence);

            if (opType == OperationType.REF) {
                if (right instanceof ValueASTNode) {
                    throw new ParsingException("Invalid reference");
                }
                return new ReferenceASTNode(left, right);
            }

            ASTNode result = new OperationASTNode(left, right, opType);

            // Constant folding optimization
            if (left instanceof ValueASTNode && right instanceof ValueASTNode) {
                try {
                    return new ValueASTNode(result.evaluate(context));
                } catch (Exception e) {
                    return result;
                }
            }

            return result;
        }

        throw new ParsingException("Unknown operator: " + operator);
    }

    /**
     * Parse parentheses - could be grouping or lambda expression
     */
    private ASTNode parseParentheses() throws ParsingException, InterruptedException {
        int startPos = stream.getPosition() - 1; // We already consumed the '('

        // Save position to check for lambda after parsing
        List<String> params = new ArrayList<>();
        boolean isLambda = false;

        // Try to parse as potential lambda parameters or expression
        if (stream.peek() != null && stream.peek().equals(")")) {
            // Empty parentheses - could be lambda with no params
            stream.next(); // consume ')'
            if (stream.hasNext() && (stream.peek().equals("->") || stream.peek().equals("=>"))) {
                isLambda = true;
            } else {
                throw new ParsingException("Empty parentheses in expression");
            }
        } else {
            // Parse contents
            int depth = 1;
            int contentStart = stream.getPosition();

            // First, scan to find matching ) and check if it's followed by -> or =>
            while (stream.hasNext() && depth > 0) {
                String token = stream.next();
                if (token.equals("(")) depth++;
                else if (token.equals(")")) depth--;
            }

            if (depth != 0) {
                throw new ParsingException("Unmatched parenthesis");
            }

            int contentEnd = stream.getPosition() - 1; // Position of ')'

            // Check if it's a lambda
            if (stream.hasNext() && (stream.peek().equals("->") || stream.peek().equals("=>"))) {
                isLambda = true;
                // Reset and parse as parameter list
                stream.setPosition(contentStart);

                while (stream.getPosition() < contentEnd) {
                    String param = stream.next();
                    if (param.equals(",")) continue;
                    if (!param.matches(Utils.IDENTIFIER_REGEX)) {
                        throw new ParsingException("Invalid lambda parameter: " + param);
                    }
                    params.add(param);
                }
                stream.next(); // consume ')'
            } else {
                // It's a grouped expression, reparse it
                stream.setPosition(contentStart);
                ASTNode expr = parse(0);

                if (!stream.peek().equals(")")) {
                    throw new ParsingException("Expected ')' after expression");
                }
                stream.next(); // consume ')'
                return expr;
            }
        }

        if (isLambda) {
            return parseLambdaBody(params);
        }

        throw new ParsingException("Invalid parenthesized expression");
    }

    /**
     * Parse lambda body after parameters: -> { ... } or => { ... }
     */
    private ASTNode parseLambdaBody(List<String> paramNames) throws ParsingException, InterruptedException {
        String arrow = stream.next(); // -> or =>
        if (!arrow.equals("->") && !arrow.equals("=>")) {
            throw new ParsingException("Expected '->' or '=>' in lambda expression");
        }

        if (!stream.peek().equals("{")) {
            throw new ParsingException("Expected '{' after lambda arrow");
        }

        stream.next(); // consume '{'
        int startPos = stream.getPosition();
        int closePos = stream.findPair("{", "}");

        if (closePos == -1) {
            throw new ParsingException("Unmatched '{' in lambda expression");
        }

        List<String> bodyTokens = stream.getRange(startPos, closePos);
        stream.setPosition(closePos + 1); // move past '}'

        // Parse the body using the old parser for now
        ASTNode body = Parser.parse(bodyTokens, context);

        List<Pair<String, Variable.Type>> args = new ArrayList<>();
        for (String param : paramNames) {
            args.add(new Pair<>(param, Variable.Type.ANY));
        }

        return new OrbitFunction(null, args.size(), args, body, Variable.Type.ANY);
    }

    /**
     * Parse single-parameter lambda: x -> { ... }
     */
    private ASTNode parseSingleParamLambda(String paramName) throws ParsingException, InterruptedException {
        List<String> params = new ArrayList<>();
        params.add(paramName);
        return parseLambdaBody(params);
    }

    /**
     * Parse function call: func(arg1, arg2, ...)
     */
    private ASTNode parseFunctionCall(ASTNode funcNode) throws ParsingException, InterruptedException {
        String funcName = ((VariableASTNode) funcNode).name();

        stream.next(); // consume '('

        List<ASTNode> arguments = new ArrayList<>();

        if (!stream.peek().equals(")")) {
            while (true) {
                arguments.add(parse(0));

                if (stream.peek().equals(",")) {
                    stream.next(); // consume ','
                    continue;
                }
                break;
            }
        }

        if (!stream.peek().equals(")")) {
            throw new ParsingException("Expected ')' after function arguments");
        }
        stream.next(); // consume ')'

        // Check for baked functions
        if (context.hasBakedFunction(funcName, arguments.size())) {
            Class<? extends BFunction> functionClass = context.getBakedFunction(funcName, arguments.size());
            try {
                BFunction instance = functionClass.getConstructor().newInstance();
                instance.setValues(arguments);
                return instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new ParsingException("Failed to instantiate baked function: " + funcName);
            }
        }

        return new FunctionCallASTNode(funcName, arguments);
    }

    /**
     * Parse array access: arr[index] or arr[i][j]...
     */
    private ASTNode parseArrayAccess(ASTNode array) throws ParsingException, InterruptedException {
        List<ASTNode> indices = new ArrayList<>();

        while (stream.hasNext() && stream.peek().equals("[")) {
            stream.next(); // consume '['

            ASTNode index = parse(0);
            indices.add(index);

            if (!stream.peek().equals("]")) {
                throw new ParsingException("Expected ']' after array index");
            }
            stream.next(); // consume ']'
        }

        return new CollectionAccessASTNode(array, indices);
    }

    /**
     * Parse ternary operator: condition ? trueExpr : falseExpr
     */
    private ASTNode parseTernary(ASTNode condition) throws ParsingException, InterruptedException {
        stream.next(); // consume '?'

        ASTNode trueExpr = parse(0);

        if (!stream.peek().equals(":")) {
            throw new ParsingException("Expected ':' in ternary expression");
        }
        stream.next(); // consume ':'

        ASTNode falseExpr = parse(0);

        // Constant folding
        if (condition instanceof ValueASTNode) {
            Object condValue = ((ValueASTNode) condition).value();
            boolean isTrue = condValue instanceof Boolean ? (Boolean) condValue : condValue != null;
            return isTrue ? trueExpr : falseExpr;
        }

        return new TernaryASTNode(condition, trueExpr, falseExpr);
    }

    /**
     * Parse array literal: [elem1, elem2, ...]
     */
    private ASTNode parseArrayLiteral() throws ParsingException, InterruptedException {
        List<ASTNode> elements = new ArrayList<>();

        if (!stream.peek().equals("]")) {
            while (true) {
                elements.add(parse(0));

                if (stream.peek().equals(",")) {
                    stream.next(); // consume ','
                    continue;
                }
                break;
            }
        }

        if (!stream.peek().equals("]")) {
            throw new ParsingException("Expected ']' after array elements");
        }
        stream.next(); // consume ']'

        return new FunctionCallASTNode("list.of", elements);
    }

    /**
     * Parse map literal: {key: value, key2: value2, ...}
     */
    private ASTNode parseMapLiteral() throws ParsingException, InterruptedException {
        List<ASTNode> pairs = new ArrayList<>();

        if (!stream.peek().equals("}")) {
            while (true) {
                // Parse key
                ASTNode key = parse(0);

                // Normalize key to string if it's an identifier
                if (key instanceof VariableASTNode) {
                    String keyName = ((VariableASTNode) key).name();
                    key = new ValueASTNode(keyName);
                } else if (key instanceof ValueASTNode) {
                    Object keyValue = ((ValueASTNode) key).value();
                    if (keyValue instanceof Character) {
                        key = new ValueASTNode(String.valueOf(keyValue));
                    }
                }

                // Expect : or =
                String separator = stream.peek();
                if (!separator.equals(":") && !separator.equals("=")) {
                    throw new ParsingException("Expected ':' or '=' in map literal");
                }
                stream.next(); // consume separator

                // Parse value
                ASTNode value = parse(0);

                pairs.add(key);
                pairs.add(value);

                if (stream.peek().equals(",")) {
                    stream.next(); // consume ','
                    continue;
                }
                break;
            }
        }

        if (!stream.peek().equals("}")) {
            throw new ParsingException("Expected '}' after map elements");
        }
        stream.next(); // consume '}'

        return new FunctionCallASTNode("map.of", pairs);
    }

    /**
     * Parse numeric literal (handles hex, binary, and various numeric types)
     */
    private ASTNode parseNumericLiteral(String token) {
        if (token.startsWith("0x")) {
            return new ValueASTNode(Integer.parseInt(token.substring(2), 16));
        } else if (token.startsWith("0b")) {
            return new ValueASTNode(Integer.parseInt(token.substring(2), 2));
        } else {
            // Try parsing as different numeric types
            Class<?>[] numericTypes = {Integer.class, Long.class, Float.class, Double.class};

            for (Class<?> numericType : numericTypes) {
                try {
                    if (numericType == Integer.class) {
                        return new ValueASTNode(Integer.parseInt(token));
                    } else if (numericType == Long.class) {
                        return new ValueASTNode(Long.parseLong(token));
                    } else if (numericType == Float.class) {
                        return new ValueASTNode(Float.parseFloat(token));
                    } else if (numericType == Double.class) {
                        return new ValueASTNode(Double.parseDouble(token));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        throw new RuntimeException("Invalid numeric literal: " + token);
    }

    /**
     * Get operator precedence (higher = binds tighter)
     */
    private int getPrecedence(String operator) {
        return switch (operator) {
            case "||" -> 1;
            case "&&" -> 2;
            case "|" -> 3;
            case "^" -> 4;
            case "&" -> 5;
            case "==", "!=" -> 6;
            case "<", "<=", ">", ">=" -> 7;
            case "<<", ">>" -> 8;
            case "+", "-" -> 9;
            case "*", "/", "%" -> 10;
            case "**" -> 11;
            case ":", "@" -> 12;
            case "!", "~" -> 13; // Unary operators
            case "?", "->", "=>" -> 0; // Ternary and lambda have lowest precedence
            case "(", "[" -> 14; // Function calls and array access bind tightest
            default -> -1;
        };
    }

    /**
     * Check if operator is right-associative
     */
    private boolean isRightAssociative(String operator) {
        return operator.equals("**");
    }

    /**
     * Check if token terminates an expression
     */
    private boolean isExpressionTerminator(String token) {
        if (token == null) return true;

        return switch (token) {
            case ";", "\n", ")", "]", "}", ",", ":", "then", "do", "does", "end", "else" -> true;
            default -> false;
        };
    }
}
