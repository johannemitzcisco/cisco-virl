package com.example.ciscovirl;

import com.example.ciscovirl.namespaces.*;
import java.io.IOException;
import java.net.InetAddress;
//import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Set;
import java.util.Map;
import com.tailf.conf.ConfBuf;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private static MaapiSchemas.CSNode rpcCs;

    private VirlComms comms;

    public ciscovirlNed(){
        this(true);
    }

    public ciscovirlNed(boolean wantReverse){
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

//            this.device = ciscovirlNed.init(deviceName);
            this.comms = new VirlComms(this, ip, port, worker.getRemoteUser(), worker.getPassword());


            LOGGER.info("CONNECTING <==");

            NedCapability capas[] = new NedCapability[1];
            capas[0] = new NedCapability("http://com/example/ciscovirl", "cisco-virl");

            NedCapability statscapas[] = new NedCapability[1];
            statscapas[0] = new NedCapability("http://com/example/ciscovirl-stats",
                                              "cisco-virl-stats");

            setConnectionData(capas,
                              statscapas,
                              this.wantReverse,  // want reverse-diff
                              TransactionIdMode.NONE);


            LOGGER.info("CONNECTING ==> OK");
        }
        catch (Exception e) {
            worker.error(NedCmd.CONNECT_GENERIC, e.getMessage()," Cntc error");
        }
    }

    private void connect() {
        LOGGER.info("CONNECT <==");
//        comms.connect();
        LOGGER.info("CONNECT ==> OK");
    }

    public boolean isAlive() {
        LOGGER.info("IS ALIVE <==");
        LOGGER.info("IS ALIVE ==> OK");
        return true;
    }

    public void reconnect(NedWorker worker) {
        LOGGER.info("RECONNECT <==");
//        comms.connect();
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
//            LOGGER.info( "Node Path: "+ Node.isPath(path));
//            LOGGER.info( "Simulation Path: "+ Simulation.isPath(path));

            // maapi.attach(tHandle, 0);
            // if (Node.isPath(path)) {
            //     JsonObject simNodeData = requestJSONData(Node.getURLSuffix(path), Node.getJsonRoot(path));
            //     maapi.setElem(tHandle, Node.getConfValueFromPath(simNodeData, path), path);
            // } else if (Simulation.isPath(path)) {
            //     JsonObject simData = requestJSONData(Simulation.getURLSuffix(path), Simulation.getJsonRoot(path));
            //     maapi.setElem(tHandle, Simulation.getConfValueFromPath(simData, path), path);
            // }
            // maapi.detach(tHandle);
            worker.showStatsResponse(null);
            LOGGER.info("SHOW STATS ===> OK");
        }
        catch (Exception e) {
            worker.error(NedEditOp.VALUE_SET, "Unable to get Running Simulation Detail");
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
//            NedTTL[] nedTTLs;
            try {
                List<StatsList> statlist = StatsListFactory.getStatsList(comms, path);
                StatsListFactory.saveToNSO(statlist, maapi, tHandle, path);

//                 JsonObject data = null;
// /*                if (Entry.isPath(path)) {
//                     LOGGER.info( "Extensions/Entry Path");
//                     data = requestXMLData(Entry.getURLSuffix(path), Entry.getJsonRoot(path));
//                 } else if (Node.isPath(path)) {
//                     LOGGER.info( "Node Path");
//                     data = requestJSONData(Node.getURLSuffix(path), Node.getJsonRoot(path));
//                 } else*/ if (Simulation.isStatsListPath(path)) {
//                     data = requestJSONData(Simulation.getURLSuffix(path), Simulation.getJsonRoot(path));
//                     Simulation.StatsListToNSO(data);
//                 }
// /*                if (data != null) {
//                     for (Map.Entry<String, JsonElement> nodeEntry : data.entrySet()) {
//                         ConfPath newPath = new ConfPath(path.toString()+"{"+nodeEntry.getKey()+"}");
//                         if (!maapi.exists(tHandle, newPath)) maapi.create(tHandle, newPath);
//                     }
//                 }
                LOGGER.info("SHOW STATS LIST ===> OK");
            } catch (Exception e) {
                worker.error(NedEditOp.VALUE_SET, "Unable to get stats list data");
                throw e;
            }
            maapi.detach(tHandle);
            worker.showStatsListResponse(10, null);
//            worker.showStatsListResponse(30, nedTTLs);
        }
        catch (Exception e) {
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

        for (int i = 0; i<ops.length; i++) {
            LOGGER.info("op " + ops[i]);
        }
        //edit(ops);
        worker.prepareResponse();
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
        StringBuilder dryRun = new StringBuilder();

        LOGGER.info("PREPARE DRY <==");
//        edit(ops, dryRun);
        try {
            worker.prepareDryResponse(dryRun.toString());
        }
        catch (IOException e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR,
                                   "Internal error when calling "+
                                   "prepareDryResponse: "+
                                   e.getMessage());
        }
        LOGGER.info("PREPARE DRY ==> OK");
    }

    public void commit(NedWorker worker, int timeout)
        throws NedException, IOException {
        LOGGER.info("COMMIT <==");
        worker.commitResponse();
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
//             maapi.attach(tHandle, 0);
//             String devPath =
//                 "/ncs:devices/device{" + deviceName + "}/config/";
//             Gson gson = new Gson();
//             JsonObject data = requestJSONData(Simulation.getURLSuffix(null), Simulation.getJsonRoot(null));
//             if (data != null) {
//                 for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
//                     if (entry.getKey().equals("~jumphost")) continue; //Gson parser barfs on something in this guys json
//                     Simulation simulation = new Simulation(entry.getKey(), this.deviceName);
//                     String simXML = execRequest(RequestType.GET, "/export/"+entry.getKey());
//                     String simJson = XML.toJSONObject(simXML).toString();
//                     LOGGER.info(XML.toJSONObject(simXML).toString(4));

//                     JsonObject root = new JsonParser().parse(simJson).getAsJsonObject();
// //                    LOGGER.info("TOPO JSON:\n"+root.toString());
//                     Topology topology = gson.fromJson(root.getAsJsonObject().get("topology").getAsJsonObject(), Topology.class);
//                     simulation.setTopology(topology);
//                     LOGGER.info("\n"+simulation.toString());
//                     simulation.toNSO(this, tHandle);
//                 }
//             }
//             maapi.detach(tHandle);
            worker.showGenericResponse();
            LOGGER.info("SHOW ==> OK");
        }
        catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
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
            LOGGER.info("RUNNING COMMAND => " + cmdname);

