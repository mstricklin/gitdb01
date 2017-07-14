package com.tinkerpop.blueprints.impls.gitdb;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static com.tinkerpop.blueprints.impls.gitdb.XElementProxy.XElement;

public class XRevision {
    static class Rev implements AutoCloseable {
        static Rev of(final Rev baseline, XStore store) {
            return new Rev(baseline, store);
        }

        @Override
        public void close() throws Exception {

        }
        void put(final XElement xe) {
            int index = store.put(xe);
            revision.put(xe.rawId(), index);
        }
        void remove(int key) {
            revision.remove(key);
        }
        int lookup(int id) {
            return revision.get(id);
        }
        XElement get(int key) {
            return store.get(lookup(key));
        }
        // =================================
        private Rev(final Rev baseline, XStore store) {
            if (null != baseline) {
                revision = newHashMap(baseline.revision);
            } else {
                revision = newHashMap();
            }
            this.store = store;
            this.baseline = baseline;
        }

        // =================================
        private static final AtomicInteger revIDCounter = new AtomicInteger(0);

        // key: id of element
        // value: index into store
        final Map<Integer, Integer> revision;
        final XStore store;
        final Rev baseline;
        final int revId = revIDCounter.getAndIncrement();


    }



}
