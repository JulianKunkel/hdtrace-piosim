package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;


public class testSwitchingTopology implements TestTopology{
	int sources = 10;
	int targets = 1;

	public void setNodes(int nodes) {
		this.sources = nodes;
	}

	public void setTargets(int targets) {
		this.targets = targets;
	}

	protected PaketRoutingAlgorithm createRoutingAndTopology(){
		return new PaketFirstRoute();
	}

	@Override
	public void createTopology(
			ArrayList<INetworkEntry> entriesOut,
			ArrayList<INetworkExit> exitsOut,
			INetworkEntry entryNode,
			INetworkExit exitNode,
			INetworkNode node,
			INetworkEdge myEdge,
			ModelBuilder mb) throws Exception
	{
		PaketRoutingAlgorithm routing = createRoutingAndTopology();
		INetworkTopology topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routing);

		// create nodes:
		INetworkNode sw = mb.cloneFromTemplate(node);
		sw.setName("SW");

		// create targets:
		for(int y = 0 ; y < targets; y++){
				INetworkNode cur;
				cur = mb.cloneFromTemplate(exitNode);
				exitsOut.add((INetworkExit) cur);
				cur.setName("T" + y );

				INetworkEdge edge = mb.cloneFromTemplate(myEdge);
				edge.setName(sw.getName() + "->" + cur.getName());

				mb.connect(topology, sw, edge , cur);
		}

		// create sources:
		for(int y = 0 ; y < sources; y++){
				INetworkNode cur;
				cur = (INetworkNode) mb.cloneFromTemplate(entryNode);
				entriesOut.add((INetworkEntry) cur);
				cur.setName("S" + y );

				INetworkEdge edge = mb.cloneFromTemplate(myEdge);

				edge.setName(cur.getName() + "->" + sw.getName());

				mb.connect(topology, cur, edge , sw);
		}

	}
}