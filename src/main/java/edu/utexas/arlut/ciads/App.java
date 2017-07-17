package edu.utexas.arlut.ciads;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.gitdb.GitGraph;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException, IllegalStateException, InterruptedException {
        GitGraph g = GitGraph.of();

        List<Vertex> vl0 = Lists.newArrayList(g.addVertex(null),
                                              g.addVertex(null));

//        Vertex v1 = g.getVertex(1);
//        Vertex v2 = g.getVertex(2);
        //g.removeVertex(v3);


        g.commit();
        g.txDump();
        g.revisionDump();
        g.repoDump();

        List<Vertex> vl1 = Lists.newArrayList(g.addVertex(null),
                                              g.addVertex(null));


        log.info("");
        g.commit();
        g.txDump();
        g.revisionDump();
        g.repoDump();

        log.info("");
        Vertex v1 = g.getVertex(1);
        g.removeVertex(v1);
        g.commit();
        g.txDump();
        g.revisionDump();
        g.repoDump();

        log.info("");
        Vertex v2 = g.getVertex(2);
        v2.setProperty("aaa", "one");
        g.commit();
        g.txDump();
        g.revisionDump();
        g.repoDump();

        log.info("");
        v2 = g.getVertex(2);
        v2.setProperty("bbb", "two");
        g.commit();
        g.txDump();
        g.revisionDump();
        g.repoDump();

        log.info("===========");
        int totalThreads = 2;
        final AtomicInteger expectedVertices = new AtomicInteger(0);
        final AtomicInteger completedThreads = new AtomicInteger(0);
        final List<Thread> threads = newArrayList();

        final GitGraph tgraph = GitGraph.of();;
        for (int i = 0; i < totalThreads; i++) {
            Thread t  = new Thread() {
                public void run() {
                    Vertex a = tgraph.addVertex(null);
                    expectedVertices.getAndAdd(1);
                    tgraph.commit();
                    completedThreads.getAndAdd(1);
                }
            };
            t.start();
            threads.add(t);
//            t.join();

        }
        for (Thread t: threads)
            t.join();
        log.info("completedThreads {}", completedThreads.get());
        log.info("vertex count {}/{}", expectedVertices.get(), size(tgraph.getVertices()));


//        Vertex a = tgraph.addVertex(null);
//        Vertex b = tgraph.addVertex(null);
//        Edge e = tgraph.addEdge(null, a, b, "friend");
//        tgraph.commit();



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
