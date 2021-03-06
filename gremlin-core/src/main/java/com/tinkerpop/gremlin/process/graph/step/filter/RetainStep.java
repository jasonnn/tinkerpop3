package com.tinkerpop.gremlin.process.graph.step.filter;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.marker.Reversible;
import com.tinkerpop.gremlin.process.util.TraversalHelper;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RetainStep<S> extends FilterStep<S> implements Reversible {

    public final String variable;

    public RetainStep(final Traversal traversal, final String variable) {
        super(traversal);
        this.variable = variable;
        final Object retainObject = this.traversal.memory().getOrCreate(this.variable, HashSet::new);
        if (retainObject instanceof Collection)
            this.setPredicate(traverser -> ((Collection) retainObject).contains(traverser.get()));
        else
            this.setPredicate(traverser -> retainObject.equals(traverser.get()));
    }

    public RetainStep(final Traversal traversal, final Collection<S> retainCollection) {
        super(traversal);
        this.variable = null;
        this.setPredicate(traverser -> retainCollection.contains(traverser.get()));
    }

    public RetainStep(final Traversal traversal, final S retainObject) {
        super(traversal);
        this.variable = null;
        this.setPredicate(traverser -> retainObject.equals(traverser.get()));
    }

    public String toString() {
        return null == this.variable ?
                super.toString() :
                TraversalHelper.makeStepString(this, this.variable);
    }
}
