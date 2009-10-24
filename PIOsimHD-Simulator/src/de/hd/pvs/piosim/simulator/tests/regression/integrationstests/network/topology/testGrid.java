package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;


public class testGrid implements TestTopology{
	int height = 3;
	int width = 3;

	public void setGrid(int height, int width){
		this.height = height;
		this.width = width;
	}

	@Override
	public void createTopology(
			ArrayList<INetworkEntry> entriesOut,
			ArrayList<INetworkExit> exitsOut,
			INetworkEntry entryNode,
			INetworkExit exitNode,
			INetworkNode node,
			INetworkEdge myEdge,
			ModelBuilder mb,
			PaketRoutingAlgorithm routing
			) throws Exception
	{
		INetworkTopology topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routing);

		final ArrayList<INetworkNode> nodes = new ArrayList<INetworkNode>();
		// arrangement to:
		// 0, 1, 2,
		// 3, 4, 5
		// 6, 7, 8

		// create nodes:
		for(int y = 0 ; y < height; y++){
			for (int x = 0; x < width; x++) {
				INetworkNode cur = mb.cloneFromTemplate(node);
				cur.setName(x + ":"  + y );
				nodes.add(cur);
				mb.addNetworkNode(cur);
			}
		}

		// create exits:
		for(int y = 0 ; y < height; y++){
			for (int x = 0; x < width; x++) {
				INetworkExit cur = mb.cloneFromTemplate(exitNode);
				exitsOut.add(cur);
				mb.addNetworkNode(cur);
				cur.setName("S" + x + ":"  + y );

				// link it.
				INetworkEdge edge = mb.cloneFromTemplate(myEdge);
				INetworkNode src = nodes.get(x + y * width);
				edge.setName(src.getName() + "->" + cur.getName());
				mb.connect(topology, src, edge , cur);
			}
		}

		// create entries.
		for(int y = 0 ; y < height; y++){
			for (int x = 0; x < width; x++) {
				INetworkEntry cur = mb.cloneFromTemplate(entryNode);
				entriesOut.add(cur);
				mb.addNetworkNode(cur);
				cur.setName("E" + x + ":"  + y );

				INetworkEdge edge = mb.cloneFromTemplate(myEdge);
				INetworkNode tgt = nodes.get(x + y * width);
				edge.setName(cur.getName() + "->" + tgt.getName());
				mb.connect(topology, cur, edge , tgt);
			}
		}

		// create horizontal edges:
		for(int y = 0 ; y < height; y++){
			for (int x = 0; x < width - 1; x++) {
				INetworkEdge edge = mb.cloneFromTemplate(myEdge);
				INetworkEdge edge2 = mb.cloneFromTemplate(myEdge);

				INetworkNode src = nodes.get(x + y * width);
				INetworkNode tgt = nodes.get(x + 1 + y * width);

				edge.setName(src.getName() + "->" + tgt.getName());
				edge2.setName(tgt.getName() + "->" + src.getName());

				mb.connect(topology, src, edge , tgt);
				mb.connect(topology, tgt, edge2 , src);
			}
		}

		// create vertical edges:

		for (int x = 0; x < width; x++) {
			for(int y = 0 ; y < height - 1 ; y++){
				INetworkEdge edge = mb.cloneFromTemplate(myEdge);
				INetworkEdge edge2 = mb.cloneFromTemplate(myEdge);

				INetworkNode src = nodes.get(x + y * width);
				INetworkNode tgt = nodes.get(x + (y+1) * width);

				edge.setName(src.getName() + "|>" + tgt.getName());
				edge2.setName(tgt.getName() + "|>" + src.getName());

				mb.connect(topology, src, edge , tgt);
				mb.connect(topology, tgt, edge2 , src);
			}
		}

		//printRouting(sim);
	}
}