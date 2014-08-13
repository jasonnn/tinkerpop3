package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.structure.Graph;
import groovy.lang.IntRange;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jason on 8/12/14.
 */
public
class MiscExtensions {
    public static
    <K, V> Object getAt(Map<K, V> self, final IntRange range) {
        final int size = self.size();
        boolean fromBiggerThanTo = range.getFrom() >= range.getTo();
        int rMax = fromBiggerThanTo ? range.getFrom() : range.getTo();
        int rMin = fromBiggerThanTo ? range.getTo() : range.getFrom();

        int high = Math.min(size - 1, rMax);
        int low = Math.max(0, rMin);

        //TODO use stream api?
        final Map tempMap = new LinkedHashMap();
        int c = 0;
        for (final Map.Entry entry : self.entrySet()) {
            if (c >= low && c <= high) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
            if (c > high) {
                break;
            }
            c++;
        }
        return tempMap;
    }

    public static
    String negative(String self) {
        return Graph.Key.isHidden(self) ? Graph.Key.unHide(self) : Graph.Key.hide(self);
    }

    public static
    <N extends Number> double mean(Iterable<N> self) {
        return mean(self.iterator());
    }

    public static
    <N extends Number> double mean(Iterator<N> self) {
        double counter = 0;
        double sum = 0;
        while (self.hasNext()) {
            counter++;
            sum += self.next().doubleValue();
        }
        return sum / counter;
    }

}
