package com.softlocked.orbit.core.evaluator;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.memory.ILocalContext;

/**
 * Used by break, continue, and return statements to control the flow of the program.
 */
public class Breakpoint {
    public enum Type {
        BREAK,
        CONTINUE,
        RETURN,
        YIELD,
        THROW
    }

    private Type type;

    private Object value;

    private ASTNode node;

    private ILocalContext context;

    public Breakpoint(Type type, Object value, ASTNode node, ILocalContext context) {
        this.type = type;
        this.value = value;
        this.node = node;
        this.context = context;
    }

    public Breakpoint(Type type, ASTNode node, ILocalContext context) {
        this.type = type;
        this.value = null;
        this.node = node;
        this.context = context;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public ASTNode getNode() {
        return node;
    }

    public ILocalContext getContext() {
        return context;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setContext(ILocalContext context) {
        this.context = context;
    }
}
