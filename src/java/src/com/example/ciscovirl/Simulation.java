package com.example.ciscovirl;

import com.tailf.conf.ConfPath;
import com.tailf.conf.ConfBuf;
import java.util.Map;
import java.util.Set;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.w3c.dom.Document;
import com.tailf.maapi.Maapi;
import com.tailf.maapi.MaapiSchemas;

import com.tailf.navu.NavuContainer;



public class Simulation implements StatsList {
    public String name;
    private String status;
    private String _statusPath ="/status";
    private String launched;
    private String _launchedPath ="/launched";
    private String expires;
    private String _expiresPath ="/expires";
    public Topology topology;
    private Document topologyDoc;
    public Simulation() {}

    public Simulation(String name) {
        this.name = name;
    }

    public boolean equals(Simulation sim) {
        return this.name.equals(sim.name);
    }

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
//System.out.println("INTANCEOF SIM: "+entry.getKey());
        Simulation.getSimulationExport(simulation, entry.getKey(), comms);
        return simulation;
    }
    public static Simulation getSimulationExport(Simulation simulation, String simulationName, VirlComms comms) throws Exception {
        simulation.setName(simulationName);
        Document topologyDoc = comms.requestXMLData("/export/"+simulationName+"?updated=true");
        simulation.setTopologyDoc(topologyDoc);
        return simulation;
    }
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath simulationpath) throws Exception {
        if (name == null) throw new Exception("Unable to Save Simulation with NULL Name");
        ConfPath simPath = new ConfPath(simulationpath.toString()+"{"+this.name+"}");
        if (!maapi.exists(tHandle, simPath)) maapi.create(tHandle, simPath);
        if (this.status != null) maapi.setElem(tHandle, this.status, new ConfPath(simPath.toString()+_statusPath));
        if (this.launched != null) maapi.setElem(tHandle, this.launched, new ConfPath(simPath.toString()+_launchedPath));
        if (this.expires != null) maapi.setElem(tHandle, this.expires, new ConfPath(simPath.toString()+_expiresPath));
        topology.saveToNSO(maapi, tHandle, new ConfPath(simPath.toString()+"/topology"));
    }
    public static String getName(ConfPath path) {
System.out.println(path.toString());
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
        String toponame = this.name;
        // if (toponame.contains("-")) {
        //     toponame = toponame.substring(0, toponame.lastIndexOf("-"));
        // }
        this.topology = new Topology(toponame, topologyDoc);
    }
    public String toString() {
        return this.name+'\n'+this.status+'\n'+this.launched+'\n'+this.expires+'\n'+this.topology.toString();
    }
}
