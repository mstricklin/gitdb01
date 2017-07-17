package com.tinkerpop.blueprints.impls.gitdb;

import com.google.common.collect.*;
import com.tinkerpop.blueprints.impls.gitdb.XEdgeProxy.XEdge;
import com.tinkerpop.blueprints.impls.gitdb.XVertexProxy.XVertex;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore.XRevision;
import com.tinkerpop.blueprints.impls.gitdb.store.XStore.XWorking;

public class XStoreFacade {

    static int addVertex(XWorking rev, XVertex v) {
        return rev.put(v);
    }

    static XVertex getVertex(XRevision rev, int id) {
        return (XVertex) rev.get(id);
    }

    static void removeVertex(XWorking rev, int id) {
        rev.remove(id);
    }

    static Iterable<Integer> getVertices(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XVertex.class)
                             .transform(Keyed.RAW_ID);
    }

    // =================================
    static int addEdge(XWorking rev, XEdge v) {
        return rev.put(v);
    }

    static XEdge getEdge(XRevision rev, int id) {
        return (XEdge) rev.get(id);
    }

    static void removeEdge(XWorking rev, int id) {
        rev.remove(id);
    }

    static Iterable<Integer> getEdges(XRevision rev) {
        return FluentIterable.from(rev.list())
                             .filter(XEdge.class)
                             .transform(Keyed.RAW_ID);
    }
}
