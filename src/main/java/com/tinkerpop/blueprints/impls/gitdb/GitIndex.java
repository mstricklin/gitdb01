/*
 * // CLASSIFICATION NOTICE: This file is UNCLASSIFIED
 */

package com.tinkerpop.blueprints.impls.gitdb;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Index;

class GitIndex<T extends XElementProxy> implements Index<T> {
    @Override
    public String getIndexName() {
        return null;
    }

    @Override
    public Class<T> getIndexClass() {
        return null;
    }

    @Override
    public void put(String key, Object value, T element) {

    }

    @Override
    public CloseableIterable<T> get(String key, Object value) {
        return null;
    }

    @Override
    public CloseableIterable<T> query(String key, Object query) {
        return null;
    }

    @Override
    public long count(String key, Object value) {
        return 0;
    }

    @Override
    public void remove(String key, Object value, T element) {

    }
}
