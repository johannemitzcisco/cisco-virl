package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import java.util.ArrayList;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class Interface implements StatsList {
    private Integer connectionIndex;
	public Integer id;
	private String name;
	private String ipv4;
	private String netPrefixLenV4;

    private String startXML = "<interface id=\"%s\" name=\"%s\" ipv4=\"%s\" netPrefixLenV4=\"%s\" /> \n";
    private String startXMLempty = "<interface id=\"%s\" name=\"%s\" /> \n";

    public String toXML() {
        String xml = null;
        if (ipv4 == null) xml = String.format(this.startXMLempty, this.id, this.name);
        else xml = String.format(this.startXML, this.id, this.name, this.ipv4, this.netPrefixLenV4);
        return xml;
    }
    public Interface() {}
    public Interface(Element interfaceNode, int index) throws Exception {
        NamedNodeMap attributes = interfaceNode.getAttributes();
        this.connectionIndex = new Integer(index);
        this.id = new Integer(attributes.getNamedItem("id").getTextContent());
        this.name = attributes.getNamedItem("name").getTextContent();
        if (attributes.getNamedItem("ipv4") != null) this.ipv4 = attributes.getNamedItem("ipv4").getTextContent();
        if (attributes.getNamedItem("netPrefixLenV4") != null) this.netPrefixLenV4 = attributes.getNamedItem("netPrefixLenV4").getTextContent();
    }
    public Interface(NavuNode interfaceModel) throws NavuException {
        this.connectionIndex = new Integer(interfaceModel.leaf(ciscovirl._connection_index).valueAsString());
        this.id = new Integer(interfaceModel.leaf(ciscovirl._id).valueAsString());
        this.name = interfaceModel.leaf(ciscovirl._name).valueAsString();
        this.ipv4 = interfaceModel.leaf(ciscovirl._ipv4).valueAsString();
        this.netPrefixLenV4 = interfaceModel.leaf(ciscovirl._netPrefixLenV4).valueAsString();
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
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath nodepath) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Node Interface with NULL Name");
        // String newPath = path.toString();
        if (!this.isStatsListPath(nodepath)) nodepath = new ConfPath(nodepath.toString()+"/interface");
        ConfPath path = new ConfPath(nodepath+"{"+this.id+"}");
        if (!maapi.exists(tHandle, path)) maapi.create(tHandle, path);
        if (this.connectionIndex != null) maapi.setElem(tHandle, this.connectionIndex.toString(), new ConfPath(path.toString()+"/connection-index"));
        if (this.name != null) maapi.setElem(tHandle, this.name, new ConfPath(path.toString()+"/name"));
        if (this.ipv4 != null) maapi.setElem(tHandle, this.ipv4, new ConfPath(path.toString()+"/ipv4"));
        if (this.netPrefixLenV4 != null) maapi.setElem(tHandle, this.netPrefixLenV4, new ConfPath(path.toString()+"/netPrefixLenV4"));
    }
    public String toString() {
        return "INTERFACE: "+this.id+", "+this.name+", "+this.ipv4+", "+this.netPrefixLenV4;
    }
}