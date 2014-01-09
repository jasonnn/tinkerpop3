package com.tinkerpop.blueprints.strategy;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Strategy;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.query.GraphQuery;
import com.tinkerpop.blueprints.util.TriFunction;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface GraphStrategy {
    public default UnaryOperator<Function<Object[],Vertex>> getAddVertexStrategy(final Strategy.Context<Graph> ctx) {
        return UnaryOperator.identity();
    }

    public default UnaryOperator<TriFunction<String, Vertex, Object[], Edge>> getAddEdgeStrategy(final Strategy.Context<Vertex> ctx) {
        return UnaryOperator.identity();
    }

    public default UnaryOperator<Supplier<Void>> getRemoveVertexStrategy(final Strategy.Context<Vertex> ctx) {
        return UnaryOperator.identity();
    }

    public default UnaryOperator<Supplier<Iterable<Vertex>>> getGraphQueryVerticesStrategy(final Strategy.Context<GraphQuery> ctx) {
        return UnaryOperator.identity();
    }

    public default UnaryOperator<Function<Object[], GraphQuery>> getGraphQueryIdsStrategy(final Strategy.Context<GraphQuery> ctx) {
        return UnaryOperator.identity();
    }
}