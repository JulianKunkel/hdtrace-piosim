package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.Node.Node;

/**
 * @author julian
 */
public class NodesC implements HardwareComponents{
	static public Node PVSSMPNode(int cpuCount){
		Node node = new Node();
		node.setName("PVS-Node");
		node.setCPUs(cpuCount);
		node.setInstructionsPerSecond(1000000);

		node.setMemorySize(1000 * MBYTE);
		return node;
	}
}
