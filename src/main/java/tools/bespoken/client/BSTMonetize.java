package tools.bespoken.client;

import tools.bespoken.util.HTTPUtil;
import tools.bespoken.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * For use in monetizing Alexa skills. Injects &gt;audio&lt; into SSML that serves ad.
 *
 * For example, an SSML payload such as this:
 *      &gt;speak&lt;Great choice. Now a word from our sponsor {ad}. What would you like to do next?&gt;/speak&lt;
 *
 * Becomes:
 *      &gt;speak&lt;Great choice. Now, a word from our sponsor &gt;audio src="URL" /&lt;. What would you like to do next?&gt;/speak&lt;
 *
 */
public class BSTMonetize {
    public static String MonetizerRequestService = "https://monetize.bespoken.tools";
    public String skillId;

    public BSTMonetize(String skillId) {
        this.skillId = skillId;
    }

    public Result injectSSML(String ssml, String ssmlNoAd) {
        if (ssml.indexOf("{ad}") == -1) {
            return new Result(ssmlNoAd, "No {ad} token found in the SSML. No place to inject ad audio.");
        }

        String url = MonetizerRequestService + "?skill=" + this.skillId;

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

    private Result noAdResult(String ssml, String error) {
        ssml = ssml.replaceFirst("\\{ad\\}", "");
        return new Result(ssml, error);
    }

    private static class Ad {
        public String id;
        public String audioURL;

        public Ad(String id, String audioURL) {
            this.id = id;
            this.audioURL = audioURL;
        }
    }

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


