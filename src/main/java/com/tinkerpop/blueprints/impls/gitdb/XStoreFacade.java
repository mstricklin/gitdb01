package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.*;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore.XRevision;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore.XWorking;

public class XStoreFacade {

    static void addVertex(XWorking rev, Iterable<XVertex> vi) {
        rev.put(Iterables.transform(vi, Keyed.UPCAST));
    }

    static XVertex getVertex(XRevision rev, int id) {
        return (XVertex) rev.get(id);
    }

    static void removeVertex(XWorking rev, Iterable<Integer> ids) {
        rev.remove(ids);
    }

    static Iterable<Integer> getVertices(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XVertex.class)
                             .transform(Keyed.GET_ID);
    }

    // =================================
    static void addEdge(XWorking rev, Iterable<XEdge> ei) {
        rev.put(Iterables.transform(ei, Keyed.UPCAST));
    }

    static XEdge getEdge(XRevision rev, int id) {
        return (XEdge) rev.get(id);
    }

    static void removeEdge(XWorking rev, Iterable<Integer> id) {
        rev.remove(id);
    }

    static Iterable<Integer> getEdges(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XEdge.class)
                             .transform(Keyed.GET_ID);
    }
}
