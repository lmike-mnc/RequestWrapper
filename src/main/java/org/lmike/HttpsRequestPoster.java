package org.lmike;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Collectors;

public class HttpsRequestPoster {
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private static final int STATUS_OK = 200;
    private enum ARGS {FILE, METHOD, URI, DATA, JSON}

    static boolean res2File;
    static String apiAuth;
    static String apiRegUnits;

    static {
        Properties prop = new Properties();
        Properties apiProp = new Properties();
        try (InputStream inputStream = HttpsRequestPoster.class.getResourceAsStream("/project.properties")) {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        res2File = Boolean.parseBoolean(prop.getProperty("resultToFile", "false"));
        try (InputStream inputStream = HttpsRequestPoster.class.getResourceAsStream("/api.properties")) {
            apiProp.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        apiAuth= apiProp.getProperty("apiAuth");
        apiRegUnits=apiProp.getProperty("apiRegUnits");
    }

    public static String getToken(){
        String token=null;
        try (InputStream inputStream = HttpsRequestPoster.class.getResourceAsStream("/package.json")) {
            if (inputStream != null) {
                String json=new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                String res=postRequest(apiAuth,json);
                if (res!=null){
                    if (res2File){
                        token=JsonPath.read(new FileInputStream(res),"$.data");

                    }else{
                        token=JsonPath.read(res,"$.data");
                    }
                    LOG.info("\n->"+Thread.currentThread().getStackTrace()[1].getMethodName()+":"+token);
                }
            }
        } catch (IOException e) {
            LOG.trace("exception",e);
        }
        return token;
    }
    public static String getFileRequest(String url, String authToken, String filename) throws IOException {
        HttpGet request = new HttpGet(url);
        // add request headers
        request.addHeader("X-Auth-Token", authToken);
        //request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                // Get HttpResponse Status
                LOG.debug(response.getStatusLine().toString());

                HttpEntity entity = response.getEntity();
                Header headers = entity.getContentType();
                LOG.debug(String.valueOf(headers));
                int status=response.getStatusLine().getStatusCode();
                if (status!=STATUS_OK){
                    LOG.error("\n->error:"+status+";"+response.getStatusLine().getReasonPhrase());
                }else {
                    File myFile = new File(filename);
                    FileOutputStream outStream = new FileOutputStream(myFile);
                    entity.writeTo(outStream);
                    outStream.close();
                }
            }
        }
        return resultToFile(filename);
    }

    public static String postRequest(String url, String bodyJSON) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        return postRequestWithAuthToken(url, null, bodyJSON);
    }

    public static String postRequestWithAuthToken(String url, String authToken, String bodyJSON) throws IOException {
        //HttpPost post = new HttpPost("https://api.infologistics.ru/api4/v4/authenticate");
        HttpPost post = new HttpPost(url);
        return getStringFromMethod(post, bodyJSON, authToken);
    }

    public static String putRequestWithAuthToken(String url, String authToken, String bodyJSON) throws IOException {
        HttpPut put = new HttpPut(url);
        return getStringFromMethod(put, bodyJSON, authToken);
    }

    private static String getStringFromMethod(HttpEntityEnclosingRequestBase method, String bodyJSON, String authToken) throws IOException {
        method.addHeader("content-type", "application/json;charset=UTF-8");
        if (authToken != null) method.addHeader("X-Auth-Token", authToken);

        StringEntity body = new StringEntity(bodyJSON);
        method.setEntity(body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(method)) {
                int status=response.getStatusLine().getStatusCode();
                if (status!=STATUS_OK){
                    LOG.error("\n->error:"+status+";"+response.getStatusLine().getReasonPhrase());
                }else{
                    return resultToFile(EntityUtils.toString(response.getEntity()));
                }
            }
        }
        return null;
    }

    public static String getRequest(String url, String authToken) throws IOException {

        HttpGet request = new HttpGet(url);
        return processRequest(authToken, request);
    }

    public static String deleteRequestWithAuthToken(String url, String authToken) throws IOException {
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
                LOG.debug(response.getStatusLine().toString());

                HttpEntity entity = response.getEntity();
                Header headers = entity.getContentType();
                LOG.debug(String.valueOf(headers));
                return resultToFile(EntityUtils.toString(response.getEntity()));
            }
        }
    }

    /**
     *
     * @param txt
     * @return text (contents) or file path (with text contents)
     * @throws IOException
     */
    static String resultToFile(final String txt) throws IOException {
        String res=txt;
        LOG.info("\n->text:"+txt);
        LOG.debug("\n"+TextTest.getBody("->text:"+txt,TextTest.txtRegexp));
        if (res2File) {
            Path tmp = Files.createTempFile("res", ".txt");
            PrintWriter pw = new PrintWriter(tmp.toFile());
            pw.println(txt);
            pw.close();
            tmp.toFile().deleteOnExit();
            res=tmp.toFile().getAbsolutePath();
        }
        LOG.info("\n->file:"+res);
        return res;
    }

    public static void main(String[] args) throws IOException {
        String token=getToken();
        String data=null;
        String method=null;
        String uri=null;
        if (token!=null){
            for(String s:args){
                if(s.toUpperCase().startsWith(String.valueOf(ARGS.FILE))){
                    data=JsonPath.read(new FileInputStream(s.split("=")[1]),"$");
                }else if(s.toUpperCase().startsWith(String.valueOf(ARGS.DATA))){
                    data=s.split("=")[1];
                }else if(s.toUpperCase().startsWith(String.valueOf(ARGS.METHOD))){
                    method=s.split("=")[1];
                }else if(s.toUpperCase().startsWith(String.valueOf(ARGS.URI))){
                    uri=s.split("=")[1];
                }

        }
            if (token!=null && uri!=null && method!=null){
                switch (method)
                {
                    case "deleteRequestWithAuthToken":
                        deleteRequestWithAuthToken(uri,token);
                        break;
                    case "getRequest":
                        getRequest(uri,token);
                        break;
                    case "postRequestWithAuthToken":
                        if(data!=null )
                        postRequestWithAuthToken(uri,token,data);
                        break;
                    case "PutRequestWithAuthToken":
                        if(data!=null )
                        putRequestWithAuthToken(uri,token,data);
                        break;
                    case "getFileRequest":
                        if(data!=null )
                        getFileRequest(uri,token,data);
                }

            }
        }
    }
}
