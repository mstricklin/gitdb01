/*
 * // CLASSIFICATION NOTICE: This file is UNCLASSIFIED
 */

package com.tinkerpop.blueprints.impls.gitdb;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class GitKeyIndex<T extends XElementProxy> extends GitIndex<T> {
    public GitKeyIndex(final Class<T> clazz, final GitGraph gg) {
        graph = gg;
    }

// key:object:element
    void createIndex(String key) {
        Map<Object, Set<Integer>> m = newHashMap();
        vIndices.put(key, m);
    }
    public void put(final String key, final Object value, final T element) {

    }

    // =================================
    Map<String, Map<Object, Set<Integer>>> vIndices = newHashMap();
//    protected Map<String, Map<Object, Set<T>>> index = new HashMap<String, Map<Object, Set<T>>>();


    private final Set<String> indexedKeys = newHashSet();
    private final GitGraph graph;

}
