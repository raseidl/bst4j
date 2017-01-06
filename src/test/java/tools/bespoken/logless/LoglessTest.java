package tools.bespoken.logless;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by jpk on 1/6/17.
 */
public class LoglessTest {
    @Test
    public void testCaptureTransaction () throws Exception {
        MockHTTP.MockResponse mockResponse = new MockHTTP.MockResponse();
        Logless.capture("TRANSACTION", new MockHTTP.MockRequest("POST", "REQUEST"), mockResponse, (HttpServletRequest request, HttpServletResponse response) -> {
            response.getWriter().write("TEST");
        });

        Assert.assertEquals("TEST", mockResponse.dataString());
    }
}
