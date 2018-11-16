package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.tailf.conf.Conf;
import com.tailf.conf.ConfInt32;
import com.tailf.conf.ConfInt8;
import com.tailf.conf.ConfKey;
import com.tailf.conf.ConfObject;
import com.tailf.conf.ConfValue;
import com.tailf.conf.ConfPath;
import com.tailf.conf.ConfNamespace;
import com.tailf.conf.ConfTag;
import com.tailf.conf.ConfUInt32;
import com.tailf.conf.ConfXMLParam;
import com.tailf.conf.ConfXMLParamValue;
import com.tailf.conf.ConfXMLParamStart;
import com.tailf.conf.ConfXMLParamStop;
import com.tailf.maapi.Maapi;
import com.tailf.maapi.MaapiException;
import com.tailf.maapi.MaapiSchemas;
import com.tailf.maapi.MaapiUserSessionFlag;
import com.tailf.ncs.ResourceManager;
import com.tailf.ncs.annotations.Scope;
import com.tailf.ncs.ns.Ncs;
import com.tailf.ned.NedCapability;
import com.tailf.ned.NedCmd;
import com.tailf.ned.NedEditOp;
import com.tailf.ned.NedErrorCode;
import com.tailf.ned.NedException;
import com.tailf.ned.NedGenericBase;
import com.tailf.ned.NedMux;
import com.tailf.ned.NedTTL;
import com.tailf.ned.NedWorker;
import com.tailf.ned.NedWorker.TransactionIdMode;
import com.tailf.navu.NavuContext;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuNode;
import com.tailf.navu.NavuList;
import com.tailf.navu.NavuListEntry;
import com.tailf.navu.NavuException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Set;
import java.util.Map;
import com.tailf.conf.ConfBuf;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
*/

public class ciscovirlNed extends NedGenericBase  {
    private String      deviceName;
//    private Device      device;
    private InetAddress ip;
    private int         port;
    private String      luser;
    private boolean     trace;
    private int         connectTimeout; // msec
    private int         readTimeout;    // msec
    private int         writeTimeout;   // msec


    private static Logger LOGGER = Logger.getLogger(ciscovirlNed.class);
    public  Maapi                    maapi = null;
    private boolean                  wantReverse=true;

    private static MaapiSchemas schemas;
    private static MaapiSchemas.CSNode cfgCs;

    private VirlComms comms;
    private HashMap<String, SimState> simStates;
//    private Tracker tracker;

    private class NodeState {
        public String name;
        public NodeState(String name, String action) {
            this.name = name;
            this.action = action;
        }
        public String action;
        public void setStart() {
            if (action == null) {
                this.action = "START";
            }
        }
        public void setStop() {
            if (action == null) {
                this.action = "STOP";
            }
        }
    }
    private class SimState extends Topology {
        HashMap<String, NodeState> actionNodes = new HashMap<>();
        public SimState(NavuContainer simModel) throws NavuException {
            super(simModel);
            this.action = "NONE";
        }
        public SimState(String simName) {
            super(simName);
            this.action = "NONE";
        }
        public String action;
        public void setStart() {
            if (this.action == "NONE") {
                this.action = "START";
            }
        }
        public void setStop() {
            if (this.action == "NONE") {
                this.action = "STOP";
            }
        }
        public void setRestart() {
            if (this.action == "NONE" || this.action == "START") {
                this.action = "RESTART";
            }
        }
        public boolean equals(SimState simState) {
            return this.name.equals(simState.name);
        }
    }
/*    @ClientEndpoint
    private class Tracker {
        public String url;
        public String token;
        Session userSession = null;
        public Tracker() throws Exception {
            JsonObject trackerinfo = comms.requestJSONData(VirlComms.RequestType.POST, "/tracking", "tracking", null);
            url = trackerinfo.get("url").getAsString();
            token = trackerinfo.get("token").getAsString();
            try {
                LOGGER.info("Creating WebSocket container");
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                LOGGER.info("Connecting to "+url+" with token "+token);
                container.connectToServer(this, new URI(url));
                LOGGER.info("Connected");
            } catch (Exception e) {
                LOGGER.info("Exception "+e);
                throw e;
            }
        }
        @OnOpen
        public void onOpen(Session userSession) {
            LOGGER.info("opening websocket");
            this.userSession = userSession;
        }
        @OnClose
        public void onClose(Session userSession, CloseReason reason) {
            LOGGER.info("closing websocket");
            this.userSession = null;
        }
        @OnMessage
        public void onMessage(String message) {
            LOGGER.info("WebSocket Message: "+message);
        }
    }
*/
    public void finalize() {
        LOGGER.info("FINALIZING ciscovirlNed ==");
    }

