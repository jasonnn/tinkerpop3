package com.tinkerpop.gremlin.process.graph.step.sideEffect;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.computer.MapReduce;
import com.tinkerpop.gremlin.process.graph.marker.MapReducer;
import com.tinkerpop.gremlin.process.graph.marker.Reversible;
import com.tinkerpop.gremlin.process.graph.marker.SideEffectCapable;
import com.tinkerpop.gremlin.process.graph.marker.VertexCentric;
import com.tinkerpop.gremlin.process.graph.step.filter.FilterStep;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.mapreduce.GroupByMapReduce;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.util.function.SFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GroupByStep<S, K, V, R> extends FilterStep<S> implements SideEffectCapable, Reversible, VertexCentric, MapReducer<Object, Collection, Object, Object, Map> {

    public Map<K, Collection<V>> groupByMap;
    public final Map<K, R> reduceMap;
    public final SFunction<S, K> keyFunction;
    public final SFunction<S, V> valueFunction;
    public final SFunction<Collection<V>, R> reduceFunction;
    public final String variable;
    public boolean vertexCentric = false;

    public GroupByStep(final Traversal traversal, final String variable, final SFunction<S, K> keyFunction, final SFunction<S, V> valueFunction, final SFunction<Collection<V>, R> reduceFunction) {
        super(traversal);
        this.variable = variable;
        this.groupByMap = this.traversal.memory().getOrCreate(this.variable, HashMap<K, Collection<V>>::new);
        this.reduceMap = new HashMap<>();
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction == null ? s -> (V) s : valueFunction;
        this.reduceFunction = reduceFunction;
        this.setPredicate(traverser -> {
            doGroup(traverser.get(), this.groupByMap, this.keyFunction, this.valueFunction);
            if (!vertexCentric) {
                if (null != reduceFunction && !this.starts.hasNext()) {
                    doReduce(this.groupByMap, this.reduceMap, this.reduceFunction);
                    this.traversal.memory().set(this.variable, this.reduceMap);
                }
            }
            return true;
        });
    }

    private static <S, K, V> void doGroup(final S s, final Map<K, Collection<V>> groupMap, final SFunction<S, K> keyFunction, final SFunction<S, V> valueFunction) {
        final K key = keyFunction.apply(s);
        final V value = valueFunction.apply(s);
        Collection<V> values = groupMap.get(key);
        if (null == values) {
            values = new ArrayList<>();
            groupMap.put(key, values);
        }
        GroupByStep.addValue(value, values);
    }

    private static <K, V, R> void doReduce(final Map<K, Collection<V>> groupMap, final Map<K, R> reduceMap, final SFunction<Collection<V>, R> reduceFunction) {
        groupMap.forEach((k, vv) -> {
            reduceMap.put(k, (R) reduceFunction.apply(vv));
        });
    }

    public static void addValue(final Object value, final Collection values) {
        if (value instanceof Iterator) {
            while (((Iterator) value).hasNext()) {
                values.add(((Iterator) value).next());
            }
        } else {
            values.add(value);
        }
    }

    public String toString() {
        return this.variable.equals(SideEffectCapable.CAP_KEY) ?
                super.toString() :
                TraversalHelper.makeStepString(this, this.variable);
    }

    public String getVariable() {
        return this.variable;
    }

    public void setCurrentVertex(final Vertex vertex) {
        this.vertexCentric = true;
        this.groupByMap = vertex.<java.util.Map<K, Collection<V>>>property(Graph.Key.hidden(this.variable)).orElse(new HashMap<>());
        if (!vertex.property(Graph.Key.hidden(this.variable)).isPresent())
            vertex.property(Graph.Key.hidden(this.variable), this.groupByMap);
    }

    public MapReduce<Object, Collection, Object, Object, Map> getMapReduce() {
        return new GroupByMapReduce(this);
    }
}
