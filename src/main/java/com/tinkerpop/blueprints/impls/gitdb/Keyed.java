// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;


import com.google.common.base.Function;

public interface Keyed {
    int rawId();
    Function<Keyed, Integer> RAW_ID = new Function<Keyed, Integer>() {
        @Override
        public Integer apply(Keyed input) {
            return input.rawId();
        }
    };
}