/*            // Find schema node for the Enumeration in the rpc reply
            MaapiSchemas.CSNode ecs = ciscovirlNed.schemas.findCSNode(
                        ciscovirlNed.rpcCs, ciscovirl.uri, "my-cmd");
            ecs = ciscovirlNed.schemas.findCSNode(ecs, ciscovirl.uri, "item");
            ecs = ciscovirlNed.schemas.findCSNode(ecs, ciscovirl.uri, "l1");

            ConfNamespace x = new ciscovirl();
            worker.commandResponse(
                new ConfXMLParam[] {
                    new ConfXMLParamStart(x, "item"),
                    new ConfXMLParamValue(
                        x, "l1",
                        ciscovirlNed.schemas.stringToValue(ecs.getType(), "On")),
                    new ConfXMLParamValue(x, "l2",new ConfInt32(44)),
                    new ConfXMLParamStop(x, "item"),

                    new ConfXMLParamStart(x, "item"),
                    new ConfXMLParamValue(
                        x, "l1",
                        ciscovirlNed.schemas.stringToValue(ecs.getType(), "Off")),
                    new ConfXMLParamValue(x, "l2",new ConfInt32(33)),
                    new ConfXMLParamStop(x, "item")
                });*/
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

    public void getTransId(NedWorker w) throws NedException, IOException {
        w.error(NedCmd.GET_TRANS_ID, "getTransId", "not supported");
    }


