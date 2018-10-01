package com.example.ciscovirl;

import java.util.ArrayList;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

// import com.tailf.conf.ConfPath;
// import com.tailf.conf.ConfValue;
// import com.tailf.conf.ConfBuf;
// import com.tailf.conf.ConfUInt32;
// import java.util.Map;
// import java.util.Set;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import com.google.gson.Gson;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;

public class Interface {
    private Integer connectionIndex;
	private Integer id;
	private String name;
	private String ipv4;
	private String netPrefixLenV4;

    public Interface(Element interfaceNode, int index) throws Exception {
//        VirlComms.nodeToString(topoDeviceNode);
        NamedNodeMap attributes = interfaceNode.getAttributes();
        this.connectionIndex = new Integer(index);
        this.id = new Integer(attributes.getNamedItem("id").getTextContent());
        this.name = attributes.getNamedItem("name").getTextContent();
        if (attributes.getNamedItem("ipv4") != null ) this.ipv4 = attributes.getNamedItem("ipv4").getTextContent();
        if (attributes.getNamedItem("netPrefixLenV4") != null ) this.netPrefixLenV4 = attributes.getNamedItem("netPrefixLenV4").getTextContent();
    }
    public static boolean isStatsListPath(ConfPath path) {
        return (path.toString().matches(".*/interface"));
    }
    public static String getStatsListURL(ConfPath path) {
        return null;
    }
    public static String getStatsListJsonPath(ConfPath path) {
        return null;
    }
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Node Interface with NULL Name");
        String newPath = path.toString();
        if (! newPath.matches(".*/interface")) newPath = newPath + "/interface";
        ConfPath interfacePath = new ConfPath(newPath+"{"+this.id+"}");
        if (!maapi.exists(tHandle, interfacePath)) maapi.create(tHandle, interfacePath);
        if (this.connectionIndex != null) maapi.setElem(tHandle, this.connectionIndex.toString(), new ConfPath(interfacePath.toString()+"/connection-index"));
        if (this.name != null) maapi.setElem(tHandle, this.name, new ConfPath(interfacePath.toString()+"/name"));
        if (this.ipv4 != null) maapi.setElem(tHandle, this.ipv4, new ConfPath(interfacePath.toString()+"/ipv4"));
        if (this.netPrefixLenV4 != null) maapi.setElem(tHandle, this.netPrefixLenV4, new ConfPath(interfacePath.toString()+"/netPrefixLenV4"));
    }
    public String toString() {
        return "INTERFACE: "+this.id+", "+this.name+", "+this.ipv4+", "+this.netPrefixLenV4;
    }
    // public static Node getInstanceOf(Map.Entry<String, JsonElement> entry, VirlComms comms) throws Exception {
    //     Gson gson = new Gson();
    //     Node node = gson.fromJson(entry.getValue(), Node.class);
    //     node.setName(entry.getKey());
    //     // Document topologyDoc = comms.requestXMLData("/export/"+entry.getKey());
    //     // node.setTopologyDoc(topologyDoc);
    //     return node;
    // }

    // public void setId(String id) {
    //     System.out.println("INTERFACE (id): "+id);
    //     this.id = id;
    // }
    // public String getId() {
    //     return this.id;
    // }
    // public void setName(String name) {
    //     System.out.println("INTERFACE (name): "+name);
    //     this.name = name;
    // }
    // public String getName() {
    //     return this.name;
    // }
    // public void setIpv4(String ipv4) {
    //     System.out.println("INTERFACE (ipv4): "+ipv4);
    //     this.ipv4 = ipv4;
    // }
    // public String getIpv4() {
    //     return ipv4;
    // }
    // public void setNetPrefixLenV4(String netPrefixLenV4) {
    //     System.out.println("INTERFACE (netPrefixLenV4): "+netPrefixLenV4);
    //     this.netPrefixLenV4 = netPrefixLenV4;
    // }
    // public String getNetPrefixLenV4() {
    //     return this.netPrefixLenV4;
    // }
}