package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.ast.object.ClassDefinitionASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.AssignVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class ClassDeclarationParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s != null && (s.equals("class") || s.equals("record"));
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        boolean isRecord = tokens.peek().equals("record");
        tokens.next();

        String className = tokens.next();
        if (className == null) {
            throw new RuntimeException("Expected class name");
        }

        if (!className.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(className)) {
            throw new RuntimeException("Invalid class name: " + className);
        }

        List<String> superClasses = new ArrayList<>();
        HashMap<String, Pair<Variable.Type, ASTNode>> fields = new HashMap<>();
        HashMap<Pair<String, Integer>, IFunction> functions = new HashMap<>();
        HashMap<Integer, ClassConstructor> constructors = new HashMap<>();

        String next = tokens.peek();
        if (next == null) {
            throw new RuntimeException("Unexpected end of file after class name");
        }

        if (next.equals(":") || next.equals("extends")) {
            tokens.next();

            while (tokens.peek() != null && !tokens.peek().equals("{") && !tokens.peek().equals("(")) {
                String superClass = tokens.next();
                if (!superClass.equals(",")) {
                    superClasses.add(superClass);
                }
            }
            next = tokens.peek();
        }

        // Record with parameters
        if (isRecord && next != null && next.equals("(")) {
            parseRecordParameters(tokens, context, className, fields, functions, constructors);
            next = tokens.peek();
        }

        // Class/record body
        if (next != null && next.equals("{")) {
            tokens.next();

            int bodyEnd = tokens.findPair("{", "}");
            if (bodyEnd == -1) {
                throw new RuntimeException("Unclosed '{' in class body");
            }

            TokenStream bodyTokens = tokens.subStream(tokens.getPosition(), bodyEnd);
            ASTNode bodyNode = Parser.parseBody(bodyTokens, context, className);
            tokens.setPosition(bodyEnd + 1);

            extractClassMembers(bodyNode, fields, functions, constructors, isRecord);
        }

        return new ClassDefinitionASTNode(className, superClasses, fields, functions, constructors);
    }

    private void parseRecordParameters(TokenStream tokens, GlobalContext context, String className,
                                      HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                                      HashMap<Pair<String, Integer>, IFunction> functions,
                                      HashMap<Integer, ClassConstructor> constructors) {
        tokens.expect("(");

        List<Pair<String, Variable.Type>> parameters = new ArrayList<>();

        while (tokens.peek() != null && !tokens.peek().equals(")")) {
            String token = tokens.next();

            if (token.equals(",")) {
                continue;
            }

            Class<?> primitiveType = GlobalContext.getPrimitiveType(token);
            if (primitiveType != null) {
                String paramName = tokens.next();
                if (paramName == null || paramName.equals(",") || paramName.equals(")")) {
                    throw new RuntimeException("Expected parameter name after type");
                }
                Variable.Type type = Variable.Type.fromJavaClass(primitiveType);
                parameters.add(new Pair<>(paramName, type));
            } else {
                parameters.add(new Pair<>(token, Variable.Type.ANY));
            }

            if (tokens.peek() != null && tokens.peek().equals(",")) {
                tokens.next();
            }
        }

        tokens.expect(")");

        BodyASTNode constructorBody = new BodyASTNode();

        for (Pair<String, Variable.Type> param : parameters) {
            fields.put(param.first, new Pair<>(param.second, new ValueASTNode(Utils.newObject(param.second.getJavaClass()))));

            constructorBody.addNode(
                new AssignVarASTNode(
                    param.first,
                    param.first.hashCode(),
                    new VariableASTNode("_" + param.first, ("_" + param.first).hashCode())
                )
            );

            param.first = "_" + param.first;
        }

        constructors.put(parameters.size(), new ClassConstructor(parameters.size(), parameters, constructorBody));

        final List<Pair<String, Variable.Type>> finalParams = new ArrayList<>(parameters);
        functions.put(
            new Pair<>("cast", 1),
            new NativeFunction("cast", List.of(Variable.Type.STRING), Variable.Type.ANY) {
                @Override
                public Object call(ILocalContext localContext, List<Object> args) {
                    String type = (String) args.get(0);
                    if (type.equals("string")) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            sb.append(className).append("(");

                            for (int i = 0; i < finalParams.size(); i++) {
                                String name = finalParams.get(i).first.substring(1);
                                Object value = new VariableASTNode(name, name.hashCode()).evaluate(localContext);
                                value = Utils.cast(value, String.class);
                                sb.append(value);

                                if (i != finalParams.size() - 1) {
                                    sb.append(", ");
                                }
                            }

                            sb.append(")");
                            return sb.toString();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
            }
        );
    }

    private void extractClassMembers(ASTNode bodyNode,
                                     HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                                     HashMap<Pair<String, Integer>, IFunction> functions,
                                     HashMap<Integer, ClassConstructor> constructors,
                                     boolean isRecord) {
        if (bodyNode instanceof BodyASTNode body) {
            for (ASTNode node : body.statements()) {
                if (node instanceof DecVarASTNode decVar) {
                    fields.put(decVar.variableName(), new Pair<>(decVar.type(), decVar.value()));
                } else if (node instanceof ClassConstructor constructor) {
                    constructors.put(constructor.getParameterCount(), constructor);
                } else if (node instanceof OrbitFunction function) {
                    functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
                } else {
                    throw new RuntimeException((isRecord ? "Invalid record body" : "Invalid class body"));
                }
            }
        } else if (bodyNode instanceof DecVarASTNode decVar) {
            fields.put(decVar.variableName(), new Pair<>(decVar.type(), decVar.value()));
        } else if (bodyNode instanceof ClassConstructor constructor) {
            constructors.put(constructor.getParameterCount(), constructor);
        } else if (bodyNode instanceof OrbitFunction function) {
            functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
        }
    }
}