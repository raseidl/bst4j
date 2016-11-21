package tools.bespoken.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jpk on 11/17/16.
 */
public class BSTMonetizeTest {
    public static String MonetizerRequestServiceTest = "https://s3.amazonaws.com/bespoken/monetize/BSTAdRequest.json";
    public static String MonetizerResponseServiceTest = "http://httpstat.us/200";

    @Before
    public void setup() {
        BSTMonetize.MonetizerRequestService = MonetizerRequestServiceTest;
        BSTMonetize.MonetizerResponseService = MonetizerResponseServiceTest;
    }

    @Test
    public void testRequestCallWithGoodToken() {
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my ssml");
        Assert.assertEquals("This is ssml <audio src=\"https://s3.amazonaws.com/bespoken/encoded/ContentPromoPrompt-encoded.mp3\" /> result", result.ssml());
    }

    @Test
    public void testRequestCallWithBadToken() {
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {AD} result", "This is my no ad ssml");
        Assert.assertNotNull(result.error());
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testRequestCallWithNoAd() {
        BSTMonetize.MonetizerRequestService = "https://s3.amazonaws.com/bespoken/monetize/BSTAdRequestEmpty.json";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testRequestCallWithBadURL() {
        BSTMonetize.MonetizerRequestService = "https://notaurl.com";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testRequestCallWithBadRequest() {
        BSTMonetize.MonetizerRequestService = "http://httpstat.us/400";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testResponseCallWithGoodData() {
        BSTMonetize monetize = new BSTMonetize("MySkill") {
            @Override
            protected void postJSON(String url, ObjectMapper mapper, JsonNode node) {
                Assert.assertEquals("TestID", node.get("adRequestID").asText());
                Assert.assertTrue(node.get("played").asBoolean());
                super.postJSON(url, mapper, node);
            }
        };
        BSTMonetize.Result result = new BSTMonetize.Result("Test", new BSTMonetize.Ad("TestID", "TestURL"));
        monetize.track(result);
    }

    @Test
    public void testResponseCallWithBadResponse() {
        BSTMonetize.MonetizerResponseService = "http://httpstat.us/400";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = new BSTMonetize.Result("Test", new BSTMonetize.Ad("TestID", "TestURL"));
        monetize.track(result);
    }

    @Test
    public void testResponseCallWithBadURL() {
        BSTMonetize.MonetizerResponseService = "http://thisisnotaurl";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = new BSTMonetize.Result("Test", new BSTMonetize.Ad("TestID", "TestURL"));
        monetize.track(result);
    }
}
