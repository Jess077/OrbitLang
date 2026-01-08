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

/**
 * Implementation of the IFunction interface which represents functions written inside of Orbit.
 * @see IFunction
 */
public class OrbitFunction implements IFunction {
    protected final String name;
    protected final int argsCount;
    protected final Variable.Type returnType;

    protected final Pair<Integer, Variable.Type>[] args;

    protected final ASTNode body;

    public OrbitFunction(String name, ASTNode body, Variable.Type returnType) {
        this.name = name;
        this.argsCount = 0;
        this.body = body;
        this.returnType = returnType;

        this.args = new Pair[0];
    }

    public OrbitFunction(String name, int argsCount, List<Pair<String, Variable.Type>> args, ASTNode body, Variable.Type returnType) {
        this.name = name;
        this.argsCount = argsCount;
        this.args = new Pair[argsCount];
        for (int i = 0; i < argsCount; i++) {
            this.args[i] = new Pair<>(args.get(i).first.hashCode(), args.get(i).second);
        }
        this.body = body;
        this.returnType = returnType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getParameterCount() {
        return argsCount;
    }

    @Override
    public Pair<Integer, Variable.Type>[] getParameters() {
        return args;
    }

    @Override
    public Variable.Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public ASTNode getBody() {
        return body;
    }

    @Override
    public Object call(ILocalContext context, Object[] args) throws InterruptedException {
        for (int i = 0; i < args.length; i++) {
            Variable.Type type = this.args[i].second;
            Object value = Utils.cast(args[i], type.getJavaClass());
            context.addVariable(this.args[i].first, new Variable(type, value));
        }

        Object result = body.evaluate(context);

        if(result instanceof Breakpoint breakpoint) {
            return breakpoint.getValue();
        } else {
            return result;
        }
    }

    @Override
    public String toString() {
        return "OrbitFunction{" +
                "name='" + name + '\'' +
                ", argsCount=" + argsCount +
                ", returnType=" + returnType +
                ", args=" + args +
                ", body=" + body +
                '}';
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