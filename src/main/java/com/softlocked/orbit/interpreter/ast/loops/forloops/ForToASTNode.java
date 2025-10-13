package com.softlocked.orbit.interpreter.ast.loops.forloops;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record ForToASTNode(ASTNode init, ASTNode end, ASTNode body) implements ASTNode {
@Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        LocalContext forContext = context.getOrCreateChild();

        Variable variable = (Variable) this.init().evaluate(forContext);

        long start = ((Number) variable.getValue()).longValue();
        long end = ((Number) this.end().evaluate(forContext)).longValue();

        switch (variable.getType()) {
            case ANY, INT -> {
                for (int i = Math.toIntExact(start); i <= end; i++) {
                    variable.setValue(i);

                    Object result = this.body().evaluate(forContext);
                    if (result instanceof Breakpoint breakpoint) {
                        if (breakpoint.getType() == Breakpoint.Type.BREAK) {
                            forContext.onRemove();
                            return null;
                        }
                        if (breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                            forContext.onRemove();
                            return breakpoint;
                        }
                    }
                }
            }
            case LONG -> {
                for (long i = start; i <= end; i++) {
                    variable.setValue(i);

                    Object result = this.body().evaluate(forContext);
                    if (result instanceof Breakpoint breakpoint) {
                        if (breakpoint.getType() == Breakpoint.Type.BREAK) {
                            forContext.onRemove();
                            return null;
                        }
                        if (breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                            forContext.onRemove();
                            return breakpoint;
                        }
                    }
                }
            }
            case FLOAT -> {
                for (float i = start; i <= end; i++) {
                    variable.setValue(i);
                    Object result = this.body().evaluate(forContext);
                    if (result instanceof Breakpoint breakpoint) {
                        if (breakpoint.getType() == Breakpoint.Type.BREAK) {
                            forContext.onRemove();
                            return null;
                        }
                        if (breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                            forContext.onRemove();
                            return breakpoint;
                        }
                    }
                }
            }
            case DOUBLE -> {
                for (double i = start; i <= end; i++) {
                    variable.setValue(i);
                    Object result = this.body().evaluate(forContext);
                    if (result instanceof Breakpoint breakpoint) {
                        if (breakpoint.getType() == Breakpoint.Type.BREAK) {
                            forContext.onRemove();
                            return null;
                        }
                        if (breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                            forContext.onRemove();
                            return breakpoint;
                        }
                    }
                }
            }
            default -> throw new RuntimeException("Invalid variable type for for loop: " + variable.getType());
        }


        forContext.onRemove();
        return null;
    }

    @Override
    public long getSize() {
        long size = 0;
        size += init.getSize();
        size += end.getSize();
        size += body.getSize();

        return size;
    }
}
