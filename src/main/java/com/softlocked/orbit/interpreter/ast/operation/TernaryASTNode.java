package com.softlocked.orbit.interpreter.ast.operation;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;

public record TernaryASTNode(ASTNode condition, ASTNode trueBranch, ASTNode falseBranch) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object condition = this.condition().evaluate(context);

        if (Evaluator.toBool(condition)) {
            return trueBranch().evaluate(context);
        } else {
            return falseBranch().evaluate(context);
        }
    }

    @Override
    public long getSize() {
        long size = 0;
        size += condition.getSize();
        size += trueBranch.getSize();
        size += falseBranch.getSize();

        return size;
    }
}