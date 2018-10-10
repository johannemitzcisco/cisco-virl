# -*- mode: python; python-indent: 4 -*-
import ncs
from ncs.application import Service
from ncs.dp import Action
from ncs.maapi import Maapi
import copy


# ------------------------
# SERVICE CALLBACK EXAMPLE
# ------------------------
class ServiceCallbacks(Service):

    # The create() callback is invoked inside NCS FASTMAP and
    # must always exist.
    @Service.create
    def cb_create(self, tctx, root, service, proplist):
        self.log.info('Service create(service=', service._path, ' ', service.name, ')')
    # The pre_modification() and post_modification() callbacks are optional,
    # and are invoked outside FASTMAP. pre_modification() is invoked before
    # create, update, or delete of the service, as indicated by the enum
    # ncs_service_operation op parameter. Conversely
    # post_modification() is invoked after create, update, or delete
    # of the service. These functions can be useful e.g. for
    # allocations that should be stored and existing also when the
    # service instance is removed.

    # @Service.pre_lock_create
    # def cb_pre_lock_create(self, tctx, root, service, proplist):
    #     self.log.info('Service plcreate(service=', service._path, ')')

    # @Service.pre_modification
    # def cb_pre_modification(self, tctx, op, kp, root, proplist):
    #     self.log.info('Service premod(service=', kp, ')')

    # @Service.post_modification
    # def cb_post_modification(self, tctx, op, kp, root, proplist):
    #     self.log.info('Service premod(service=', kp, ')')

class LoadRunningVirlTopology(Action):
    @Action.action
    def cb_action(self, uinfo, name, kp, input, output):
        virl_server = input.virl_server
        simulation_name = input.simulation_name
        topology_name = input.topology_name
        self.log.info('ACTION NAME: %s' % name)
        self.log.info('KEYPATH: %s' % (kp))
        self.log.info('VIRL SERVER: %s' % (virl_server))
        self.log.info('SIMULATION NAME: %s' % (simulation_name))
        self.log.info('NEW TOPOLOGY NAME: %s' % (topology_name))
        with ncs.maapi.Maapi() as m:
            with ncs.maapi.Session(m, uinfo.username, uinfo.context):
                with m.start_write_trans() as t:
                    root = ncs.maagic.get_root(t)
                    topology = root.devices.device[virl_server].live_status.simulations.simulation[simulation_name].topology
                    self.copy_topology(root, topology, topology_name)
                    t.apply()
        output.result = "SUCCESS!!"

    def copy_topology(self, root, newtopo, newtopo_name):
        virl = root.virl
        topo = virl.topology.create(newtopo_name)
        topo.annotation_xmlns = newtopo.annotation_xmlns
        topo.annotation_schemaVersion = newtopo.annotation_schemaVersion
        topo.annotation_xsiSchemaLocation = newtopo.annotation_xsiSchemaLocation
        topo.annotation_xmlnsXSI = newtopo.annotation_xmlnsXSI
        extensions = topo.extensions
        for e in newtopo.extensions.entry:
            entry = extensions.entry.create(e.key)
            entry.type = e.type
            entry.value = e.value
        for n in newtopo.node:
            node = topo.node.create(n.name)
            node.connection_index = n.connection_index
            node.type = n.type
            node.subtype = n.subtype
            node.location = n.location
            node.excludeFromLaunch = n.excludeFromLaunch
            extensions = node.extensions
            for e in n.extensions.entry:
                entry = extensions.entry.create(e.key)
                entry.type = e.type
                entry.value = e.value
            for i in n.interface:
                interface = node.interface.create(i.id)
                interface.connection_index = i.connection_index
                interface.name = i.name
                interface.ipv4 = i.ipv4
                interface.netPrefixLenV4 = i.netPrefixLenV4
        for c in newtopo.connection:
            connection = topo.connection.create(c.src, c.dst)


class StopSimulation(Action):
    @Action.action
    def cb_action(self, uinfo, name, kp, input, output):
        virl_server_name = input.virl_server
        simulation_name = input.simulation_name
        self.log.info('ACTION NAME: %s' % name)
        self.log.info('KEYPATH: %s' % (kp))
        self.log.info('VIRL SERVER: %s' % (virl_server_name))
        self.log.info('VIRL SIMULATION: %s' % (simulation_name))
        output.result = "FAILED"
        with ncs.maapi.Maapi() as m:
            with ncs.maapi.Session(m, uinfo.username, uinfo.context):
                with m.start_read_trans() as t:
                    virl_cmds = ncs.maagic.get_root(t).devices.device[virl_server_name].config.cisco_virl__commands
                    simstop_input = virl_cmds.Stop_Simulation.get_input()
                    simstop_input.simulation_name = simulation_name
                    output.result = virl_cmds.Stop_Simulation(simstop_input).result

