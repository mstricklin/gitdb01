// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

@Slf4j
public class XVertexProxy extends XElementProxy implements Vertex {
    // so annoying. I blame James Gosling for my carpal tunnel.
    public static Function<XVertexProxy, Vertex> UPCAST = new Function<XVertexProxy, Vertex>() {
        @Override
        public Vertex apply(XVertexProxy vp) {
            return vp;
        }
    };
    public static Function<Integer, XVertexProxy> makeVertex(final GitGraph graph) {
        return new Function<Integer, XVertexProxy>() {
            @Override
            public XVertexProxy apply(Integer id) {
                return XVertexProxy.of(id, graph);
            }
        };
    }


    public static XVertexProxy of(final XVertex xv, final GitGraph gg) {
        return new XVertexProxy(xv.rawId(), gg);
    }
    public static XVertexProxy of(int id, final GitGraph graph) {
        return new XVertexProxy(id, graph);
    }

    XVertexProxy(int id, final GitGraph graph) {
        super(id, graph);
    }
    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
//        log.debug("getEdges for {} {}", this, direction);
        XVertex xv = getImpl();
        if (direction.equals(Direction.OUT)) {
            return getEdges(xv.outEdges, asList(labels));
        } else if (direction.equals((Direction.IN))) {
            return getEdges(xv.inEdges, asList(labels));
        } else if (direction.equals((Direction.BOTH))) {
            Iterable<Integer> edges = Iterables.concat( xv.inEdges, xv.outEdges );
            return getEdges(edges, asList(labels));
        }
        return Collections.emptyList();
    }
    // =================================
    // annoyingly, an empty label values is a special case for 'all'
    private List<Edge> getEdges(Iterable<Integer> edgeIDs, final Collection<String> labels) {
        Iterator<XEdgeProxy> ie = Iterators.transform(edgeIDs.iterator(), XEdgeProxy.makeEdge(graph));
        if (labels.isEmpty()) {
            return new ImmutableList.Builder<Edge>()
                    .addAll(ie)
                    .build();
        }
        Predicate<XEdgeProxy> labelled = new Predicate<XEdgeProxy>() {
            @Override
            public boolean apply(XEdgeProxy ep) {
                return labels.contains(ep.getLabel());
            }
        };
        return new ImmutableList.Builder<Edge>()
                .addAll(Iterators.filter(ie, labelled))
                .build();
    }
    // =================================
    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return new VerticesFromEdgesIterable(this, direction, labels);
    }
    @Override
    public VertexQuery query() {
        return new DefaultVertexQuery(this);
    }
    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        return graph.addEdge(null, this, inVertex, label);
    }
    // =================================
    void addOutEdge(int edgeId) {
        getMutableImpl().outEdges.add(edgeId);
    }
    void addInEdge(int edgeId) {
        getMutableImpl().inEdges.add(edgeId);
    }
    void removeEdge(int edgeID) {
        getMutableImpl().outEdges.remove(edgeID);
        getMutableImpl().inEdges.remove(edgeID);
    }
    public void remove() {
        this.graph.removeVertex(this);
    }
    // =================================
//    @Override
//    public String toString() {
//        return StringFactory.vertexString(this);
//    }
    @Override
    public String toString() {
        return getImpl().toString();
    }
    // =================================
    @Override
    protected XVertex getImpl() {
        return graph.getVertexImpl(rawId());
    }
    @Override
    protected XVertex getMutableImpl() {
        return graph.mutableVertexImpl(rawId());
    }

    public static class XVertex extends XElementProxy.XElement {
        private XVertex() { // no-arg ctor for jackson-json rehydration
            super();
            outEdges = null;
            inEdges = null;
        }
        public XVertex(int id) {
            super(id);
            outEdges = newHashSet();
            inEdges = newHashSet();
        }
        XVertex(final XVertex xv) {
            super(xv);
            outEdges = newHashSet(xv.outEdges);
            inEdges = newHashSet(xv.inEdges);
        }

        public static final XVertex deleted() {
            return DELETED;
        }

        private final Set<Integer> outEdges;
        private final Set<Integer> inEdges;
        private static final XVertex DELETED = new XVertex();

        public String toString() {
            return String.format("XVertex(%s, outEdges=%s, inEdges=%s)", super.toString(), outEdges, inEdges);
        }
    }
    // =================================
}
