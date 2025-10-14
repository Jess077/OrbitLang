package com.softlocked.orbit.interpreter.memory;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.AssignVarASTNode;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.libraries.*;
import com.softlocked.orbit.libraries.Math.Math_Library;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.list.CacheList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The global context used to store global variables, functions, and classes.
 * It is also used as the root to all local contexts.
 * @see LocalContext
 */
public class GlobalContext extends LocalContext {
    private final Map<Pair<String, Integer>, IFunction> functions = new TreeMap<>();
    private final Map<Pair<String, Integer>, Class<? extends BFunction>> bakedFunctions = new TreeMap<>();

    protected Int2ObjectOpenHashMap<IFunction> easyAccessFunctions = new Int2ObjectOpenHashMap<>();

    private static final HashMap<String, Class<?>> primitives = new HashMap<>();
    private final HashMap<String, OrbitClass> classes = new HashMap<>();

    private final HashSet<String> importedModules = new HashSet<>();
    private final HashSet<String> importedFiles = new HashSet<>();

    private static final List<OrbitJavaLibrary> builtInLibraries = new ArrayList<>();

    private final AtomicBoolean markedForDeletion = new AtomicBoolean(false);
    private boolean forceExit = false;

    public boolean isMarkedForDeletion() {
        return markedForDeletion.get();
    }

    public boolean isForcedExit() {
        return forceExit;
    }

    public void markForDeletion() {
        markedForDeletion.set(true);
    }

    private final HashMap<IFunction, CacheList<LocalContext>> functionContexts = new HashMap<>();

    public LocalContext getOrCreateFunctionContext(IFunction function) {
        CacheList<LocalContext> contexts = functionContexts.get(function);

        if(contexts == null) {
            contexts = new CacheList<>();
            functionContexts.put(function, contexts);
        }

        if(contexts.realSize() > contexts.size()) {
            return contexts.getNext();
        }
        LocalContext context = new LocalContext(this);
        contexts.addNext(context);
        return context;
    }

    public void freeFunctionContext(IFunction function) {
        CacheList<LocalContext> contexts = functionContexts.get(function);

        if(contexts != null) {
            contexts.move(-1);
        }
    }

    public void markForDeletion(boolean forceExit) {
        markedForDeletion.set(true);

        if(forceExit) {
            this.forceExit = true;
        }
    }

    static {
        primitives.put("int", int.class);
        primitives.put("float", float.class);
        primitives.put("double", double.class);
        primitives.put("long", long.class);
        primitives.put("short", short.class);
        primitives.put("byte", byte.class);
        primitives.put("char", char.class);
        primitives.put("bool", boolean.class);
        primitives.put("string", String.class);

        primitives.put("void", void.class);

        primitives.put("var", Object.class);
        primitives.put("let", Object.class);
        primitives.put("object", Object.class);

        primitives.put("array", Object[].class);

        primitives.put("list", List.class);
        primitives.put("map", Map.class);

        primitives.put("ref", Variable.class);
        primitives.put("reference", Variable.class);

        primitives.put("coroutine", Coroutine.class);
        primitives.put("consumer", Consumer.class);

        builtInLibraries.add(new Standard_Library());
        builtInLibraries.add(new Container_Library());
        builtInLibraries.add(new Time_Library());
        builtInLibraries.add(new Types_Library());
        builtInLibraries.add(new Math_Library());
        builtInLibraries.add(new Eval_Library());

        builtInLibraries.add(new Input_Library());

        builtInLibraries.add(new Coroutine_Library());
    }

    public static Class<?> getPrimitiveType(String name) {
        return primitives.get(name);
    }

    public OrbitClass getClassType(String name) {
        return classes.get(name);
    }

    @Override
    public IFunction getFunction(String name, int parameterCount) {
        IFunction func = functions.get(new Pair<>(name, parameterCount));

        if (func != null) {
            return func;
        }

        func = functions.get(new Pair<>(name, -1));

        return func;
    }

    @Override
    public void addFunction(IFunction function) {
        functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);

        easyAccessFunctions.put(
                easyAccessFunctions.size(), function
        );

