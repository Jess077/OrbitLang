package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;

public record DeleteVarASTNode(String variableName, int hash) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        context.removeVariable(hash);
        return null;
    }

    @Override
    public long getSize() {
        return Variable.getSize(variableName);
    }
}
