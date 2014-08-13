package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.process.Traversal;
import groovy.lang.MetaBeanProperty;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedMethod;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by jason on 8/12/14.
 */
public
class GremlinMetaClass extends MetaClassImpl {
    public
    GremlinMetaClass(Class theClass, MetaMethod[] add) {
        super(theClass, add);
    }

    public
    GremlinMetaClass(Class theClass) {
        super(theClass);
    }

    public
    GremlinMetaClass(MetaClassRegistry registry, Class theClass, MetaMethod[] add) {
        super(registry, theClass, add);
    }

    public
    GremlinMetaClass(MetaClassRegistry registry, Class theClass) {
        super(registry, theClass);
    }

    @Override
    public synchronized
    void initialize() {
        Predicate<CachedMethod> publicMethod = CachedMethod::isPublic;
        Predicate<CachedMethod> noArgs = (cm) -> cm.getParamsCount() == 0;
        Predicate<CachedMethod> isTraversal = (cm) -> Traversal.class.isAssignableFrom(cm.getReturnType());
        Predicate<CachedMethod> all = publicMethod.and(noArgs).and(isTraversal);
        Stream.of(getTheCachedClass().getMethods())
              .filter(all)
              .forEach((cm) -> addMetaBeanProperty(new MetaBeanProperty(cm.getName(), cm.getReturnType(), cm, null)));

        super.initialize();
    }
}
