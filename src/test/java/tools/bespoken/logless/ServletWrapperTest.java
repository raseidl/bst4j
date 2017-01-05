package tools.bespoken.logless;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import tools.bespoken.util.JSONUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.Writer;


/**
 * Created by jpk on 1/4/17.
 */
public class ServletWrapperTest {
    @Test
    public void testRequest () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(4, json.get("logs").size());
                Assert.assertEquals("INFO", json.get("logs").get(0).get("log_type").textValue());
                Assert.assertEquals("request", json.get("logs").get(0).get("tags").get(0).textValue());
                Assert.assertEquals("REQUEST", json.get("logs").get(0).get("payload").asText());
                Assert.assertEquals("DEBUG", json.get("logs").get(1).get("log_type").textValue());
                Assert.assertEquals("OUT TEST", json.get("logs").get(1).get("payload").textValue());
                Assert.assertEquals("ERROR", json.get("logs").get(2).get("log_type").textValue());
                Assert.assertEquals("ERR TEST", json.get("logs").get(2).get("payload").textValue());
                Assert.assertEquals("response", json.get("logs").get(3).get("tags").get(0).textValue());
                Assert.assertEquals("RESPONSE", json.get("logs").get(3).get("payload").asText());
            }
        });

        Servlet mockServlet = new MockServlet("RESPONSE");
        ServletWrapper wrapper = new ServletWrapper(logless, mockServlet);

        MockHTTP.MockResponse response = new MockHTTP.MockResponse();
        wrapper.service(new MockHTTP.MockRequest("POST", "REQUEST"), response);
        Assert.assertEquals("RESPONSE", response.dataString());
    }

    @Test
    public void testRequestWithRuntimeException () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(2, json.get("logs").size());
                //Assert.assertEquals("response", json.get("logs").get(1).get("tags").get(0).textValue());
                Assert.assertEquals("RuntimeException: What happened?", json.get("logs").get(1).get("payload").asText());

                String stack = json.get("logs").get(1).get("stack").asText();
                System.out.println(stack.split("\n")[0]);
                Assert.assertTrue(stack.split("\n")[0].startsWith("tools.bespoken.logless.ServletWrapperTest$3.doPost(ServletWrapperTest.java"));
            }
        });

        Servlet mockServlet = new MockServlet("RESPONSE") {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                throw new RuntimeException("What happened?");
            }
        };

        try {
            ServletWrapper wrapper = new ServletWrapper(logless, mockServlet);
            MockHTTP.MockResponse response = new MockHTTP.MockResponse();
            wrapper.service(new MockHTTP.MockRequest("POST", "REQUEST"), response);
            Assert.fail("Should not get here");
        } catch (Exception e) {

        }
    }

    @Test
    public void testRequestWithPrintWriterServlet () throws Exception {
        Logless logless = newLogless(new IVerifier() {
            @Override
            public void verify(JsonNode json) {
                Assert.assertEquals(2, json.get("logs").size());
                Assert.assertEquals("response", json.get("logs").get(1).get("tags").get(0).textValue());
                Assert.assertEquals("RESPONSE", json.get("logs").get(1).get("payload").asText());
            }
        });

        Servlet mockServlet = new MockServlet("RESPONSE") {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                response.getOutputStream().print(this.response);;
            }
        };
        ServletWrapper wrapper = new ServletWrapper(logless, mockServlet);

        MockHTTP.MockResponse response = new MockHTTP.MockResponse();
        wrapper.service(new MockHTTP.MockRequest("POST", "REQUEST"), response);
        Assert.assertEquals("RESPONSE", response.dataString());
    }

    public static class MockServlet extends HttpServlet {
        protected String response;

        public MockServlet(String response) {
            this.response = response;
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            System.out.println("OUT TEST");
            System.err.println("ERR TEST");
            response.getOutputStream().print(this.response);
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }
    }

    public Logless newLogless(IVerifier verifier) {
        Logless logless = new Logless("6739f6ae-c54c-4869-85ea-416b8f00ed2e") {
            @Override
            public LoglessContext newContext() {
                return new LoglessContext(this.source) {
                    public void transmit(String jsonString) {
                        super.transmit(jsonString);
                        JsonNode json = JSONUtil.toJSON(jsonString);
                        verifier.verify(json);
                    }
                };
            }
        };

        return logless;
    }

    public interface IVerifier {
        void verify(JsonNode json);
    }
}
