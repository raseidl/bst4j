package bespoken.logless;

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
        this.captureRequest(logless.newContext(), sessionStartedRequest, session);
        this.wrappedSpeechlet.onSessionStarted(sessionStartedRequest, session);
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        return this.wrappedSpeechlet.onLaunch(launchRequest, session);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        LoglessContext context = logless.newContext();
        this.captureRequest(context, intentRequest, session);
        return this.captureResponse(context, this.wrappedSpeechlet.onIntent(intentRequest, session));
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {
        this.wrappedSpeechlet.onSessionEnded(sessionEndedRequest, session);
    }
}
