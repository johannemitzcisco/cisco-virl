package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Entry {
    private String key;
    private String type;
    public String value;

    private String startXML = "<entry key=\"%s\" type=\"%s\">";
    private String endXml = "</entry> \n";

    public String toXML() {
        String xml = String.format(this.startXML,this.key.replace("&nbsp", " "),this.type);
        if (this.value != null) xml = xml + this.value;
        xml = xml + this.endXml;
        return xml;
    }
    public Entry() {}
    public Entry(String key) {
        this.key = key;
    }
    public Entry(org.w3c.dom.Element node) throws Exception {
       	NamedNodeMap attributes = node.getAttributes();
    	this.key = attributes.getNamedItem("key").getTextContent().replace(" ", "&nbsp");
    	this.type = attributes.getNamedItem("type").getTextContent();
    	this.value = node.getTextContent();
    }
    public Entry(NavuNode entryModel) throws NavuException {
        this.key = entryModel.leaf(ciscovirl._key).valueAsString();
        this.type = entryModel.leaf(ciscovirl._type).valueAsString();
        this.value = entryModel.leaf(ciscovirl._value).valueAsString();
    }
    public static String getKey(NavuNode entry) throws NavuException {
        return entry.leaf(ciscovirl._key).valueAsString();
    }
    public static String getKey(org.w3c.dom.Element node) throws NavuException {
        NamedNodeMap attributes = node.getAttributes();
        return attributes.getNamedItem("key").getTextContent().replace(" ", "&nbsp");
    }
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath extensionspath) throws Exception {
        ConfPath path = new ConfPath(extensionspath.toString()+"/entry{"+this.key+"}");
        if (!maapi.exists(tHandle, path)) maapi.create(tHandle, path);
        if (this.type != null) maapi.setElem(tHandle, this.type, new ConfPath(path.toString()+"/type"));
        if (this.value != null) maapi.setElem(tHandle, this.value, new ConfPath(path.toString()+"/value"));
    }
    public String toString() {
    	return "ENTRY: "+this.key+", "+this.type+", "+this.value;
    }
}
