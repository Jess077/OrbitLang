package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

public record AssignVarASTNode(String variableName, int hash, ASTNode value) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object value = this.value().evaluate(context);

        Variable variable = context.getVariable(hash);

        if (variable == null) {
            throw new RuntimeException("Variable " + this.variableName() + " not found");
        }

        if (variable.getType().getJavaClass().equals(Variable.class)) {
            variable.setValue(value);

            return variable;
        }
        variable.setValue(Utils.cast(value, variable.getType().getJavaClass()));

        return variable;
    }

    @Override
    public long getSize() {
        return Variable.getSize(variableName) + value.getSize();
    }
}
