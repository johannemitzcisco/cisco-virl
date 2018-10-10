# cisco-virl
Cisco NSO NED for [Cisco's VIRL](https://learningnetworkstore.cisco.com/virtual-internet-routing-lab-virl/cisco-personal-edition-pe-20-nodes-virl-20) 

There are 2 parts to this NED due to the nature of Cisco's VIRL.  The VIRL server itself does not have much configuration data that changes regularly, you usually install and configure it and then start/stop simulations containing VMs, and the connections between them, running in it.  Paired with the VM Maestro Designer UI the workflow is to design a topopology in the UI and then "send" the request to start a simulation based on the topology, which is represented as an XML file, and stop the simulation.

**Part 1**: This is the standard NED parts you would expect, a model of the live-status of running simulations along with Start and Stop actions under the device's command tree.

**Part 2**: This is a representation of design aspect that the VM Maestro UI normally fulfills.  This part (/virl) allows you to design a VIRL topology model and then Start and Stop simulations running in a VIRL server.  It also has an action that will pull a currently running simulation's topology into the design topology model.

