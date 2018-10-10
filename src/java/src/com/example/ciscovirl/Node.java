package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import java.util.ArrayList;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Node implements StatsList {
    private Integer connectionIndex;
    public String name;
    private String type;
    private String subtype;
    public String state;
    public String excludeFromLaunch = "false";
    private String location = "50,50";
    public boolean reachable = false;
    public Extensions extensions;
    @SerializedName("interface") public ArrayList<Interface> interfaces;

    private String startXML = "<node name=\"%s\" type=\"%s\" subtype=\"%s\" location=\"%s\"> \n";
    private String endXml = "</node> \n";

    public String toXML() {
        String xml = String.format(startXML, this.name, this.type, this.subtype, this.location);
        xml = xml + extensions.toXML();
        for (Interface intf : interfaces) {
            xml = xml + intf.toXML();
        }
        xml = xml + this.endXml;
        return xml;
    }
    public Node() {
        this.interfaces = new ArrayList<Interface>();
    }
    public Node(String name, Integer connectionIndex) {
        this();
        this.name = name;
        this.connectionIndex = connectionIndex;
    }
    public Node(NavuNode nodeModel) throws NavuException {
        this();
        if (nodeModel.leaf(ciscovirl._connection_index).valueAsString() != null) {
            this.connectionIndex = new Integer(nodeModel.leaf(ciscovirl._connection_index).valueAsString());
        }
        this.name = nodeModel.leaf(ciscovirl._name).valueAsString();
        this.type = nodeModel.leaf(ciscovirl._type).valueAsString();
        this.subtype = nodeModel.leaf(ciscovirl._subtype).valueAsString();
//        this.state = nodeModel.leaf(ciscovirl._state).valueAsString();
        this.excludeFromLaunch = nodeModel.leaf(ciscovirl._excludeFromLaunch).valueAsString();
        this.location = nodeModel.leaf(ciscovirl._location).valueAsString();
        this.extensions = new Extensions(nodeModel.container(ciscovirl._extensions));
        for (NavuNode intrf : nodeModel.list(ciscovirl._interface).children()) {
            interfaces.add(new Interface(intrf));
        }
    }
	public Node(Element topoDeviceNode, int index) throws Exception {
        this();
		this.connectionIndex = new Integer(index);
    	NamedNodeMap attributes = topoDeviceNode.getAttributes();
    	this.name = attributes.getNamedItem("name").getTextContent();
//System.out.print("ADD NODE: "+this.name+" "+this.connectionIndex+"\n");
    	this.type = attributes.getNamedItem("type").getTextContent();
        this.subtype = attributes.getNamedItem("subtype").getTextContent();
        if (attributes.getNamedItem("location") != null) this.location = attributes.getNamedItem("location").getTextContent();
        // if (attributes.getNamedItem("excludeFromLaunch") != null) 
        //     System.out.println("Node: "+this.name+" excludeFromLaunch: "+attributes.getNamedItem("excludeFromLaunch").getTextContent());
        // else 
        //     System.out.println("Node: "+this.name+" excludeFromLaunch: NOT SET");
        if (attributes.getNamedItem("excludeFromLaunch") != null) this.excludeFromLaunch = attributes.getNamedItem("excludeFromLaunch").getTextContent();
    	Element extensionNode = (Element) topoDeviceNode.getElementsByTagName("extensions").item(0);
        this.extensions = new Extensions(extensionNode);
        NodeList interfaceNodes = topoDeviceNode.getElementsByTagName("interface");
        for (int i = 0; i < interfaceNodes.getLength(); i++) {
            Element node = (Element) interfaceNodes.item(i);
            interfaces.add(new Interface(node, i+1));
        }
	}
    public static boolean isStatsListPath(ConfPath path) {
        return (path.toString().matches(".*/node"));
    }
    public static String getStatsListURL(ConfPath path) {
        return "/nodes/"+Simulation.getName(path);
    }
    public static String getStatsListJsonPath(ConfPath path) {
        return Simulation.getName(path);
    }
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath topologypath) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Node with NULL Name");
        ConfPath path = new ConfPath(topologypath.toString());
        if (!this.isStatsListPath(topologypath)) path = new ConfPath(topologypath.toString()+"/node");
        ConfPath nodePath = new ConfPath(path.toString()+"{"+this.name+"}");
        if (!maapi.exists(tHandle, nodePath)) maapi.create(tHandle, nodePath);
        maapi.setElem(tHandle, this.excludeFromLaunch, new ConfPath(nodePath.toString()+"/excludeFromLaunch"));
        if (this.isStatsListPath(topologypath)) {
            maapi.setElem(tHandle, new Boolean(this.reachable).toString(), new ConfPath(nodePath.toString()+"/reachable"));
            if (this.state != null) maapi.setElem(tHandle, this.state, new ConfPath(nodePath.toString()+"/state"));
        } else {
            if (this.connectionIndex != null) maapi.setElem(tHandle, this.connectionIndex.toString(), new ConfPath(nodePath.toString()+"/connection-index"));
            if (this.type != null) maapi.setElem(tHandle, this.type, new ConfPath(nodePath.toString()+"/type"));
            if (this.subtype != null) maapi.setElem(tHandle, this.subtype, new ConfPath(nodePath.toString()+"/subtype"));
            if (this.location != null) maapi.setElem(tHandle, this.location, new ConfPath(nodePath.toString()+"/location"));
            if (this.extensions != null) 
            	this.extensions.saveToNSO(maapi, tHandle, nodePath);
            if (this.interfaces != null) {
    	        for (Interface i: this.interfaces) {
        	        i.saveToNSO(maapi, tHandle, nodePath);
            	}
            }
        }
    }
    public static Node getInstanceOf(Map.Entry<String, JsonElement> entry) throws Exception {
        Gson gson = new Gson();
        Node node = gson.fromJson(entry.getValue(), Node.class);
        node.setName(entry.getKey());
        return node;
    }
    public static Node getInstanceOf(Map.Entry<String, JsonElement> entry, VirlComms comms) throws Exception {
        Node node = Node.getInstanceOf(entry);
        return node;
    }
    public static String getName(ConfPath path) {
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(path.toString());
        matcher.find();
        matcher.find();
        matcher.find();
        return matcher.group(1);
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}
