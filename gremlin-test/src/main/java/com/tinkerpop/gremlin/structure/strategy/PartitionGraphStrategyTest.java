package com.tinkerpop.gremlin.structure.strategy;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class PartitionGraphStrategyTest extends AbstractGremlinTest {
    private static final String partition = Graph.Key.hidden("partition");

    public PartitionGraphStrategyTest() {
        super(new PartitionGraphStrategy(partition, "A"));
    }

    @Test
    public void shouldAppendPartitionToVertex() {
        final Vertex v = g.addVertex("any", "thing");

        assertNotNull(v);
        assertEquals("thing", v.property("any").value());
        assertEquals("A", v.property(partition).value());
    }

    @Test
    public void shouldAppendPartitionToEdge() {
        final Vertex v1 = g.addVertex("any", "thing");
        final Vertex v2 = g.addVertex("some", "thing");
        final Edge e = v1.addEdge("connectsTo", v2, "every", "thing");

        assertNotNull(v1);
        assertEquals("thing", v1.property("any").value());
        assertEquals("A", v2.property(partition).value());

        assertNotNull(v2);
        assertEquals("thing", v2.property("some").value());
        assertEquals("A", v2.property(partition).value());

        assertNotNull(e);
        assertEquals("thing", e.property("every").value());
        assertEquals("connectsTo", e.label());
        assertEquals("A", e.property(partition).value());
    }

    @Test
    public void shouldWriteVerticesToMultiplePartitions() {
        final Vertex vA = g.addVertex("any", "a");
        final PartitionGraphStrategy strategy = (PartitionGraphStrategy) ((StrategyWrappedGraph) g).strategy().getGraphStrategy().get();
        strategy.setWritePartition("B");
        final Vertex vB = g.addVertex("any", "b");

        assertNotNull(vA);
        assertEquals("a", vA.property("any").value());
        assertEquals("A", vA.property(partition).value());

        assertNotNull(vB);
        assertEquals("b", vB.property("any").value());
        assertEquals("B", vB.property(partition).value());

        /* not applicable to SubgraphStrategy
        final GraphTraversal t = g.V();
        assertTrue(t.strategies().get().stream().anyMatch(o -> o.getClass().equals(PartitionGraphStrategy.PartitionGraphTraversalStrategy.class)));
        */

        g.V().forEach(v -> {
            assertTrue(v instanceof StrategyWrappedVertex);
            assertEquals("a", v.property("any").value());
        });

        strategy.removeReadPartition("A");
        strategy.addReadPartition("B");

        g.V().forEach(v -> {
            assertTrue(v instanceof StrategyWrappedVertex);
            assertEquals("b", v.property("any").value());
        });

        strategy.addReadPartition("A");
        assertEquals(new Long(2), g.V().count().next());
    }

    @Test
    public void shouldThrowExceptionOnvInDifferentPartition() {
        final Vertex vA = g.addVertex("any", "a");
        assertEquals(vA.id(), g.v(vA.id()).id());

        final PartitionGraphStrategy strategy = (PartitionGraphStrategy) ((StrategyWrappedGraph) g).strategy().getGraphStrategy().get();
        strategy.clearReadPartitions();

        try {
            g.v(vA.id());
        } catch (Exception ex) {
            final Exception expected = Graph.Exceptions.elementNotFound(Vertex.class, vA.id());
            assertEquals(expected.getClass(), ex.getClass());
            assertEquals(expected.getMessage(), ex.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionOneInDifferentPartition() {
        final Vertex vA = g.addVertex("any", "a");
        final Edge e = vA.addEdge("knows", vA);
        assertEquals(e.id(), g.e(e.id()).id());

        final PartitionGraphStrategy strategy = (PartitionGraphStrategy) ((StrategyWrappedGraph) g).strategy().getGraphStrategy().get();
        strategy.clearReadPartitions();

        try {
            g.e(e.id());
        } catch (Exception ex) {
            final Exception expected = Graph.Exceptions.elementNotFound(Edge.class, e.id());
            assertEquals(expected.getClass(), ex.getClass());
            assertEquals(expected.getMessage(), ex.getMessage());
        }
    }

    @Test
    public void shouldWriteToMultiplePartitions() {
        final Vertex vA = g.addVertex("any", "a");
        final Vertex vAA = g.addVertex("any", "aa");
        final Edge eAtoAA = vA.addEdge("a->a", vAA);

        final PartitionGraphStrategy strategy = (PartitionGraphStrategy) ((StrategyWrappedGraph) g).strategy().getGraphStrategy().get();
        strategy.setWritePartition("B");
        final Vertex vB = g.addVertex("any", "b");
        vA.addEdge("a->b", vB);

        strategy.setWritePartition("C");
        final Vertex vC = g.addVertex("any", "c");
        final Edge eBtovC = vB.addEdge("b->c", vC);
        final Edge eAtovC = vA.addEdge("a->c", vC);

        /* not applicable to SubgraphStrategy
        final GraphTraversal t = g.V();
        assertTrue(t.strategies().get().stream().anyMatch(o -> o.getClass().equals(PartitionGraphStrategy.PartitionGraphTraversalStrategy.class)));
        */

        strategy.clearReadPartitions();
        assertEquals(new Long(0), g.V().count().next());
        assertEquals(new Long(0), g.E().count().next());

        strategy.addReadPartition("A");
        assertEquals(new Long(2), g.V().count().next());
        assertEquals(new Long(1), g.E().count().next());
        assertEquals(new Long(1), g.v(vA.id()).outE().count().next());
        assertEquals(eAtoAA.id(), g.v(vA.id()).outE().next().id());
        assertEquals(new Long(1), g.v(vA.id()).out().count().next());
        assertEquals(vAA.id(), g.v(vA.id()).out().next().id());

        strategy.addReadPartition("B");
        assertEquals(new Long(3), g.V().count().next());
        assertEquals(new Long(2), g.E().count().next());

        strategy.addReadPartition("C");
        assertEquals(new Long(4), g.V().count().next());
        assertEquals(new Long(4), g.E().count().next());

        strategy.removeReadPartition("A");
        strategy.removeReadPartition("B");

        assertEquals(new Long(1), g.V().count().next());
        // two edges are in the "C" partition, but one each of their incident vertices are not
        assertEquals(new Long(0), g.E().count().next());

        assertEquals(new Long(0), g.v(vC.id()).inE().count().next());
        assertEquals(new Long(0), g.v(vC.id()).in().count().next());

        strategy.addReadPartition("B");
        // only one edge in, due to excluded vertices; vA is not in {B,C}
        assertEquals(new Long(1), g.v(vC.id()).inE().count().next());
        assertEquals(new Long(1), g.v(vC.id()).in().count().next());
        assertEquals(vC.id(), g.e(eBtovC.id()).inV().id().next());
        assertEquals(vB.id(), g.e(eBtovC.id()).outV().id().next());
        assertEquals(vC.id(), g.e(eAtovC.id()).inV().id().next());
        assertFalse(g.e(eAtovC.id()).outV().hasNext());

        /* not applicable to SubgraphStrategy
        strategy.addReadPartition("A");
        g.v(vA.id()).out().out().forEach(v -> {
            assertTrue(v instanceof StrategyWrapped);
            assertFalse(((StrategyWrappedElement) v).getBaseElement() instanceof StrategyWrapped);
        });

        g.v(vA.id()).outE().inV().outE().forEach(e -> {
            assertTrue(e instanceof StrategyWrapped);
            assertFalse(((StrategyWrappedElement) e).getBaseElement() instanceof StrategyWrapped);
        });
        */
    }

    @Test
    public void shouldWrapProperties() {
        final Vertex v = g.addVertex("any", "a");
        final Edge e = v.addEdge("to", v, "all", "a");

        assertTrue(v.property("any") instanceof StrategyWrappedProperty);
        assertTrue(v.properties().get("any") instanceof StrategyWrappedProperty);

        assertTrue(e.property("all") instanceof StrategyWrappedProperty);
        assertTrue(e.properties().get("all") instanceof StrategyWrappedProperty);

        assertTrue(g.V().property("any").next() instanceof StrategyWrappedProperty);
        assertTrue(g.E().property("any").next() instanceof StrategyWrappedProperty);

    }
}
