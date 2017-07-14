package com.tinkerpop.blueprints.impls.gitdb;

import java.util.concurrent.atomic.AtomicInteger;

import static com.tinkerpop.blueprints.impls.gitdb.XElementProxy.XElement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class XStore {

    int put(XElement e) {
        Integer id = idCounter.incrementAndGet();
        store.put(id, e);
        return id;
    }
    XElement get(Integer id) {
        return store.getIfPresent(id);
    }

    // =================================
    private final AtomicInteger idCounter = new AtomicInteger(0);

    Cache<Integer, XElement> store = CacheBuilder.newBuilder()
                                                 .maximumSize(1000)
                                                 .build();

}