    public ciscovirlNed(){
        this(true);
    }

    public ciscovirlNed(boolean wantReverse){
        LOGGER.info("STARTING ciscovirlNed ==");
        this.wantReverse = wantReverse;
    }

    public ciscovirlNed(String deviceName,
                InetAddress ip,
                int port,
                String luser,
                boolean trace,
                int connectTimeout,
                int readTimeout,
                int writeTimeout,
                NedMux mux,
                NedWorker worker,
                boolean wantReverse)  {

        try {
            this.deviceName = deviceName;
            this.ip = ip;
            this.port = port;
            this.luser = luser;
            this.trace = trace;
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.writeTimeout = writeTimeout;
            this.wantReverse = wantReverse;

            this.comms = new VirlComms(ip, port, worker.getRemoteUser(), worker.getPassword());
            LOGGER.info("CONNECTING <==");

            NedCapability capas[] = new NedCapability[1];
            capas[0] = new NedCapability("http://com/example/ciscovirl", "cisco-virl");

            NedCapability statscapas[] = new NedCapability[1];
            statscapas[0] = new NedCapability("http://com/example/ciscovirl-stats",
                                              "cisco-virl-stats");

            this.schemas = Maapi.getSchemas();
            this.cfgCs =
                this.schemas.findCSNode(Ncs.uri,"/devices/device/config");

            setConnectionData(capas,
                              statscapas,
                              this.wantReverse,  // want reverse-diff
                              TransactionIdMode.NONE);

//            this.tracker = new Tracker();

            LOGGER.info("CONNECTING ==> OK");
        }
        catch (Exception e) {
            worker.error(NedErrorCode.NED_INTERNAL_ERROR.getValue(), e.getMessage()," Cntc error");
        }
    }

