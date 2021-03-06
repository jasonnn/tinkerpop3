package com.tinkerpop.gremlin.console.commands

import com.tinkerpop.gremlin.console.plugin.ConsolePluginAcceptor
import com.tinkerpop.gremlin.console.Mediator
import com.tinkerpop.gremlin.console.plugin.PluggedIn
import com.tinkerpop.gremlin.groovy.plugin.Artifact
import com.tinkerpop.gremlin.groovy.plugin.GremlinPlugin
import groovy.grape.Grape
import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class UseCommand extends ComplexCommandSupport {
    private final Mediator mediator

    public UseCommand(final Groovysh shell, final Mediator mediator) {
        super(shell, ":use", ":u", ["install", "now", "list"], "now")
        this.mediator = mediator
    }

    def Object do_now = { List<String> arguments ->
        final def dep = createDependencyRecord(arguments)
        final def pluginsThatNeedRestart = grabDeps(dep, false)
        final def msgs = ["loaded: " + arguments]
        if (pluginsThatNeedRestart.size() > 0) {
            msgs << "The following plugins may not function properly unless they are 'installed':"
            msgs.addAll(pluginsThatNeedRestart)
            msgs << "Try :use with the 'install' option then restart the console"
        }

        return msgs
    }

    def do_list = { List<String> arguments ->
        return mediator.loadedPlugins.collect { k, v -> k + (v.installed ? "[installed]" : "") }
    }

    def Object do_install = { List<String> arguments ->
        final def dep = createDependencyRecord(arguments)
        final def pluginsThatNeedRestart = grabDeps(dep, true)

        final def dependencyLocations = Grape.resolve([classLoader: shell.getInterp().getClassLoader()], null, dep)

        def fileSep = System.getProperty("file.separator")
        def extClassPath = System.getProperty("user.dir") + fileSep + "ext" + fileSep + (String) dep.module

        new File(extClassPath).mkdirs()

        def fs = FileSystems.default
        def target = fs.getPath(extClassPath)

        dependencyLocations.each {
            def from = fs.getPath(it.path)
            Files.copy(from, target.resolve(from.fileName), StandardCopyOption.REPLACE_EXISTING)
        }

        return "loaded: " + arguments + (pluginsThatNeedRestart.size() == 0 ? "" : " - restart the console to use $pluginsThatNeedRestart")
    }

    private def grabDeps(final Map<String, Object> map, final boolean installed) {
        Grape.grab(map)

        def pluginsThatNeedRestart = [] as Set
        def additionalDeps = [] as Set

        // note that the service loader utilized the classloader from the groovy shell as shell class are available
        // from within there given loading through Grape.
        ServiceLoader.load(GremlinPlugin.class, shell.getInterp().getClassLoader()).forEach { plugin ->
            if (!mediator.loadedPlugins.containsKey(plugin.name)) {
                if (plugin.requireRestart())
                    pluginsThatNeedRestart << plugin.name
                else {
                    plugin.pluginTo(new ConsolePluginAcceptor(shell, io))
                    mediator.loadedPlugins.put(plugin.name, new PluggedIn(plugin, installed))
                }

                if (plugin.additionalDependencies().isPresent())
                    additionalDeps.addAll(plugin.additionalDependencies().get().flatten())
            }
        }

        additionalDeps.each { Grape.grab(makeDepsMap((Artifact) it)) }

        return pluginsThatNeedRestart
    }

    private def createDependencyRecord(final List<String> arguments) {
        final String group = arguments.get(0)
        final String module = arguments.get(1)
        final String version = arguments.get(2)

        if (group == null || group.isEmpty())
            throw new IllegalArgumentException("Group cannot be null or empty")

        if (module == null || module.isEmpty())
            throw new IllegalArgumentException("Module cannot be null or empty")

        if (version == null || version.isEmpty())
            throw new IllegalArgumentException("Version cannot be null or empty")

        return makeDepsMap(new Artifact(group, module, version))
    }

    private def makeDepsMap(final Artifact artifact) {
        final Map<String, Object> map = new HashMap<>()
        map.put("classLoader", shell.getInterp().getClassLoader())
        map.put("group", artifact.getGroup())
        map.put("module", artifact.getArtifact())
        map.put("version", artifact.getVersion())
        map.put("changing", false)
        return map
    }
}
