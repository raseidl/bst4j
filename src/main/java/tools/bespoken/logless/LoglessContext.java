package tools.bespoken.logless;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Created by jpk on 11/8/16.
 */
public class LoglessContext {
    private List<Log> queue;
    private String transactionID;
    private String source;

    public LoglessContext (String source) {
        this.source = source;
        this.transactionID = UUID.randomUUID().toString();
        this.resetQueue();
        System.setOut(new PrintStreamWrapper(this, System.out, LogType.DEBUG));
        System.setErr(new PrintStreamWrapper(this, System.err, LogType.ERROR));
    }

    public void log(LogType logType, Object data, String [] parameters, String [] tags) {
        Log log;
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

    public void logException(LogType logType, Throwable e, String [] tags) {
        StringBuilder stack = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            stack.append(element.toString() + "\n");
        }
        Log log = new Log(logType, e.getClass().getSimpleName() + ": " + e.getMessage(), stack.toString(), tags);
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
        this.queue = new ArrayList<>();
    }

    protected void transmit(String jsonString) {
        try {
            transmitImpl(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void transmitImpl(String jsonString) throws Exception {
        long startTime = System.currentTimeMillis();
        String loglessURL = "https://" + Logless.Domain + "/v1/receive";
        URL url = new URI(loglessURL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
        bos.write(jsonString.getBytes());
        bos.flush();
        connection.getInputStream();

        System.out.append("Time: " + (System.currentTimeMillis() - startTime) + "\n");
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
        public String stack;
        public String [] tags;
        public String timestamp;

        public Log(LogType logType, Object payload, String stack, String[] tags) {
            this.logType = logType;
            this.payload = payload;
            this.stack = stack;
            this.tags = tags;
            if (this.tags == null) {
                this.tags = new String[0];
            }
            this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        }
    }

    public static class LogBatch {
        public List<Log> logs;
        public String source;

        @JsonProperty("transaction_id")
        public String transactionID;
    }

}


