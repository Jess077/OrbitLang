package com.softlocked.orbit.interpreter.ast.object;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.ConstVar;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.memory.ILocalContext;

public record ConstObjASTNode(String variableName, int hash, ASTNode value, String clazz) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object value = this.value().evaluate(context);

        OrbitClass clazz = context.getRoot().getClassType(this.clazz);

        if(clazz == null) {
            throw new RuntimeException("Class " + this.clazz + " not found");
        }

        if(!(value instanceof OrbitObject obj)) {
            throw new RuntimeException("Cannot declare a value of type " + value.getClass().getName() + " as a class");
        }

        if(!obj.getClazz().extendsClass(clazz)) {
            throw new RuntimeException(obj.getClazz().name() + " does not extend " + clazz.name());
        }

        ConstVar variable = new ConstVar(Variable.Type.CLASS, value);

        context.addVariable(hash, variable);

        return variable;
    }

    @Override
    public long getSize() {
        long size = 0;
        size += Variable.getSize(variableName);
        size += value.getSize();
        size += Variable.getSize(clazz);

        return size;
    }
}
