package com.softlocked.orbit.interpreter.ast.loops;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record WhileASTNode(ASTNode condition, ASTNode body) implements ASTNode {

    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        LocalContext newContext = new LocalContext(context);

        if(condition instanceof ValueASTNode valueASTNode) {
            boolean condition = Evaluator.toBool(valueASTNode.evaluate(newContext));

            while (condition) {
                Object result = this.body().evaluate(newContext);

                if (result instanceof Breakpoint breakpoint) {
                    if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                        newContext.onRemove();
                        return null;
                    }
                    if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                        newContext.onRemove();
                        return breakpoint;
                    }
                }
            }

            newContext.onRemove();
            return null;
        }

        while (Evaluator.toBool(this.condition().evaluate(newContext))) {
            Object result = this.body().evaluate(newContext);

            if (result instanceof Breakpoint breakpoint) {
                if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                    newContext.onRemove();
                    return null;
                }
                if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                    newContext.onRemove();
                    return breakpoint;
                }
            }
        }

        newContext.onRemove();
        return null;
    }

    @Override
    public long getSize() {
        long size = 0;
        size += condition.getSize();
        size += body.getSize();

        return size;
    }
}
