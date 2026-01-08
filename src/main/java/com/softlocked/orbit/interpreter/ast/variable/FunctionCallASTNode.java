package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallASTNode implements ASTNode {
    private final String name;
    private final List<ASTNode> args;

    private IFunction cachedFunction;
//    private List<Object> cachedEvaluatedArgs;
    private Object[] cachedEvaluatedArgs;

    private boolean hasParams = false;

    public IFunction getCachedFunction(ILocalContext context) {
        if (cachedFunction == null) {
            cachedFunction = context.getFunction(name, args.size());

            if (cachedFunction == null) {
                throw new RuntimeException("Function " + name + " with " + args.size() + " arguments not found");
            }
            cachedEvaluatedArgs = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                cachedEvaluatedArgs[i] = null;
            }
        }

        return cachedFunction;
    }

    public FunctionCallASTNode(String name, List<ASTNode> args) {
        this.name = name;
        this.args = args;
    }

    public FunctionCallASTNode(IFunction function, List<ASTNode> args) {
        this.name = function.getName();
        this.args = args;
        this.cachedFunction = function;
    }

    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        // Caching
        if (cachedFunction == null) {
            cachedFunction = context.getFunction(name, args.size());

            if (cachedFunction == null) {
                throw new RuntimeException("Function " + name + " with " + args.size() + " arguments not found");
            }
            cachedEvaluatedArgs = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                cachedEvaluatedArgs[i] = null;
            }
        }

        if (cachedFunction.getParameterCount() != -1) {
            Pair<Integer, Variable.Type>[] parameters = cachedFunction.getParameters();

            for (int i = 0; i < args.size(); i++) {
                ASTNode arg = args.get(i);

                Variable.Type type = parameters[i].second;

                if(type == Variable.Type.CONSUMER) {
                    cachedEvaluatedArgs[i] = new Consumer(arg);
                } else {
                    cachedEvaluatedArgs[i] = arg.evaluate(context);
                }
            }
        } else {
            for (int i = 0; i < args.size(); i++) {
                ASTNode arg = args.get(i);
                cachedEvaluatedArgs[i] = arg.evaluate(context);
            }
        }

        if (cachedFunction instanceof NativeFunction) {
            // Cast argument types
            if (cachedFunction.getParameterCount() != -1) {
                for (int i = 0; i < cachedEvaluatedArgs.length; i++) {
                    cachedEvaluatedArgs[i] = Utils.cast(cachedEvaluatedArgs[i], cachedFunction.getParameters()[i].second.getJavaClass());
                }
            }

            Object result = cachedFunction.call(context, cachedEvaluatedArgs);

            if (result instanceof Breakpoint breakpoint) {
                return breakpoint.getValue();
            }

            return result;
        }

        LocalContext localContext = context.getRoot().getOrCreateFunctionContext(cachedFunction);

        Object result = cachedFunction.call(localContext, cachedEvaluatedArgs);

        context.getRoot().freeFunctionContext(cachedFunction);

        if (result instanceof Breakpoint breakpoint) {
            localContext.onRemove();
            return breakpoint.getValue();
        }

        localContext.onRemove();
        return result;
    }

    public String name() {
        return name;
    }

    public List<ASTNode> args() {
        return args;
    }

    @Override
    public long getSize() {
        return args.stream().mapToLong(ASTNode::getSize).sum() + Variable.getSize(name);
    }
}
