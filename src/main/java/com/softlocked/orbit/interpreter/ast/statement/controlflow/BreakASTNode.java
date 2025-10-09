package com.softlocked.orbit.interpreter.ast.statement.controlflow;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.InternalException;
import com.softlocked.orbit.memory.ILocalContext;

public class BreakASTNode implements ASTNode {

    private Breakpoint.Type type;
    private ASTNode value;

    // Optional cached breakpoint
    private Breakpoint cachedBreakpoint;

    public BreakASTNode(Breakpoint.Type type, ASTNode value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        // Handle THROW type separately
        if (type == Breakpoint.Type.THROW) {
            Object evaluatedValue = this.value.evaluate(context);

            if (!(evaluatedValue instanceof OrbitObject exception)
                    || !exception.getClazz().extendsClass(context.getRoot().getClassType("exception"))) {
                throw new RuntimeException("Attempted to throw a non-exception object.");
            }

            throw new InternalException(exception);
        }

        // Evaluate value if not null
        Object evaluatedValue = value != null ? value.evaluate(context) : null;

        // Cache the breakpoint
        if (cachedBreakpoint == null) {
            cachedBreakpoint = new Breakpoint(type, evaluatedValue, this, context);
        } else {
            cachedBreakpoint.setType(type);
            cachedBreakpoint.setValue(evaluatedValue);
        }

        return cachedBreakpoint;
    }

    @Override
    public long getSize() {
        return value != null ? value.getSize() : 0;
    }
}
