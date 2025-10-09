package com.softlocked.orbit.memory;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Generic implementation of ILocalContext
 * @see ILocalContext
 */
public class LocalContext implements ILocalContext {
    protected ILocalContext parent;
    protected final GlobalContext root;

    protected Int2ObjectOpenHashMap<Variable> variables = new Int2ObjectOpenHashMap<>();

    // for prototyping, make an array map to store variables
    protected Variable[] arrayVariables = new Variable[256];

    public LocalContext(ILocalContext parent) {
        this.parent = parent;
        this.root = parent.getRoot();

        onInit();
    }

    public LocalContext() {
        this.parent = null;
        this.root = (GlobalContext) this;
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onRemove() {
    }

    @Override
    public void addVariable(int id, Variable value) {
        variables.put(id, value);
    }

    @Override
    public Variable getVariable(int id) {
        Variable variable = variables.get(id);

        if (variable != null) {
            return variable;
        } else {
            if(parent != null) {
                return parent.getVariable(id);
            }
            else {
                return null;
            }
        }
    }

    public void clearVariables() {
        variables.clear();
    }

    @Override
    public Int2ObjectOpenHashMap<Variable> getVariables() {
        return variables;
    }

    @Override
    public void removeVariable(int id) {
        Variable variable = variables.remove(id);

        if (variable == null && parent != null) {
            parent.removeVariable(id);
        }
    }

    @Override
    public ILocalContext getParent() {
        return parent;
    }

    @Override
    public void setParent(ILocalContext parent) {
        this.parent = parent;
    }

    @Override
    public GlobalContext getRoot() {
        return root;
    }

    @Override
    public IFunction getFunction(String name, int parameterCount) {
        return root.getFunction(name, parameterCount);
    }

    @Override
    public void addFunction(IFunction function) {
        root.addFunction(function);
    }
}
