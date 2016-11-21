package tools.bespoken.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tools.bespoken.util.HTTPUtil;
import tools.bespoken.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * For use in monetizing Alexa skills. Injects &lt;audio&gt; into SSML that serves ad.<br><br>
 *
 * Call {@link tools.bespoken.client.BSTMonetize#injectSSML(String, String)} to inject an ad into SSML.<br><br>
 *
 * Call {@link tools.bespoken.client.BSTMonetize#track(Result)} to indicate the ad was "served" the user.<br>
 *     This means it was delivered as part of a response.
 *
 */
public class BSTMonetize {
    public static String MonetizerDomain = "monetize.bespoken.tools";
    public static String MonetizerRequestService = "https://" + MonetizerDomain + "/adRequest";
    public static String MonetizerResponseService = "https://" + MonetizerDomain + "/adResponse";
    public String skillId;

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
     * If no ad is found, the second paratmer, ssmlNoAd will be used.
     *
     * @param ssml The SSML payload with {ad} token that will be monetized
     * @param ssmlNoAd The fallback SSML to be used if no ad is available to be served
     * @return Result object, which includes the SSML
     */
    public Result injectSSML(String ssml, String ssmlNoAd) {
        if (ssml.indexOf("{ad}") == -1) {
            return new Result(ssmlNoAd, "No {ad} token found in the SSML. No place to inject ad audio.");
        }

        String url = MonetizerRequestService + "?skillID=" + this.skillId + "&adType=DIALOG";

        try {
            String payloadString = HTTPUtil.get(url).asString();
            JsonNode payload = JSONUtil.toJSON(payloadString);

            if (payload.has("audioURL") && !payload.get("audioURL").isNull()) {
                Ad ad = new Ad(payload.get("adRequestID").asText(), payload.get("audioURL").asText());
                ssml = ssml.replaceFirst("\\{ad\\}", "<audio src=\"" + ad.audioURL + "\" />");
                return new Result(ssml, ad);
            } else {
                return new Result(ssmlNoAd);
            }
        } catch (Exception e) {
            return new Result(ssmlNoAd, e.getMessage());
        }
    }

    /**
     * Tracks that an ad was played.
     *
     * This should be called when the response is served.
     *
     * @param result
     */
    public void track(BSTMonetize.Result result) {
        if (!result.injected()) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("adRequestID", result.ad().id);
        node.put("played", result.injected());

        String url = MonetizerResponseService;

        postJSON(url, mapper, node);
    }

    /**
     * For testability
     */
    protected void postJSON(String url, ObjectMapper mapper, JsonNode node) {
        try {
            String jsonString = mapper.writeValueAsString(node);
            HTTPUtil.post(url, jsonString.getBytes());
        } catch (Exception e) {
            System.err.println("BSTMonetize Response Error: " + e.getMessage());
        }
    }

    private Result noAdResult(String ssml, String error) {
        ssml = ssml.replaceFirst("\\{ad\\}", "");
        return new Result(ssml, error);
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
     * Contains the SSML to be used as well as some additional metadata used by calls to {@link BSTMonetize#track(Result)}.
     */
    public static class Result {
        private String ssml;
        private Ad ad;
        private String error;

        public Result(String ssml) {
            this.ssml = ssml;
        }

        public Result(String ssml, Ad ad) {
            this(ssml);
            this.ad = ad;
        }

        public Result(String ssml, String error) {
            this(ssml);
            this.error = error;
        }

        public Ad ad() {
            return ad;
        }

        public boolean injected() {
            return ad != null;
        }

        public String ssml() {
            return this.ssml;
        }

        public String error() {
            return this.error;
        }

        public boolean hasError() {
            return this.error != null;
        }
    }
}


