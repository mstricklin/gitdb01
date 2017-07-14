package com.tinkerpop.blueprints.impls.gitdb;


public abstract class Action {
    Action(final XCache c) {
        this.cache = c;
    }
    abstract void apply();
    final XCache cache;

    // =================================
    public static class AddVertex extends Action {
        AddVertex(int id, final XCache c) {
            super(c);
            this.id = id;
        }
        @Override
        void apply() {
            cache.addVertex(id);
        }
        @Override
        public String toString() {
            return "AddVertex " + id;
        }
        private final int id;
    }
    // =================================
    public static class RemoveVertex extends Action {
        RemoveVertex(int id, final XCache c) {
            super(c);
            this.id = id;
        }
        @Override
        void apply() {
            cache.removeVertex(id);
        }
        @Override
        public String toString() {
            return "RemoveVertex " + id;
        }
        private final int id;
    }
    // =================================
    public static class AddEdge extends Action {
        AddEdge(int id, int outVertex, int inVertex, String label, final XCache c) {
            super(c);
            this.id = id;
            this.outVertex = outVertex;
            this.inVertex = inVertex;
            this.label = label;
        }
        @Override
        void apply() {
            cache.addEdge(id, outVertex, inVertex, label);
        }
        @Override
        public String toString() {
            return "AddEdge " + id;
        }
        private final int id, outVertex, inVertex;
        private final String label;
    }
    // =================================
    public static class RemoveEdge extends Action {
        RemoveEdge(int id, final XCache c) {
            super(c);
            this.id = id;
        }
        @Override
        void apply() {
            cache.removeEdge(id);
        }
        @Override
        public String toString() {
            return "RemoveEdge " + id;
        }
        private final int id;
    }
    // =================================


}
