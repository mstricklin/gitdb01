package com.tinkerpop.blueprints.impls.gitdb.store;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import com.tinkerpop.blueprints.impls.gitdb.Keyed;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class XStore {
    public static final Lock lock = new ReentrantLock();
    public void lock() {
        lock.lock();
    }
    public void unlock() {
        lock.unlock();
    }

    public XStore() {
        head = new XRevision(this);
    }

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
        Integer id = objectCounter.getAndIncrement();
        cache.put(id, o);
        return id;
    }

    private Object get(Integer id) {
        return cache.getIfPresent(id);
    }

    // =================================
    private abstract static class XAbstractRevision {
        private XAbstractRevision(XStore s) {
            this.store = s;
            revId = objectCounter.getAndIncrement();
            baseline = null;
        }
        private XAbstractRevision(XAbstractRevision rev) {
            this.revId = rev.revId;
            this.store = rev.store;
            this.baseline = rev.baseline;
        }
        protected Integer lookup(int k) {
            return getIndex().get(k);
        }
        public Object get(int key) {
            Integer id = lookup(key);
            return null != id ? store.get(id) : null;
        }
        public Iterable<Object> list() {
            return FluentIterable.from(getIndex().values())
                                 .transform(Functions.forMap(store.cache.asMap()));
        }
        abstract Map<Integer, Integer> getIndex();
        @Override
        public int hashCode() {
            return revId;
        }
        @Override
        abstract public String toString();
        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (other == null) return false;
//            if (getClass() != other.getClass()) return false;
            if (! (other instanceof XAbstractRevision)) return false;
            XAbstractRevision otherA = (XAbstractRevision)other;
            return revId.equals(otherA.revId);
        }
        public void dump() {
            if (!log.isInfoEnabled())
                return;
            log.info("=== revision '{}' dump ===", revId);
            for (Integer id : getIndex().values()) {
                Object o = store.cache.getIfPresent(id);
                log.info("\t{} => {}", id, o);
            }
        }
        final Integer revId;
        protected final XStore store;
        protected final XAbstractRevision baseline;
        //        private final XRevision baseline;
        // almost certainly don't need concurrent access, since a revision will
        // be mutable from one thread when created, then read-only from there on
        // out.
        // TODO: do we need to make a mutable and immutable version of XWorking?
    }
    // =================================
    public static class XRevision extends XAbstractRevision {
        public static XRevision of(final XStore store) {
            return new XRevision(store);
        }
        public static XRevision of(final XWorking baseline) {
            return new XRevision(baseline);
        }
        private XRevision(XStore s) {
            super(s);
            this.index = ImmutableMap.of();
        }
        private XRevision(XWorking w) {
            super(w);
            revId = objectCounter.getAndIncrement();
            this.index = ImmutableMap.copyOf(w.index);
        }
        Map<Integer, Integer> getIndex() {
            return index;
        }
        public String toString() {
            return "revision " + revId.toString();
        }

        private final ImmutableMap<Integer, Integer> index;
    }

    // =================================
    public static class XWorking extends XAbstractRevision {


        public static XWorking of(final XRevision baseline) {
            return new XWorking(baseline);
        }

        private XWorking(XStore store) {
            super(store.head);
            index = newHashMap();
            log.info("new XWorking de novo {}", revId);
        }

        private XWorking(XRevision baseline) {
            super(baseline);
            index = newHashMap(baseline.index);
            log.info("new XWorking from baseline {}", revId);
        }

        public int put(Keyed k) {
            int id = store.put(k);
            log.info("put OID {} ({})", id, k.rawId());
            index.put(k.rawId(), id);
            return id;
        }

        public void remove(int id) {
            log.info("remove OID {} ({})", lookup(id), id);
            index.remove(id);
        }

        public void commit() {
            log.info("commit working tree {} sz {}", revId, index.size());
            // TODO a merge here...
            XRevision rev = XRevision.of(this);
            store.head = rev;
        }
        Map<Integer, Integer> getIndex() {
            return index;
        }

        public String toString() {
            return "working tree " + revId.toString();
        }

        private final Map<Integer, Integer> index;
    }

    // =================================
    private static final AtomicInteger objectCounter = new AtomicInteger(0);

    // TODO: parameterize maximumSize
    private final Cache<Integer, Object> cache = CacheBuilder.newBuilder()
                                                             .maximumSize(2000)
                                                             .build();
    private XRevision head;
}
