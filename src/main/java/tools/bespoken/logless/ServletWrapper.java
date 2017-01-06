package tools.bespoken.logless;

import com.fasterxml.jackson.databind.JsonNode;
import tools.bespoken.util.IOUtils;
import tools.bespoken.util.JSONUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by jpk on 1/4/17.
 */
public class ServletWrapper extends HttpServlet {
    private Servlet servlet;
    private Logless logless;

    public ServletWrapper(Logless logless, Servlet servlet) {
        this.servlet = servlet;
        this.logless = logless;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        // If this is not an HTTP Servlet request, just ignore it
        // This should not ever happen
        if (servletRequest instanceof HttpServletRequest) {
            handleServletCall(this.logless, (HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, new IServletHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                    ServletWrapper.this.servlet.service(request, response);
                }
            });
        }
    }

    static void handleServletCall(Logless logless, HttpServletRequest request, HttpServletResponse response, IServletHandler handler) throws ServletException, IOException {
        LoglessContext context = logless.newContext();

        // Log the incoming payload
        ResettableStreamRequest requestWrapper = new ResettableStreamRequest(request);
        String requestData = new String(IOUtils.toByteArray(requestWrapper.getReader()));

        // Convert to JSON, if appropriate based on the content-type
        Object requestPayload = formatPayload(request.getHeader("Content-Type"), requestData);
        context.log(LoglessContext.LogType.INFO, requestPayload, null, new String[]{"request"});

        // Reset the request stream
        requestWrapper.resetInputStream();

        CapturableStreamResponse responseWrapper = new CapturableStreamResponse(response);
        try {
            // Call the servlet, do normal processing
            handler.handle(requestWrapper, responseWrapper);
            responseWrapper.flush();

            // Log the response
            String responseString = new String(responseWrapper.getBytes());
            Object responsePayload = formatPayload(response.getHeader("Content-Type"), responseString);
            context.log(LoglessContext.LogType.INFO, responsePayload, null, new String[]{"response"});
            context.flush();

            // Do any exception handling below
        } catch (ServletException e) {
            throw handleException(context, e);
        } catch (IOException e) {
            throw handleException(context, e);
        } catch (RuntimeException e) {
            throw handleException(context, e);
        }
    }

    private static Object formatPayload(String contentType, String payloadString) {
        Object payload = payloadString;
        if (contentType != null && contentType.equals("application/json")) {
            try {
                payload = JSONUtil.toJSON(payloadString);
            } catch (Exception e) {
                // Ignore any exception on JSON parsing - just return the raw string instead
            }
        }
        return payload;
    }

    private static <T extends Exception> T handleException (LoglessContext context, T e) {
        context.logException(LoglessContext.LogType.ERROR, e, null);
        context.flush();
        return e;
    }

    //Resettable Request from here:
    // https://gist.github.com/calo81/2071634
    private static class ResettableStreamRequest extends HttpServletRequestWrapper {
        private HttpServletRequest request;
        private ResettableServletInputStream servletStream;

        public ResettableStreamRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.request = request;

            byte [] rawData = IOUtils.toByteArray(this.request.getReader());
            this.servletStream = new ResettableServletInputStream(rawData);
        }


        public void resetInputStream() {
            servletStream.reset();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(servletStream));
        }

    }

    private static class ResettableServletInputStream extends ServletInputStream {
        private ByteArrayInputStream stream;
        private byte [] data;
        public ResettableServletInputStream(byte [] data) {
            this.data = data;
            this.stream = new ByteArrayInputStream(data);
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Read Listener not handled for Logless");
        }

        @Override
        public boolean isFinished() {
            return stream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        public void reset () {
            this.stream = new ByteArrayInputStream(data);
        }
    }

    //Resettable output from here:
    //  https://gist.github.com/lm2s/4400492
    public static class CapturableServletOutputStream extends ServletOutputStream {

        private ServletOutputStream originalStream;
        private ByteBuffer copyBuffer = ByteBuffer.allocate(10000);

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException("Write Listener not handled for Logless");
        }

        public CapturableServletOutputStream(ServletOutputStream output) {
            originalStream = output;
        }

        public void write(int b) throws IOException {
            originalStream.write(b);
            copyBuffer.put((byte) b);
        }

        public byte[] data() {
            this.copyBuffer.flip();
            byte [] bytes = new byte[copyBuffer.remaining()];
            this.copyBuffer.get(bytes);
            return bytes;
        }
    }

    // ResponseWrapper.java
    private static class CapturableStreamResponse extends HttpServletResponseWrapper {
        private CapturableServletOutputStream outputStream;
        private PrintWriter writer;

        public CapturableStreamResponse(HttpServletResponse response) {
            super(response);
            try {
                this.outputStream = new CapturableServletOutputStream(response.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public ServletOutputStream getOutputStream() {
            return this.outputStream;
        }

        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(this.getOutputStream(), true);
            }
            return writer;
        }

        // Flushes content if necessary
        //  The Writer and OutputStream are never used for the same response,
        //  so only flushes the writer if it is in use
        public void flush() {
            if (this.writer != null) {
                writer.flush();
            }
        }

        public void setContentLength(int length) {
            super.setContentLength(length);
        }

        public void setContentType(String type) {
            super.setContentType(type);
        }

        public byte [] getBytes () {
            return this.outputStream.data();
        }
    }
}
