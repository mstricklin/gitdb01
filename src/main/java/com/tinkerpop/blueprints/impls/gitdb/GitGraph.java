// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import static com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.*;
import static com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.tinkerpop.blueprints.*;

import com.tinkerpop.blueprints.util.StringFactory;
import lombok.extern.slf4j.Slf4j;

// Actions
// 1. vertex: add, remove, get, list [getEdges, get neighbors] (4+2)
//     1a. vertexProperty: set (add/change), remove, list (3)
// 2. edge: add, remove, get, list [getEnds, getLabel] (4+2)
//     2a. edgeProperty: set (add/change), remove, list (3)
// 3. keyIndex: create, remove, list (3)
//     3a.  (implicit) put, get, remove, count [getName] (4+1)
// 4. index: create, get, list, drop (4)
//     4a.  put, get, remove, count [getName] (4+1)

// MUTATING Actions
// 1. vertex: add, remove
//     1a. vertexProperty: set (add/change), remove
// 2. edge: add, remove
//     2a. edgeProperty: set (add/change), remove
// 3. keyIndex: create, remove
//     3a.  (implicit) put, remove
// 4. index: create, get, drop
//     4a.  put, remove


@Slf4j
public class GitGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph {

    private static final Features FEATURES = new Features();

    static {
        // TODO: revisit this...
        FEATURES.supportsSerializableObjectProperty = true; // TODO will this survive serialization?
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true; // TODO will this survive serialization?
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true; // TODO will this survive serialization?
        FEATURES.supportsStringProperty = true;

        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = false; //true; // TODO
        FEATURES.isWrapper = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = true;
        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    // =================================
    /*
    74fc730e803e0bfca94aa9ae7f7f156cd9773502
    80ac59cdf2c3d79693e2140b2717fe707ba43e5e
    f3f4be74e50c9cf036cc7d6b26f558f5c5f3fa51
    */
    public static final String anOID = "74fc730e803e0bfca94aa9ae7f7f156cd9773502";

    public static GitGraph of() throws IOException {
        return new GitGraph();
    }

    public GitGraph() throws IOException {
        idCounter.set(1);
        cache = XCache.of(this);
    }

    @Override
    public Features getFeatures() {
        return FEATURES;
    }

    public void baselineDump() {
        repo().dump();
    }

    public void txDump() {
        tx().dump();
    }

    XTransactionGraph tx() {
        return threadTransaction.get();
    }

    XCache repo() {
        return this.cache;
    }

    // =================================
    @Override
    public Vertex addVertex(Object id) {
        return tx().addVertex(id);
    }

    @Override
    public Vertex getVertex(Object id) {
        return tx().getVertex(id);
    }
    XVertex getVertexImpl(int id) {
        return tx().vertexImpl(id);
    }
    XVertex mutableVertexImpl(int id) {
        return tx().mutableVertexImpl(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        tx().removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return tx().getVertices();
    }

    Iterator<Integer> vertices() {
        // TODO: move to repo
        return Collections.emptyIterator();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return tx().getVertices(key, value);
    }

    // =================================
    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return tx().addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(Object id) {
        return tx().getEdge(id);
    }
    XEdge getEdgeImpl(int id) {
        return tx().edgeImpl(id);
    }
    XEdge mutableEdgeImpl(int id) {
        return tx().mutableEdgeImpl(id);
    }

    @Override
    public void removeEdge(Edge edge) {
        tx().removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return tx().getEdges();
    }

    Iterator<Integer> edges() {
        // TODO: move to repo
        return Collections.emptyIterator();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return tx().getEdges(key, value);
    }

    // =================================
    @Override
    public GraphQuery query() {
        return tx().query();
    }
    // =================================
    @Override
    public void shutdown() {
        // TODO: keep a list of transactions & commit
    }
    @Override
    public void commit() {
        // should we lock here?
        tx().commit();
    }

    @Override
    public void rollback() {
        tx().rollback();
    }

    @Deprecated
    @Override
    public void stopTransaction(Conclusion conclusion) {
    }

    // =================================
    // Index is an external structure, independent of vertices/edges.
    @Override
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter[] indexParameters) {
        return tx().createIndex(indexName, indexClass, indexParameters);
    }

    @Override
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        return tx().getIndex(indexName, indexClass);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return tx().getIndices();
    }

    @Override
    public void dropIndex(String indexName) {
        tx().dropIndex(indexName);
    }

    // KeyIndex add index to existing key/value properties
    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter[] indexParameters) {
        tx().createKeyIndex(key, elementClass, indexParameters);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        tx().dropKeyIndex(key, elementClass);
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return tx().getIndexedKeys(elementClass);
    }

    // =================================
    public String toString() {
//        return StringFactory.graphString(this, "vertices:" + vertex.size() + " edges:" + edge.size());
        // TODO: print HEAD rev?
        return StringFactory.graphString(this, "foo");
    }

    // =================================
    private final AtomicInteger idCounter = new AtomicInteger();
    int nextId() {
        // TODO: move to repo!!!
        return idCounter.getAndIncrement();
    }

    // =================================
    private ThreadLocal<XTransactionGraph> threadTransaction = new ThreadLocal<XTransactionGraph>() {
        @Override
        protected XTransactionGraph initialValue() {
            return new XTransactionGraph(GitGraph.this);
        }
    };
    private final GitKeyIndex<XVertexProxy> vertexKeyIndex = new GitKeyIndex(XVertexProxy.class, this);
    private final GitKeyIndex<XEdgeProxy> edgeKeyIndex = new GitKeyIndex(XEdgeProxy.class, this);



    private final XCache cache;

}
