package tools.bespoken.logless;

import com.amazon.speech.speechlet.*;

/**
 * Created by jpk on 11/10/16.
 */
public class SpeechletWrapper implements Speechlet {
    private Speechlet wrappedSpeechlet = null;
    private Logless logless = null;

    public SpeechletWrapper(Logless logless, Speechlet wrappedSpeechlet) {
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

    public void captureRequest(LoglessContext context, SpeechletRequest request, Session session) {
        context.log(LoglessContext.LogType.INFO, new RequestTuple(request, session), null, new String [] {"request"});
    }

    public SpeechletResponse captureResponse(LoglessContext context, SpeechletResponse response) {
        context.log(LoglessContext.LogType.INFO, response, null, new String [] {"response"});
        context.flush();
        return response;
    }

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, sessionStartedRequest, session);
        try {
            this.wrappedSpeechlet.onSessionStarted(sessionStartedRequest, session);
            context.flush();
        } catch (SpeechletException e) {
            throw handleException(context, e);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, launchRequest, session);
        SpeechletResponse response;
        try {
            response = this.wrappedSpeechlet.onLaunch(launchRequest, session);
        } catch (SpeechletException e) {
            throw handleException(context, e);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }

        return this.captureResponse(context, response);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, intentRequest, session);
        SpeechletResponse response;
        try {
            response = this.wrappedSpeechlet.onIntent(intentRequest, session);
        } catch (SpeechletException e) {
            throw handleException(context, e);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }

        return this.captureResponse(context, response);
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, sessionEndedRequest, session);
        try {
            this.wrappedSpeechlet.onSessionEnded(sessionEndedRequest, session);
            context.flush();
        } catch (SpeechletException e) {
            throw handleException(context, e);
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