// Now below here we have specific code to fake the data that is kept on the
// devices. In reality, this data is on the device and it's the
// NEDs job to read and write that data

// Here we fake this, in order to have this skeleton code
// a bit more realistic
// The data we keep here is the equvalent of what is modelled
// in cisco-virl.yang


/*    public static Device init(String name) throws MaapiException, NedException {
        if (devices == null) {
            ciscovirlNed.schemas = Maapi.getSchemas();
            ciscovirlNed.cfgCs =
                ciscovirlNed.schemas.findCSNode(Ncs.uri,"/devices/device/config");
            ciscovirlNed.rpcCs =
                ciscovirlNed.schemas.findCSNode(Ncs.uri,
                                        "/devices/device/rpc/rpc-my-cmd");

            devices = new ArrayList<Device>();
        }

        Device device = getDevice(name);
        if (device == null) {
            device = new Device(name);
            devices.add(device);
        }

        return device;
    }

    public static ArrayList<Device> devices = null;

    public static class Device {
        final static int default_y = 12;
        final static int default_z = 13;

        private String name;
        private ArrayList<Row> rows;

        Device(String aName) {
            rows =  new ArrayList<Row>();
            this.name = aName;
            devices.add(this);

            // Start with some fake data
            rows.add(new Row(17));
            rows.add(new Row(42));
            rows.add(new Row(4711));
        }

        public Row getRow(ConfKey k) {
            int i = ((ConfInt32)k.elementAt(0)).intValue();
            for (Row r: rows) {
                if (r.k == i) {
                    return r;
                }
            }

            Row r = new Row(i);
            rows.add(r);

            return r;
        }
    }

    public static Device getDevice(String dev) throws NedException {
        for (Device d: devices) {
            if (d.name.equals(dev)) {
                return d;
            }
        }
        return null;
    }

    public enum Direction {
        Up, Down, NotUsed
    }

    public static class Row {
        int k;
        int x;            // optional uint32
        boolean xIsset;   // to cater for non-set x
        int y;            // int8, default 12
        int z;            // int8 default 13
        Direction e;      // optional YANG enumeration

        Row(int k) {
            this.k = k;
            xIsset = false;
            this.e = Direction.NotUsed;

            this.y = Device.default_y;
            this.z = Device.default_z;
        }
    }

    public void edit(NedEditOp[] ops)
        throws NedException {
        edit(ops, null);
    }

    public void edit(NedEditOp[] ops, StringBuilder dryRun)
        throws NedException {

        try {
            for (NedEditOp op: ops) {
                switch (op.getOperation()) {
                case NedEditOp.CREATED:
                    create(op, dryRun);
                    break;
                case NedEditOp.DELETED:
                    delete(op, dryRun);
                    break;
                case NedEditOp.MOVED:
                    break;
                case NedEditOp.VALUE_SET:
                    valueSet(op, dryRun);
                    break;
                case NedEditOp.DEFAULT_SET:
                    defaultSet(op, dryRun);
                    break;
                }
            }
        } catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR, "", e);
        }
    }

    private ConfObject[] getKP(NedEditOp op) throws NedException {
        ConfPath cp = op.getPath();
        ConfObject[] kp;

        try {
            kp = cp.getKP();
        } catch (Exception e) {
            throw new NedException(NedErrorCode.NED_INTERNAL_ERROR,
                                   "Internal error, cannot get key path: "
                                   +e.getMessage());
        }
        return kp;
    }

    public void create(NedEditOp op, StringBuilder dryRun)
        throws NedException  {

        ConfObject[] kp = getKP(op);
        ConfKey key = (ConfKey)kp[0];
        int i = ((ConfInt32)key.elementAt(0)).intValue();
        if (dryRun == null) {
            device.rows.add(new Row(i));
        } else {
            dryRun.append("new Row[");
            dryRun.append(i);
            dryRun.append("]");
            dryRun.append('\n');
        }
    }

    public void valueSet(NedEditOp op, StringBuilder dryRun)
        throws NedException  {
        ConfObject[] kp = getKP(op);
        ConfKey key = (ConfKey)kp[1];
        ConfTag tag = (ConfTag)kp[0];
        Row r = device.getRow(key);

        if (tag.getTag().compareTo("x") == 0) {
            ConfUInt32 v = (ConfUInt32) op.getValue();
            if (dryRun == null) {
                r.x = (int)v.longValue();
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].x = ");
                dryRun.append(v);
                dryRun.append('\n');
            }
        }
        else if (tag.getTag().compareTo("y") == 0) {
            ConfInt8 v = (ConfInt8) op.getValue();
            if (dryRun == null) {
                r.y = v.intValue();
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].y = ");
                dryRun.append(v);
                dryRun.append('\n');
            }
        }
        else if (tag.getTag().compareTo("z") == 0) {
            ConfInt8 v = (ConfInt8) op.getValue();
            if (dryRun == null) {
                r.z = v.intValue();
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].z = ");
                dryRun.append(v);
                dryRun.append('\n');
            }
        }
        else if (tag.getTag().compareTo("e") == 0) {

            MaapiSchemas.CSNode ecs = ciscovirlNed.schemas.findCSNode(
                    ciscovirlNed.cfgCs, ciscovirl.uri, "row");
            ecs = ciscovirlNed.schemas.findCSNode(ecs, ciscovirl.uri, "e");
            ConfValue v = (ConfValue)op.getValue();
            String str = ciscovirlNed.schemas.valueToString(ecs.getType(), v);
            if (dryRun == null) {
                if (str.compareTo("Up") == 0) {
                    r.e = Direction.Up;
                } else if (str.compareTo("Down") == 0) {
                    r.e = Direction.Down;
                }
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].e = ");
                dryRun.append(str);
                dryRun.append('\n');
            }
        }
    }

    public void defaultSet(NedEditOp op, StringBuilder dryRun)
        throws NedException  {
        ConfObject[] kp = getKP(op);
        LOGGER.info("default set for " + op.getPath());
        ConfKey key = (ConfKey)kp[1];
        ConfTag tag = (ConfTag)kp[0];
        Row r = device.getRow(key);

        if (tag.getTagHash() == ciscovirl._y) {
            if (dryRun == null) {
                r.y =  Device.default_y;
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].y = ");
                dryRun.append(Device.default_y);
                dryRun.append('\n');
            }
        } else if (tag.getTagHash() == ciscovirl._z) {
            if (dryRun == null) {
                r.z = Device.default_z;
            } else {
                dryRun.append("Row[");
                dryRun.append(r.k);
                dryRun.append("].z = ");
                dryRun.append(Device.default_z);
                dryRun.append('\n');
            }
        }
    }

    public void delete(NedEditOp op, StringBuilder dryRun)
        throws NedException  {
        ConfObject[] kp = getKP(op);
        if (kp[0] instanceof ConfKey) {
            ConfKey key = (ConfKey)kp[0];
            Row r = device.getRow(key);
            device.rows.remove(r);
            if (dryRun == null) {
                device.rows.remove(r);
            } else {
                dryRun.append("delete Row[");
                dryRun.append(r.k);
                dryRun.append("]");
                dryRun.append('\n');
            }
        } else if (kp[1] instanceof ConfKey) {
            ConfKey key = (ConfKey)kp[1];
            ConfTag tag = (ConfTag)kp[0];
            Row r = device.getRow(key);
            if (dryRun != null) {
                dryRun.append("delete Row[");
                dryRun.append(r.k);
                dryRun.append("].");
                dryRun.append(tag.getTag());
                dryRun.append('\n');
            } else {
                if (tag.getTag().compareTo("x") == 0) {
                    r.x = 0;
                    r.xIsset = false;
                } else if (tag.getTag().compareTo("e") == 0) {
                    r.e = Direction.NotUsed;
                }
            }
        }
    }*/
}
