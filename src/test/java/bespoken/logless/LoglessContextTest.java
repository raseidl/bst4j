package bespoken.logless;

import bespoken.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by jpk on 11/8/16.
 */
public class LoglessContextTest {

    @Test
    public void testStringLog () {
        LoglessContext context = newContext(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(36, json.get("transaction_id").asText().length());
                Assert.assertEquals("bst4j", json.get("source").asText());
                Assert.assertEquals("INFO", json.get("logs").get(0).get("log_type").asText());
                Assert.assertEquals(2, json.get("logs").get(0).get("tags").size());
                Assert.assertEquals("TagTest", json.get("logs").get(0).get("tags").get(0).asText());
                Assert.assertEquals("Test", json.get("logs").get(0).get("payload").asText());
                Assert.assertEquals(24, json.get("logs").get(0).get("timestamp").asText().length());
                Assert.assertTrue(json.get("logs").get(0).get("timestamp").asText().endsWith("Z"));
            }
        });
        context.log(LoglessContext.LogType.INFO, "Test", null, new String[] {"TagTest", "AnotherTag"});
        context.flush();
    }

    @Test
    public void testJSONLog () {
        LoglessContext context = newContext(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals("String", json.get("logs").get(0).get("payload").get("testString").asText());
                Assert.assertEquals(1, json.get("logs").get(0).get("payload").get("testInt").asInt());
            }
        });
        context.log(LoglessContext.LogType.INFO, new RichObject("String", 1), null, new String[] {"TagTest"});
        context.flush();
    }

    @Test
    public void testExceptionLog () {
        LoglessContext context = newContext(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals("ERROR", json.get("logs").get(0).get("log_type").asText());
                String stack = json.get("logs").get(0).get("stack").asText();
                String [] stackParts = stack.split("\n");
                Assert.assertTrue(stackParts[0].startsWith("bespoken.logless.LoglessContextTest.testExceptionLog(LoglessContextTest.java"));
                Assert.assertEquals("ExceptionTag", json.get("logs").get(0).get("tags").get(0).asText());
                Assert.assertEquals("Exception: This is an Exception", json.get("logs").get(0).get("payload").asText());
            }
        });
        context.logException(LoglessContext.LogType.ERROR, new Exception("This is an Exception"), new String[] {"ExceptionTag"});
        context.flush();
    }


    public LoglessContext newContext(IVerifier verifier) {
        LoglessContext context = new LoglessContext("bst4j") {
            public void transmit(String jsonString) {
                super.transmit(jsonString);
                JsonNode json = JSONUtil.toJSON(jsonString);
                verifier.verify(json);
            }
        };
        return context;
    }

    public interface IVerifier {
        public void verify(JsonNode json);
    }

    public static class RichObject {
        public String testString;
        public int testInt;

        public RichObject(String testString, int testInt) {
            this.testString = testString;
            this.testInt = testInt;
        }
    }
}