class StartSimulation(Action):
    @Action.action
    def cb_action(self, uinfo, name, kp, input, output):
        virl_server_name = input.virl_server
        self.log.info('ACTION NAME: %s' % name)
        self.log.info('KEYPATH: %s' % (kp))
        self.log.info('VIRL SERVER: %s' % (virl_server_name))
        self.log.info('TRAN HANDLE: %s %s' % (uinfo.actx_thandle, uinfo.context))
        output.result = "FAILED"
        with ncs.maapi.Maapi() as m:
           with ncs.maapi.Session(m, uinfo.username, uinfo.context):
#            with m.start_read_trans() as t:
                with m.start_trans_in_trans(uinfo.actx_thandle,1) as t:
                    topology = ncs.maagic.get_node(t, kp)
                    payload = self.get_topology_xml(topology)
                    self.log.info('PAYLOAD: \n%s \n' % (payload))
                    virl_cmds = ncs.maagic.get_root(t).devices.device[virl_server_name].config.cisco_virl__commands
                    simstart_input = virl_cmds.Start_Simulation.get_input()
                    simstart_input.topology_name = topology.name
                    simstart_input.topology_xml = payload
                    output.result = virl_cmds.Start_Simulation(simstart_input).result


    def get_topology_xml(self, topology):
        xstr = lambda s: s or ""
        topo_start_xml = '''<?xml version='1.0' encoding='UTF-8' standalone='yes'?>
<topology
    xmlns="%s"
    xmlns:xsi="%s" schemaVersion="%s" xsi:schemaLocation="%s">'''
        extensions_start_xml = '<extensions>'
        entry_xml = '<entry key="%s" type="%s">%s</entry>'
        extensions_end_xml = '</extensions>'
        node_start_xml = '<node name="%s" type="%s" subtype="%s" location="%s" excludeFromLaunch="%s">'
        interface_xml = '<interface id="%s" name="%s" ipv4="%s" netPrefixLenV4="%s"/>'
        interface_xml_empty = '<interface id="%s" name="%s"/>'
        node_end_xml = '</node>'
        connection_xml = '<connection dst="%s" src="%s"/>'
        topo_end_xml = '</topology>'
        topo_xml = topo_start_xml % (topology.annotation_xmlns, topology.annotation_xmlnsXSI, topology.annotation_schemaVersion, topology.annotation_xsiSchemaLocation)
        topo_xml = topo_xml + '\n' + extensions_start_xml
        for entry in topology.extensions.entry:
            topo_xml = topo_xml + '\n' + entry_xml % (entry.key, entry.type, xstr(entry.value))
        topo_xml = topo_xml + '\n' + extensions_end_xml
        for node in topology.node:
            topo_xml = topo_xml + '\n' + node_start_xml % (node.name, node.type, node.subtype, node.location, str(node.excludeFromLaunch).lower())
            topo_xml = topo_xml + '\n' + extensions_start_xml
            for entry in node.extensions.entry:
                topo_xml = topo_xml + '\n' + entry_xml % (entry.key, entry.type, xstr(entry.value))
            topo_xml = topo_xml + '\n' + extensions_end_xml
            for interface in node.interface:
                if (interface.ipv4 is None):
                    topo_xml = topo_xml + '\n' + interface_xml_empty % (interface.id, interface.name)
                else:
                    topo_xml = topo_xml + '\n' + interface_xml % (interface.id, interface.name, xstr(interface.ipv4), xstr(interface.netPrefixLenV4))
            topo_xml = topo_xml + '\n' + node_end_xml 
        for connection in topology.connection:
            topo_xml = topo_xml + '\n' + connection_xml % (xstr(connection.dst), xstr(connection.src))
        topo_xml = topo_xml + '\n' + topo_end_xml
        topo_xml = topo_xml.replace('&nbsp', ' ')
        return topo_xml






# ---------------------------------------------
# COMPONENT THREAD THAT WILL BE STARTED BY NCS.
# ---------------------------------------------
class Main(ncs.application.Application):
    def setup(self):
        # The application class sets up logging for us. It is accessible
        # through 'self.log' and is a ncs.log.Log instance.
        self.log.info('Main RUNNING')

        # Service callbacks require a registration for a 'service point',
        # as specified in the corresponding data model.
        #
#        self.register_service('virl-servicepoint', ServiceCallbacks)
        self.register_action('loadRunningVirlTopology-action', LoadRunningVirlTopology)
        self.register_action('StartSimulation-action', StartSimulation)
        self.register_action('StopSimulation-action', StopSimulation)

        # If we registered any callback(s) above, the Application class
        # took care of creating a daemon (related to the service/action point).

        # When this setup method is finished, all registrations are
        # considered done and the application is 'started'.

    def teardown(self):
        # When the application is finished (which would happen if NCS went
        # down, packages were reloaded or some error occurred) this teardown
        # method will be called.

        self.log.info('Main FINISHED')
