package tools.bespoken.logless;

import com.amazon.speech.speechlet.*;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

/**
 * Logless will automatically capture logs and diagnostics for your Java Lambda or Servlet.
 *
 * To use it with a Speechlet, simply wrap it like so:
 * <pre><code>
 *     Speechlet wrapper = Logless.capture("292fbf19-61fd-4ec6-8a8d-60fea5193904", new HelloWorldSpeechlet());
 * </code></pre>
 *
 * To use it as a standalone servlet,
 * <pre><code>
 *     Servlet wrapper = Logless.capture("292fbf19-61fd-4ec6-8a8d-60fea5193904", new HelloWorldServlet());
 * </code></pre>
 *
 * That's all there is to it. Then you can see all your logs through our handy dashboard!
 *
 * We will effortlessly capture and format:
 * <ul>
 *     <li>Request and response payloads
 *     <li>Console output (including instrumentation for timing and all debug levels)
 *     <li>Error and stack traces
 * </ul>
 *
 */
public class Logless {
    public static String Domain = "logless.bespoken.tools";
    public String source;

    // Override system out and error
    static {
        System.setOut(new PrintStreamWrapper(System.out, LoglessContext.LogType.DEBUG));
        System.setErr(new PrintStreamWrapper(System.err, LoglessContext.LogType.ERROR));
    }

    public Logless(String source) {
        this.source = source;
    }

    /**
     *
     * @param source
     * @param speechlet
     * @return
     */
    public static Speechlet capture(String source, Speechlet speechlet) {
        Logless logless = new Logless(source);
        return new SpeechletWrapper(logless, speechlet);
    }

    public static Servlet capture(String source, Servlet servlet) {
        Logless logless = new Logless(source);
        return new ServletWrapper(logless, servlet);
    }

    public LoglessContext newContext() {
        LoglessContext context = new LoglessContext(this.source);
        return context;
    }
}
