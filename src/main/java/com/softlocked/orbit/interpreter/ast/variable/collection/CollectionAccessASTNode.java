package com.softlocked.orbit.interpreter.ast.variable.collection;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionAccessASTNode implements ASTNode {
    public ASTNode collection;
    public List<ASTNode> indices;

    public CollectionAccessASTNode(ASTNode collection, List<ASTNode> indices) {
        this.collection = collection;
        this.indices = indices;
    }

    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        if (context.getRoot().isMarkedForDeletion()) throw new InterruptedException("Context marked for deletion");

        Object collection = this.collection.evaluate(context);

        List<Object> indices = new ArrayList<>();
        for (ASTNode index : this.indices) {
            indices.add(index.evaluate(context));
        }

        if (collection instanceof List) {
            List<Object> list = (List<Object>) collection;
            int depth = indices.size();
            while (depth > 1) {
                int index = (int) Utils.cast(indices.get(indices.size() - depth), Integer.class);
                if (index < 0) {
                    index += list.size();
                }
                list = (List<Object>) list.get(index);
                depth--;
            }
            int lastIndex = (int) Utils.cast(indices.getLast(), Integer.class);
            if (lastIndex < 0) {
                lastIndex += list.size();
            }
            return list.get(lastIndex);
        } else if (collection instanceof String string) {
            if (indices.size() == 1) {
                return string.charAt(((Number) indices.getFirst()).intValue());
            } else {
                throw new RuntimeException("Invalid number of indices for string access");
            }
        } else if (collection instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) collection;
            int depth = indices.size();
            while (depth > 1) {
                Object key = indices.get(indices.size() - depth);
                if (!map.containsKey(key)) {
                    throw new RuntimeException("Key not found in map");
                }
                map = (Map<Object, Object>) map.get(key);
                depth--;
            }
            Object lastKey = indices.get(indices.size() - 1);
            return map.get(lastKey);
        } else if (collection instanceof OrbitObject obj) {
            return obj.callFunction("collection.get", List.of(indices));
        } else {
            throw new RuntimeException("Invalid collection type for access");
        }
    }

    @Override
    public String toString() {
        return "CollectionAccessASTNode{" +
                "collection=" + collection +
                ", indices=" + indices +
                '}';
    }

    @Override
    public long getSize() {
        long size = 0;
        size += collection.getSize();
        size += indices.stream().mapToLong(ASTNode::getSize).sum();
        return size;
    }
}
