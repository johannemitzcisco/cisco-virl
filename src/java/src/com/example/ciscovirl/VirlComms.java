package com.example.ciscovirl;

import java.io.IOException;
import java.net.InetAddress;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

public class VirlComms {
    private CloseableHttpClient httpclient;
    private CredentialsProvider credsProvider;
    private String baseAPIURL;
    private VirlComms comms;
    private ciscovirlNed ned;

    private enum RequestType {
        GET, POST;
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
    private String execRequest(Object requestType, String requestURL) throws IOException{
        connect();
        String requestString = baseAPIURL+requestURL;
System.out.println("EXEC ("+requestType.toString()+") REQUEST: " + requestURL);
        try {
            HttpGet httpget = new HttpGet(requestString);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                String responseStr = EntityUtils.toString(response.getEntity());
System.out.println("REQUEST XML DATA: \n" + responseStr);
                return responseStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
    public Document requestXMLData (String urlSuffix) throws Exception {
        String xml = execRequest(RequestType.GET, urlSuffix);
        return convertStringToDocument(xml);
//         String json = XML.toJSONObject(xml).toString();
// //        LOGGER.info(XML.toJSONObject(xml).toString(4));
//         JsonObject retJsonObj = new JsonParser().parse(json).getAsJsonObject();
//         String[] pathElements = jsonPath.split("/");
//         for (String elem: pathElements) {
//             retJsonObj = retJsonObj.getAsJsonObject().get(elem).getAsJsonObject();
//         }
//         return retJsonObj;
    }
    public JsonObject requestJSONData (String urlSuffix, String jsonPath) throws IOException {
        String json = execRequest(RequestType.GET, urlSuffix);
        JsonObject retJsonObj = new JsonParser().parse(json).getAsJsonObject();
        String[] pathElements = jsonPath.split("/");
        for (String elem: pathElements) {
            retJsonObj = retJsonObj.getAsJsonObject().get(elem).getAsJsonObject();
        }
        return retJsonObj;
    }
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