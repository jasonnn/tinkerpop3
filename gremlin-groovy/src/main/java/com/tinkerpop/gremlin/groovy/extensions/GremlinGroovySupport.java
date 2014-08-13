package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;

import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jason on 8/12/14.
 */
public
class GremlinGroovySupport {
    static final Set<String> steps;
    private static final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

    static {
        steps = Stream.of(GraphTraversal.class, Graph.class, Vertex.class, Edge.class, Element.class)
                      .flatMap((c) -> Stream.of(c.getMethods()))
                      .filter((m) -> Traversal.class.isAssignableFrom(m.getReturnType()))
                      .map(Method::getName)
                      .collect(Collectors.toSet());
    }


    public static
    boolean isStep(String step) {
        return steps.contains(step);
    }

    public static
    Set<String> getSteps() {
        return Collections.unmodifiableSet(steps);
    }

    public static
    Step compile(final String script) throws ScriptException {
        return (Step) engine.eval(script, engine.createBindings());
    }

    public static void addStep(final String stepName) {
        steps.add(stepName);
    }
}
