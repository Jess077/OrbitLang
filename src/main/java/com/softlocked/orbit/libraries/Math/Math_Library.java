package com.softlocked.orbit.libraries.Math;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.math.SimplexNoise;

import java.util.List;

public class Math_Library implements OrbitJavaLibrary {

    @Override
    public void load(GlobalContext context) {
        context.addFunction(new NativeFunction("math.sin", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.sin((double) args[0]);
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) BFunction_Sin.class;
            }
        });

        context.addFunction(new NativeFunction("math.cos", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.cos((double) args[0]);
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) BFunction_Cos.class;
            }
        });

        context.addFunction(new NativeFunction("math.tan", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.tan((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.asin", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.asin((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.acos", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.acos((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.atan", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.atan((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.atan2", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.atan2((double) args[0], (double) args[1]);
            }
        });

        context.addFunction(new NativeFunction("math.toDegrees", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.toDegrees((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.toRadians", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.toRadians((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.exp", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.exp((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.log", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.log((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.log10", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.log10((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.sqrt", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.sqrt((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.cbrt", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.cbrt((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.pow", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.pow((double) args[0], (double) args[1]);
            }
        });

        context.addFunction(new NativeFunction("math.abs", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.abs((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.ceil", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.ceil((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.floor", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.floor((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.round", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.round((double) args[0]);
            }
        });

        context.addFunction(new NativeFunction("math.max", List.of(Variable.Type.ANY, Variable.Type.ANY), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                Object value = args[0];
                Object value2 = args[1];
                return Math.max(((Number)value).doubleValue(), ((Number)value2).doubleValue());
            }

//            @Override
//            public <T extends BFunction> Class<T> getBakedFunction() {
//                return (Class<T>) BFunction_Max.class;
//            }
        });

        context.addFunction(new NativeFunction("math.min", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.min((double) args[0], (double) args[1]);
            }
        });

        context.addFunction(new NativeFunction("math.random", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.random();
            }
        });

        context.addFunction(new NativeFunction("math.random", List.of(Variable.Type.INT, Variable.Type.INT), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return (int) (Math.random() * ((int) args[1] - (int) args[0] + 1)) + (int) args[0];
            }
        });

        context.addFunction(new NativeFunction("math.PI", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.PI;
            }
        });

        context.addFunction(new NativeFunction("math.E", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.E;
            }
        });

        // Perlin noise generation
        context.addFunction(new NativeFunction("math.noise", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return SimplexNoise.noise((double) args[0], (double) args[1]);
            }
        });

        // remainder
        context.addFunction(new NativeFunction("math.remainder", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, Object[] args) {
                return Math.IEEEremainder((double) args[0], (double) args[1]);
            }
        });
    }
}
