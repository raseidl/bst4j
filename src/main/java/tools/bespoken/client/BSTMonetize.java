package tools.bespoken.client;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.User;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.bespoken.util.HTTPUtil;
import tools.bespoken.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * For use in monetizing Alexa skills. Injects &lt;audio&gt; into SSML that serves ad.<br><br>
 *
 * Call {@link tools.bespoken.client.BSTMonetize#prime(String)} to queue an ad for playback.
 * Best practice is to call it on the LaunchRequest (or first IntentRequest) for the user.<br><br>
 *
 * Call {@link tools.bespoken.client.BSTMonetize#injectSSML(String, String, String)} to inject the queued ad into SSML.<br><br>
 *
 * Example:<br>
 * <pre>
 *     BSTMonetize monetize = new BSTMonetize("MySkillID");
 *     SpeechletResponse.newSpeechletResponse(monetize.injectSSML(session.getUser().getUserId(),
 *          "&lt;speak&gt;Hi! Now a word from our sponsor {ad}! What do you want to do now?&lt;/speak&gt;",
 *          "&lt;speak&gt;Hi!What do you want to do now?&lt;/speak&gt;")
 *     ).asSsmlOutputSpeech(), repromptSpeech, card);
 * </pre>
 */
public class BSTMonetize {
    private static Map<String, Ad> adBySession = new WeakHashMap<String, Ad>();
    public static String MonetizerDomain = "monetization.bespoken.tools";
    public static String MonetizerPrimeService = "https://" + MonetizerDomain + "/v1/prime";
    public static String MonetizerFetchService = "https://" + MonetizerDomain + "/v1/fetch";

    private String skillId;

    public BSTMonetize(String skillId) {
        this.skillId = skillId;
    }

    /**
     * Prepares an ad for playback.<br><br>
     *
     * This should be called when the skill launches to prepare an ad to be served.<br><br>
     *
     * Once primed, it can be injected on subsequent intent requests.
     *
     * @param userID The user ID from the Alexa request
     */
    public void prime(String userID) {
        String url = MonetizerPrimeService + "?skillID=" + this.skillId
                + "&userID=" + userID
                + "&adType=DIALOG";
        try {
            HTTPUtil.get(url);
        } catch (Exception e) {
            System.err.println("BSTMonetize Error fetching ad: " + e.getMessage());
        }
    }

    /**
     * Injects an SSML payload with an ad. It looks for an {ad} token to replace.<br><br>
     *
     * For example, an SSML payload such as this:<br>
     *      <code>&lt;speak&gt;Great choice. Now a word from our sponsor {ad}. What would you like to do next?&lt;/speak&gt;</code><br><br>
     *
     * Becomes:<br>
     *      <code>&lt;speak&gt;Great choice. Now, a word from our sponsor &lt;audio src="URL" /&gt; &lt;audio src="trackingURL" /&gt;. What would you like to do next?&lt;/speak&gt;</code><br><br>
     *
     * If no ad is found, the second parameter, ssmlNoAd will be used.<br><br>
     *
     * Once an ad is injected, another one will be automatically queued for playback - no need to call
     * {@link tools.bespoken.client.BSTMonetize#prime(String)} again.<br><br>
     *
     * @param userID The Alexa user ID
     * @param ssml The SSML payload with {ad} token that will be monetized
     * @param ssmlNoAd The fallback SSML to be used if no ad is available to be served
     * @return Result object, which includes the SSML
     */
    public Payload injectSSML(String userID, String ssml, String ssmlNoAd) {
        if (ssml.indexOf("{ad}") == -1) {
            return new Payload(ssmlNoAd, "No {ad} token found in the SSML. No place to inject ad audio.");
        }

        Ad ad = fetchAd(userID);
        if (ad != null) {
            ssml = ssml.replaceFirst("\\{ad\\}",
                "<audio src=\"" + ad.audioURL + "\" />" +
                "<audio src=\"" + ad.trackingURL + "\" />");
            return new Payload(ssml);
        } else {
            return new Payload(ssmlNoAd);
        }
    }

    /**
     * Loads an ad from the server
     * @return
     */
    private Ad fetchAd(String userID) {
        String url = MonetizerFetchService + "?skillID=" + this.skillId
                + "&userID=" + userID
                + "&adType=DIALOG";
        Ad ad = null;
        try {
            String payloadString = HTTPUtil.get(url).asString();
            JsonNode payload = JSONUtil.toJSON(payloadString);

            if (payload.has("audioURL") && !payload.get("audioURL").isNull()) {
                ad = new Ad(payload.get("requestID").asText(),
                        payload.get("audioURL").asText(),
                        payload.get("trackingURL").asText());
            }
        } catch (Exception e) {
            System.err.println("BSTMonetize Error fetching ad: " + e.getMessage());
        }
        return ad;
    }

    public static class Ad {
        public String id;
        public String audioURL;
        public String trackingURL;

        public Ad(String id, String audioURL, String trackingURL) {
            this.id = id;
            this.audioURL = audioURL;
            this.trackingURL = trackingURL;
        }
    }

    /**
     * Payload for call to {@link BSTMonetize#injectSSML(String, String, String)}.<br><br>
     *
     * Contains the SSML to be used as well as some additional metadata.
     */
    public static class Payload {
        private String ssml;
        private Ad ad;
        private String error;

        public Payload(String ssml) {
            this.ssml = ssml;
        }

        public Payload(String ssml, String error) {
            this(ssml);
            this.error = error;
        }

        public Ad ad() {
            return ad;
        }

        public boolean injected() {
            return ad != null;
        }

        public String asSsmlString() {
            return this.ssml;
        }

        public SsmlOutputSpeech asSsmlOutputSpeech() {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml(this.ssml);
            return outputSpeech;
        }

        public String error() {
            return this.error;
        }

        public boolean hasError() {
            return this.error != null;
        }
    }
}


