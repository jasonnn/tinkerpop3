package com.tinkerpop.gremlin.console

import com.tinkerpop.gremlin.console.commands.InstallCommand
import com.tinkerpop.gremlin.console.commands.PluginCommand
import com.tinkerpop.gremlin.console.commands.RemoteCommand
import com.tinkerpop.gremlin.console.commands.SubmitCommand
import com.tinkerpop.gremlin.console.commands.UninstallCommand
import com.tinkerpop.gremlin.console.plugin.PluggedIn
import com.tinkerpop.gremlin.console.util.ArrayIterator
//import com.tinkerpop.gremlin.groovy.loaders.GremlinLoader
import com.tinkerpop.gremlin.groovy.plugin.GremlinPlugin
import jline.console.history.FileHistory
import org.codehaus.groovy.tools.shell.ExitNotification
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.InteractiveShellRunner

import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class Console {
    static {
        // this is necessary so that terminal doesn't lose focus to AWT
        System.setProperty("java.awt.headless", "true")
    }

    private static final String HISTORY_FILE = ".gremlin_groovy_history"
    private static final String STANDARD_INPUT_PROMPT = "gremlin> "
    private static final String STANDARD_RESULT_PROMPT = "==>"
    private static final String IMPORT_SPACE = "import "
    private static final String IMPORT_STATIC_SPACE = "import static "
    private static final String NULL = "null"

    private Iterator tempIterator = Collections.emptyIterator()

    private final IO io = new IO(System.in, System.out, System.err)
    private final Groovysh groovy = new Groovysh()

    public Console(final String initScriptFile) {
        io.out.println()
        io.out.println("         \\,,,/")
        io.out.println("         (o o)")
        io.out.println("-----oOOo-(3)-oOOo-----")

        final Mediator mediator = new Mediator(this)
        groovy.register(new UninstallCommand(groovy, mediator))
        groovy.register(new InstallCommand(groovy, mediator))
        groovy.register(new PluginCommand(groovy, mediator))
        groovy.register(new RemoteCommand(groovy, mediator))
        groovy.register(new SubmitCommand(groovy, mediator))

        // hide output temporarily while imports execute
        showShellEvaluationOutput(false)

        // add the default imports
        new ConsoleImportCustomizerProvider().getCombinedImports().stream()
                .collect { IMPORT_SPACE + it }.each { groovy.execute(it) }
        new ConsoleImportCustomizerProvider().getCombinedStaticImports().stream()
                .collect { IMPORT_STATIC_SPACE + it }.each { groovy.execute(it) }

        final InteractiveShellRunner runner = new InteractiveShellRunner(groovy, handlePrompt)
        runner.setErrorHandler(handleError)
        try {
            final FileHistory history = new FileHistory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + HISTORY_FILE))
            groovy.setHistory(history)
            runner.setHistory(history)
        } catch (IOException ignored) {
            io.err.println("Unable to create history file: " + HISTORY_FILE)
        }

        //GremlinLoader.load()

        // check for available plugins.  if they are in the "active" plugins list then "activate" them
        def activePlugins = Mediator.readPluginState()
        ServiceLoader.load(GremlinPlugin.class, groovy.getInterp().getClassLoader()).each { plugin ->
            if (!mediator.availablePlugins.containsKey(plugin.class.name)) {
                def pluggedIn = new PluggedIn(plugin, groovy, io, false)
                mediator.availablePlugins.put(plugin.class.name, pluggedIn)

                if (activePlugins.contains(plugin.class.name)) {
                    pluggedIn.activate()
                    io.out.println("plugin activated: " + plugin.getName())
                }
            }
        }

        // remove any "uninstalled" plugins from plugin state as it means they were installed, activated, but not
        // deactivated, and are thus hanging about
        mediator.writePluginState()

        // start iterating results to show as output
        showShellEvaluationOutput(true)
        if (initScriptFile != null) initializeShellWithScript(initScriptFile)

        try {
            runner.run()
        } catch (ExitNotification ignored) {
            // occurs on exit
        } catch (Throwable t) {
            t.printStackTrace()
        } finally {
            try {
                mediator.close().get(3, TimeUnit.SECONDS)
            } catch (Exception ignored) {
                // ok if this times out - just trying to be polite on shutdown
            } finally {
                System.exit(0)
            }
        }
    }

    def showShellEvaluationOutput(final boolean show) {
        if (show)
            groovy.setResultHook(handleResultIterate)
        else
            groovy.setResultHook(handleResultShowNothing)
    }

    private def handlePrompt = { STANDARD_INPUT_PROMPT }

    private def handleResultShowNothing = { args -> null }

    private def handleResultIterate = { result ->
        try {
            // necessary to save persist history to file
            groovy.getHistory().flush()
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e)
        }

        while (true) {
            if (this.tempIterator.hasNext()) {
                while (this.tempIterator.hasNext()) {
                    final Object object = this.tempIterator.next()
                    io.out.println(buildResultPrompt() + ((null == object) ? NULL : object.toString()))
                }
                return null
            } else {
                try {
                    if (result instanceof Iterator) {
                        this.tempIterator = (Iterator) result
                        if (!this.tempIterator.hasNext()) return null
                    } else if (result instanceof Iterable) {
                        this.tempIterator = ((Iterable) result).iterator()
                        if (!this.tempIterator.hasNext()) return null
                    } else if (result instanceof Object[]) {
                        this.tempIterator = new ArrayIterator((Object[]) result)
                        if (!this.tempIterator.hasNext()) return null
                    } else if (result instanceof Map) {
                        this.tempIterator = ((Map) result).entrySet().iterator()
                        if (!this.tempIterator.hasNext()) return null
                    } else {
                        io.out.println(buildResultPrompt() + ((null == result) ? NULL : result.toString()))
                        return null
                    }
                } catch (final Exception e) {
                    this.tempIterator = Collections.emptyIterator()
                    throw e
                }
            }
        }
    }

    private def handleError = { err ->
        if (err instanceof Throwable) {
            try {
                final Throwable e = (Throwable) err
                String message = e.getMessage()
                if (null != message) {
                    message = message.replace("startup failed:", "")
                    io.err.println(message.trim())
                } else {
                    io.err.println(e)
                }

                io.err.print("Display stack trace? [yN] ")
                io.err.flush()
                String line = new BufferedReader(io.in).readLine()
                if (null == line)
                    line = ""
                io.err.print(line.trim())
                io.err.println()
                if (line.trim().equals("y") || line.trim().equals("Y")) {
                    e.printStackTrace(io.err)
                }
            } catch (Exception ignored) {
                io.err.println("An undefined error has occurred: " + err)
            }
        } else {
            io.err.println("An undefined error has occurred: " + err.toString())
        }

        return null
    }

    private static String buildResultPrompt() {
        final String groovyshellProperty = System.getProperty("gremlin.prompt")
        if (groovyshellProperty != null)
            return groovyshellProperty

        final String groovyshellEnv = System.getenv("GREMLIN_PROMPT")
        if (groovyshellEnv != null)
            return groovyshellEnv

        return STANDARD_RESULT_PROMPT
    }

    private void initializeShellWithScript(final String initScriptFile) {
        String line = ""
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(initScriptFile), Charset.forName("UTF-8")))
            while ((line = reader.readLine()) != null) {
                groovy.execute(line)
            }
            reader.close()
        } catch (FileNotFoundException ignored) {
            io.err.println(String.format("Gremlin initialization file not found at [%s].", initScriptFile))
            System.exit(1)
        } catch (IOException ignored) {
            io.err.println(String.format("Bad line in Gremlin initialization file at [%s].", line))
            System.exit(1)
        }
    }

    public static void main(final String[] args) {
        new Console(args.length == 1 ? args[0] : null)
    }
}
