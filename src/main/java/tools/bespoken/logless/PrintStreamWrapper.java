package tools.bespoken.logless;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This is a singleton, which holds references to the various contexts
 * It makes sure any log statement is sent to the correct context
 */
public class PrintStreamWrapper extends PrintStream {
    // Associate the context with a thread
    private static ThreadLocal<LoglessContext> contextTracker = new ThreadLocal();
    public static void addContext(LoglessContext context) {
        contextTracker.set(context);
    }

    public PrintStream wrappedStream;
    public LoglessContext.LogType logType;

    public PrintStreamWrapper(PrintStream stream, LoglessContext.LogType logType) {
        super(new NullOutputStream());
        this.wrappedStream = stream;
        this.logType = logType;
    }

    @Override
    public void write(byte [] bytes, int offset, int length) {
        wrappedStream.write(bytes, offset, length);
        contextTracker.get().log(logType, new String(bytes, offset, length), null, null);
    }

    @Override
    public void println(String s) {
        wrappedStream.println(s);
        contextTracker.get().log(logType, s, null, null);
    }

    @Override
    public void print(String s) {
        wrappedStream.print(s);
        contextTracker.get().log(logType, s, null, null);
    }
    public static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Do nothing
        }
    }
}
