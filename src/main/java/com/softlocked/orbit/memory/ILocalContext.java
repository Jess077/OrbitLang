package com.softlocked.orbit.memory;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;

/**
 * Represents a container for local variables. This is used to store the values of variables in a local scope.
 */
public interface ILocalContext {
    void onInit();

    void onRemove();

    void addVariable(int id, Variable value);

    Variable getVariable(int id);

    Int2ObjectOpenHashMap<Variable> getVariables();

    void removeVariable(int id);

    ILocalContext getParent();

    void setParent(ILocalContext parent);

    LocalContext getChild();

    void setChild(LocalContext child);

    GlobalContext getRoot();

    IFunction getFunction(String name, int parameterCount);

    void addFunction(IFunction function);

    LocalContext getOrCreateChild();
}
