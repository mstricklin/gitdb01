package edu.utexas.arlut.ciads;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.gitdb.GitGraph;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException, IllegalStateException {
        GitGraph g = GitGraph.of();

        List<Vertex> vl = Lists.newArrayList(g.addVertex(null),
                g.addVertex(null),
                g.addVertex(null));

        Vertex v1 = g.getVertex(1);
        Vertex v2 = g.getVertex(2);
        Vertex v3 = g.getVertex(3);
        g.removeVertex(v3);
        g.txDump();

        v1.addEdge("foo", v2);
        v1.setProperty("aaa", "one");

//        final List<Object> listB = newArrayList();
//        listB.add("try1");
//        listB.add(2);
//        log.info("listB {}", listB);
//
//        v1.setProperty("keyListMixed", listB);
//        log.info("v1 {}", v1);
//        List<Object> lo = v1.getProperty("keyListMixed");
//        log.info("lo {}", lo);

        g.txDump();
        g.baselineDump();
        g.commit();
        g.txDump();
        g.baselineDump();

        v2 = g.getVertex(2);
        v2.setProperty("bbb", "two");
        log.info("");
        g.txDump();
        g.baselineDump();

        log.info("");
        g.commit();
        g.txDump();
        g.baselineDump();

        log.info("");
        log.info("remove v2");
        g.removeVertex(v2);
        g.txDump();
        g.commit();
        g.txDump();
        g.baselineDump();



//        g.addEdge(null, v1, v2, "foo");
//
//
//        g.txDump();
//        log.info("v0 keys {}", v1.getPropertyKeys());
//        log.info("v0->(aaa) {}", v1.getProperty("aaa"));
//        log.info("v0->(bbb) {}", v1.getProperty("bbb"));
//
//        g.dump();
//        g.removeVertex(v3);
//        g.dump();
//
//
//
//        log.info("=== add edge ====");
//        v2.addEdge("FooEdge", v1);
//        g.dump();
//
//
//        log.info("v2 edges OUT  {}", v2.getEdges(Direction.OUT));
//        log.info("v2 edges IN   {}", v2.getEdges(Direction.IN));
//        log.info("v2 edges BOTH {}", v2.getEdges(Direction.BOTH));
//
////        log.info("=== remove v2 ====");
////        g.removeVertex(v2);
//
//
//        log.info("=== self edge ====");
//        v1.addEdge("self-edge", v1);
//        g.dump();
//
//        log.info("v1 edges OUT  {}", v1.getEdges(Direction.OUT));
//        log.info("v1 edges IN   {}", v1.getEdges(Direction.IN));
//        log.info("v1 edges BOTH {}", v1.getEdges(Direction.BOTH));
//
//        log.info("=== remove self vertex====");
//        g.removeVertex(v1);
//
//        //        trySetProperty(vertexA, "keyDate", new Date(), graph.getFeatures().supportsSerializableObjectProperty);
//        v2.setProperty("keyDate", "aaa");
//        log.info("v2: {}", v2);
//        log.info("v2: {}", ((XVertexProxy)v2).properties());




//        log.info("===========");
//        for (Vertex v: g.getVertices()) {
//           log.info("{} => {}", v.getId(), v);
//        }
    }

}
