// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.gitdb;

public class GitExceptionFactory {
    public static IllegalArgumentException vertexDeleted(final Object id) {
        return new IllegalArgumentException("Vertex has been deleted: " + id);
    }
}
