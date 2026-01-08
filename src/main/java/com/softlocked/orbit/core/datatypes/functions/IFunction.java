package com.softlocked.orbit.core.datatypes.functions;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

public interface IFunction extends ASTNode {
    String getName();

    int getParameterCount();

    Pair<Integer, Variable.Type>[] getParameters();

    Variable.Type getReturnType();

    boolean isNative();

    Object call(ILocalContext context, Object[] args) throws InterruptedException;

    ASTNode getBody();

    @Override
    default Object evaluate(ILocalContext context) {
        context.addFunction(this);
        return null;
    }

    @Override
    default long getSize() {
        return Variable.getSize(getName()) + 4 + getParameterCount() * (4 + 4) + 4;
    }

    void setID(int id);

    int getID();
}