    public boolean isConnection(String deviceId,
                                InetAddress ip,
                                int port,
                                String luser,
                                boolean trace,
                                int connectTimeout, // msecs
                                int readTimeout,    // msecs
                                int writeTimeout) { // msecs
        return ((this.deviceName.equals(deviceName)) &&
                (this.ip.equals(ip)) &&
                (this.port == port) &&
                (this.luser.equals(luser)) &&
                (this.trace == trace) &&
                (this.connectTimeout == connectTimeout) &&
                (this.readTimeout == readTimeout) &&
                (this.writeTimeout == writeTimeout));
    }

/*
 * If the device has commands, i,e reboot etc that are - just - commands
 * that do not manipulate the configuration, we model those commands
 * in the YANG model, and get invoked here. The task of this code is to
 * look at the input params, invoke the cmd on the device and return
 * data - according to the YANG model
 *
 */
    public void command(NedWorker worker, String cmdname, ConfXMLParam[] p)
        throws NedException, IOException {

        try {
            LOGGER.info("RUNNING COMMAND ==> " + cmdname);

            ConfNamespace ns = new ciscovirl();
            ConfXMLParam[] result = null;
            if (cmdname.equals("Start-Simulation")) {
                String topologyName = null;
                String topologyXML = null;
                for (ConfXMLParam param : p) {
                    if (param.getTag().equals("topology-name")) {
                        topologyName = param.getValue().toString();
                    }
                    else if (param.getTag().equals("topology-xml")) {
                        topologyXML = param.getValue().toString();
                    }
                   // LOGGER.info("INPUT PARAM NAME: "+param.getTag());
                   // LOGGER.info("INPUT PARAM VALUE:\n"+param.getValue());
                }
                String reply = comms.execRequest(VirlComms.RequestType.POST, "/launch?session="+topologyName, topologyXML);
                result = new ConfXMLParam[] {
                    new ConfXMLParamValue(ns, "result", new ConfBuf("SUCCESS"))
                    };
            } else if (cmdname.equals("Stop-Simulation")) {
                String simulationName = null;
                for (ConfXMLParam param : p) {
                    if (param.getTag().equals("simulation-name")) {
                        simulationName = param.getValue().toString();
                    }
                    if (simulationName == null) throw new Exception ("Simulation Name cannot be null");
                   // LOGGER.info("INPUT PARAM NAME: "+param.getTag());
                   // LOGGER.info("INPUT PARAM VALUE:\n"+param.getValue());
                }
                String reply = comms.execRequest(VirlComms.RequestType.GET,"/stop/"+simulationName, null);
                result = new ConfXMLParam[] {
                    new ConfXMLParamValue(ns, "result", new ConfBuf("SUCCESS"))
                    };
            }
            LOGGER.info("RUNNING COMMAND <== " + cmdname + " COMPLETED");
            // if (result == null) worker.commandResponse();
            worker.commandResponse(result);
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
        }
    }

/**
 * Establish a new connection to a device and send response to
 * NCS with information about the device.
 *
 * @param deviceId name of device
 * @param ip address to connect to device
 * @param port port to connect to
 * @param luser name of local NCS user initiating this connection
 * @param trace indicates if trace messages should be generated or not
 * @param connectTimeout in milliseconds
 * @param readTimeout in milliseconds
 * @param writeTimeout in milliseconds
 * @return the connection instance
 **/
    public NedGenericBase newConnection(String deviceId,
                                        InetAddress ip,
                                        int port,
                                        String luser,
                                        boolean trace,
                                        int connectTimeout, // msecs
                                        int readTimeout,    // msecs
                                        int writeTimeout,   // msecs
                                        NedMux mux,
                                        NedWorker worker ) {
        LOGGER.info("newConnection() <==");
        ciscovirlNed ned = null;

        ned = new ciscovirlNed(deviceId, ip, port, luser, trace,
                       connectTimeout, readTimeout, writeTimeout,
                       mux, worker,
                       wantReverse );
        LOGGER.info("NED invoking newConnection() ==> OK");
        return ned;
    }

    private void connect() {
        LOGGER.info("CONNECT <==");
        LOGGER.info("CONNECT ==> OK");
    }

    public boolean isAlive() {
        LOGGER.info("IS ALIVE <==");
        LOGGER.info("IS ALIVE ==> OK");
        return true;
    }

    public void reconnect(NedWorker worker) {
        LOGGER.info("RECONNECT <==");
        LOGGER.info("RECONNECT ==> OK");
    }

    public String device_id() {
        LOGGER.info("DEVICE-ID <==");
        LOGGER.info("DEVICE-ID ==> OK");
        return deviceName;
    }

    // should return "cli" or "generic"
    public String type() {
        return "generic";
    }
    // Which YANG modules are covered by the class
    public String [] modules() {
        LOGGER.info("modules");
        return new String[] { "cisco-virl", "cisco-virl-stats" };
    }

    // Which identity is implemented by the class
    public String identity() {
        return "cisco-virl:cisco-virl-id";
    }

    /*
     * The generic show command is to
     * grab all configuration from the device and
     * populate the transaction handle  passed to us.
     **/

