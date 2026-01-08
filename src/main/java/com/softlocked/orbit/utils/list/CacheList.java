package com.softlocked.orbit.utils.list;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * An array list implementation that stores its elements permanently in memory.
 * Basically has a 'movable cursor'
 */
public class CacheList<T> extends AbstractList<T> {
    private final List<T> list = new ArrayList<>();
    private int cursor = 0;

    public void move(int steps) {
        cursor += steps;
        if (cursor < 0) cursor = 0;
        if (cursor > list.size()) cursor = list.size();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    public T getNext() {
        if (cursor >= list.size()) {
            throw new IndexOutOfBoundsException("Cursor is at the end of the list");
        }
        return list.get(cursor++);
    }

    public void addNext(T element) {
        if (cursor < list.size()) {
            list.set(cursor, element);
        } else {
            list.add(element);
        }
        cursor++;
    }

    public boolean add(T element) {
        return list.add(element);
    }

    @Override
    public int size() {
        return cursor;
    }

    public int realSize() {
        return list.size();
    }
}
