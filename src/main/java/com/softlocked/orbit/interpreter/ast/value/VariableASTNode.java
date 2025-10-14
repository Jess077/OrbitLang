package com.softlocked.orbit.interpreter.ast.value;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;

public record VariableASTNode(String name, int hash) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        Variable variable = context.getVariable(hash);

        if (variable == null) {
            return null;
        }
        return variable.getValue();
    }

    @Override
    public long getSize() {
        return Variable.getSize(name);
    }
}
