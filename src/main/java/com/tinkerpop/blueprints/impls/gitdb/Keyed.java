// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;


import com.google.common.base.Function;
import com.google.common.base.Functions;

public interface Keyed {
    int rawId();
    Function<Keyed, Integer> GET_ID = new Function<Keyed, Integer>() {
        @Override
        public Integer apply(Keyed input) {
            return input.rawId();
        }
    };
    Function<Keyed, Keyed> UPCAST = Functions.identity();

}
