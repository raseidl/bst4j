package bespoken.logless;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by jpk on 11/8/16.
 */
public class LoglessContextTest {

    @Test
    public void testSimpleLogs () {
        LoglessContext context = new LoglessContext("bst4j");
        context.log(LoglessContext.LogType.INFO, "Test", null, new String[] {"TagTest"});
        context.flush();
    }
}
