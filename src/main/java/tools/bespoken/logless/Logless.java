package tools.bespoken.logless;

import com.amazon.speech.speechlet.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
 * Alternatively, you can selectively wrap Servlet endpoints for greater flexibility, such as this:
 * <pre><code>
 *     Logless.capture("292fbf19-61fd-4ec6-8a8d-60fea5193904", request, response, new IServletHandler() {
 *         public void call() throws IOException, ServletException {
 *             // Main body of servlet processing
 *         }
 *     });
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
     * Captures all interactions with a Speechlet for the given source.
     *
     * Returns a wrapped Speechlet to be used in code.
     *
     * @param source
     *     The bespoken app token
     * @param speechlet
     *     The speechlet which should be wrapped
     * @return
     */
    public static Speechlet capture(String source, Speechlet speechlet) {
        Logless logless = new Logless(source);
        return new SpeechletWrapper(logless, speechlet);
    }

    /**
     * Captures all interactions with a SpeechletV2 for the given source.
     *
     * Returns a wrapped SpeechletV2 to be used in code.
     *
     * @param source
     *     The bespoken app token
     * @param speechlet
     *     The speechlet which should be wrapped
     * @return
     */
    public static SpeechletV2 capture(String source, SpeechletV2 speechlet) {
        Logless logless = new Logless(source);
        return new SpeechletV2Wrapper(logless, speechlet);
    }

    /**
     * Captures all interactions with a Servlet for the given source.
     *
     * Returns a wrapped Servlet to be used in code.
     *
     * @param source
     *     The bespoken app token
     * @param servlet
     *     The servlet which should be wrapped
     * @return
     */
    public static Servlet capture(String source, Servlet servlet) {
        Logless logless = new Logless(source);
        return new ServletWrapper(logless, servlet);
    }

    /**
     * Captures a particular servlet interaction.
     *
     * @param source
     * @param request
     * @param response
     * @param runnable
     * @throws ServletException
     * @throws IOException
     */
    public static void capture(String source, HttpServletRequest request, HttpServletResponse response, IServletHandler runnable)
            throws ServletException, IOException {
        Logless logless = new Logless(source);
        ServletWrapper.handleServletCall(logless, request, response, runnable);
    }


    public LoglessContext newContext() {
        LoglessContext context = new LoglessContext(this.source);
        return context;
    }
}
