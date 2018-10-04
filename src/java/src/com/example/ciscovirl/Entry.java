package com.example.ciscovirl;

import com.tailf.maapi.Maapi;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import com.tailf.conf.ConfPath;
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

public class Entry {
    private String key;
    private String type;
    private String value;
    public Entry(org.w3c.dom.Element node) throws Exception {
System.out.println("NEW ENTRY: "+node.getNodeType()+": "+node.getNodeName()+": "+node.getParentNode().getNodeName());
		// VirlComms.nodeToString(node);
   	NamedNodeMap attributes = node.getAttributes();
    	this.key = attributes.getNamedItem("key").getTextContent().replace(" ", "&nbsp");
    	this.type = attributes.getNamedItem("type").getTextContent();
    	this.value = node.getTextContent();
//System.out.println(this.toString());
    }
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        ConfPath newPath = new ConfPath(path.toString()+"/extensions/entry{"+this.key+"}");
        if (!maapi.exists(tHandle, newPath)) maapi.create(tHandle, newPath);
        if (this.type != null) maapi.setElem(tHandle, this.type, new ConfPath(newPath.toString()+"/type"));
        if (this.value != null) maapi.setElem(tHandle, this.value, new ConfPath(newPath.toString()+"/value"));
    }
    public String toString() {
    	return "ENTRY: "+this.key+", "+this.type+", "+this.value;
    }
//     public void setKey(String key) {
//     	System.out.println("ENTRY (key): "+key);
//         this.key = key;
//     }
//     public String getKey() {
//         return this.key;
//     }
//     public void setType(String type) {
//     	System.out.println("ENTRY (type): "+type);
//         this.type = type;
//     }
//     public String getType() {
//         return type;
//     }
//     public void setContent(String content) {
//     	System.out.println("ENTRY (content): "+content);
//         this.content = content;
//     }
//     public String getContent() {
//         return this.content;
//     }

//     public static boolean isPath(ConfPath path) {
//         return (path.toString().matches(".*/entry") || path.toString().matches(".*/entry\\{.*\\}/.*[^/]"));
//     }
//     public static String getURLSuffix(ConfPath path) {
//     	return "/export/"+Simulation.getName(path);
//     }
//     public static String getJsonRoot(ConfPath path) {
//     	String pathStr = path.toString();
//     	pathStr = pathStr.subString(pathStr.lastIndexOf("/topology"),pathStr.length()).replace("\\{.*\\}","").replace("/entry","");
// System.out.println("PATHSTRING: "+pathStr);
//     	return pathStr;
//     }
//     public static String getName(ConfPath path) {
//         Pattern pattern = Pattern.compile("\\{(.*?)\\}");
//         Matcher matcher = pattern.matcher(path.toString());
//         String result = null;
//         while (matcher.find()) {
//         	result = matcher.group(1)
//         }
//         return result;
//     }
//     public static ConfValue getConfValueFromPath(JsonObject simJson, ConfPath path) {
//         Gson gson = new Gson();
//         String simName = Entry.getName(path);
// //        System.out.println("getValueFromPath (simName): '"+simName+"'");
//         int index = 0;
//         for (Map.Entry<String, JsonElement> entry : simJson.entrySet()) {
// //	        System.out.println("getValueFromPath: Checking Entry: '"+entry.getKey()+"'");
//         	if (simName.equals(entry.getKey().toString())) {
// 	            Simulation simulation = gson.fromJson(entry.getValue(), Simulation.class);
// 	            simulation.setName(entry.getKey());
// 		        String[] pathSplit = path.toString().split("/",0);
// //		        System.out.println("getValueFromPath: pathSplit: "+pathSplit);
// 		        String attributeName = pathSplit[pathSplit.length-1];
// //		        System.out.println("getValueFromPath: Attribute to Return: "+attributeName);
// 		        switch (attributeName) {
// 		        	case "name" :
// 		        		return new ConfBuf(simulation.getName());
// 		        	case "status" :
// 		        		return new ConfBuf(simulation.getStatus());
// 		        	case "launched" :
// 		        		return new ConfBuf(simulation.getLaunched());
// 		        	case "expires" :
// 		        		return new ConfBuf(simulation.getExpires());
// 		        	default:
// 		        		return null;
// 		        }
// 	        }
// 	        index++;
//         }
//         return null;
//     }

}
