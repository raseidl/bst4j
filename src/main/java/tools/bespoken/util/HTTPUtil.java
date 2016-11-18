package tools.bespoken.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Created by jpk on 11/17/16.
 */
public class HTTPUtil {
    public static Response get (String urlString) throws Exception {
        long startTime = System.currentTimeMillis();
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setDoInput(true);
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteBuffer payload = ByteBuffer.allocate(10000);
        byte [] buffer = new byte[2048];
        int bytesRead = bis.read(buffer);
        while (bytesRead > -1) {
            payload.put(buffer, 0, bytesRead);
            bytesRead = bis.read(buffer);
        }

        System.out.append("Time: " + (System.currentTimeMillis() - startTime) + "\n");
        payload.flip();
        return new Response(connection.getResponseCode(), payload.array());
    }

    public static Response post (String urlString, byte [] postData) throws Exception {
        long startTime = System.currentTimeMillis();
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
        bos.write(postData, 0, postData.length);

        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteBuffer payload = ByteBuffer.allocate(10000);
        byte [] buffer = new byte[2048];
        int bytesRead = bis.read(buffer);
        while (bytesRead > -1) {
            payload.put(buffer, 0, bytesRead);
            bytesRead = bis.read(buffer);
        }

        System.out.append("Time: " + (System.currentTimeMillis() - startTime) + "\n");
        payload.flip();
        return new Response(connection.getResponseCode(), payload.array());
    }

    public static class Response {
        public int statusCode;
        public byte [] data;

        public Response(int statusCode, byte[] data) {
            this.statusCode = statusCode;
            this.data = data;
        }

        public String asString () {
            return new String(data);
        }
    }
}
