package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.LinkedList;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.Router.Router;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;

/**
 * Test the I/O redirectors.
 *
 * @author julian
 */
public class IORedirectionTest extends IOTest {

	private void initRedirection() throws Exception{
		final IORedirection redirection = new IORedirection();
		final Model m = mb.getModel();
		m.addIORedirectionLayer(redirection);

		final Router router = new Router();
		router.setName("Router");

		final ClientProcess cp = model.getClientProcesses().get(0);

		final NIC cpNIC = cp.getNetworkInterface();

		final NIC nic = (NIC) mb.cloneFromTemplate( cpNIC.getTemplate() );
		nic.setName("RouterN" + nic.getName());
		router.setNetworkInterface(nic);

		mb.addRouter(cp.getParentComponent(), router);

		// now interconnect the components similar to the ClientProcess.
		final LinkedList<INetworkEdge> edges = topology.getEdges(cpNIC);

		final INetworkEdge firstEdge = edges.get(0);
		final INetworkNode switchNode = topology.getEdgeTarget(firstEdge);


		NetworkEdge e1 = (NetworkEdge) mb.cloneFromTemplate(firstEdge.getTemplate());
		NetworkEdge e2 = (NetworkEdge) mb.cloneFromTemplate(firstEdge.getTemplate());
		e1.setName("Router_TX" + e1.getName());
		e2.setName("Router_RX" + e2.getName());
		mb.connect(topology, nic, e1, switchNode);
		mb.connect(topology, switchNode, e2, nic);

		for(ClientProcess client : m.getClientProcesses()){
			redirection.addModifyingComponent(client.getIdentifier().getID());
		}

		for (Server s: m.getServers()) {
			redirection.addRedirect(s.getIdentifier().getID(), router.getIdentifier().getID());
		}
	}

	@Override
	protected void setupOneNodeOneServer(int smtClients, ServerCacheLayer cacheLayer) throws Exception {
		super.setupOneNodeOneServer(smtClients, cacheLayer);
		initRedirection();
	}
}
