package com.softlocked.orbit.interpreter.function.coroutine;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.List;

public class CoroutineFunction extends OrbitFunction {
    public CoroutineFunction(String name, ASTNode body) {
        super(name, body, Variable.Type.ANY);
    }

    public CoroutineFunction(String name, int argsCount, List<Pair<String, Variable.Type>> args, ASTNode body) {
        super(name, argsCount, args, body, Variable.Type.ANY);
    }

    @Override
    public Object call(ILocalContext context, Object[] args) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

<<<<<<< HEAD
//        for (int i = 0; i < args.length; i++) {
//            Object value = Utils.cast(args.get(i), this.args.get(i).second.getJavaClass());
//            context.addVariable(this.args.get(i).first.hashCode(), new Variable(this.args.get(i).second, value));
//        }
=======
        for (int i = 0; i < args.length; i++) {
            Object value = Utils.cast(args[i], this.args[i].second.getJavaClass());
            context.addVariable(this.args[i].first.hashCode(), new Variable(this.args[i].second, value));
        }
>>>>>>> pr/1

        return new Coroutine(context, this, List.of(args));
    }
}
