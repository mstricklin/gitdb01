// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.base.Function;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XEdgeProxy extends XElementProxy implements Edge {
    // so annoying...
    public static Function<XEdgeProxy, Edge> UPCAST = new Function<XEdgeProxy, Edge>() {
        @Override
        public Edge apply(XEdgeProxy ep) {
            return ep;
        }
    };
    public static Function<Integer, XEdgeProxy> makeEdge(final GitGraph graph) {
        return new Function<Integer, XEdgeProxy>() {
            @Override
            public XEdgeProxy apply(Integer id) {
                return XEdgeProxy.of(id, graph);
            }
        };
    }
    public static XEdgeProxy of(final XEdge xe, final GitGraph gg) {
        return new XEdgeProxy(xe.rawId(), xe.outId, xe.inId, xe.label, gg);
    }
    public static XEdgeProxy of(int id, final GitGraph gg) {
        return new XEdgeProxy(id, gg);
    }

    public static XEdgeProxy of(int id, int outVertexId, int inVertexId, String label, final GitGraph graph) {
        return new XEdgeProxy(id, outVertexId, inVertexId, label, graph);
    }

    XEdgeProxy(int id_, GitGraph graph_) {
        super(id_, graph_);
//        outId = outVertexId;
//        inId = inVertexId;
//        label = label_;
    }
    XEdgeProxy(int id_, int outVertexId, int inVertexId, String label_, GitGraph graph_) {
        super(id_, graph_);
//        outId = outVertexId;
//        inId = inVertexId;
//        label = label_;
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.OUT))
            return getOutVertex();
        if (direction.equals(Direction.IN))
            return getInVertex();
        throw ExceptionFactory.bothIsNotSupported();
    }

    XVertexProxy getOutVertex() {
        return XVertexProxy.of(getImpl().outId, graph);
    }

    XVertexProxy getInVertex() {
        return XVertexProxy.of(getImpl().inId, graph);
    }

    public void remove() {
        this.graph.removeEdge(this);
    }

    // =================================
    @Override
    public String getLabel() {
        return getImpl().label;
    }
    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }
    @Override
    protected XEdge getImpl() {
        return graph.getEdgeImpl(rawId());
    }
    @Override
    protected XEdge getMutableImpl() {
        return graph.mutableEdgeImpl(rawId());
    }

    // =================================
    public static class XEdge extends XElement {
        private XEdge() {
            this(-1, -1, -1, "");
        }
        XEdge(final XEdge xe) {
            super(xe);
            this.outId = xe.outId;
            this.inId = xe.inId;
            this.label = xe.label;
        }
        XEdge(int id, int outId, int inId, String label) {
            super(id);
            this.outId = outId;
            this.inId = inId;
            this.label = label;
        }

        private final int outId, inId;
        private final String label;

        public String toString() {
            return String.format("XEdge(%s, label=%s, outId=%s, inId=%s)", super.toString(), label, outId, inId);
        }
    }

    // =================================
//    private final int outId, inId;
//    private final String label;


}
