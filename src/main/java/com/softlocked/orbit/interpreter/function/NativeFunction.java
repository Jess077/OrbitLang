package com.softlocked.orbit.interpreter.function;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IFunction interface which represents functions written in Java.
 * This class should be overridden to implement a new function, as the call method is abstract.
 * @see IFunction
 */
public class NativeFunction implements IFunction {
    private final String name;
    private final int argsCount;

    private final Variable.Type returnType;

    private final List<Variable.Type> args = new ArrayList<>();

    Pair<String, Variable.Type>[] cachedParams;

    public NativeFunction(String name, int argsCount, Variable.Type returnType) {
        this.name = name;
        this.argsCount = argsCount;
        this.returnType = returnType;

        for (int i = 0; i < argsCount; i++) {
            args.add(Variable.Type.ANY);
        }

        if (argsCount < 0) return;
        cachedParams = new Pair[argsCount];

        for (int i = 0; i < args.size(); i++) {
            cachedParams[i] = new Pair<>("arg" + i, args.get(i));
        }
    }

    public NativeFunction(String name, List<Variable.Type> args, Variable.Type returnType) {
        this.name = name;
        this.argsCount = args.size();
        this.returnType = returnType;
        this.args.addAll(args);

        cachedParams = new Pair[argsCount];

        for (int i = 0; i < args.size(); i++) {
            cachedParams[i] = new Pair<>("arg" + i, args.get(i));
        }
    }

    @Override
    public ASTNode getBody() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getParameterCount() {
        return argsCount;
    }

    @Override
    public Pair<String, Variable.Type>[] getParameters() {
        return cachedParams;
    }

    @Override
    public Variable.Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public Object call(ILocalContext context, Object[] args) {
        throw new UnsupportedOperationException("Native functions must be overridden to be called");
    }

    public <T extends BFunction> Class<T> getBakedFunction() {
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
}
