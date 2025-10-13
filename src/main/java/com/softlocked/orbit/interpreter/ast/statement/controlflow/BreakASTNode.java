package com.softlocked.orbit.interpreter.ast.statement.controlflow;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.InternalException;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
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

        // partial TCO
        if (type == Breakpoint.Type.RETURN) {
            if(value instanceof FunctionCallASTNode funcCall) {
                IFunction func = funcCall.getCachedFunction(context);

                context.getRoot().freeFunctionContext(func);

                return funcCall.evaluate(context);
            }
//            if(value instanceof OperationASTNode op) {
//                FunctionCallASTNode funcCall = findTailCall(op);
//                if(funcCall != null) {
//                    System.out.println("Optimizing tail call for function: " + funcCall);
//                    IFunction func = funcCall.getCachedFunction(context);
//
//                    context.getRoot().freeFunctionContext(func);
//
//                    return funcCall.evaluate(context);
//                }
//            }
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

    public static FunctionCallASTNode findTailCall(ASTNode node) {
        if (node instanceof FunctionCallASTNode funcCall) {
            return funcCall;
        } else if (node instanceof OperationASTNode op) {
            // Search right subtree
            FunctionCallASTNode rightResult = findTailCall(op.right());
            if (rightResult != null) {
                return rightResult;
            }

            // Search left subtree
            return findTailCall(op.left());
        }
        // Not a function call or operation
        return null;
    }

    @Override
    public long getSize() {
        return value != null ? value.getSize() : 0;
    }
}
