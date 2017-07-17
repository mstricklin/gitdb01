package com.tinkerpop.blueprints.impls.gitdb.store;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import com.tinkerpop.blueprints.impls.gitdb.Keyed;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class XStore {

    public XRevision getHead() {
        return head;
    }

    public void dump() {
        if (!log.isInfoEnabled())
            return;
        log.info("=== Repository dump ===");
        for (Map.Entry<Integer, Object> e : cache.asMap().entrySet()) {
            log.info("\t{} => {}", e.getKey(), e.getValue());
        }
    }

    // synchronize?
    private int put(Object o) {
        Integer id = revCounter.getAndIncrement();
        log.info("put {}", id);
        cache.put(id, o);
        return id;
    }

    private Object get(Integer id) {
        return cache.getIfPresent(id);
    }


    public static class XRevision {
        public static XRevision of(final XStore.XRevision baseline, final XStore store) {
            return new XStore.XRevision(baseline, store);
        }

        private XRevision(XStore.XRevision baseline, XStore store) {
            if (null != baseline) {
                index = newHashMap(baseline.index);
            } else {
                index = newHashMap();
            }
            this.store = store;
            revId = revCounter.getAndIncrement();
            log.info("new XRevision {}", revId);
        }

        public int put(Keyed k) {
            int id = store.put(k);
            index.put(k.rawId(), id);
            return id;
        }

        public Object get(int key) {
            Integer id = index.get(key);
            return null != id ? store.get(id) : null;
        }

        public void remove(int id) {
            index.remove(id);
        }

        public Iterable<Object> list() {
            return FluentIterable.from(index.values())
                                 .transform(Functions.forMap(store.cache.asMap()));
        }

        public void commit() {
            log.info("commit revision {}", revId);
            // TODO a merge here...
            store.head = this;
        }

        public void dump() {
            if (!log.isInfoEnabled())
                return;
            log.info("=== Revision '{}' dump ===", revId);
            for (Integer id : index.values()) {
                Object o = store.cache.getIfPresent(id);
                log.info("\t{} => {}", id, o);
            }
        }

        private final Integer revId;
        private final XStore store;
        // almost certainly don't need concurrent access, since a revision will
        // be mutable from one thread when created, then read-only from there on
        // out.
        // TODO: do we need to make a mutable and immutable version of XRevision?
        private final Map<Integer, Integer> index;
    }

    // =================================
    private static final AtomicInteger revCounter = new AtomicInteger(0);

    // TODO: parameterize maximumSize
    private final Cache<Integer, Object> cache = CacheBuilder.newBuilder()
                                                             .maximumSize(2000)
                                                             .build();
    private XRevision head;
}
