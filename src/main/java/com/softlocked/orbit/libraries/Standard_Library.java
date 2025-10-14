package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.JarLoader;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Standard_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // print
        context.addFunction(new NativeFunction("print", -1, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                StringJoiner joiner = new StringJoiner(" ");

                try {
                    for (Object arg : args) {
                        joiner.add(Utils.cast(arg, String.class) + "");
                    }
                } catch (Exception e) {
                    throw new RuntimeException();
                }

                System.out.println(joiner);

                return null;
            }
        });

        // printn (print without newline)
        context.addFunction(new NativeFunction("printn", -1, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                StringJoiner joiner = new StringJoiner(" ");

                try {
                    for (Object arg : args) {
                        joiner.add(Utils.cast(arg, String.class) + "");
                    }
                } catch (Exception e) {
                    throw new RuntimeException();
                }

                System.out.print(joiner);

                return null;
            }
        });

        // typeof
        context.addFunction(new NativeFunction("typeof", 1, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Object obj = args[0];

                if(obj == null) {
                    return "null";
                }

                return Variable.Type.getTypeName(obj);
            }
        });

        // isNull
        context.addFunction(new NativeFunction("isNull", 1, Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return args[0] == null;
            }
        });

        // toString
        context.addFunction(new NativeFunction("toString", 1, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], String.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toInt
        context.addFunction(new NativeFunction("toInt", 1, Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Integer.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toFloat
        context.addFunction(new NativeFunction("toFloat", 1, Variable.Type.FLOAT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Float.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toDouble
        context.addFunction(new NativeFunction("toDouble", 1, Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Double.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toLong
        context.addFunction(new NativeFunction("toLong", 1, Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Long.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toByte
        context.addFunction(new NativeFunction("toByte", 1, Variable.Type.BYTE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Byte.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toShort
        context.addFunction(new NativeFunction("toShort", 1, Variable.Type.SHORT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Short.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toChar
        context.addFunction(new NativeFunction("toChar", 1, Variable.Type.CHAR) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Character.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // toBool
        context.addFunction(new NativeFunction("toBool", 1, Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                try {
                    return Utils.cast(args[0], Boolean.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // exit()
        context.addFunction(new NativeFunction("exit", 0, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                context.getRoot().markForDeletion(true);
                return null;
            }
        });

    }
}
