package bespoken.logless;

import com.amazon.speech.speechlet.*;

/**
 * Created by jpk on 11/8/16.
 */
public class Logless {
    public static String Domain = "logless.bespoken.tools";

    public static Speechlet capture(String source, Speechlet speechlet) {
        return new SpeechletWrapper(source, speechlet);
    }
}
