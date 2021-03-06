package com.tinkerpop.gremlin.process.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SingleIterator<T> implements Iterator<T>, Serializable {

    private final T t;
    private boolean alive = true;

    public SingleIterator(final T t) {
        this.t = t;
    }

    public boolean hasNext() {
        return this.alive;
    }

    public T next() {
        if (!this.alive)
            throw FastNoSuchElementException.instance();
        else {
            this.alive = false;
            return t;
        }
    }
}
