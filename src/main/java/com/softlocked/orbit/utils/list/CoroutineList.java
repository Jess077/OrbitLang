package com.softlocked.orbit.utils.list;

import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * A list implementation that is designed for iterating over values yielded by a coroutine.
 */
public class CoroutineList extends AbstractList<Object> {
    private Coroutine coroutine;

    public CoroutineList(Coroutine coroutine) {
        this.coroutine = coroutine;
    }

    @Override
    public int size() {
        int size = 0;

        while (!coroutine.isFinished()) {
            try {
                coroutine.resume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            size++;
        }

        try {
            coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return size;
    }

    @Override
    public Object get(int index) {
        while (!coroutine.isFinished() && index > 0) {
            try {
                coroutine.resume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            index--;
        }

        Object value = null;
        try {
            value = coroutine.resume();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    @Override
    public boolean isEmpty() {
        return coroutine.isFinished();
    }

    @Override
    public boolean contains(Object o) {
        Object value = null;

        do {
            try {
                value = coroutine.resume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (!coroutine.isFinished() && !value.equals(o));

        try {
            coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return value.equals(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                boolean finished = coroutine.isFinished();

                if (finished) {
                    try {
                        coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                return !finished;
            }

            @Override
            public Object next() {
                if(coroutine.isAsync()) {
                    while (coroutine.getReturnValue() == null) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    return coroutine.resume();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
