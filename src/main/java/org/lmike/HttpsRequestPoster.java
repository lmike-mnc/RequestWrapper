package org.lmike;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class HttpsRequestPoster {
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    static boolean res2File;

    static {
        Properties prop = new Properties();
        try (InputStream inputStream = HttpsRequestPoster.class.getResourceAsStream("/project.properties")) {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        res2File = Boolean.parseBoolean(prop.getProperty("resultToFile", "false"));
    }

    public static String GetFileRequest(String url, String authToken, String filename) throws IOException {
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

    public static String PostRequest(String url, String bodyJSON) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        HttpPost post = new HttpPost(url);
        return PostRequestWithAuthToken(url, bodyJSON, null);
    }

    public static String PostRequestWithAuthToken(String url, String bodyJSON, String authToken) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        HttpPost post = new HttpPost(url);
        return getStringFromMethod(bodyJSON, authToken, post);
    }

    public static String PutRequestWithAuthToken(String url, String bodyJSON, String authToken) throws IOException {
        HttpPut put = new HttpPut(url);
        return getStringFromMethod(bodyJSON, authToken, put);
    }

    private static String getStringFromMethod(String bodyJSON, String authToken, HttpEntityEnclosingRequestBase method) throws IOException {
        method.addHeader("content-type", "application/json;charset=UTF-8");
        if (authToken != null) method.addHeader("X-Auth-Token", authToken);

        StringEntity body = new StringEntity(bodyJSON);
        method.setEntity(body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(method)) {
                //System.out.println("11");
                //System.out.println(EntityUtils.toString(response.getEntity()));
                return resultToFile(EntityUtils.toString(response.getEntity()));
            }
        }
    }

    public static String GetRequest(String url, String authToken) throws IOException {

        HttpGet request = new HttpGet(url);
        return processRequest(authToken, request);
    }

    public static String DeleteRequestWithAuthToken(String url, String authToken) throws IOException {
        HttpDelete request = new HttpDelete(url);

        // add request headers
        return processRequest(authToken, request);
    }

    private static String processRequest(String authToken, HttpRequestBase request) throws IOException {
        request.addHeader("X-Auth-Token", authToken);
        //request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                // Get HttpResponse Status
                System.out.println(response.getStatusLine().toString());

                HttpEntity entity = response.getEntity();
                Header headers = entity.getContentType();
                System.out.println(headers);

                return resultToFile(EntityUtils.toString(response.getEntity()));
            }
        }
    }

    static String resultToFile(String txt) throws IOException {
        if (res2File) {
            Path tmp = Files.createTempFile("res", ".txt");
            PrintWriter pw = new PrintWriter(tmp.toFile());
            pw.println(txt);
            return tmp.toFile().getAbsolutePath();
        }
        return txt;
    }

    public static void main(String[] args) {

    }
}
