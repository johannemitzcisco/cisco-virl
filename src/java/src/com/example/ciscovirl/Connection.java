package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class Connection implements StatsList {
	private String src;
	private String dst;

    private String startXML = "<connection src=\"%s\" dst=\"%s\" /> \n";

    public String toXML() {
        String xml = String.format(this.startXML, this.src, this.dst);
        return xml;
    }
    public Connection() {}
    public Connection(NavuNode connModel) throws NavuException {
        this.src = connModel.leaf(ciscovirl._src).valueAsString();
        this.dst = connModel.leaf(ciscovirl._dst).valueAsString();
    }
    public Connection(Element connectionNode) throws Exception {
        NamedNodeMap attributes = connectionNode.getAttributes();
        this.src = attributes.getNamedItem("src").getTextContent();
        this.dst = attributes.getNamedItem("dst").getTextContent();
    }
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        if (this.src == null) throw new Exception("Unable to Save Interface with NULL src");
        if (!this.isStatsListPath(path)) path = new ConfPath(path.toString()+"/connection");
        path = new ConfPath(path+"{"+this.src+" "+this.dst+"}");
        if (!maapi.exists(tHandle, path)) maapi.create(tHandle, path);
    }
    public static boolean isStatsListPath(ConfPath path) {
        return (path.toString().matches(".*/connection"));
    }
    public static String getStatsListURL(ConfPath path) {
        return null;
    }
    public static String getStatsListJsonPath(ConfPath path) {
        return null;
    }
    public String toString() {
        return "CONNECTION: "+this.src+", "+this.dst;
    }
}