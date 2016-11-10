package bespoken.logless;

import com.amazon.speech.speechlet.*;

/**
 * Created by jpk on 11/8/16.
 */
public class Logless {
    public static String Domain = "logless.bespoken.tools";
    public String source;

    public Logless(String source) {
        this.source = source;
    }

    public static Speechlet capture(String source, Speechlet speechlet) {
        Logless logless = new Logless(source);
        return new SpeechletWrapper(logless, speechlet);
    }

    public LoglessContext newContext() {
        return new LoglessContext(this.source);
    }
}
