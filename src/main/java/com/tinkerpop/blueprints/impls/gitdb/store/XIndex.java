package com.tinkerpop.blueprints.impls.gitdb.store;

import com.tinkerpop.blueprints.impls.gitdb.Keyed;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class XIndex {

    public static XIndex of(final XIndex baseline) {
        return new XIndex(baseline);
    }

    private XIndex(final XIndex baseline) {
        this.baseline = baseline;
        store = baseline.store;
        this.indexId = objectCounter.getAndIncrement();
    }
    public void put(Iterable<Keyed> it) {
        for (Keyed k: it)
            index.put(k.rawId(), store.put(k));
    }

    public void remove(Iterable<Integer> it) {
        for (Integer k: it)
            index.remove(k);
    }

    private Integer lookup(int k) {
        return index.get(k);
    }


    protected final Integer indexId;
    protected final XStore store;
    private final XIndex baseline;
    private final Map<Integer, Integer> index = newHashMap();

    private static final AtomicInteger objectCounter = new AtomicInteger(0);

}
