package com.tinkerpop.gremlin.process.graph.step.sideEffect;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class GroupCountTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_groupCountXnameX();

    public abstract Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_name_groupCount();

    public abstract Traversal<Vertex, Map<Object, Long>> get_g_V_filterXfalseX_groupCount();

    public abstract Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX();

    public abstract Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX();

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_outXcreatedX_groupCountXnameX() {
        final Traversal<Vertex, Map<Object, Long>> traversal = get_g_V_outXcreatedX_groupCountXnameX();
        System.out.println("Testing: " + traversal);
        final Map<Object, Long> map = traversal.next();
        assertEquals(map.size(), 2);
        assertEquals(map.get("lop"), Long.valueOf(3l));
        assertEquals(map.get("ripple"), Long.valueOf(1l));
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_outXcreatedX_name_groupCount() {
        final Traversal<Vertex, Map<Object, Long>> traversal = get_g_V_outXcreatedX_name_groupCount();
        System.out.println("Testing: " + traversal);
        final Map<Object, Long> map = traversal.next();
        assertEquals(map.size(), 2);
        assertEquals(map.get("lop").longValue(), 3l);
        assertEquals(map.get("ripple").longValue(), 1l);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_filterXfalseX_groupCount() {
        final Traversal<Vertex, Map<Object, Long>> traversal = get_g_V_filterXfalseX_groupCount();
        System.out.println("Testing: " + traversal);
        final Map<Object, Long> map = traversal.next();
        assertEquals(map.size(), 0);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX() {
        List<Traversal<Vertex, Map<Object, Long>>> traversals = new ArrayList<>();
        traversals.add(get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX());
        traversals.add(get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX());
        traversals.forEach(traversal -> {
            System.out.println("Testing: " + traversal);
            final Map<Object, Long> map = traversal.next();
            assertFalse(traversal.hasNext());
            assertEquals(map.size(), 4);
            assertEquals(map.get("lop").longValue(), 4l);
            assertEquals(map.get("ripple").longValue(), 2l);
            assertEquals(map.get("josh").longValue(), 1l);
            assertEquals(map.get("vadas").longValue(), 1l);
        });
    }

    public static class JavaGroupCountTest extends GroupCountTest {
        public Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_groupCountXnameX() {
            return (Traversal) g.V().out("created").groupCount(v -> v.value("name"));
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_name_groupCount() {
            return (Traversal) g.V().out("created").value("name").groupCount();
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_filterXfalseX_groupCount() {
            return (Traversal) g.V().filter(t -> false).groupCount();
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX() {
            return g.V().as("x").out()
                    .groupCount("a", v -> v.value("name"))
                    .jump("x", h -> h.getLoops() < 2).cap("a");
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX() {
            return g.V().as("x").out()
                    .groupCount("a", v -> v.value("name"))
                    .jump("x", 2).cap("a");
        }
    }

    public static class JavaComputerGroupCountTest extends GroupCountTest {
        public Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_groupCountXnameX() {
            return (Traversal) g.V().out("created").groupCount(v -> v.value("name")).submit(g.compute());
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_outXcreatedX_name_groupCount() {
            return (Traversal) g.V().out("created").value("name").groupCount().submit(g.compute());
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_filterXfalseX_groupCount() {
            return (Traversal) g.V().filter(t -> false).groupCount().submit(g.compute());
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX() {
            return g.V().as("x").out()
                    .groupCount("a", v -> v.value("name"))
                    .jump("x", t -> t.getLoops() < 2).<Map<Object, Long>>cap("a").submit(g.compute());
        }

        public Traversal<Vertex, Map<Object, Long>> get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX() {
            return g.V().as("x").out()
                    .groupCount("a", v -> v.value("name"))
                    .jump("x", 2).<Map<Object, Long>>cap("a").submit(g.compute());
        }
    }
}