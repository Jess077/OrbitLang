package com.softlocked.orbit.interpreter.ast.object;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

public record ClassDefinitionASTNode(String name, List<String> superClasses, HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                                     HashMap<Pair<String, Integer>, IFunction> functions, HashMap<Integer, ClassConstructor> constructors) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        GlobalContext globalContext = context.getRoot();

        List<OrbitClass> superClasses = superClasses().stream().map(globalContext::getClassType).toList();

        globalContext.addClass(new OrbitClass(this.name(), superClasses, this.fields(), this.functions(), this.constructors()));

        return null;
    }

    @Override
    public long getSize() {
        long size = 0;
        size += Variable.getSize(name);

        for (String superClass : superClasses) {
            size += Variable.getSize(superClass);
        }

        for (Pair<Variable.Type, ASTNode> field : fields.values()) {
            size += field.second.getSize();
        }

        for (String field : fields.keySet()) {
            size += Variable.getSize(field);
        }

        for (IFunction function : functions.values()) {
            size += function.getSize();
        }

        for (Pair<String, Integer> function : functions.keySet()) {
            size += Variable.getSize(function.first);
            size += Variable.getSize(function.second);
        }

        for (ClassConstructor constructor : constructors.values()) {
            size += constructor.getSize();
        }

        return size;
    }
}
