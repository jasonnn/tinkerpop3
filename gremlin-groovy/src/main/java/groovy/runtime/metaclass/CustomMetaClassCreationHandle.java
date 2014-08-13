package groovy.runtime.metaclass;

import com.tinkerpop.gremlin.groovy.extensions.GremlinMetaClass;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jason on 8/13/14.
 */
public
class CustomMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {
    private static final Set<Class> classes = new HashSet<>(Arrays.asList(GraphTraversal.class,
                                                                          Graph.class,
                                                                          Vertex.class,
                                                                          Edge.class,
                                                                          Element.class));

    @Override
    protected
    MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (classes.contains(theClass))
            return new GremlinMetaClass(registry, theClass);
        return super.createNormalMetaClass(theClass, registry);
    }
}
