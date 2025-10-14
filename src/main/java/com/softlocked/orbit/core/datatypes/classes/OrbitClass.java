package com.softlocked.orbit.core.datatypes.classes;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Used for representing a class in the Orbit language
 *
 * @see Variable
 */
public record OrbitClass(String name, List<OrbitClass> superClasses,
                         HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                         HashMap<Pair<String, Integer>, IFunction> functions,
                         HashMap<Integer, ClassConstructor> constructors) {
    public OrbitClass(String name, List<OrbitClass> superClasses, HashMap<String, Pair<Variable.Type, ASTNode>> fields, HashMap<Pair<String, Integer>, IFunction> functions, HashMap<Integer, ClassConstructor> constructors) {
        this.name = name;
        this.superClasses = superClasses;
        this.fields = fields;
        this.functions = functions;
        this.constructors = constructors;

        // Add fields from super classes if they are not overridden
        if (superClasses != null) {
            for (OrbitClass superClass : superClasses) {
                if (superClass.fields() != null) {
                    for (String s : superClass.fields().keySet()) {
                        if (!this.fields.containsKey(s)) {
                            this.fields.put(s, superClass.fields().get(s));
                        }
                    }
                }
            }
        }

        // Add constructors from super classes if they are not overridden
        if (superClasses != null) {
            for (OrbitClass superClass : superClasses) {
                if (superClass.constructors() != null) {
                    for (Integer integer : superClass.constructors().keySet()) {
                        if (!this.constructors.containsKey(integer)) {
                            this.constructors.put(integer, superClass.constructors().get(integer));
                        }
                    }
                }
            }
        }
    }

    public boolean extendsClass(OrbitClass clazz) {
        if (this.equals(clazz)) {
            return true;
        }

        if (this.superClasses != null) {
            for (OrbitClass superClass : this.superClasses) {
                if (superClass.extendsClass(clazz)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrbitClass clazz) {
            return clazz.name().equals(this.name);
        }

        return false;
    }

    public OrbitObject createInstance(List<Object> args, GlobalContext rootContext) throws InterruptedException {
        return new OrbitObject(this, args, rootContext);
    }

    public static class ClassBuilder {
        private String name;
        private Set<String> superClasses;
        private HashMap<String, Pair<Variable.Type, ASTNode>> fields;
        private HashMap<Pair<String, Integer>, IFunction> functions;
        private HashMap<Integer, ClassConstructor> constructors;

        private final GlobalContext context;

        public ClassBuilder(GlobalContext context) {
            this.context = context;
        }

        public ClassBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ClassBuilder addSuperClass(String superClass) {
            if (superClasses == null) {
                superClasses = new HashSet<>();
            }

            superClasses.add(superClass);

            return this;
        }

        public ClassBuilder addField(String name, Variable.Type type, Object value) {
            if (fields == null) {
                fields = new HashMap<>();
            }

            fields.put(name, new Pair<>(type, new ValueASTNode(value)));

            return this;
        }

        public ClassBuilder addFunction(String name, int arity, BiConsumer<ILocalContext, Object[]> function) {
            if (functions == null) {
                functions = new HashMap<>();
            }

            IFunction func = new IFunction() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public int getParameterCount() {
                    return arity;
                }

                @Override
                public Pair<String, Variable.Type>[] getParameters() {
                    return null;
                }

                @Override
                public Variable.Type getReturnType() {
                    return Variable.Type.ANY;
                }

                @Override
                public boolean isNative() {
                    return true;
                }

                @Override
                public Object call(ILocalContext context, Object[] args) throws InterruptedException {
                    function.accept(context, args);
                    return null;
                }

                @Override
                public ASTNode getBody() {
                    return null;
                }

                int id;

                @Override
                public void setID(int id) {
                    this.id = id;
                }

                @Override
                public int getID() {
                    return id;
                }
            };

            functions.put(new Pair<>(name, arity), func);

            return this;
        }

        public ClassBuilder addConstructor(int arity, ClassConstructor constructor) {
            if (constructors == null) {
                constructors = new HashMap<>();
            }

            constructors.put(arity, constructor);

            return this;
        }

        public OrbitClass build() {
            List<OrbitClass> superClasses = new ArrayList<>();

            if (this.superClasses != null) {
                for (String superClass : this.superClasses) {
                    OrbitClass clazz = context.getClassType(superClass);

                    if (clazz == null) {
                        throw new IllegalArgumentException("Class " + superClass + " does not exist");
                    }

                    superClasses.add(clazz);
                }
            }

            return new OrbitClass(name, superClasses, fields, functions, constructors);
        }
    }
}
