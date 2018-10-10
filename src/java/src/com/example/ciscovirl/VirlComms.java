package com.example.ciscovirl;

import java.io.IOException;
import java.net.InetAddress;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.HttpEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import org.xml.sax.InputSource;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.tailf.ned.NedException;
import com.tailf.ned.NedErrorCode;

public class VirlComms {
    private CloseableHttpClient httpclient;
    private CredentialsProvider credsProvider;
    private String baseAPIURL;
    private VirlComms comms;
    private ciscovirlNed ned;

    public enum RequestType {
        PUT, GET, POST;
    }

	public VirlComms(ciscovirlNed ned, InetAddress ip, int port, String username, String password) {
		this.ned = ned;
        this.baseAPIURL = "http://"+ip.getHostAddress()+":"+port+"/simengine/rest";
        this.credsProvider = new BasicCredentialsProvider();
        this.credsProvider.setCredentials(
                new AuthScope(ip.getHostAddress(), port),
                new UsernamePasswordCredentials(username, password));
    }
    public void connect() {
        this.httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
    }
    public String execRequest(Object requestType, String requestURL, String requestPayload, boolean dryRun) throws NedException {
        try {
            connect();
            String requestString = baseAPIURL+requestURL;
//    System.out.println("EXEC ("+requestType.toString()+") REQUEST: " + requestURL);
            try {
                String responseStr = null;
                CloseableHttpResponse response = null;
                if (requestType == RequestType.PUT) {
                    HttpPut httput = new HttpPut(requestString);
                    if (dryRun) return httput.toString();
                    try {
                        response = httpclient.execute(httput);
                        responseStr = EntityUtils.toString(response.getEntity());
                        if (response.getStatusLine().getStatusCode() != 200) {
    System.out.println("REQUEST("+httput.getURI().toString());
    System.out.println("RESPONSE CODE ("+response.getStatusLine()+") RESPONSE:\n" + responseStr);
                            throw new NedException(NedErrorCode.CONNECT_CONNECTION_REFUSED, response.getStatusLine().toString());
                        }
                    } finally {
                        response.close();
                    }
                } else if (requestType == RequestType.GET) {
                    HttpGet httpget = new HttpGet(requestString);
                    if (dryRun) return httpget.toString();
                    try {
                        response = httpclient.execute(httpget);
                        responseStr = EntityUtils.toString(response.getEntity());
                        if (response.getStatusLine().getStatusCode() != 200) {
    System.out.println("REQUEST("+httpget.getURI().toString());
    System.out.println("RESPONSE CODE ("+response.getStatusLine()+") RESPONSE:\n" + responseStr);
                            throw new NedException(NedErrorCode.CONNECT_CONNECTION_REFUSED, response.getStatusLine().toString());
                        }
                    } finally {
                        response.close();
                    }
                } else if  (requestType == RequestType.POST) {
                    HttpPost httppost = new HttpPost(requestString);
                    if (dryRun) return httppost.toString() + "\n" + requestPayload;
                    if (requestPayload != null) {
                        StringEntity payload = new StringEntity(requestPayload, ContentType.APPLICATION_XML);
                        httppost.setEntity(payload);
                    }
                    try {
                        response = httpclient.execute(httppost);
                        responseStr = EntityUtils.toString(response.getEntity());
                        if (response.getStatusLine().getStatusCode() != 200) {
    System.out.println("REQUEST("+httppost.getURI().toString());
    System.out.println("RESPONSE CODE ("+response.getStatusLine()+") RESPONSE:\n" + responseStr);
    System.out.println("REQUEST PAYLOAD:\n" + requestPayload);
                            throw new NedException(NedErrorCode.CONNECT_CONNECTION_REFUSED, response.getStatusLine().toString());
                        }
                    } finally {
                        response.close();
                    }
                }
                return responseStr;
            } finally {
                httpclient.close();
            }
        } catch (IOException e) {
            throw new NedException(NedErrorCode.CONNECT_CONNECTION_REFUSED, "IO Communication problem", e);
        }
    }
    public String execRequest(Object requestType, String requestURL) throws Exception {
        return execRequest(requestType, requestURL, null, false);
    }
    public String execRequest(Object requestType, String requestURL, String requestPayload) throws Exception {
        return execRequest(requestType, requestURL, requestPayload, false);
    }
    public Document requestXMLData (String urlSuffix) throws Exception {
        String xml = execRequest(RequestType.GET, urlSuffix);
        return convertStringToDocument(xml);
    }
    private JsonObject requestJSONData (Object requestType, String urlSuffix, String jsonPath, String payload) throws Exception {
        String json = execRequest(requestType, urlSuffix, payload, false);
System.out.println("RESPONSE:\n" + json);
        JsonObject retJsonObj = new JsonParser().parse(json).getAsJsonObject();
        if (jsonPath == null) return retJsonObj;
        String[] pathElements = jsonPath.split("/");
        for (String elem: pathElements) {
            retJsonObj = retJsonObj.getAsJsonObject().get(elem).getAsJsonObject();
        }
        return retJsonObj;
    }
    public JsonObject requestJSONData (String urlSuffix, String jsonPath) throws Exception {
        return requestJSONData(RequestType.GET, urlSuffix, jsonPath, null);
    }
    // public String requestExecuteCommand (String urlSuffix, String payload) throws Exception {
    //     return execRequest(RequestType.POST, urlSuffix, payload);
    //     // return requestJSONData(RequestType.POST, urlSuffix, jsonPath, payload);
    // }
    private static Document convertStringToDocument(String xmlStr) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        builder = factory.newDocumentBuilder();  
        return builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
    }
    public static void docToString(Document newDoc) throws Exception{
        DOMSource domSource = new DOMSource(newDoc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        System.out.println("DOCtoSTRING: \n"+sw.toString());  
    }
    public static void nodeToString(Node node) throws Exception {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        System.out.println("NODEtoSTRING: \n"+sw.toString());  
    }
}