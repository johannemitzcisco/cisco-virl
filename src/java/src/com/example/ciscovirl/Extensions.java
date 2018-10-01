package com.example.ciscovirl;

import java.util.ArrayList;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;

import org.w3c.dom.NodeList;
//import org.w3c.dom.Node;
import org.w3c.dom.Element;

// import javax.xml.xpath.XPathFactory;
// import javax.xml.xpath.XPath;
// import javax.xml.xpath.XPathExpression;
// import javax.xml.xpath.XPathConstants;
// import javax.xml.xpath.XPathExpressionException;
// import org.w3c.dom.Document;


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

public class Extensions {
	private ArrayList<Entry> entrys;

	public Extensions(org.w3c.dom.Element extensionsNode) throws Exception {
		entrys = new ArrayList<Entry>();
		VirlComms.nodeToString(extensionsNode);
        NodeList entryNodes = extensionsNode.getElementsByTagName("entry");
		for (int i = 0; i < entryNodes.getLength(); i++) {
	        org.w3c.dom.Element node = (Element) entryNodes.item(i);
	        entrys.add(new Entry(node));
        }
	}
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
		for (Entry entry: entrys) {
	        entry.saveToNSOLiveStatus(maapi, tHandle, path);
        }
    }
	public String toString() {
		String extStr = "";
		for (Entry entry: entrys) {
			extStr = extStr+"\n "+entry.toString();
		}
		return "EXTENSIONS:\n  "+extStr+"\n";
	}

	// public void setEntry(List<Entry> entry) {
	// 	this.entry = entry;
	// }
	// public List<Entry> getEntry() {
 //    	System.out.println("EXTENSIONS (entry): "+entry.toString());
	// 	return entry;
	// }
}