    public void show(NedWorker worker, int tHandle)
        throws NedException, IOException {
        try {
            LOGGER.info("SHOW <==");
            LOGGER.info("THANDLE:" + tHandle);
            if (maapi == null)
                maapi = ResourceManager.getMaapiResource(this, Scope.INSTANCE);
            LOGGER.info( this.toString()  + " Attaching to Maapi " + maapi);
            maapi.attach(tHandle, 0);
            JsonObject data = comms.requestJSONData(Simulation.getStatsListURL(null), Simulation.getStatsListJsonPath(null));
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                Simulation simulation = Simulation.getInstanceOf(entry, comms);
                LOGGER.info("ADDING SIM: "+simulation.name);
                Topology topology = simulation.topology;
                topology.saveToNSO(maapi, tHandle, this.deviceName);
                // To get Node Status information
                ConfPath simPath = new ConfPath("/ncs:devices/device{"+deviceName+"}/live-status/cisco-virlstats:simulations/simulation{"+simulation.name+"}");
                JsonObject nodedata = comms.requestJSONData(Node.getStatsListURL(simPath), Node.getStatsListJsonPath(simPath));
                for (Map.Entry<String, JsonElement> nodeentry : nodedata.entrySet()) {
                    Node.getInstanceOf(nodeentry).saveToNSO(maapi, tHandle, topology.configPath);
                }
            }
            maapi.detach(tHandle);
            worker.showGenericResponse();
            LOGGER.info("SHOW ==> OK");
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
        }
    }

    /*
     *   This method must at-least fill in all the keys of the list it
     *   is passed, it may do more though. In this example  code we
     *   choose to not let the code in showStatsList() fill in the full
     *   entries, thus forcing an invocation of showStats()
     */
    public void showStatsList(NedWorker worker, int tHandle, ConfPath path)
        throws NedException, IOException {
        try {
            LOGGER.info("SHOW STATS LIST <==");
            if (maapi == null)
                maapi = ResourceManager.getMaapiResource(this, Scope.INSTANCE);

            LOGGER.info( this.toString()  + " Attaching2 to Maapi " + maapi +
                         " for " + path.toString());
            maapi.attach(tHandle, 0);
            try {
                List<StatsList> statlist = StatsListFactory.getStatsList(comms, path);
                StatsListFactory.saveToNSO(statlist, maapi, tHandle, path);
                LOGGER.info("SHOW STATS LIST ===> OK");
            } catch (Exception e) {
                worker.error(NedEditOp.VALUE_SET, "Unable to get stats list data");
                throw e;
            }
            maapi.detach(tHandle);
            worker.showStatsListResponse(10, null);
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
        }
    }

    /*
     * This method must at-least populate the path it is given,
     * it may do more though
     */

    public void showStats(NedWorker worker, int tHandle, ConfPath path)
        throws NedException, IOException {
        try {
            LOGGER.info("SHOW STATS <==");
            if (maapi == null)
                maapi = ResourceManager.getMaapiResource(this, Scope.INSTANCE);
            LOGGER.info( this.toString()  + " Attaching to Maapi " + maapi +
                         " for " + path);
            worker.showStatsResponse(null);
            LOGGER.info("SHOW STATS ===> OK");
        }
        catch (Exception e) {
            worker.error(NedEditOp.VALUE_SET, "Unable to get Running Simulation Detail");
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
        }
    }

    /**
     * Is invoked by NCS to take the configuration to a new state.
     * We retrive a rev which is a transaction handle to the
     * comming write operation then we write operations towards the device.
     * If all succeded we transition to commit phase or if
     * prepare fails we transition to abort phase.
     *
     * @param w - is the processing worker. It should be used for sending
     * responses to NCS.
     * @param data is the commands for transforming the configuration to
     * a new state.
     */

    public void prepare(NedWorker worker, NedEditOp[] ops)
        throws NedException, IOException {
        LOGGER.info("PREPARE <==");

        try {
            edit(worker, ops);
            worker.prepareResponse();
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR,
                                   "Internal error when calling "+
                                   "prepareDryResponse: "+
                                   e.getMessage(),e);
        }
    }

    /**
     * Is invoked by NCS to ask the NED what actions it would take towards
     * the device if it would do a prepare.
     *
     * The NED can send the preformatted output back to NCS through the
     * call to  {@link com.tailf.ned.NedWorker#prepareDryResponse(String)
     * prepareDryResponse()}
     *
     * The Ned should invoke the method
     * {@link com.tailf.ned.NedWorker#prepareDryResponse(String)
     *   prepareDryResponse()} in <code>NedWorker w</code>
     * when the operation is completed.
     *
     * If the functionality is not supported or an error is detected
     * answer this through a call to
     * {@link com.tailf.ned.NedWorker#error(int,String,String) error()}
     * in <code>NedWorker w</code>.
     *
     * @param w
     *    The NedWorker instance currently responsible for driving the
     *    communication
     *    between NCS and the device. This NedWorker instance should be
     *    used when communicating with the NCS, ie for sending responses,
     *    errors, and trace messages. It is also implements the
     *    {@link NedTracer}
     *    API and can be used in, for example, the {@link SSHSession}
     *    as a tracer.
     *
     * @param ops
     *    Edit operations representing the changes to the configuration.
     */
    public void prepareDry(NedWorker worker, NedEditOp[] ops)
        throws NedException {

        LOGGER.info("PREPARE DRY <==");
        try {
            StringBuilder dryRun = new StringBuilder();
            edit(worker, ops, dryRun);
            worker.prepareDryResponse(dryRun.toString());
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR,
                                   "Internal error when calling "+
                                   "prepareDryResponse: "+
                                   e.getMessage(),e);
        }
        LOGGER.info("PREPARE DRY ==> OK");
    }

    public void commit(NedWorker worker, int timeout)
        throws NedException {
        LOGGER.info("COMMIT <==" + this.simStates.size());
        try {
            for (SimState simstate : this.simStates.values()) {
                LOGGER.info("COMMIT: "+simstate.name+ " " + simstate.action);
                switch (simstate.action) {
                case "NONE":
                    String startnodes = null;
                    String stopnodes = null;
                    for (NodeState nodestate : simstate.actionNodes.values()) {
                        if (nodestate.action == "START") {
                            if (startnodes == null) startnodes = nodestate.name;
                            else startnodes = ","+nodestate.name;
                        }
                        if (nodestate.action == "STOP") {
                            if (stopnodes == null) stopnodes = nodestate.name;
                            else stopnodes = ","+nodestate.name;
                        }
                    }
                    if (startnodes != null) {
                        comms.execRequest(VirlComms.RequestType.PUT, 
                            "/update/"+simstate.name+"/start?nodes="+startnodes, 
                            null, false);
                    }
                    if (stopnodes != null) {
                        comms.execRequest(VirlComms.RequestType.PUT, 
                            "/update/"+simstate.name+"/stop?nodes="+stopnodes, 
                            null, false);
                    }
                    break;
                case "START":
                    comms.execRequest(VirlComms.RequestType.POST, 
                            "/launch?session="+simstate.name, simstate.toXML(), false);
                    break;
                case "STOP":
                    comms.execRequest(VirlComms.RequestType.GET, 
                            "/stop/"+simstate.name, 
                            null, false);
                    break;
                case "RESTART":
                    comms.execRequest(VirlComms.RequestType.GET, 
                            "/stop/"+simstate.name+"?wait=30", 
                            null, false);
                    comms.execRequest(VirlComms.RequestType.POST, 
                            "/launch?session="+simstate.name, simstate.toXML(), false);
                    break;
                }
            }
            LOGGER.info("COMMIT ==>");
            worker.commitResponse();
        } catch (IOException e) {
            worker.error(NedEditOp.MODIFIED, NedErrorCode.NED_EXTERNAL_ERROR, e.getMessage());
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, 
                e.getMessage(), e);
        }
    }

    /**
     * Is invoked by NCS to abort the configuration to a previous state.
     *
     * @param w is the processing worker. It should be used for sending
     * responses to NCS. * @param data is the commands for taking the config
     * back to the previous
     * state. */

    public void abort(NedWorker worker , NedEditOp[] ops)
        throws NedException, IOException {
        LOGGER.info("ABORT <==");
        //edit(ops);
        worker.abortResponse();
        LOGGER.info("ABORT ==> OK");
    }


    public void revert(NedWorker worker , NedEditOp[] ops)
        throws NedException, IOException {
        LOGGER.info("REVERT <==");
        //edit(ops);
        worker.revertResponse();
        LOGGER.info("REVERT ==> OK");
    }


    public void persist(NedWorker worker)
        throws NedException, IOException {
        LOGGER.info("PERSIST <==");
        worker.persistResponse();

    }

    public void close(NedWorker worker)
        throws NedException, IOException {
        close();
    }

    public void close() {
        LOGGER.info("CLOSE <==");
        try {
            if (maapi != null)
                ResourceManager.unregisterResources(this);
        }
        catch (Exception e) {
            ;
        }
        LOGGER.info("CLOSE ==> OK");
    }

    public void edit(NedWorker worker, NedEditOp[] ops)
        throws NedException, Exception {
        edit(worker, ops, null);
    }

    public void edit(NedWorker worker, NedEditOp[] ops, StringBuilder dryRun)
        throws NedException, Exception {

        if (maapi == null)
            maapi = ResourceManager.getMaapiResource(this, Scope.INSTANCE);
        maapi.startUserSession("system",
                                InetAddress.getByName("localhost"),
                                "system",
                                new String[] { "admin" },
                                MaapiUserSessionFlag.PROTO_TCP);
        maapi.attach(worker.getToTransactionId(), new Ncs().hash(), worker.getUsid());
        NavuContainer root = new NavuContainer(maapi, worker.getToTransactionId(), new Ncs().hash());
        this.simStates = new HashMap<String, SimState>();
        try {
            for (NedEditOp op: ops) {
                LOGGER.debug("OPERATION: " + op);
                switch (op.getOperation()) {
                case NedEditOp.CREATED:
                    create(root, op, dryRun);
                    break;
                case NedEditOp.DELETED:
                    delete(root, op, dryRun);
                    break;
                case NedEditOp.MODIFIED:
                    modified(root, op, dryRun);
                    break;
                case NedEditOp.MOVED:
                    break;
                case NedEditOp.VALUE_SET:
                    valueSet(root, op, dryRun);
                    break;
                case NedEditOp.DEFAULT_SET:
                    defaultSet(op, dryRun);
                    break;
                default:
                    LOGGER.debug("OPERATION NOT SUPPORTED");
                }
            }
            if (dryRun != null) {
                for (SimState simstate : this.simStates.values()) {
                    LOGGER.info("DRYRUN: "+simstate.name+ " " + simstate.action);
                    switch (simstate.action) {
                    case "NONE":
                        String startnodes = null;
                        String stopnodes = null;
                        for (NodeState nodestate : simstate.actionNodes.values()) {
                            if (nodestate.action == "START") {
                                if (startnodes == null) startnodes = nodestate.name;
                                else startnodes = ","+nodestate.name;
                            }
                            if (nodestate.action == "STOP") {
                                if (stopnodes == null) stopnodes = nodestate.name;
                                else stopnodes = ","+nodestate.name;
                            }
                        }
                        if (startnodes != null) {
                            dryRun.append(comms.execRequest(VirlComms.RequestType.PUT, 
                                "/update/"+simstate.name+"/start?nodes="+startnodes, 
                                null, true)+"\n");
                        }
                        if (stopnodes != null) {
                            dryRun.append(comms.execRequest(VirlComms.RequestType.PUT, 
                                "/update/"+simstate.name+"/stop?nodes="+stopnodes, 
                                null, true)+"\n");
                        }
                        break;
                    case "START":
                        dryRun.append(comms.execRequest(VirlComms.RequestType.POST, 
                                "/launch?session="+simstate.name, simstate.toXML(), true)+"\n \n");
                        break;
                    case "STOP":
                        dryRun.append(comms.execRequest(VirlComms.RequestType.GET, 
                                "/stop/"+simstate.name, 
                                null, true)+"\n \n");
                        break;
                    case "RESTART":
                        dryRun.append(comms.execRequest(VirlComms.RequestType.GET, 
                                "/stop/"+simstate.name+"?wait=30", 
                                null, true)+"\n \n");
                        dryRun.append(comms.execRequest(VirlComms.RequestType.POST, 
                                "/launch?session="+simstate.name, simstate.toXML(), true)+"\n \n");
                        break;
                    }
                } 
            }
        maapi.detach(worker.getToTransactionId());

        } catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, 
                e.getMessage(), e);
        }
    }

    private ArrayList<ConfObject> getkeypath(NedEditOp op) throws NedException {
        try {
            ConfPath cp = op.getPath();
            ArrayList<ConfObject> keypath = new ArrayList<ConfObject> (Arrays.asList(cp.getKP()));
            for (ConfObject k : keypath) {
            }
            return keypath;
        } catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR,
                                   "Internal error, cannot get key path: "
                                   +e.getMessage());
        }
    }

    private String getSimName(ArrayList<ConfObject> keypath) {
        return keypath.get(keypath.size()-2).toString().replace("{","").replace("}","");
    }
    private void addSimState(NavuContainer root, ArrayList<ConfObject> keypath, String state) throws NedException {
        String simName = getSimName(keypath);
        if (! this.simStates.containsKey(simName)) { 
            try {
                NavuContainer device = root.container(Ncs._devices).list(Ncs._device).elem(deviceName);
                NavuContainer sim = device.container(Ncs._config).list(ciscovirl._simulation).elem(simName);
                SimState simstate = new SimState(sim);
                simstate.action = state;
                this.simStates.put(simName, simstate);
            } catch (NavuException e) {
                throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, e.getMessage());
            }
        }
    }
    private String keyValue(ConfKey key) {
        return key.toString().replace("{","").replace("}","");
    }
    public void create(NavuContainer root, NedEditOp op, StringBuilder dryRun) throws NedException {
        ArrayList<ConfObject> keypath = getkeypath(op);
        ConfKey key = (ConfKey)keypath.get(0);
        ConfTag tag = (ConfTag)keypath.get(1);
        String simName = getSimName(keypath);

        if (tag.getTagHash() == ciscovirl._simulation) {
            // This is a newly created simulation, load all the details from CDB new state
            LOGGER.info("Creating Simulation... ");
            addSimState(root, keypath, "START");
        }
    }
    public void modified(NavuContainer root, NedEditOp op, StringBuilder dryRun) throws NedException {
        ArrayList<ConfObject> keypath = getkeypath(op);
        ConfTag tag = null;
        ConfObject obj = keypath.get(0);
        if (obj instanceof ConfTag) tag = (ConfTag) obj;
        else tag = (ConfTag)keypath.get(1);
        if (tag.getTagHash() == ciscovirl._simulation) {
            // This is a newly created simulation, load all the details from CDB new state
            LOGGER.info("Modifying Simulation... ");
            addSimState(root, keypath, "NONE");
        }
    }

    public void valueSet(NavuContainer root, NedEditOp op, StringBuilder dryRun)
        throws Exception  {
        ArrayList<ConfObject> keypath = getkeypath(op);
        ConfKey key = (ConfKey)keypath.get(1);
        ConfTag tag = (ConfTag)keypath.get(0);
        ConfTag nodeStateTag = new ConfTag("cisco-virl", "excludeFromLaunch");
        String simName = getSimName(keypath);
        SimState simstate = simStates.get(simName);
        if (simstate.action.equals("START") || simstate.action.equals("RESTART")) {
            // We have already loaded all the details
            return;
        }
LOGGER.info("VALUESET: tag: "+tag.toString()+" key: "+key.toString());
        if (!tag.equals(nodeStateTag)) {
            LOGGER.info("Setting Simulation to Restart");
            simstate.setRestart();
        } else {
            LOGGER.info("NODE STATE VALUE: "+op.getValue());
            if (op.getValue().toString().equals("true")) {
                NodeState nodestate = new NodeState(key.elementAt(0).toString(), "STOP");
                if (!simstate.actionNodes.containsKey(nodestate.name)) {
                    LOGGER.info("Setting Node to Stop");
                    simstate.actionNodes.put(nodestate.name, nodestate);
                }
            } else {
                NodeState nodestate = new NodeState(key.elementAt(0).toString(), "START");
                LOGGER.info("Setting Node to Start");
                simstate.actionNodes.put(nodestate.name, nodestate);
            }
        }
    }

    public void defaultSet(NedEditOp op, StringBuilder dryRun)
        throws NedException  {
        ArrayList<ConfObject> keypath = getkeypath(op);
        ConfKey key = (ConfKey)keypath.get(0);
        LOGGER.info("Default KEY: " + op.getPath());
    }

    public void delete(NavuContainer root, NedEditOp op, StringBuilder dryRun)
        throws NedException  {
        ArrayList<ConfObject> keypath = getkeypath(op);
        ConfTag tag = null;
        ConfObject obj = keypath.get(0);
        if (obj instanceof ConfTag) tag = (ConfTag) obj;
        else tag = (ConfTag)keypath.get(1);
        LOGGER.info("DELETE TAG: "+tag.toString());
        if (tag.getTagHash() == ciscovirl._simulation) {
            LOGGER.info("Deleting Simulation... ");
            String simName = getSimName(keypath);
            if (! this.simStates.containsKey(simName)) { 
                SimState simstate = new SimState(simName);
                simstate.setStop();
                this.simStates.put(simName, simstate);
            } else {
                this.simStates.get(simName).setStop();
            }
        } else if (tag.getTagHash() == ciscovirl._node) {
            LOGGER.info("Restarting Simulation... ");
            String simName = getSimName(keypath);
                this.simStates.get(simName).setRestart();
        }
    }


    public void getTransId(NedWorker w) throws NedException, IOException {
        w.error(NedCmd.GET_TRANS_ID, "getTransId", "not supported");
    }

    // private SimState buildSimulation (ArrayDeque<ConfObject> keyqueue) {
    //     ConfKey key = null;
    //     ConfTag tag = null;
    //     if (keyqueue.peek() instanceof ConfTag) {
    //         tag = (ConfTag)keyqueue.pop();
    //     } else {
    //         key = (ConfKey)keyqueue.pop();
    //         tag = (ConfTag)keyqueue.pop();
    //     }
    //     SimState sim = null;
    //     if (! keyqueue.isEmpty()) sim = buildSimulation(keyqueue.clone());
    //     LOGGER.info("BUILD tag: "+tag+" key: "+key);
    //     switch (tag.getTagHash()) {
    //     case ciscovirl._simulation:
    //         sim = this.simStates.get(keyValue(key));
    //         if (sim == null) {
    //             LOGGER.info("Create Simulation: ");
    //             sim = new SimState(keyValue(key));
    //             this.simStates.put(keyValue(key), sim);
    //         }
    //         break;
    //     case ciscovirl._extensions:
    //         LOGGER.info("Create Extensions: ");
    //         ConfKey parentkey = (ConfKey)keyqueue.pop();
    //         ConfTag parenttag = (ConfTag)keyqueue.pop();
    //         if (parenttag.getTagHash() == ciscovirl._simulation) {
    //             // This is a simulation extension
    //             if (sim.extensions == null) sim.extensions = new Extensions();
    //         } else {
    //             // This is a node extension
    //             ArrayList<com.example.ciscovirl.Node> nodes = sim.nodes;
    //             com.example.ciscovirl.Node node = nodes.get(nodes.indexOf(keyValue(parentkey)));
    //             if (node.extensions == null) node.extensions = new Extensions();
    //         }
    //         break;
    //     case ciscovirl._entry:
    //         LOGGER.info("Create Entry: ");
    //         ConfTag eparenttag = (ConfTag)keyqueue.pop(); // pop extensions off
    //         ConfKey eparentkey = (ConfKey)keyqueue.pop(); // get grandparent
    //         eparenttag = (ConfTag)keyqueue.pop();
    //         if (eparenttag.getTagHash() == ciscovirl._simulation) {
    //             // This is a simulation extension entry
    //             sim.extensions.entrys.add(new Entry(keyValue(key)));
    //         } else {
    //             // This is a node extension
    //             ArrayList<com.example.ciscovirl.Node> nodes = sim.nodes;
    //             com.example.ciscovirl.Node node = nodes.get(nodes.indexOf(keyValue(eparentkey)));
    //             node.extensions.entrys.add(new Entry(keyValue(key)));
    //         }
    //         break;
    //     case ciscovirl._node:
    //         LOGGER.info("Create Node: " + keyValue(key));
    //         LOGGER.info("Create Node: " + sim.nodes.size());
    //         sim.nodes.add(new com.example.ciscovirl.Node(keyValue(key), new Integer(sim.nodes.size())));
    //         break;
    //     case ciscovirl._connection:
    //         break;
    //     }
    //     return sim;
    // }

}
