package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.Node.Node;

/**
 * @author julian
 */
public class NodesC implements HardwareComponents{
	static public Node PVSSMPNode(int cpuCount){
		return PVSSMPNode(cpuCount, 12000);
	}

	static public Node PVSSMPNode(int cpuCount, long memoryInMB){
		Node node = new Node();
		node.setName("PVS-Node");
		node.setCPUs(cpuCount);
		node.setInstructionsPerSecond(266*1000000);

		assert(memoryInMB > 10);
		node.setMemorySize(memoryInMB * MBYTE);
		return node;
	}
}
