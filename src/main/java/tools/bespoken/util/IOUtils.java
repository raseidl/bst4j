package tools.bespoken.util;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by jpk on 1/4/17.
 */
public class IOUtils {
    public static byte [] toByteArray(Reader reader) throws IOException {
        char[] charArray = new char[8 * 1024];
        StringBuilder builder = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(charArray, 0, charArray.length)) != -1) {
            builder.append(charArray, 0, numCharsRead);
        }
        byte[] targetArray = builder.toString().getBytes();
        return targetArray;
    }

    public static String toString(Reader reader) throws IOException {
        return new String(IOUtils.toByteArray(reader));
    }
}
