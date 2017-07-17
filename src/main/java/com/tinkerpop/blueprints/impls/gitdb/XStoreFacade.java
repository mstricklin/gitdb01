package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.collect.*;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore.XRevision;

public class XStoreFacade {

    static int addVertex(XRevision rev, XVertex v) {
        return rev.put(v);
    }

    static XVertex getVertex(XRevision rev, int id) {
        return (XVertex) rev.get(id);
    }

    static void removeVertex(XRevision rev, int id) {
        rev.remove(id);
    }

    static Iterable<Integer> getVertices(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XVertex.class)
                             .transform(Keyed.RAW_ID);
    }

    // =================================
    static int addEdge(XRevision rev, XEdge v) {
        return rev.put(v);
    }

    static XEdge getEdge(XRevision rev, int id) {
        return (XEdge) rev.get(id);
    }

    static void removeEdge(XRevision rev, int id) {
        rev.remove(id);
    }

    static Iterable<Integer> getEdges(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XEdge.class)
                             .transform(Keyed.RAW_ID);
    }
}
