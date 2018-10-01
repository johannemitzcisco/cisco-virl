package com.example.ciscovirl;

import java.io.IOException;
import com.tailf.conf.ConfPath;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import com.tailf.maapi.Maapi;
import org.w3c.dom.Document;


public class StatsListFactory {
	public static ArrayList<StatsList> getStatsList(VirlComms comms, ConfPath path) throws Exception {
		ArrayList<StatsList> list = new ArrayList<StatsList>();
		if (Interface.isStatsListPath(path)) {
			Simulation simulation = Simulation.getSimulationExport(new Simulation(), Simulation.getName(path), comms);
			String nodeName = Node.getName(path);
			for (Node node: simulation.topology.nodes) {
				if (node.getName().equals(nodeName)) {
					for (Interface interface: node.interfaces) {
						list.add(interface);
					}
				}
				break;
			}
		}
		else if (Connection.isStatsListPath(path)) {
			Simulation simulation = Simulation.getSimulationExport(new Simulation(), Simulation.getName(path), comms);
			for (Connection connection: simulation.topology.connections) {
				list.add(connection);
			}
		}
		else if (Node.isStatsListPath(path)) {
			JsonObject data = comms.requestJSONData(Node.getStatsListURL(path), Node.getStatsListJsonPath(path));
			for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
				Node node = Node.getInstanceOf(entry, comms);
				list.add(node);
			}
		}
		else if (Simulation.isStatsListPath(path)) {
            JsonObject data = comms.requestJSONData(Simulation.getStatsListURL(path), Simulation.getStatsListJsonPath(path));
		    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
		    	Simulation simulation = Simulation.getInstanceOf(entry, comms);
	            list.add(simulation);
	        }
        }
        return list;
	}
	public static void saveToNSO(List<StatsList> list, Maapi maappi, int tHandle, ConfPath basePath) throws Exception {
		for (StatsList item: list) {
System.out.println("saveToNSO Basepath: "+basePath+"==================================================================");
			item.saveToNSOLiveStatus(maappi, tHandle, basePath);
		}
	}
} 