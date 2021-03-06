// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.tinkerpop.blueprints.impls.gitdb.GitGraphUtil.castInt;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.*;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XTransactionGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph {
    XTransactionGraph(final GitGraph gg, final XStore.XRevision baseline) {
        this.graph = gg;
        this.baseline = baseline;
        this.name = Integer.toString(transactionCounter.getAndIncrement()) + " (" + Thread.currentThread().getName() + ')';
        log.trace("Start new TX '{}'", this);
    }

    // =================================
    @Override
    public Vertex addVertex(Object id) {
        XVertex xv = new XVertex(graph.nextId());
        mutatedVertices.put(xv.rawId(), xv);
        return XVertexProxy.of(xv, graph);
    }

    @Override
    public Vertex getVertex(Object id) {
        // TODO how to handle non-existent or deleted nodes?
        try {
            XVertex xv = vertexImpl(castInt(id));
            if (null != xv)
                return XVertexProxy.of(xv, graph);
        } catch (XCache.NotFoundException e) {
            log.error("could not find vertex by key {}", id);
        } catch (NumberFormatException e) {
            log.error("could not use vertex key {}", id);
        }
        return null;
    }

    XVertex vertexImpl(int id) {
        if (deletedVertices.contains(id))
            return null;
        XVertex v = mutatedVertices.get(id);
        return (null == v) ? XStoreFacade.getVertex(baseline, id) : v;
    }

    XVertex mutableVertexImpl(int id) {
        if (deletedVertices.contains(id))
            return null;
        if (mutatedVertices.containsKey(id))
            return mutatedVertices.get(id);
        XVertex xv = XStoreFacade.getVertex(baseline, id);
        if (null != xv) {
            XVertex xvm = new XVertex(xv);
            mutatedVertices.put(xvm.rawId(), xvm);
            return xvm;
        }
        return null;
    }

    @Override
    public void removeVertex(Vertex vertex) {
        // TODO: remove from indices
        try {
            checkNotNull(vertex);
            for (Edge e : vertex.getEdges(Direction.OUT)) {
                removeEdge(e);
            }
            for (Edge e : vertex.getEdges(Direction.IN)) {
                removeEdge(e);
            }
            Integer key = ((XVertexProxy) vertex).rawId();
            mutatedVertices.remove(key);
            deletedVertices.add(key);
        } catch (XCache.NotFoundException e) {
            log.error("Vertex {} not found", vertex);
        }
    }

    @Override
    public Iterable<Vertex> getVertices() {

        // union the baseline vertices to the transaction vertices. Unfortunately,
        // we need to flatten from iterables, b/c we need to compare the two sets.
        Set<Integer> u = Sets.union(newHashSet(XStoreFacade.getVertices(baseline)),
                                    mutatedVertices.keySet());

        // Then filter out the deleted vertices.
        return FluentIterable.from(u)
                             .filter(not(in(deletedVertices)))
                             .transform(XVertexProxy.makeVertex(graph))
                             .transform(XVertexProxy.UPCAST)
                             .toList();

        // May be a more efficient implementation than flattening everything. The mutated keys
        // (are probably much) smaller set, so take the (probably much) larger baseline keys,
        // remove the duplicated mutated keys, then add the mutated keys back
//        return FluentIterable.from(XStoreFacade.getVertices(baseline))
//                             .filter(not(in(mutatedVertices.keySet())))
//                             .append(mutatedVertices.keySet())
//                             .filter(not(in(deletedVertices)))
//                             .transform(XVertexProxy.makeVertex(graph))
//                             .transform(XVertexProxy.UPCAST);
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        // TODO: use indices
        return new PropertyFilteredIterable<>(key, value, this.getVertices());
    }


    // =================================
    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        // TODO: add to indices
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();
        XVertexProxy oV = (XVertexProxy) outVertex;
        XVertexProxy iV = (XVertexProxy) inVertex;
        // Check: are these real vertices?
        XEdge xe = new XEdge(graph.nextId(), oV.rawId(), iV.rawId(), label);
        mutatedEdges.put(xe.rawId(), xe);
        XEdgeProxy e = XEdgeProxy.of(xe, graph);
        oV.addOutEdge(e.rawId());
        iV.addInEdge(e.rawId());
        return e;
    }

    @Override
    public Edge getEdge(Object id) {
        // TODO how to handle non-existent or deleted nodes?
        try {
            XEdge xe = edgeImpl(castInt(id));
            if (null != xe)
                return XEdgeProxy.of(xe, graph);
        } catch (XCache.NotFoundException e) {
            log.error("could not find edge by key {}", id);
        } catch (NumberFormatException e) {
            log.error("could not use edge key {}", id);
        }
        return null;
    }

    XEdge edgeImpl(int id) {
        if (deletedEdges.contains(id))
            return null;
        XEdge e = mutatedEdges.get(id);
        return (null == e) ? XStoreFacade.getEdge(baseline, id) : e;
    }

    XEdge mutableEdgeImpl(int id) {
        if (deletedEdges.contains(id))
            return null;
        if (mutatedEdges.containsKey(id))
            return mutatedEdges.get(id);
        XEdge xe = XStoreFacade.getEdge(baseline, id);
        if (null != xe) {
            XEdge xem = new XEdge(xe);
            mutatedEdges.put(xem.rawId(), xem);
            return xem;
        }
        return null;
    }

    @Override
    public void removeEdge(Edge edge) {
        // TODO: remove from indices
        try {
            checkNotNull(edge);

            XEdgeProxy ep = (XEdgeProxy) edge;

            XVertexProxy vOut = ep.getOutVertex();
            vOut.removeEdge(ep.rawId());

            XVertexProxy vIn = ep.getInVertex();
            vIn.removeEdge(ep.rawId());

            mutatedEdges.remove(ep.rawId());
            deletedEdges.add(ep.rawId());
        } catch (XCache.NotFoundException e) {
            log.error("Edge {} not found", edge);
        }
    }

    @Override
    public Iterable<Edge> getEdges() {
        // Union the baseline edges to the transaction vertices. Unfortunately,
        // we need to flatten from iterables, b/c we need to compare the two sets.
        Set<Integer> u = Sets.union(newHashSet(XStoreFacade.getEdges(baseline)),
                                    mutatedEdges.keySet());
        // Then filter out the deleted edges.
        return FluentIterable.from(u)
                             .filter(not(in(deletedEdges)))
                             .transform(XEdgeProxy.makeEdge(graph))
                             .transform(XEdgeProxy.UPCAST)
                             .toList();

        // May be a more efficient implementation than flattening everything. The mutated keys
        // (are probably much) smaller set, so take the (probably much) larger baseline keys,
        // remove the duplicated mutated keys, then add the mutated keys back
//        return FluentIterable.from(XStoreFacade.getEdges(baseline))
//                             .filter(not(in(mutatedEdges.keySet())))
//                             .append(mutatedEdges.keySet())
//                             .filter(not(in(deletedEdges)))
//                             .transform(XEdgeProxy.makeEdge(graph))
//                             .transform(XEdgeProxy.UPCAST);

    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        // TODO: use indices
        return new PropertyFilteredIterable<>(key, value, this.getEdges());
    }
    // =================================

    @Override
    public Features getFeatures() {
        return graph.getFeatures();
    }


    @Override
    public GraphQuery query() {
        return null;
    }

    // =================================
    private void clear() {
        mutatedVertices.clear();
        deletedVertices.clear();
        mutatedEdges.clear();
        deletedEdges.clear();
    }

    @Deprecated
    @Override
    public void stopTransaction(Conclusion conclusion) {
        // *sigh* why are we forced to override deprecated methods?
    }

    @Override
    public void shutdown() {
        commit();
    }

    @Override
    public void commit() {
        log.info("=== TX '{}' Commit >>>", this);

        graph.repo().lock(); // TODO: lock target revision only?

        XStore.XRevision head = graph.repo().getHead();
        if (baseline.equals(head)) { // TODO check chain for branches
            log.info("FAST_FORWARD");
        } else {
            log.info("NOT A FAST_FORWARD!!");
        }
        log.info("head {} baseline {}", head, baseline);
        XStore.XWorking rev = XStore.XWorking.of(baseline.equals(head) ? baseline
                                                                       : head);

        XStoreFacade.addVertex(rev, mutatedVertices.values());
        XStoreFacade.removeVertex(rev, deletedVertices);
        XStoreFacade.addEdge(rev, mutatedEdges.values());
        XStoreFacade.removeEdge(rev, deletedEdges);
        rev.commit();
        clear();
        log.info("\t<<< end commit ===");

        graph.repo().unlock();
    }

    @Override
    public void rollback() {
        clear();
    }

    // =================================
    <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {
        log.info("createVertexKeyIndex");
        if (Vertex.class.isAssignableFrom(elementClass)) {
//            keyIndex.createVertexIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
//            this.edgeKeyIndex.createKeyIndex(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    // =================================
    void dump() {
        if (!log.isInfoEnabled())
            return;
        log.info("=== TX '{}' dump ===", this);
        if (!mutatedVertices.isEmpty() || !deletedVertices.isEmpty()) {
            log.info("  Vertices {}", mutatedVertices.size());
            for (XVertex xv : mutatedVertices.values()) {
                log.info("\t{}", xv);
            }
            if (!deletedVertices.isEmpty())
                log.info("\tX {}", deletedVertices);
        }
        if (!mutatedEdges.isEmpty() || !deletedEdges.isEmpty()) {
            log.info("  Edges {}", mutatedEdges.size());
            for (XEdge xe : mutatedEdges.values()) {
                log.info("\t{}", xe);
            }
            if (!deletedEdges.isEmpty())
                log.info("\tX {}", deletedEdges);
        }
    }
    // =================================


    // keyIndices
    // indices


    @Override
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter[] indexParameters) {
        return null;
    }

    @Override
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        return null;
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return null;
    }

    @Override
    public void dropIndex(String indexName) {

    }

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {

    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter[] indexParameters) {

    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return null;
    }

    // =================================
    @Override
    public String toString() {
        return name;
    }

    // =================================
    private final GitGraph graph;

    private Map<Integer, XVertex> mutatedVertices = newHashMap();
    private Set<Integer> deletedVertices = newHashSet();

    private Map<Integer, XEdge> mutatedEdges = newHashMap();
    private Set<Integer> deletedEdges = newHashSet();

    XStore.XRevision baseline;

    private static final AtomicInteger transactionCounter = new AtomicInteger(0);
    final String name;
}
