package io.ryan.protocol.client.HttpClient;


import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.protocol.client.AbstractRpcClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClientImpl extends AbstractRpcClient {

    public HttpClientImpl(String hostname, Integer port) {
        super(hostname, port);
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            URL url = new URL("http", getHostname(), getPort(), "/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            // Serialize the invocation object to JSON or any other format
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            objectOutputStream.close();

            // Get the response and deserialize it
            InputStream inputStream = httpURLConnection.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            objectInputStream.close();

            return rpcResponse;

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL Exception occurred while sending request: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("IO Exception occurred while sending request: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found while deserializing response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
