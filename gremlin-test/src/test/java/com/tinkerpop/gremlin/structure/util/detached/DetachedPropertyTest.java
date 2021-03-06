package com.tinkerpop.gremlin.structure.util.detached;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.SingleGraphTraversal;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class DetachedPropertyTest {

    private DetachedProperty mp;

    @Before
    public void setup() {
        final Vertex v = mock(Vertex.class);
        when(v.id()).thenReturn("1");
        when(v.label()).thenReturn("person");

        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k");
        when(p.getElement()).thenReturn(v);
        when(p.value()).thenReturn("val");


        this.mp = DetachedProperty.detach(p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotConstructWithNullProperty() {
        DetachedProperty.detach(null);
    }

    @Test
    public void shouldConstructMicroPropertyWithPropertyFromVertex() {
        assertEquals("k", mp.key());
        assertEquals("val", mp.value());
        assertEquals(DetachedVertex.class, mp.getElement().getClass());
    }

    @Test
    public void shouldConstructMicroPropertyWithPropertyFromEdge() {
        final Vertex v1 = mock(Vertex.class);
        final Vertex v2 = mock(Vertex.class);
        final Edge e = mock(Edge.class);
        when(e.id()).thenReturn("14");
        when(e.label()).thenReturn("knows");
        when(e.outV()).thenReturn(new SingleGraphTraversal(v1));
        when(e.inV()).thenReturn(new SingleGraphTraversal(v2));

        when(v1.id()).thenReturn("1");
        when(v1.label()).thenReturn("person");
        when(v2.id()).thenReturn("2");
        when(v2.label()).thenReturn("person");

        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k");
        when(p.getElement()).thenReturn(e);
        when(p.value()).thenReturn("val");

        final DetachedProperty mp = DetachedProperty.detach(p);
        assertEquals("k", mp.key());
        assertEquals("val", mp.value());
        assertEquals(DetachedEdge.class, mp.getElement().getClass());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotSupportRemove() {
        this.mp.remove();
    }

    @Test
    public void shouldBeEqualsProperties() {
        final Vertex v = mock(Vertex.class);
        when(v.id()).thenReturn("1");
        when(v.label()).thenReturn("person");
        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k");
        when(p.getElement()).thenReturn(v);
        when(p.value()).thenReturn("val");

        final DetachedProperty mp2 = DetachedProperty.detach(p);

        assertTrue(mp2.equals(this.mp));
    }

    @Test
    public void shouldNotBeEqualsPropertiesAsThereIsDifferentElement() {
        final Vertex v = mock(Vertex.class);
        when(v.id()).thenReturn("2");
        when(v.label()).thenReturn("person");
        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k");
        when(p.getElement()).thenReturn(v);
        when(p.value()).thenReturn("val");

        final DetachedProperty mp2 = DetachedProperty.detach(p);

        assertFalse(mp2.equals(this.mp));
    }

    @Test
    public void shouldNotBeEqualsPropertiesAsThereIsDifferentKey() {
        final Vertex v = mock(Vertex.class);
        when(v.id()).thenReturn("1");
        when(v.label()).thenReturn("person");
        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k1");
        when(p.getElement()).thenReturn(v);
        when(p.value()).thenReturn("val");

        final DetachedProperty mp2 = DetachedProperty.detach(p);

        assertFalse(mp2.equals(this.mp));
    }

    @Test
    public void shouldNotBeEqualsPropertiesAsThereIsDifferentValue() {
        final Vertex v = mock(Vertex.class);
        when(v.id()).thenReturn("1");
        when(v.label()).thenReturn("person");
        final Property p = mock(Property.class);
        when(p.key()).thenReturn("k");
        when(p.getElement()).thenReturn(v);
        when(p.value()).thenReturn("val1");

        final DetachedProperty mp2 = DetachedProperty.detach(p);

        assertFalse(mp2.equals(this.mp));
    }
}
