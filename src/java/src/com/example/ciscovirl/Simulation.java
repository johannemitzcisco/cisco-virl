package com.example.ciscovirl;

import com.tailf.conf.ConfPath;
//import com.tailf.conf.ConfValue;
import com.tailf.conf.ConfBuf;
//import com.tailf.conf.ConfUInt32;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import com.tailf.maapi.Maapi;



public class Simulation implements StatsList {
    private String name;
    private String status;
    private String _statusPath ="/status";
    private String launched;
    private String _launchedPath ="/launched";
    private String expires;
    private String _expiresPath ="/expires";
    public Topology topology;
    private Document topologyDoc;

    public Simulation() {}

    public static boolean isStatsListPath(ConfPath path) {
        return (path.toString().matches(".*/simulation"));
    }
    public static String getStatsListURL(ConfPath path) {
        return "/list";
    }
    public static String getStatsListJsonPath(ConfPath path) {
        return "simulations";
    }
    public static Simulation getInstanceOf(Map.Entry<String, JsonElement> entry, VirlComms comms) throws Exception {
        Gson gson = new Gson();
        Simulation simulation = gson.fromJson(entry.getValue(), Simulation.class);
        Simulation.getSimulationExport(simulation, entry.getKey(), comms);
        return simulation;
    }
    public static Simulation getSimulationExport(Simulation simulation, String simulationName, VirlComms comms) throws Exception {
        simulation.setName(simulationName);
        Document topologyDoc = comms.requestXMLData("/export/"+simulationName+"?updated=true");
        simulation.setTopologyDoc(topologyDoc);
        return simulation;
    }
    public void saveToNSOLiveStatus(Maapi maapi, int tHandle, ConfPath path) throws Exception {
        if (name == null) throw new Exception("Unable to Save Simulation with NULL Name");
        ConfPath newPath = new ConfPath(path.toString()+"{"+this.name+"}");
        if (!maapi.exists(tHandle, newPath)) maapi.create(tHandle, newPath);
        if (this.status != null) maapi.setElem(tHandle, this.status, new ConfPath(newPath.toString()+_statusPath));
        if (this.launched != null) maapi.setElem(tHandle, this.launched, new ConfPath(newPath.toString()+_launchedPath));
        if (this.expires != null) maapi.setElem(tHandle, this.expires, new ConfPath(newPath.toString()+_expiresPath));
        topology.saveToNSOLiveStatus(maapi, tHandle, newPath);
    }
    public static String getName(ConfPath path) {
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(path.toString());
        matcher.find();
        matcher.find();
        return matcher.group(1);
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTopologyDoc(Document topologyDoc) throws Exception {
        this.topologyDoc = topologyDoc;
        VirlComms.docToString(topologyDoc);
        String toponame = this.name;
        if (toponame.contains("-")) {
            toponame = toponame.substring(0, toponame.lastIndexOf("-"));
        }
        this.topology = new Topology(toponame, topologyDoc);
    }
    public String toString() {
        return this.name+'\n'+this.status+'\n'+this.launched+'\n'+this.expires+'\n'+this.topology.toString();
    }





    // public Simulation(String name, String deviceName) {
    //     setName(name);
    //     setDeviceName(deviceName);
    // }
//     public void setName(String name) {
//         this.name = name;
//     }
//     public String getName() {
//         return this.name;
//     }
//     public void toNSOName(ciscovirlNed ned, int tHandle) {
//         if (name != null) maapi.setElem(tHandle, Simulation.getConfValueFromPath(simData, path), path);



//     }
//     public void setDeviceName(String deviceName) {
//         this.deviceName = deviceName;
//     }
//     public String getDeviceName() {
//         return this.deviceName;
//     }
//     public void setStatus(String status) {
//         this.status = status;
//     }
//     public String getStatus() {
//         return status;
//     }
//     public void setLaunched(String launched) {
//         this.launched = launched;
//     }
//     public String getLaunched() {
//         return launched;
//     }
//     public void setExpires(String expires) {
//         this.expires = expires;
//     }
//     public String getExpires() {
//         if (this.expires == null) {
//             this.expires = "NEVER";
//         } 
//         return this.expires;
//     }
//     public void setTopology(Topology topology) {
//     	topology.setName(getName().substring(0,getName().lastIndexOf("-")));
//         this.topology = topology;
//     }
//     public Topology getTopology() {
//         return topology;
//     }
//     public ConfPath getConfigPath() throws Exception {
//     	return new ConfPath("/devices/device{"+getDeviceName()+"}/config/simulations/simulation{"+getName()+"}");
//     }

//     public void StatsListToNSO(ciscovirlNed ned, int tHandle, JsonObject data) throws Exception {
//         if (data != null) {
//             for (Map.Entry<String, JsonElement> nodeEntry : data.entrySet()) {
//                 ConfPath newPath = new ConfPath(path.toString()+"{"+nodeEntry.getKey()+"}");
//                 if (!maapi.exists(tHandle, newPath)) maapi.create(tHandle, newPath);
//             }
//         }
//         if (!ned.maapi.exists(tHandle, getConfigPath())) ned.maapi.create(tHandle, getConfigPath());
// //        topology.toNSO(ned, tHandle, this);
//     }

//     public String toString() {
//         return getName()+'\n'+getStatus()+'\n'+getLaunched()+'\n'+getExpires()+'\n'+getTopology().toString();
//     }

//     public static boolean isStatsListPath(ConfPath path) {
//         return (path.toString().matches(".*/simulation"));
//     }
//     public static boolean isPath(ConfPath path) {
//         return (path.toString().matches(".*/simulation") || path.toString().matches(".*/simulation\\{.*\\}/.*[^/]"));
//     }
//     public static String getURLSuffix(ConfPath path) {
//     	return "/list";
//     }
//     public static String getJsonRoot(ConfPath path) {
//     	return "simulations";
//     }
//     public static ConfValue getConfValueFromPath(JsonObject simJson, ConfPath path) {
//     	if (! Simulation.isPath(path)) return null;
//         Gson gson = new Gson();
//         String simName = Simulation.getName(path);
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
