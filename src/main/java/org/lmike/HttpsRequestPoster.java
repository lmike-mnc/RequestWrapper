package org.lmike;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HttpsRequestPoster {
    public String PostRequest(String url, String bodyJSON) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        HttpPost post = new HttpPost(url);
        post.addHeader("content-type", "application/json;charset=UTF-8");
        //String bodyJSON = "{\"username\":\"onepoint_br@mtt.ru\",\"password\":\"Boss-2021\"}";
        try{
            StringEntity body = new StringEntity(bodyJSON);
            post.setEntity(body);
        } catch (Exception e){
            System.out.println(e);
        }

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpClient.execute(post)) {
                    return EntityUtils.toString(response.getEntity());
                }
            }
    }

    public String GetFileRequest(String url, String authToken, String filename) throws IOException {
        HttpGet request = new HttpGet(url);
        // add request headers
        request.addHeader("X-Auth-Token", authToken);
        //request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    // Get HttpResponse Status
                    System.out.println(response.getStatusLine().toString());

                    HttpEntity entity = response.getEntity();
                    Header headers = entity.getContentType();
                    System.out.println(headers);

                    File myFile = new File(filename);
                    FileOutputStream outStream = new FileOutputStream(myFile);
                    entity.writeTo(outStream);
                    outStream.close();
                }
            }
        return filename;
    }

    public String PostRequestWithAuthToken(String url, String bodyJSON, String authToken) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        HttpPost post = new HttpPost(url);
        return getStringFromMethod(bodyJSON, authToken, post);
    }

    public String PutRequestWithAuthToken(String url, String bodyJSON, String authToken) throws IOException {
        HttpPut put = new HttpPut(url);
        return getStringFromMethod(bodyJSON, authToken, put);
    }

    private String getStringFromMethod(String bodyJSON, String authToken, HttpEntityEnclosingRequestBase method) throws IOException {
        method.addHeader("content-type", "application/json;charset=UTF-8");
        method.addHeader("X-Auth-Token", authToken);

        StringEntity body = new StringEntity(bodyJSON);
        method.setEntity(body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(method)) {
                //System.out.println("11");
                //System.out.println(EntityUtils.toString(response.getEntity()));
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    public String GetRequest(String url, String authToken) throws IOException {

        HttpGet request = new HttpGet(url);
        return processRequest(authToken, request);
    }

    public String DeleteRequestWithAuthToken(String url, String authToken) throws IOException {
        HttpDelete request = new HttpDelete(url);

        // add request headers
        return processRequest(authToken, request);
    }

    private String processRequest(String authToken, HttpRequestBase request) throws IOException {
        request.addHeader("X-Auth-Token", authToken);
        //request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    // Get HttpResponse Status
                    System.out.println(response.getStatusLine().toString());

                    HttpEntity entity = response.getEntity();
                    Header headers = entity.getContentType();
                    System.out.println(headers);

                    return EntityUtils.toString(response.getEntity());
                }
            }
    }

}
