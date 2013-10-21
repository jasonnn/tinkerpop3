package com.tinkerpop.blueprints.tinkergraph;

import com.tinkerpop.blueprints.computer.ComputeResult;
import com.tinkerpop.blueprints.computer.GraphComputer;
import com.tinkerpop.blueprints.computer.GraphMemory;
import com.tinkerpop.blueprints.computer.Isolation;
import com.tinkerpop.blueprints.computer.VertexMemory;
import com.tinkerpop.blueprints.computer.VertexProgram;
import com.tinkerpop.blueprints.util.StreamFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraphComputer implements GraphComputer {

    private Isolation isolation = Isolation.BSP;
    private VertexProgram vertexProgram;
    private final TinkerGraph graph;
    private final TinkerGraphMemory graphMemory = new TinkerGraphMemory();
    private TinkerVertexMemory vertexMemory = new TinkerVertexMemory(this.isolation);

    public TinkerGraphComputer(final TinkerGraph graph) {
        this.graph = graph;
    }

    public GraphComputer isolation(final Isolation isolation) {
        this.isolation = isolation;
        this.vertexMemory = new TinkerVertexMemory(isolation);
        return this;
    }

    public GraphComputer program(final VertexProgram program) {
        this.vertexProgram = program;
        return this;
    }

    public ComputeResult submit() {
        final long time = System.currentTimeMillis();
        this.vertexMemory.setComputeKeys(this.vertexProgram.getComputeKeys());
        this.vertexProgram.setup(this.graphMemory);

        boolean done = false;
        while (!done) {
            StreamFactory.parallelStream(this.graph.query().vertices()).forEach(vertex ->
                    vertexProgram.execute(((TinkerVertex) vertex).createClone(TinkerVertex.State.CENTRIC, vertex.getId().toString(), vertexMemory), graphMemory));

            this.vertexMemory.completeIteration();
            this.graphMemory.incrIteration();
            done = this.vertexProgram.terminate(this.graphMemory);
        }

        this.graphMemory.setRuntime(System.currentTimeMillis() - time);
        return new ComputeResult() {
            @Override
            public GraphMemory getGraphMemory() {
                return graphMemory;
            }

            @Override
            public VertexMemory getVertexMemory() {
                return vertexMemory;
            }
        };
    }
}