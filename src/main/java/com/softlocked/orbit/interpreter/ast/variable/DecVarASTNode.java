package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

public record DecVarASTNode(String variableName, int hash, ASTNode value, Variable.Type type) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object value = this.value().evaluate(context);

        Variable old = context.getVariable(hash);

        if (old != null) {
            old.setValue(Utils.cast(value, old.getType().getJavaClass()));
            old.setType(this.type());
            return old;
        }

        Object casted = Utils.cast(value, this.type().getJavaClass());

        Variable variable = new Variable(this.type(), casted);

        context.addVariable(hash, variable);

        return variable;
    }

    @Override
    public long getSize() {
        return Variable.getSize(variableName) + value.getSize();
    }
}