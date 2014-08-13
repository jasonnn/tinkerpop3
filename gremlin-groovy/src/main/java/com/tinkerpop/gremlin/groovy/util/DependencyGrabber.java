package com.tinkerpop.gremlin.groovy.util;

import com.tinkerpop.gremlin.groovy.plugin.Artifact;
import groovy.grape.Grape;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public
class DependencyGrabber {
    public
    DependencyGrabber(final ClassLoader cl, final String extensionDirectory) {
        this.classLoaderToUse = cl;
        this.extensionDirectory = extensionDirectory;
    }

    public
    void copyDependenciesToPath(final Artifact artifact) throws IOException {
        final Map<String, Object> dep = makeDepsMap(artifact);
        final String extClassPath = getPathFromDependency(dep);
        final File f = new File(extClassPath);

        if (f.exists())
            throw new IllegalStateException("a module with the name " + String.valueOf(dep.get("module")) + " is already installed");
        if (!f.mkdirs())
            throw new IOException("could not create directory at " + String.valueOf(f));

        LinkedHashMap<String, ClassLoader> map = new LinkedHashMap<String, ClassLoader>(1);
        map.put("classLoader", this.classLoaderToUse);
        final URI[] dependencyLocations = Grape.resolve((Map)map, Collections.emptyList(), dep);
        final FileSystem fs = FileSystems.getDefault();
        final Path target = fs.getPath(extClassPath);

        DefaultGroovyMethods.each(dependencyLocations, new Closure<Path>(this, this) {
            public
            Path doCall(URI it) throws IOException {
                Path from = fs.getPath(it.getPath());
                return Files.copy(from, target.resolve(from.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }

            public
            Path doCall() throws IOException {
                return doCall(null);
            }

        });
    }

    public
    void copyDependenciesToPath(final String group, final String artifact, final String version) throws IOException {
        copyDependenciesToPath(new Artifact(group, artifact, version));
    }

    private
    String getPathFromDependency(final Map<String, Object> dep) {
        String fileSep = System.getProperty("file.separator");
        return this.extensionDirectory + fileSep + (String) dep.get("module");
    }

    private
    Map<String, Object> makeDepsMap(final Artifact artifact) {
        final Map<String, Object> map = new HashMap<String, Object>();
        ((HashMap<String, Object>) map).put("classLoader", this.classLoaderToUse);
        ((HashMap<String, Object>) map).put("group", artifact.getGroup());
        ((HashMap<String, Object>) map).put("module", artifact.getArtifact());
        ((HashMap<String, Object>) map).put("version", artifact.getVersion());
        ((HashMap<String, Object>) map).put("changing", false);
        return map;
    }

    private final ClassLoader classLoaderToUse;

    private final String extensionDirectory;
}
