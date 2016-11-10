package bespoken.logless;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import org.junit.Test;

import java.util.Date;

/**
 * Created by jpk on 11/10/16.
 */
public class SpeechletWrapperTest {

    @Test
    public void testIntentRequest () throws Exception {
        SpeechletWrapper wrapper = new SpeechletWrapper("bst4j", new MockSpeechlet());
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
            return response;
        }

        @Override
        public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

        }
    }
}
