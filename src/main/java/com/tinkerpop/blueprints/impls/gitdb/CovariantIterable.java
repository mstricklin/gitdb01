// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import java.util.Iterator;

public class CovariantIterable<T> implements Iterable<T> {

    public CovariantIterable(Iterable<? extends T> it) {
        this.it = it;
    }
    @Override
    public Iterator<T> iterator() {
        return new CovariantIterator<T>(it.iterator());
    }

    private final Iterable<? extends T> it;

    static class CovariantIterator<T> implements Iterator<T> {

        private final Iterator<? extends T> it;

        CovariantIterator(Iterator<? extends T> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            return it.next();
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
}
