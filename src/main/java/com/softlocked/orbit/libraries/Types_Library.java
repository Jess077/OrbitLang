package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import java.util.Arrays;
import java.util.List;

public class Types_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // string type
        context.addFunction(new NativeFunction("string.length", List.of(Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).length();
            }
        });

        context.addFunction(new NativeFunction("string.concat", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return args[0] + ((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.equals", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return args[0].equals(args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.contains", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).contains((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.startsWith", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).startsWith((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.endsWith", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).endsWith((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.substring", List.of(Variable.Type.STRING, Variable.Type.INT, Variable.Type.INT), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).substring((int) args[1], (int) args[2]);
            }
        });

        context.addFunction(new NativeFunction("string.replace", List.of(Variable.Type.STRING, Variable.Type.STRING, Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).replace((String) args[1], (String) args[2]);
            }
        });

        context.addFunction(new NativeFunction("string.split", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return List.of(((String) args[0]).split((String) args[1]));
            }
        });

        context.addFunction(new NativeFunction("string.trim", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).trim();
            }
        });

        context.addFunction(new NativeFunction("string.toUpper", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).toUpperCase();
            }
        });

        context.addFunction(new NativeFunction("string.toLower", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).toLowerCase();
            }
        });

        context.addFunction(new NativeFunction("string.charAt", List.of(Variable.Type.STRING, Variable.Type.INT), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return String.valueOf(((String) args[0]).charAt((int) args[1]));
            }
        });

        context.addFunction(new NativeFunction("string.indexOf", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).indexOf((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.lastIndexOf", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).lastIndexOf((String) args[1]);
            }
        });

        context.addFunction(new NativeFunction("string.isEmpty", List.of(Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).isEmpty();
            }
        });

        context.addFunction(new NativeFunction("string.isBlank", List.of(Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).isBlank();
            }
        });

        context.addFunction(new NativeFunction("string.matches", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((String) args[0]).matches((String) args[1]);
            }
        });

        // ref type
        context.addFunction(new NativeFunction("ref.get", List.of(Variable.Type.REFERENCE), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return ((Variable) args[0]).getValue();
            }
        });

        context.addFunction(new NativeFunction("ref.set", List.of(Variable.Type.REFERENCE, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                ((Variable) args[0]).setValue(args[1]);
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.type", List.of(Variable.Type.REFERENCE), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Variable ref = (Variable) args[0];

                return Variable.Type.getTypeName(ref.getValue());
            }
        });

        context.addFunction(new NativeFunction("ref.pointer", List.of(Variable.Type.REFERENCE), Variable.Type.REFERENCE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Variable ref = (Variable) args[0];
                Object value = ref.getValue();

                while(value instanceof Variable) {
                    value = ((Variable) value).getValue();
                }

                return value;
            }
        });

        context.addFunction(new NativeFunction("ref.ref", List.of(Variable.Type.REFERENCE, Variable.Type.REFERENCE), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                ((Variable) args[0]).setValue(args[1]);
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.deref", List.of(Variable.Type.REFERENCE), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                ((Variable) args[0]).setValue(null);
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.identity", List.of(Variable.Type.REFERENCE), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Variable ref = (Variable) args[0];
                Object value = ref.getValue();
                while(value instanceof Variable) {
                    value = ((Variable) value).getValue();
                }

                return System.identityHashCode(value);
            }
        });

        context.addFunction(new NativeFunction("ref.hexIdentity", List.of(Variable.Type.REFERENCE), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Variable ref = (Variable) args[0];
                Object value = ref.getValue();
                while(value instanceof Variable) {
                    value = ((Variable) value).getValue();
                }

                return Integer.toHexString(System.identityHashCode(value));
            }
        });

        context.addFunction(new NativeFunction("consumer.accept", -1, Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                if(!(args[0] instanceof Consumer consumer))
                    throw new RuntimeException("Invalid consumer");

                try {
                    return consumer.accept(context, Arrays.copyOfRange(args, 1, args.length));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        context.addFunction(new NativeFunction("consumer", List.of(Variable.Type.CONSUMER), Variable.Type.CONSUMER) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return args[0];
            }
        });
    }
}
