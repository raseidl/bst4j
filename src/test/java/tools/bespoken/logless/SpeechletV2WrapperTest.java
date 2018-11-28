package tools.bespoken.logless;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import tools.bespoken.util.JSONUtil;

/**
 * Created by rseidl on 27/11/18.
 */
public class SpeechletV2WrapperTest {

    @Test
    public void testLaunchRequest() throws Exception {
        Logless logless = newLogless(json -> {
            Assert.assertEquals(2, json.get("logs").size());
            Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
            Assert.assertEquals("LaunchRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
        });

        SpeechletV2Wrapper wrapper = new SpeechletV2Wrapper(logless, new MockSpeechletV2());
        LaunchRequest launch = LaunchRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        SpeechletRequestEnvelope<LaunchRequest> requestEnvelope = SpeechletRequestEnvelope.<LaunchRequest>builder()
                .withSession(session)
                .withRequest(launch)
                .build();

        wrapper.onLaunch(requestEnvelope);
    }

    @Test
    public void testOnSessionStarted() throws Exception {
        Logless logless = newLogless(json -> {
            Assert.assertEquals(1, json.get("logs").size());
            Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
            Assert.assertEquals("SessionStartedRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
        });

        SpeechletV2Wrapper wrapper = new SpeechletV2Wrapper(logless, new MockSpeechletV2());
        SessionStartedRequest request = SessionStartedRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope = SpeechletRequestEnvelope.<SessionStartedRequest>builder()
                .withSession(session)
                .withRequest(request)
                .build();

        wrapper.onSessionStarted(requestEnvelope);
    }

    @Test
    public void testOnSessionEnded() throws Exception {
        Logless logless = newLogless(json -> {
            Assert.assertEquals(1, json.get("logs").size());
            Assert.assertNotNull(json.get("logs").get(0).get("payload").get("request"));
            Assert.assertEquals("SessionEndedRequest", json.get("logs").get(0).get("payload").get("request").get("type").asText());
        });

        SpeechletV2Wrapper wrapper = new SpeechletV2Wrapper(logless, new MockSpeechletV2());
        SessionEndedRequest request = SessionEndedRequest.builder().withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope = SpeechletRequestEnvelope.<SessionEndedRequest>builder()
                .withSession(session)
                .withRequest(request)
                .build();

        wrapper.onSessionEnded(requestEnvelope);
    }

    @Test
    public void testIntentRequest() throws Exception {
        Logless logless = newLogless(json -> {
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
        });

        SpeechletV2Wrapper wrapper = new SpeechletV2Wrapper(logless, new MockSpeechletV2());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        SpeechletRequestEnvelope<IntentRequest> requestEnvelope = SpeechletRequestEnvelope.<IntentRequest>builder()
                .withSession(session)
                .withRequest(request)
                .build();

        wrapper.onIntent(requestEnvelope);
    }

    @Test
    public void testIntentRequestWithOtherException() throws Exception {
        Logless logless = newLogless(json -> {
            Assert.assertEquals(2, json.get("logs").size());
            Assert.assertNotNull(json.get("logs").get(1).get("stack").asText());
            Assert.assertEquals("RuntimeException: What happened Still?", json.get("logs").get(1).get("payload").asText());
        });

        SpeechletV2Wrapper wrapper = new SpeechletV2Wrapper(logless, new MockSpeechletV2());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("ExceptionID2").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        SpeechletRequestEnvelope<IntentRequest> requestEnvelope = SpeechletRequestEnvelope.<IntentRequest>builder()
                .withSession(session)
                .withRequest(request)
                .build();

        try {
            wrapper.onIntent(requestEnvelope);
            Assert.fail("This should always throw an exception");
        } catch (RuntimeException e) {

        } catch (Exception e) {
            Assert.fail("This should always throw a runtime exception");
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

    public static class MockSpeechletV2 implements SpeechletV2 {
        @Override
        public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {

        }

        @Override
        public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
            return null;
        }

        @Override
        public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
            if (requestEnvelope.getRequest().getRequestId().equals("ExceptionID2")) {
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
        public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {

        }
    }
}
