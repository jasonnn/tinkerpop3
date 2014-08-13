package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.groovy.GFunction;
import com.tinkerpop.gremlin.groovy.GSupplier;
import com.tinkerpop.gremlin.process.Path;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.util.function.SFunction;
import groovy.lang.Closure;
import groovy.lang.Range;

import java.util.List;
import java.util.Map;

/**
 * Created by jason on 8/12/14.
 */
public
class GraphTraversalExtensions {
    public static
    <S, E> GraphTraversal<S, E> getAt(GraphTraversal<S, E> self, final Integer index) {
        return self.range(index, index);
    }

    public static
    <S, E> GraphTraversal<S, E> getAt(GraphTraversal<S, E> self, final Range range) {
        return (self).range((Integer) range.getFrom(), (Integer) range.getTo());
    }

    // THE CODE BELOW IS REQUIRED UNTIL GROOVY 2.3+ FIXES VAR ARG CONVERSION OF CLOSURES TO LAMBDAS

    public static
    <S, E> GraphTraversal<S, Path> path(GraphTraversal<S, E> self, final Closure... pathClosures) {
        return (self).path(GFunction.make(pathClosures));
    }

    public static
    <S, E> GraphTraversal<S, Map<String, E>> select(GraphTraversal<S, E> self, final List<String> asLabels) {
        return self.select(asLabels, new SFunction[0]);
    }

    public static
    <S, E> GraphTraversal<S, Map<String, E>> select(GraphTraversal<S, E> self,
                                                    final List<String> asLabels,
                                                    final Closure... stepClosures) {
        return self.select(asLabels, GFunction.make(stepClosures));
    }

    public static
    <S, E> GraphTraversal<S, Map<String, E>> select(GraphTraversal<S, E> self,
        final Closure...stepClosures){
        return (self).select(GFunction.make(stepClosures));
    }

    public static
    <S, E> GraphTraversal<S, E> tree(GraphTraversal<S, E> self,
                                                    final Closure...branchClosures){

        return (self).tree(GFunction.make(branchClosures));
    }

//    <S, E> GraphTraversal<S, E> pageRank(GraphTraversal<S, E> self,
//                                     final Closure closure){
//
//        final Closure newClosure = closure.dehydrate();
//        return  self.pageRank(new GSupplier(newClosure));
//    }
}