        if(function instanceof NativeFunction nativeFunction) {
            Class<? extends BFunction> bakedFunction = nativeFunction.getBakedFunction();

            if(bakedFunction != null) {
                bakedFunctions.put(new Pair<>(function.getName(), function.getParameterCount()), bakedFunction);
            }
        }
    }

    public Class<? extends BFunction> getBakedFunction(String name, int parameterCount) {
        return bakedFunctions.get(new Pair<>(name, parameterCount));
    }

    public boolean hasBakedFunction(String name, int parameterCount) {
        return bakedFunctions.containsKey(new Pair<>(name, parameterCount));
    }

    public void removeFunction(String name, int parameterCount) {
        functions.remove(new Pair<>(name, parameterCount));
    }

    public void removeClass(String name) {
        classes.remove(name);
    }

    public void addClass(OrbitClass orbitClass) {
        if(classes.containsKey(orbitClass.name()))
            throw new RuntimeException("Class " + orbitClass.name() + " already defined");

        classes.put(orbitClass.name(), orbitClass);

        if(orbitClass.constructors().isEmpty()) {
            addFunction(
                    new NativeFunction(orbitClass.name(), 0, Variable.Type.CLASS) {
                        @Override
                        public Object call(ILocalContext context, Object[] args) {
                            try {
                                return new OrbitObject(orbitClass, null, context.getRoot());
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
            );
        } else {
            for (Map.Entry<Integer, ClassConstructor> entry : orbitClass.constructors().entrySet()) {
                functions.put(
                        new Pair<>(orbitClass.name(), entry.getKey()),
                        new NativeFunction(orbitClass.name(), entry.getKey(), Variable.Type.CLASS) {
                            @Override
                            public Object call(ILocalContext context, Object[] args) {
                                try {
                                    return new OrbitObject(orbitClass, List.of(args), context.getRoot());
                                } catch (InterruptedException e) {
                                    return null;
                                }
                            }
                        }
                );
            }
        }
    }

    public GlobalContext() {
        try {
            addClass(new OrbitClass(
                    "exception", // name
                    new ArrayList<>(), // superClasses
                    new HashMap<>(Map.of(
                            "message", new Pair<>(Variable.Type.STRING, null)
                    )), // fields
                    new HashMap<>(Map.of(
                            new Pair<>("getMessage", 0), new NativeFunction("getMessage", 0, Variable.Type.STRING) {
                                @Override
                                public Object call(ILocalContext context, Object[] args) {
                                    return context.getVariable("message".hashCode()).getValue();
                                }
                            }
                    )), // functions
                    new HashMap<>(Map.of(1, new ClassConstructor(
                            1,
                            List.of(new Pair<>("msg", Variable.Type.STRING)),
                            new AssignVarASTNode("message", "message".hashCode(), new VariableASTNode("msg", "msg".hashCode())
                    ))) // constructors
            )));
        } catch (Exception ignored) {}

        for(OrbitJavaLibrary library : builtInLibraries) {
            library.load(this);
        }
    }

    public void importFile(String path) throws IOException, ParsingException {
        byte[] file = Files.readAllBytes(Paths.get(path));
        String code = new String(file);

        List<String> tokens = new Lexer(code).tokenize();

        ASTNode ast = Parser.parse(tokens, this);

        importModule(ast, new File(path).getParent());
    }

    public void importModule(ASTNode ast, String path) {
//        if(ast instanceof ImportASTNode) {
//            if(ast instanceof ImportFileASTNode importFileASTNode) {
//                if(importedFiles.contains(path + File.separator + importFileASTNode.fileName())) {
//                    return;
//                }
//
//                importedFiles.add(path + File.separator + importFileASTNode.fileName());
//
//                importFileASTNode.importFile(this, path);
//            }
//            else if(ast instanceof ImportModuleASTNode importModuleASTNode) {
//                if(importedModules.contains(importModuleASTNode.moduleName())) {
//                    return;
//                }
//
//                importedModules.add(importModuleASTNode.moduleName());
//
//                importModuleASTNode.importFile(this, path);
//            }
//        }
//        else if(ast instanceof BodyASTNode body) {
//            for(ASTNode node : body.statements()) {
//                if(node instanceof ImportASTNode) {
//                    if(node instanceof ImportFileASTNode importFileASTNode) {
//                        if(importedFiles.contains(path + File.separator + importFileASTNode.fileName())) {
//                            return;
//                        }
//
//                        importedFiles.add(path + File.separator + importFileASTNode.fileName());
//
//                        importFileASTNode.importFile(this, path);
//                    }
//                    else if(node instanceof ImportModuleASTNode importModuleASTNode) {
//                        if(importedModules.contains(importModuleASTNode.moduleName())) {
//                            return;
//                        }
//
//                        importedModules.add(importModuleASTNode.moduleName());
//
//                        importModuleASTNode.importFile(this, path);
//                    }
//                }
//                else {
//                    node.evaluate(this);
//                }
//            }
//        } else {
//            ast.evaluate(this);
//        }
    }
}
