package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.util.ExceptionFactory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class GitGraphUtil {
    static Integer castInt(Object i) {
        if (null == i)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        final Integer intId;
        if (i instanceof Integer)
            intId = (Integer) i;
        else if (i instanceof Number)
            intId = ((Number) i).intValue();
        else
            intId = Double.valueOf(i.toString()).intValue();
        return intId;
    }

    private static <T> Iterable<T> once(final Iterator<T> source) {
        return new Iterable<T>() {
            private AtomicBoolean exhausted = new AtomicBoolean();
            @Override
            public Iterator<T> iterator() {
                Preconditions.checkState(!exhausted.getAndSet(true));
                return source;
            }
        };
    }

}
