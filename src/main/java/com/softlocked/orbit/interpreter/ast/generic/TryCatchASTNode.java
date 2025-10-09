package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.InternalException;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public record TryCatchASTNode(ASTNode tryBlock, ASTNode catchBlock, String exceptionName) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        try {
            Object result = tryBlock.evaluate(context);
            if (result instanceof Breakpoint breakpoint && breakpoint.getType() != Breakpoint.Type.YIELD) {
                return breakpoint.getValue();
            }

            return result;
        } catch (InternalException e) {
            OrbitObject exception = e.getObject();
            context.addVariable(exceptionName.hashCode(), new Variable(Variable.Type.CLASS, exception));
            return catchBlock.evaluate(context);
        } catch (RuntimeException e) {
            OrbitObject exception = new OrbitObject(context.getRoot().getClassType("exception"), List.of(e.getMessage()), context.getRoot());
            context.addVariable(exceptionName.hashCode(), new Variable(Variable.Type.CLASS, exception));
            return catchBlock.evaluate(context);
        }
    }

    @Override
    public long getSize() {
        long size = 0;
        size += tryBlock.getSize();
        size += catchBlock.getSize();
        size += Variable.getSize(exceptionName);

        return size;
    }
}
