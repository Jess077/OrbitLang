package com.softlocked.orbit.libraries.Math;

import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.memory.ILocalContext;

public class BFunction_Max extends BFunction {
    @Override
    public Object evaluate(ILocalContext context) throws InterruptedException {
        Object value = values.get(0).evaluate(context);
        Object value2 = values.get(1).evaluate(context);

        return Math.max(((Number)value).doubleValue(), ((Number)value2).doubleValue());
    }
}
