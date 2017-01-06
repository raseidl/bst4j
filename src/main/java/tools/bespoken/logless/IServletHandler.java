package tools.bespoken.logless;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Simple task wrapper for servlet with correct exceptions for servlet processing
 */
public interface IServletHandler {
    void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
