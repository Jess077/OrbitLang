package com.softlocked.orbit.interpreter.ast.statement;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record ConditionalASTNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object condition = this.condition().evaluate(context);

        LocalContext newContext = context.getOrCreateChild();
        if (Evaluator.toBool(condition)) {
            Object result = this.thenBranch().evaluate(newContext);

            newContext.onRemove();

            return result;
        } else {
            if (this.elseBranch() == null) {
                newContext.onRemove();
                return null;
            }
            Object result = this.elseBranch().evaluate(newContext);

            newContext.onRemove();

            return result;
        }
    }

    @Override
    public long getSize() {
        long size = 0;
        size += condition.getSize();
        size += thenBranch.getSize();
        if (elseBranch != null) {
            size += elseBranch.getSize();
        }

        return size;
    }
}
