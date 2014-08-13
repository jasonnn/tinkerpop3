package com.tinkerpop.gremlin.groovy.extensions;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import com.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import com.tinkerpop.gremlin.structure.io.graphson.GraphSONReader;
import com.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import com.tinkerpop.gremlin.structure.io.kryo.KryoReader;
import com.tinkerpop.gremlin.structure.io.kryo.KryoWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jason on 8/12/14.
 */
public
class SerializationExtensions {
    // GraphML loading and saving
    public static
    void loadGraphML(final Graph self, final String fileObject) throws IOException {
        final GraphMLReader reader = GraphMLReader.build().create();
        try {
            reader.readGraph(new URL(fileObject).openStream(), self);
        } catch (final MalformedURLException e) {
            reader.readGraph(new FileInputStream(fileObject), self);
        }
    }

    public static
    void saveGraphML(final Graph self, final String fileObject) throws IOException {

        GraphMLWriter.build().create().writeGraph(new FileOutputStream(fileObject), self);
    }

    // GraphSON loading and saving
    public static
    void loadGraphSON(final Graph self, final String fileObject) throws IOException {
        final GraphSONReader reader = GraphSONReader.build().create();
        try {
            reader.readGraph(new URL(fileObject).openStream(), self);
        } catch (final MalformedURLException e) {
            reader.readGraph(new FileInputStream(fileObject), self);
        }
    }

    public static
    void saveGraphSON(final Graph self, final String fileObject) throws IOException {
        GraphSONWriter.build().create().writeGraph(new FileOutputStream(fileObject), self);
    }

    // Kryo loading and saving
    public static
    void loadKryo(final Graph self, final String fileObject) throws IOException {
        final KryoReader reader = KryoReader.build().create();
        try {
            reader.readGraph(new URL(fileObject).openStream(), self);
        } catch (final MalformedURLException e) {
            reader.readGraph(new FileInputStream(fileObject), self);
        }
    }

    public static
    void saveKryo(final Graph self, final String fileObject) throws IOException {
        KryoWriter.build().create().writeGraph(new FileOutputStream(fileObject), self);
    }

}
