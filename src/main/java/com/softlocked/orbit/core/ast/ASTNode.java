package com.softlocked.orbit.core.ast;

import com.softlocked.orbit.memory.ILocalContext;

/**
 * Represents a node in the abstract syntax tree
 */
public interface ASTNode {
    /**
     * Evaluates the node and returns the result
     * @param context The local context to use
     * @return The result of the evaluation
     */
    Object evaluate(ILocalContext context) throws InterruptedException;

    /**
     * Returns the size of the node in bytes
     * @return The size of the node in bytes
     */
    long getSize();
}
