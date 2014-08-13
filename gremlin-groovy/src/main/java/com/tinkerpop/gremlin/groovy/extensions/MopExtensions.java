package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Created by jason on 8/12/14.
 */
public
class MopExtensions {

    public static
    <S, E, E2> GraphTraversal<S, E2> propertyMissing(GraphTraversal<S, E> self, String name) {
        if (GremlinGroovySupport.isStep(name)) {
            return invokeStepMethod(self, name);
        }
        else {
            return self.value(name);
        }
    }


    public static
    Object propertyMissing(Graph self, final String name) {
        if (GremlinGroovySupport.isStep(name)) {
            return invokeStepMethod(self, name);
        }
        else {
            throw new MissingPropertyException(name, self.getClass());
        }
    }

    public static
    Object propertyMissing(Element self, final String name) {
        if (GremlinGroovySupport.isStep(name)) {
            return invokeStepMethod(self, name);
        }
        else {
            return self.value(name);
            //  throw new MissingPropertyException(name, self.getClass());
        }
    }

    public static
    void propertyMissing(Element self, final String name, Object value) {
        self.property(name, value);
    }


    public static
    Object propertyMissing(Traverser self, final String name) {
        return InvokerHelper.getProperty(self.get(), name);
    }

    public static
    Object methodMissing(Traverser self, final String name, Object[] args) {
        return InvokerHelper.invokeMethod(self.get(), name, args);
    }

    private static
    <E> E invokeStepMethod(Object self, String name) {
        return (E) InvokerHelper.invokeMethod(self, name, null);
    }


}
