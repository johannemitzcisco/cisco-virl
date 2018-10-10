package com.example.ciscovirl;

import com.tailf.conf.ConfPath;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.tailf.maapi.Maapi;

interface StatsList {
	 static boolean isStatsListPath(ConfPath path) {return false;};
	 static String getStatsListURL(ConfPath path) {return null;};
	 static String getStatsListJsonPath(ConfPath path) {return null;};
	 abstract void saveToNSO(Maapi maappi, int tHandle, ConfPath path) throws Exception ;
} 