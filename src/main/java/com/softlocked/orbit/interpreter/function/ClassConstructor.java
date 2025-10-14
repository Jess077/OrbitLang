package com.softlocked.orbit.interpreter.function;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ClassConstructor implements IFunction {
    private final int argsCount;
    private final Pair<String, Variable.Type>[] args;

    private final ASTNode body;

    @Override
    public String getName() {
        return "ClassConstructor";
    }

    @Override
    public int getParameterCount() {
        return argsCount;
    }

    @Override
    public Pair<String, Variable.Type>[] getParameters() {
        return args;
    }

    @Override
    public Variable.Type getReturnType() {
        return Variable.Type.CLASS;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public ASTNode getBody() {
        return body;
    }

    public ClassConstructor(int argsCount, List<Pair<String, Variable.Type>> args, ASTNode body) {
        this.argsCount = argsCount;
        this.args = new Pair[argsCount];
        for (int i = 0; i < argsCount; i++) {
            this.args[i] = args.get(i);
        }
        this.body = body;
    }

    @Override
    public Object call(ILocalContext context, Object[] args) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        for (int i = 0; i < args.length; i++) {
            Object value = Utils.cast(args[i], this.args[i].second.getJavaClass());
            context.addVariable(this.args[i].first.hashCode(), new Variable(this.args[i].second, value));
        }

        Object result = body.evaluate(context);

        if(result instanceof Breakpoint) {
            return ((Breakpoint) result).getValue();
        } else {
            return result;
        }
    }

    int id;

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
