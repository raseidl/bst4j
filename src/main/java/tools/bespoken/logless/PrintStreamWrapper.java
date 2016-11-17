package tools.bespoken.logless;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by jpk on 11/10/16.
 */
public class PrintStreamWrapper extends PrintStream {
    private static ThreadLocal<Boolean> printedTracker = new ThreadLocal() {
        @Override
        protected Boolean initialValue() {
            return new Boolean(false);
        }
    };
    public PrintStream wrappedStream;
    public LoglessContext context;
    public LoglessContext.LogType logType;

    public PrintStreamWrapper(LoglessContext context, PrintStream stream, LoglessContext.LogType logType) {
        super(new NullOutputStream());
        this.wrappedStream = stream;
        this.context = context;
        this.logType = logType;
    }

    public void println(String s) {
        wrappedStream.println(s);
        context.log(logType, s, null, null);

    }

    public void print(String s) {
        wrappedStream.print(s);
        context.log(logType, s, null, null);
    }

    public static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Do nothing
        }
    }
}
