package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

import com.tailf.maapi.Maapi;
import com.tailf.conf.ConfPath;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuLeaf;
import com.tailf.navu.NavuException;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;


public class Topology {
	private Topology Topology;
    public String name;
    private String xmlns;
    private String schemaVersion;
    private String xsiSchemaLocation;
    private String xmlnsXSI;
    public Extensions extensions;
    @SerializedName("node") public ArrayList<com.example.ciscovirl.Node> nodes;
    @SerializedName("connection") public ArrayList<Connection> connections;
    public ConfPath configPath;
    private String startXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n"
                            + "<topology \n"
                            + "   xmlns=\"%s\" \n"
                            + "   xmlns:xsi=\"%s\" schemaVersion=\"%s\" xsi:schemaLocation=\"%s\"> \n";
    private String endXml = "</topology>";

    public String toXML() {
        String xml = String.format(startXML,this.xmlns,this.xmlnsXSI,this.schemaVersion,this.xsiSchemaLocation);
        xml = xml + extensions.toXML();
        for (com.example.ciscovirl.Node node : nodes) {
            xml = xml + node.toXML();
        }
        for (Connection conn : connections) {
            xml = xml + conn.toXML();
        }
        xml = xml + endXml;
        return xml;
    }
    public Topology() {
        this.nodes = new ArrayList<com.example.ciscovirl.Node>();
        this.connections = new ArrayList<Connection>();
        this.extensions = new Extensions();
    }
    public Topology(String topologyName) {
        this();
        this.name = topologyName;
    }
    public Topology(NavuContainer simModel) throws NavuException {
        this();
        this.name = simModel.leaf(ciscovirl._name).valueAsString();
        this.xmlns = simModel.leaf(ciscovirl._annotation_xmlns).valueAsString();
        this.schemaVersion = simModel.leaf(ciscovirl._annotation_schemaVersion).valueAsString();
        this.xsiSchemaLocation = simModel.leaf(ciscovirl._annotation_xsiSchemaLocation).valueAsString();
        this.xmlnsXSI = simModel.leaf(ciscovirl._annotation_xmlnsXSI).valueAsString();
        this.extensions = new Extensions(simModel.container(ciscovirl._extensions));
        for (NavuNode node : simModel.list(ciscovirl._node).children()) {
            this.nodes.add(new com.example.ciscovirl.Node(node));
        }
        for (NavuNode conn: simModel.list(ciscovirl._connection).children()) {
            this.connections.add(new Connection(conn));
        }
    }
    public Topology(String name, Document topologyDoc) throws Exception {
        this();
        this.name = name;
        NamedNodeMap attributes = topologyDoc.getDocumentElement().getAttributes();

        this.xmlns = attributes.getNamedItem("xmlns").getTextContent();
        this.schemaVersion = attributes.getNamedItem("schemaVersion").getTextContent();
        this.xmlnsXSI = attributes.getNamedItem("xmlns:xsi").getTextContent();
        this.xsiSchemaLocation = attributes.getNamedItem("xsi:schemaLocation").getTextContent();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile("/topology/extensions");
        Element extensionNode = (Element) expr.evaluate(topologyDoc, XPathConstants.NODE);
        this.extensions = new Extensions(extensionNode);

        NodeList topoNodes = topologyDoc.getElementsByTagName("node");
        for (int i = 0; i < topoNodes.getLength(); i++) {
            Element node = (Element) topoNodes.item(i);
            nodes.add(new com.example.ciscovirl.Node(node, i+1));
        }

        NodeList connNodes = topologyDoc.getElementsByTagName("connection");
        for (int i = 0; i < connNodes.getLength(); i++) {
            Element node = (Element) connNodes.item(i);
            connections.add(new Connection(node));
        }
    }
    public void saveToNSO(Maapi maapi, int tHandle, ConfPath topologypath) throws Exception {
        if (this.name == null) throw new Exception("Unable to Save Topology with NULL Name");
        if (!maapi.exists(tHandle, topologypath)) maapi.create(tHandle, topologypath);
//System.out.println("TOPO NAME: "+this.name);
        if (this.name != null) maapi.setElem(tHandle, this.name, new ConfPath(topologypath.toString()+"/name"));
        if (!topologypath.toString().contains("live-status")) {
            if (this.xmlns != null) maapi.setElem(tHandle, this.xmlns, new ConfPath(topologypath.toString()+"/annotation-xmlns"));
            if (this.schemaVersion != null) maapi.setElem(tHandle, this.schemaVersion, new ConfPath(topologypath.toString()+"/annotation-schemaVersion"));
            if (this.xsiSchemaLocation != null) maapi.setElem(tHandle, this.xsiSchemaLocation, new ConfPath(topologypath.toString()+"/annotation-xsiSchemaLocation"));
            if (this.xmlnsXSI != null) maapi.setElem(tHandle, this.xmlnsXSI
                , new ConfPath(topologypath.toString()+"/annotation-xmlnsXSI"));
            this.extensions.saveToNSO(maapi, tHandle, new ConfPath(topologypath.toString()));
            if (nodes != null) {
                for (com.example.ciscovirl.Node n: this.nodes) {
                    n.saveToNSO(maapi, tHandle, topologypath);
                }
            }
            if (connections != null) {
                for (Connection c: this.connections) {
                    c.saveToNSO(maapi, tHandle, topologypath);
                }
            }
        }
    }
    public boolean equals(Topology topology) {
        return this.name.equals(topology.name);
    }
    public void saveToNSO(Maapi maapi, int tHandle, String deviceName) throws Exception {
        configPath = new ConfPath("/devices/device{"+deviceName+"}/config/simulation{"+this.name+"}");
        this.saveToNSO(maapi, tHandle, configPath);
    }
}
