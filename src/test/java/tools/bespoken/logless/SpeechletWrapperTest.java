package tools.bespoken.logless;

import tools.bespoken.util.JSONUtil;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jpk on 11/10/16.
 */
public class SpeechletWrapperTest {

    @Test
    public void testLaunchRequest () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(2, json.get("logs").size());
                Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
                Assert.assertEquals("LaunchRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        LaunchRequest launch = LaunchRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        wrapper.onLaunch(launch, session);
    }

    @Test
    public void testOnSessionStarted () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(1, json.get("logs").size());
                Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
                Assert.assertEquals("SessionStartedRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        SessionStartedRequest request = SessionStartedRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        wrapper.onSessionStarted(request, session);
    }

    @Test
    public void testOnSessionEnded () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(1, json.get("logs").size());
                Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
                Assert.assertEquals("SessionEndedRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        SessionEndedRequest request = SessionEndedRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        wrapper.onSessionEnded(request, session);
    }

    @Test
    public void testIntentRequest () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(4, json.get("logs").size());
                Assert.assertEquals("INFO", json.get("logs").get(0).get("log_type").textValue());
                Assert.assertEquals("request", json.get("logs").get(0).get("tags").get(0).textValue());
                Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
                Assert.assertNotNull(json.get("logs").get(0).get("payload").get("session"));
                Assert.assertEquals("DEBUG", json.get("logs").get(1).get("log_type").textValue());
                Assert.assertEquals("SystemOutTest", json.get("logs").get(1).get("payload").textValue());
                Assert.assertEquals("ERROR", json.get("logs").get(2).get("log_type").textValue());
                Assert.assertEquals("SystemErrTest", json.get("logs").get(2).get("payload").textValue());
                Assert.assertEquals("response", json.get("logs").get(3).get("tags").get(0).textValue());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        wrapper.onIntent(request, session);
    }

    @Test
    public void testIntentRequestWithSpeechletException () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(2, json.get("logs").size());
                Assert.assertNotNull(json.get("logs").get(1).get("stack").asText());
                Assert.assertEquals("SpeechletException: What happened?", json.get("logs").get(1).get("payload").asText());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("ExceptionID").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();

        try {
            wrapper.onIntent(request, session);
            Assert.fail("This should always throw an exception");
        } catch (SpeechletException e) {

        } catch (Exception e) {
            Assert.fail("This should always throw a speechlet exception");
        }
    }

    @Test
    public void testIntentRequestWithOtherException () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(2, json.get("logs").size());
                Assert.assertNotNull(json.get("logs").get(1).get("stack").asText());
                Assert.assertEquals("RuntimeException: What happened Still?", json.get("logs").get(1).get("payload").asText());
            }
        });

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("ExceptionID2").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        try {
            wrapper.onIntent(request, session);
            Assert.fail("This should always throw an exception");
        } catch (RuntimeException e) {
            
        } catch (Exception e) {
            Assert.fail("This should always throw a runtime exception");
        }
    }

    public static class MockSpeechlet implements Speechlet {
        @Override
        public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {

        }

        @Override
        public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
            return null;
        }

        @Override
        public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
            if (intentRequest.getRequestId().equals("ExceptionID")) {
                throw new SpeechletException("What happened?");
            } else if (intentRequest.getRequestId().equals("ExceptionID2")) {
                throw new RuntimeException("What happened Still?");
            }

            SpeechletResponse response = new SpeechletResponse();
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Test");
            response.setOutputSpeech(outputSpeech);
            System.out.println("SystemOutTest");
            System.err.println("SystemErrTest");
            return response;
        }

        @Override
        public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

        }
    }

    public Logless newLogless(IVerifier verifier) {
        Logless logless = new Logless("6739f6ae-c54c-4869-85ea-416b8f00ed2e") {
            @Override
            public LoglessContext newContext() {
                return new LoglessContext(this.source) {
                    public void transmit(String jsonString) {
                        super.transmit(jsonString);
                        JsonNode json = JSONUtil.toJSON(jsonString);
                        verifier.verify(json);
                    }
                };
            }
        };

        return logless;
    }

    public interface IVerifier {
        void verify(JsonNode json);
    }
}
