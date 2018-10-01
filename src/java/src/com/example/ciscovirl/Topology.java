package com.example.ciscovirl;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
// import com.tailf.conf.ConfValue;
// import com.tailf.conf.ConfBuf;
// import com.tailf.conf.ConfUInt32;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;


public class Topology {
	private Simulation simulation;
    private String name;
    private String xmlns;
    private String schemaVersion;
    private String xsiSchemaLocation;
    private String xmlnsXSI;
    private Extensions extensions;
    @SerializedName("node") public ArrayList<com.example.ciscovirl.Node> nodes;
    @SerializedName("connection") public ArrayList<Connection> connections;

    public Topology() {
    }
    public Topology(String name, Document topologyDoc) throws Exception {
        this.name = name;
        this.nodes = new ArrayList<com.example.ciscovirl.Node>();
        this.connections = new ArrayList<Connection>();
        NamedNodeMap attributes = topologyDoc.getDocumentElement().getAttributes();

        this.xmlns = attributes.getNamedItem("xmlns").getTextContent();
        this.schemaVersion = attributes.getNamedItem("schemaVersion").getTextContent();
        this.xmlnsXSI = attributes.getNamedItem("xmlns:xsi").getTextContent();
        this.xsiSchemaLocation = attributes.getNamedItem("xsi:schemaLocation").getTextContent();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile("/topology/extensions");
        Element extensionNode = (Element) expr.evaluate(topologyDoc, XPathConstants.NODE);
        this.extensions = new Extensions(extensionNode);

        NodeList topoNodes = topologyDoc.getElementsByTagName("node");
        for (int i = 0; i < topoNodes.getLength(); i++) {
            Element node = (Element) topoNodes.item(i);
            nodes.add(new com.example.ciscovirl.Node(node, i+1));
        }

        NodeList connNodes = topologyDoc.getElementsByTagName("connection");
        for (int i = 0; i < connNodes.getLength(); i++) {
            Element node = (Element) connNodes.item(i);
            connections.add(new Connection(node));
        }
    }
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath simPath) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Topology with NULL Name");
        ConfPath topoPath = new ConfPath(simPath.toString()+"/topology");
        if (this.name != null) maapi.setElem(tHandle, this.name, new ConfPath(topoPath.toString()+"/name"));
        if (this.xmlns != null) maapi.setElem(tHandle, this.xmlns, new ConfPath(topoPath.toString()+"/annotation-xmlns"));
        if (this.schemaVersion != null) maapi.setElem(tHandle, this.schemaVersion, new ConfPath(topoPath.toString()+"/annotation-schemaVersion"));
        if (this.xsiSchemaLocation != null) maapi.setElem(tHandle, this.xsiSchemaLocation, new ConfPath(topoPath.toString()+"/annotation-xsiSchemaLocation"));
        if (this.xmlnsXSI != null) maapi.setElem(tHandle, this.xmlnsXSI
            , new ConfPath(topoPath.toString()+"/annotation-xmlnsXSI"));
        this.extensions.saveToNSOLiveStatus(maapi, tHandle, new ConfPath(topoPath.toString()));
        if (nodes != null) {
            for (com.example.ciscovirl.Node n: this.nodes) {
                n.saveToNSOLiveStatus(maapi, tHandle, new ConfPath(topoPath.toString()));
            }
        }
        if (connections != null) {
            for (Connection c: this.connections) {
                c.saveToNSOLiveStatus(maapi, tHandle, new ConfPath(topoPath.toString()));
            }
        }
    }

    // public Topology(String name) {
    //     setName(name);
    // }
    // public void setName(String name) {
    //     System.out.println("TOPOLOGY (name): "+name);
    //     this.name = name;
    // }
    // public String getName() {
    //     return this.name;
    // }
    // public void setSchemaVersion(String schemaVersion) {
    //     this.schemaVersion = schemaVersion;
    // }
    // public String getSchemaVersion() {
    //     return this.schemaVersion;
    // }
    // public void setSimulation(Simulation simulation) {
    //     this.simulation = simulation;
    // }
    // public Simulation getSimulation() {
    //     return this.simulation;
    // }

    // public void setExtensions(Extensions extensions) {
    //     this.extensions = extensions;
    // }
    // public Extensions getExtensions() {
    //     return extensions;
    // }
    // public void setNodes(List<Node> nodes) {
    //     this.nodes = nodes;
    // }
    // public List<Node> getNode() {
    //     return nodes;
    // }
    // public void setConnection(List<Connection> connections) {
    //     this.connections = connections;
    // }
    // public List<Connection> getConnection() {
    //     return this.connections;
    // }
    // public String toString() {
    // 	String str = getName()+", "+getSchemaVersion()+"\n";
    // 	str = str + extensions.toString()+"\n";
    // 	for (Node n: nodes) {
    // 		str = str + n.toString()+"\n";
    // 	}
    // 	for (Connection c: connections) {
    // 		str = str + c.toString()+"\n";
    // 	}
    //     return str;
    // }
    // public ConfPath getConfigPath() throws Exception {
    // 	return new ConfPath(simulation.getConfigPath()+"/topology");
    // }
    // public void toNSO(ciscovirlNed ned, int tHandle, Simulation simulation) throws Exception {
    // 	this.simulation = simulation;
    // 	for (Node n: nodes) {
	   //      n.toNSO(ned, tHandle, this);
    // 	}
    // }





//     public static boolean isPath(ConfPath path) {
//         return (path.toString().matches(".*/topology") || path.toString().matches(".*/topology\\{.*\\}/.*[^/]"));
//     }
//     public static String getURLSuffix(ConfPath path) {
// //    	if (!Topology.isPath(path)) return null;
//     	return "/list";
//     }
//     public static String getJsonRoot(ConfPath path) {
//     	return "topologys";
//     }
//     public static String getName(ConfPath path) {
// //    	if (! Topology.isPath(path)) return null;
//         Pattern pattern = Pattern.compile("\\{(.*?)\\}");
//         Matcher matcher = pattern.matcher(path.toString());
//         matcher.find();
//         matcher.find();
//         return matcher.group(1);
//     }
//     public static ConfValue getConfValueFromPath(JsonObject simJson, ConfPath path) {
//     	if (! Topology.isPath(path)) return null;
//         Gson gson = new Gson();
//         String simName = Topology.getName(path);
// //        System.out.println("getValueFromPath (simName): '"+simName+"'");
//         int index = 0;
//         for (Map.Entry<String, JsonElement> entry : simJson.entrySet()) {
// //	        System.out.println("getValueFromPath: Checking Entry: '"+entry.getKey()+"'");
//         	if (simName.equals(entry.getKey().toString())) {
// 	            Topology topology = gson.fromJson(entry.getValue(), Topology.class);
// 	            topology.setName(entry.getKey());
// 		        String[] pathSplit = path.toString().split("/",0);
// //		        System.out.println("getValueFromPath: pathSplit: "+pathSplit);
// 		        String attributeName = pathSplit[pathSplit.length-1];
// //		        System.out.println("getValueFromPath: Attribute to Return: "+attributeName);
// 		        switch (attributeName) {
// 		        	case "name" :
// 		        		return new ConfBuf(topology.getName());
// 		        	case "status" :
// 		        		return new ConfBuf(topology.getStatus());
// 		        	case "launched" :
// 		        		return new ConfBuf(topology.getLaunched());
// 		        	case "expires" :
// 		        		return new ConfBuf(topology.getExpires());
// 		        	default:
// 		        		return null;
// 		        }
// 	        }
// 	        index++;
//         }
//         return null;
//     }
}
