package com.example.ciscovirl;

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

public class Connection implements StatsList {
	private String src;
	private String dst;

    public Connection(Element connectionNode) throws Exception {
        NamedNodeMap attributes = connectionNode.getAttributes();
        this.src = attributes.getNamedItem("src").getTextContent();
        this.dst = attributes.getNamedItem("dst").getTextContent();
    }
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        if (this.src == null) throw new Exception("Unable to Save Interface with NULL src");
        String newPath = path.toString();
        if (! newPath.matches(".*/connection")) newPath = newPath + "/connection"; 
        ConfPath connectionPath = new ConfPath(newPath+"{"+this.src+" "+this.dst+"}");
        if (!maapi.exists(tHandle, connectionPath)) maapi.create(tHandle, connectionPath);
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
    // public void setSrc(String src) {
    //     System.out.println("CONNECTION (src): "+src);
    //     this.src = src;
    // }
    // public String getSrc() {
    //     return this.src;
    // }
    // public void setDst(String dst) {
    //     System.out.println("CONNECTION (dst): "+dst);
    //     this.dst = dst;
    // }
    // public String getDst() {
    //     return this.dst;
    // }
}