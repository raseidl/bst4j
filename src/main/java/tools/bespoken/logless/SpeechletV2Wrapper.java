package tools.bespoken.logless;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.*;

/**
 * Created by rseidl on 27/11/18.
 */
public class SpeechletV2Wrapper implements SpeechletV2 {
    private SpeechletV2 wrappedSpeechlet = null;
    private Logless logless = null;

    public SpeechletV2Wrapper(Logless logless, SpeechletV2 wrappedSpeechlet) {
        this.logless = logless;
        this.wrappedSpeechlet = wrappedSpeechlet;
    }

    public static class RequestTuple {
        public SpeechletRequest request;
        public Session session;

        public RequestTuple(SpeechletRequest request, Session session) {
            this.request = request;
            this.session = session;
        }
    }

    public void captureRequest(LoglessContext context, SpeechletRequestEnvelope requestEnvelope) {
        Session session = requestEnvelope.getSession();
        SpeechletRequest request = requestEnvelope.getRequest();
        context.log(LoglessContext.LogType.INFO, new RequestTuple(request, session), null, new String [] {"request"});
    }

    public SpeechletResponse captureResponse(LoglessContext context, SpeechletResponse response) {
        context.log(LoglessContext.LogType.INFO, response, null, new String [] {"response"});
        context.flush();
        return response;
    }

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, requestEnvelope);
        try {
            this.wrappedSpeechlet.onSessionStarted(requestEnvelope);
            context.flush();
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, requestEnvelope);
        SpeechletResponse response;
        try {
            response = this.wrappedSpeechlet.onLaunch(requestEnvelope);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }

        return this.captureResponse(context, response);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, requestEnvelope);
        SpeechletResponse response;
        try {
            response = this.wrappedSpeechlet.onIntent(requestEnvelope);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }

        return this.captureResponse(context, response);
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, requestEnvelope);
        try {
            this.wrappedSpeechlet.onSessionEnded(requestEnvelope);
            context.flush();
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }
    }

    private <T extends Exception> T handleException(LoglessContext context, T e) {
        context.logException(LoglessContext.LogType.ERROR, e, null);
        context.flush();
        return e;
    }
}
