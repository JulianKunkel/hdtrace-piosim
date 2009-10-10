/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;

public interface TestTopology{
	public void createTopology(
			ArrayList<INetworkEntry> entriesOut,
			ArrayList<INetworkExit> exitsOut,
			INetworkEntry entryNode,
			INetworkExit exitNode,
			INetworkNode node,
			INetworkEdge myEdge,
			ModelBuilder mb
			) throws Exception;
}