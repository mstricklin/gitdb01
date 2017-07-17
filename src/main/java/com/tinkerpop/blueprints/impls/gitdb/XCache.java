// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.XElementProxy.XElement;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class XCache {
    public static XCache of(GitGraph gg) {
        return new XCache(gg);
    }

    public class NotFoundException extends RuntimeException {
        public NotFoundException() {
            super();
        }
    }
    public void dump() {
        if (!log.isInfoEnabled())
            return;
        log.info("=== XCache Baseline dump ===");
        log.info("  Vertices");
        for (XVertex v : Iterables.filter(cache.asMap().values(), XVertex.class)) {
            log.info("\t{} => {}", v.rawId(), v);
        }
        log.info("  Edges");
        for (XEdge e : Iterables.filter(cache.asMap().values(), XEdge.class)) {
            log.info("\t{} => {}", e.rawId(), e);
        }
    }

    public void addVertex(XVertex xv) {
        cache.put(xv.rawId(), xv);
    }
    public void addVertex(int id) {
        cache.put(id, new XVertex(id));
    }

    public XVertex getVertex(int id) {
        return (XVertex)cache.getIfPresent(id);
    }
    // TODO: rename? getVerticesKeys?
    public Iterable<Integer> getVertices() {
        Map<Integer, XElement> m = Maps.filterValues(cache.asMap(),
                                                     Predicates.instanceOf(XVertex.class));
        return m.keySet();
    }
    public void removeVertex(int id) {
        cache.invalidate(id);
    }
    public boolean containsVertex(int id) {
        return (null != cache.getIfPresent(id));
    }
    // =================================
    public void addEdge(int id, int outVertex, int inVertex, String label) {
        XEdge xe = new XEdge(id, outVertex, inVertex, label);
        cache.put(id, xe);
    }
    public void addEdge(XEdge xe) {
        cache.put(xe.rawId(), xe);
    }
    public XEdge getEdge(int id) {
        return (XEdge)cache.getIfPresent(id);
    }
    public Iterator<Integer> getEdges() {
        Map<Integer, XElement> m = Maps.filterValues(cache.asMap(),
                                                     Predicates.instanceOf(XEdge.class));
        return m.keySet().iterator();
    }
    public void removeEdge(int id) {
        cache.invalidate(id);
    }
    public boolean containsEdge(int id) {
        return (null != cache.getIfPresent(id));
    }
    // =================================
    private XCache(final GitGraph gg) {
        graph = gg;
    }

    private final GitGraph graph;
    // =================================

    private final Cache<Integer, XElement> cache
            = CacheBuilder.newBuilder()
                          .maximumSize(2000)
                          .build();

    private final AtomicInteger idCounter = new AtomicInteger();
}
