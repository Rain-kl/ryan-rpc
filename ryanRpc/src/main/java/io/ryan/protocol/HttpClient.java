package io.ryan.protocol;

import io.ryan.common.Invocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpClient {

    public String send(String hostname, int port, Invocation invocation) {
        try {
            URL url = new URL("http", hostname, port, "/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            // Serialize the invocation object to JSON or any other format
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(invocation);
            objectOutputStream.flush();
            objectOutputStream.close();

            // Get the response code
            InputStream inputStream = httpURLConnection.getInputStream();

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL Exception occurred while sending request: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("IO Exception occurred while sending request: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
