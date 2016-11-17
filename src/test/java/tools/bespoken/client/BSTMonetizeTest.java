package tools.bespoken.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jpk on 11/17/16.
 */
public class BSTMonetizeTest {
    public static String MonetizerRequestServiceTest = "https://s3.amazonaws.com/bespoken/monetize/BSTAdRequest.json";

    @Before
    public void setup() {
        BSTMonetize.MonetizerRequestService = MonetizerRequestServiceTest;
    }

    @Test
    public void testCallWithGoodToken() {
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my ssml");
        Assert.assertEquals("This is ssml <audio src=\"https://s3.amazonaws.com/bespoken/encoded/ContentPromoPrompt-encoded.mp3\" /> result", result.ssml());
    }

    @Test
    public void testCallWithBadToken() {
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {AD} result", "This is my no ad ssml");
        Assert.assertNotNull(result.error());
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testCallWithNoAd() {
        BSTMonetize.MonetizerRequestService = "https://s3.amazonaws.com/bespoken/monetize/BSTAdRequestEmpty.json";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testCallWithBadURL() {
        BSTMonetize.MonetizerRequestService = "https://notaurl.com";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }

    @Test
    public void testCallWithBadRequest() {
        BSTMonetize.MonetizerRequestService = "http://httpstat.us/400";
        BSTMonetize monetize = new BSTMonetize("MySkill");
        BSTMonetize.Result result = monetize.injectSSML("This is ssml {ad} result", "This is my no ad ssml");
        Assert.assertEquals("This is my no ad ssml", result.ssml());
    }
}
