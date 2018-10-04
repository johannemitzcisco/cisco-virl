package com.example.ciscovirl;

import java.util.ArrayList;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
// import com.tailf.conf.ConfValue;
// import com.tailf.conf.ConfBuf;
// import com.tailf.conf.ConfUInt32;

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
    private Topology topology;
    private Integer connectionIndex;
    private String name;
    private String type;
    private String subtype;
    private String state;
    private String excludeFromLaunch = "false";
    private String location = "50,50";
    private Extensions extensions;
    @SerializedName("interface") public ArrayList<Interface> interfaces;

	public Node(Element topoDeviceNode, int index) throws Exception {
        this.interfaces = new ArrayList<Interface>();
		this.connectionIndex = new Integer(index);
//		VirlComms.nodeToString(topoDeviceNode);
    	NamedNodeMap attributes = topoDeviceNode.getAttributes();
    	this.name = attributes.getNamedItem("name").getTextContent();
    	this.type = attributes.getNamedItem("type").getTextContent();
        this.subtype = attributes.getNamedItem("subtype").getTextContent();
        if (attributes.getNamedItem("location") != null) this.location = attributes.getNamedItem("location").getTextContent();
        if (attributes.getNamedItem("excludeFromLaunch") != null) 
            System.out.println("Node: "+this.name+" excludeFromLaunch: "+attributes.getNamedItem("excludeFromLaunch").getTextContent());
        else 
            System.out.println("Node: "+this.name+" excludeFromLaunch: NOT SET");
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
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Node with NULL Name");
        String newPath = path.toString();
        if (newPath.matches(".*/topology")) newPath = newPath + "/node"; // Call came from Topology, add node Node
        ConfPath nodePath = new ConfPath(newPath+"{"+this.name+"}");
        if (!maapi.exists(tHandle, nodePath)) maapi.create(tHandle, nodePath);
        if (this.connectionIndex != null) maapi.setElem(tHandle, this.connectionIndex.toString(), new ConfPath(nodePath.toString()+"/connection-index"));
        if (this.type != null) maapi.setElem(tHandle, this.type, new ConfPath(nodePath.toString()+"/type"));
        if (this.subtype != null) maapi.setElem(tHandle, this.subtype, new ConfPath(nodePath.toString()+"/subtype"));
        if (this.location != null) maapi.setElem(tHandle, this.location, new ConfPath(nodePath.toString()+"/location"));
        if (this.excludeFromLaunch != null) maapi.setElem(tHandle, this.excludeFromLaunch, new ConfPath(nodePath.toString()+"/excludeFromLaunch"));
        if (this.state != null) maapi.setElem(tHandle, this.state, new ConfPath(nodePath.toString()+"/state"));
        if (this.extensions != null) 
        	this.extensions.saveToNSOLiveStatus(maapi, tHandle, nodePath);
        if (this.interfaces != null) {
	        for (Interface i: this.interfaces) {
    	        i.saveToNSOLiveStatus(maapi, tHandle, nodePath);
        	}
        }
    }
    public static Node getInstanceOf(Map.Entry<String, JsonElement> entry, VirlComms comms) throws Exception {
        Gson gson = new Gson();
        Node node = gson.fromJson(entry.getValue(), Node.class);
        node.setName(entry.getKey());
        // Document topologyDoc = comms.requestXMLData("/export/"+entry.getKey());
        // node.setTopologyDoc(topologyDoc);
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
//     public static String getURLSuffix(ConfPath path) {
//     	return "/nodes/"+Node.getSimulationID(path);
//     }
//     public static String getJsonRoot(ConfPath path) {
//     	return Node.getSimulationID(path);
//     }
//     public Node(String name) {
//         setName(name);
//     }
//     public void setTopology(Topology topology) {
//         this.topology = topology;
//     }
//     public Topology getTopology() {
//         return topology;
//     }
//     public void setIndex(int index) {
//         this.index = index;
//     }
//     public int getIndex() {
//         return index;
//     }
//     public void setName(String name) {
//         this.name = name;
//     }
//     public String getName() {
//         return this.name;
//     }
//     public void setState(String state) {
//         this.state = state;
//     }
//     public String getState() {
//         return state;
//     }
//     public void setExtensions(Extensions extensions) {
//         this.extensions = extensions;
//     }
//     public Extensions getExtensions() {
//         return extensions;
//     }
//     public void setSubtype(String subtype) {
//         this.subtype = subtype;
//     }
//     public String getSubtype() {
//         return this.subtype;
//     }
//     public void setInterface(List<Interface> interfaces) {
//         this.interfaces = interfaces;
//     }
//     public List<Interface> getInterface() {
//         return interfaces;
//     }
//     public String toString() {
//         String str = "NODE: "+getName()+", "+getIndex()+", "+getState()+", "+getSubtype()+"\n"+extensions.toString();
//         for (Interface i: interfaces) {
//             str = str + i.toString()+"\n";
//         }
//         return str;
//     }
//     public ConfPath getConfigPath() throws Exception {
//         return new ConfPath(topology.getConfigPath()+"/node{"+getName()+"}");
//     }
//     public void toNSO(ciscovirlNed ned, int tHandle, Topology topology) throws Exception {
//         this.topology = topology;
//         if (!ned.maapi.exists(tHandle, getConfigPath())) ned.maapi.create(tHandle, getConfigPath());
//         ConfPath statePath = new ConfPath("/devices/device{"+topology.getSimulation().getDeviceName()+
//             "}/live-status/simulations/simulation{"+topology.getSimulation().getName()+
//             "}/node{"+getName()+
//             "}/state");
//         JsonObject simNodeData = ned.requestJSONData(Node.getURLSuffix(statePath), Node.getJsonRoot(statePath));
//         setState(Node.getConfValueFromPath(simNodeData, statePath).toString());
//         ned.maapi.setElem(tHandle, getState(), getConfigPath()+"/state");
//     }



//     public static boolean isPath(ConfPath path) {
//         return (path.toString().matches(".*/node\\{.*\\}/.*[^/]") || path.toString().matches(".*/simulation\\{.*\\}/nodes/node"));
//     }
//     public static String getSimulationID(ConfPath path) {
//         Pattern pattern = Pattern.compile("\\{(.*?)\\}");
//         Matcher matcher = pattern.matcher(path.toString());
//         matcher.find();
//         matcher.find();
//         return matcher.group(1);
//     }
//     public static String getURLSuffix(ConfPath path) {
//     	return "/nodes/"+Node.getSimulationID(path);
//     }
//     public static String getJsonRoot(ConfPath path) {
//     	return Node.getSimulationID(path);
//     }
//     public static String getName(ConfPath path) {
//     	if (! Node.isPath(path)) return null;
//         Pattern pattern = Pattern.compile("\\{(.*?)\\}");
//         Matcher matcher = pattern.matcher(path.toString());
//         matcher.find();
//         matcher.find();
//         matcher.find();
//         return matcher.group(1);
//     }
//     // TODO: Throw Exeption
//     public static ConfValue getConfValueFromPath(JsonObject nodeJson, ConfPath path) {
//         Gson gson = new Gson();
//         String nodeName = Node.getName(path);
// //        System.out.println("getValueFromPath (nodeName): ""+nodeName+""");
//         int index = 0;
//         for (Map.Entry<String, JsonElement> entry : nodeJson.entrySet()) {
// //	        System.out.println("getValueFromPath: Checking Entry: ""+entry.getKey()+""");
//         	if (nodeName.equals(entry.getKey().toString())) {
// 	            Node node = gson.fromJson(entry.getValue(), Node.class);
// 	            node.setName(entry.getKey());
// 		        String[] pathSplit = path.toString().split("/",0);
// //		        System.out.println("getValueFromPath: pathSplit: "+pathSplit);
// 		        String attributeName = pathSplit[pathSplit.length-1];
// //		        System.out.println("getValueFromPath: Attribute to Return: "+attributeName);
// 		        switch (attributeName) {
// 		        	case "name" :
// 		        		return new ConfBuf(node.getName());
// 		        	case "index" :
// 		        		return new ConfUInt32(node.getIndex());
// 		        	case "state" :
// 		        		return new ConfBuf(node.getState());
// 		        	case "subtype" :
// 		        		return new ConfBuf(node.getSubtype());
// 		        	default:
// 		        		return null;
// 		        }
// 	        }
// 	        index++;
//         }
//         return null;
//     }
}
