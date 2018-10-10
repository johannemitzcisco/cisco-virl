package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import java.util.HashMap;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class Extensions {
	public HashMap<String, Entry> entrys;
    private String startXML = "<extensions> \n";
    private String endXml = "</extensions> \n";

    public String toXML() {
    	String xml = startXML;
    	for (Entry entry: entrys.values()) {
    		xml = xml + entry.toXML();
    	}
    	xml = xml + this.endXml;
    	return xml;
    }
	public Extensions() {
		entrys = new HashMap<String, Entry>();
	}
    public Extensions(NavuContainer extensionModel) throws NavuException {
    	this();
        for (NavuNode entry: extensionModel.list(ciscovirl._entry).children()) {
        	entrys.put(Entry.getKey(entry), new Entry(entry));
        }
    }
	public Extensions(org.w3c.dom.Element extensionsNode) throws Exception {
    	this();
        NodeList entryNodes = extensionsNode.getElementsByTagName("entry");
		for (int i = 0; i < entryNodes.getLength(); i++) {
	        org.w3c.dom.Element node = (Element) entryNodes.item(i);
	        entrys.put(Entry.getKey(node), new Entry(node));
        }
	}
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath topo_or_node_path) throws Exception {
		for (Entry entry: entrys.values()) {
	        entry.saveToNSO(maapi, tHandle, new ConfPath(topo_or_node_path.toString()+"/extensions"));
        }
    }
	public String toString() {
		String extStr = "";
		for (Entry entry: entrys.values()) {
			extStr = extStr+"\n "+entry.toString();
		}
		return "EXTENSIONS:\n  "+extStr+"\n";
	}
}