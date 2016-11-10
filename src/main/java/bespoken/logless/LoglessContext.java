package bespoken.logless;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import bespoken.util.ReflectionUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpk on 11/8/16.
 */
public class LoglessContext {
    private static ThreadPoolExecutor executor;
    static {
        executor = new ThreadPoolExecutor(1, 10, 1000, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }

    private List<Log> queue = null;
    private String transactionID = null;
    private String source;


    public LoglessContext (String source) {
        this.source = source;
        this.transactionID = UUID.randomUUID().toString();
        this.resetQueue();
        System.setOut(new PrintStreamWrapper(this, System.out));
    }

    public void log(LogType logType, Object data, String [] parameters, String [] tags) {
        Log log = null;
        if (data instanceof String) {
            String dataString = (String) data;
            if (parameters != null) {
                dataString = String.format(dataString, parameters);
            }
            log = new Log(logType, dataString, null, tags);
        } else {
            log = new Log(logType, data, null, tags);
        }

        queue.add(log);
    }

    public void flush () {
        LogBatch batch = new LogBatch();

        batch.logs = queue;
        batch.source = this.source;
        batch.transactionID = this.transactionID;

        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(batch);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.transmit(json);
    }

    private void resetQueue () {
        this.queue = new ArrayList<Log>();
    }

    private void transmit(String jsonString) {
        try {
            transmitImpl(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void transmitImpl(String jsonString) throws Exception {
        String loglessURL = "https://" + Logless.Domain + "/v1/receive";
        URL url = new URI(loglessURL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.getOutputStream().write(jsonString.getBytes());
        connection.getOutputStream().flush();
        connection.getInputStream();
    }

    public enum LogType {
        ERROR, WARNING, INFO, DEBUG
    }

    public static class Log {

        @JsonProperty("log_type")
        public LogType logType;

        @JsonProperty("transaction_id")
        public String transactionID;

        @JsonProperty("payload")
        public Object payload;
        public String [] stack;
        public String [] tags;
        public String timestamp;

        public Log(LogType logType, Object payload, String[] stack, String[] tags) {
            this.logType = logType;
            this.payload = payload;
            this.stack = stack;
            this.tags = tags;
            this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
            //System.out.println("Timestamp: " + this.timestamp);
        }
    }

    public static class LogBatch {
        public List<Log> logs;
        public String source;

        @JsonProperty("transaction_id")
        public String transactionID;
    }

    public static class PrintStreamWrapper extends PrintStream {
        public PrintStream wrappedStream;
        public LoglessContext context;

        public PrintStreamWrapper(LoglessContext context, PrintStream stream) {
            super((OutputStream) ReflectionUtil.get(stream, "out"));
            this.wrappedStream = stream;
            this.context = context;
        }

        public void println(String s) {
            context.log(LogType.DEBUG, s, null, null);
        }
    }
}


