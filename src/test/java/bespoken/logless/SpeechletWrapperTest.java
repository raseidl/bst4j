package bespoken.logless;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Created by jpk on 11/10/16.
 */
public class SpeechletWrapperTest {

    @Test
    public void testIntentRequest () throws Exception {
        Logless logless = new Logless("6757fc65-3ecb-409d-8685-3fdf4043bdd6") {
            @Override
            public LoglessContext newContext() {
                return new LoglessContext(this.source) {
                    @Override
                    protected void transmit(String jsonString) {
                        super.transmit(jsonString);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = null;
                        try {
                            node = mapper.readTree(jsonString);
                        } catch (Exception e) {

                        }
                        Assert.assertEquals(node.get("logs").size(), 4);
                        Assert.assertEquals(node.get("logs").get(0).get("log_type").textValue(), "INFO");
                        Assert.assertEquals(node.get("logs").get(0).get("tags").get(0).textValue(), "request");
                        Assert.assertNotNull(node.get("logs").get(0).get("payload").get("request"));
                        Assert.assertNotNull(node.get("logs").get(0).get("payload").get("session"));
                        Assert.assertEquals(node.get("logs").get(1).get("log_type").textValue(), "DEBUG");
                        Assert.assertEquals(node.get("logs").get(1).get("payload").textValue(), "SystemOutTest");
                        Assert.assertEquals(node.get("logs").get(2).get("log_type").textValue(), "ERROR");
                        Assert.assertEquals(node.get("logs").get(2).get("payload").textValue(), "SystemErrTest");
                        Assert.assertEquals(node.get("logs").get(3).get("tags").get(0).textValue(), "response");
                    }
                };
            }
        };

        SpeechletWrapper wrapper = new SpeechletWrapper(logless, new MockSpeechlet());
        Intent intent = Intent.builder().withName("Test").build();
        IntentRequest request = IntentRequest.builder().withIntent(intent).withRequestId("RequestId").build();
        Session session = Session.builder().withSessionId("TestSessionID").build();
        wrapper.onIntent(request, session);


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
}
