package tools.bespoken.client;

import com.amazon.speech.ui.SsmlOutputSpeech;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.bespoken.util.HTTPUtil;
import tools.bespoken.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * For use in monetizing Alexa skills. Injects &lt;audio&gt; into SSML that serves ad.<br><br>
 *
 * Call {@link tools.bespoken.client.BSTMonetize#injectSSML(String, String)} to inject an ad into SSML.<br><br>
 *
 * Example:<br>
 * <pre>
 *     BSTMonetize monetize = new BSTMonetize("MySkillID");
 *     SpeechletResponse.newSpeechletResponse(monetize.injectSSML(
 *          "&lt;speak&gt;Hi! Now a word from our sponsor {ad}! What do you want to do now?&lt;/speak&gt;",
 *          "&lt;speak&gt;Hi!What do you want to do now?&lt;/speak&gt;")
 *     ).asSsmlOutputSpeech(), repromptSpeech, card);
 * </pre>
 */
public class BSTMonetize {
    public static String MonetizerDomain = "monetization.bespoken.tools";
    public static String MonetizerRequestService = "https://" + MonetizerDomain + "/v1/adRequest";
    public static String MonetizerTrackerService = "https://" + MonetizerDomain + "/v1/adTracker";

    private String skillId;

    public BSTMonetize(String skillId) {
        this.skillId = skillId;
    }

    /**
     * Injects an SSML payload with an ad. It looks for an {ad} token to replace.<br><br>
     *
     * For example, an SSML payload such as this:<br>
     *      <code>&lt;speak&gt;Great choice. Now a word from our sponsor {ad}. What would you like to do next?&lt;/speak&gt;</code><br><br>
     *
     * Becomes:<br>
     *      <code>&lt;speak&gt;Great choice. Now, a word from our sponsor &lt;audio src="URL" /&gt;. What would you like to do next?&lt;/speak&gt;</code><br><br>
     *
     * If no ad is found, the second parameter, ssmlNoAd will be used.
     *
     * @param ssml The SSML payload with {ad} token that will be monetized
     * @param ssmlNoAd The fallback SSML to be used if no ad is available to be served
     * @return Result object, which includes the SSML
     */
    public Payload injectSSML(String ssml, String ssmlNoAd) {
        if (ssml.indexOf("{ad}") == -1) {
            return new Payload(ssmlNoAd, "No {ad} token found in the SSML. No place to inject ad audio.");
        }

        String url = MonetizerRequestService + "?skillName=" + this.skillId + "&adType=DIALOG";

        try {
            String payloadString = HTTPUtil.get(url).asString();
            JsonNode payload = JSONUtil.toJSON(payloadString);

            if (payload.has("audioURL") && !payload.get("audioURL").isNull()) {
                // Besides the ad itself, we all put in an ad tracker
                String trackerURL = MonetizerTrackerService + "?adRequestID=" + payload.get("adRequestID").asText();

                Ad ad = new Ad(payload.get("adRequestID").asText(), payload.get("audioURL").asText());
                ssml = ssml.replaceFirst("\\{ad\\}",
                        "<audio src=\"" + ad.audioURL + "\" />" +
                        "<audio src=\"" + trackerURL + "\" />");
                return new Payload(ssml);
            } else {
                return new Payload(ssmlNoAd);
            }
        } catch (Exception e) {
            return new Payload(ssmlNoAd, e.getMessage());
        }
    }

    public static class Ad {
        public String id;
        public String audioURL;

        public Ad(String id, String audioURL) {
            this.id = id;
            this.audioURL = audioURL;
        }
    }

    /**
     * Payload for call to {@link BSTMonetize#injectSSML(String, String)}.<br><br>
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

        public Payload(String ssml, Ad ad) {
            this(ssml);
            this.ad = ad;
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


