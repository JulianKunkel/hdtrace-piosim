package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareExtensions;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Create a single I/O server and interconnect it with the given node.
 * @author julian
 */
public class PluginIOServer implements HardwareExtension {
	final Server serverTemplate;
	final IOSubsystem ioTemplate;
	final NIC  nicTemplate;
	final ServerCacheLayer cacheLayerTemplate;
	final NetworkEdge edgeTemplate;

	public PluginIOServer(Server serverTemplate,
			NIC  nicTemplate,
			IOSubsystem ioTemplate,
			ServerCacheLayer cacheLayerTemplate,
			NetworkEdge edgeTemplate)
	{
		this.nicTemplate = nicTemplate;
		this.serverTemplate = serverTemplate;
		this.ioTemplate = ioTemplate;
		this.cacheLayerTemplate = cacheLayerTemplate;
		this.edgeTemplate = edgeTemplate;
	}

	@Override
	public void extendNetworkNode(String prefix, NetworkNode nodeToInterconnect,  ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(serverTemplate);
		mb.addTemplateIf(ioTemplate);
		mb.addTemplateIf(cacheLayerTemplate);
		mb.addTemplateIf(edgeTemplate);
		mb.addTemplateIf(nicTemplate);

		final Server s = mb.cloneFromTemplate(serverTemplate);
		final IOSubsystem io = mb.cloneFromTemplate(ioTemplate);
		final ServerCacheLayer cache = mb.cloneFromTemplate(cacheLayerTemplate);
		final NIC nic = mb.cloneFromTemplate(nicTemplate);

		final NetworkEdge e1 = mb.cloneFromTemplate(edgeTemplate);
		final NetworkEdge e2 = mb.cloneFromTemplate(edgeTemplate);

		s.setCacheImplementation(cache);
		s.setIOsubsystem(io);
		s.setNetworkInterface(nic);

		s.setName(prefix + "S" + serverTemplate.getName());
		cache.setName(prefix + "C" + cacheLayerTemplate.getName());
		io.setName(prefix + "IO" + ioTemplate.getName());
		nic.setName(prefix + "N" + nicTemplate.getName());
		e1.setName(prefix + "_TX" + edgeTemplate.getName());
		e2.setName(prefix + "_RX" + edgeTemplate.getName());

		mb.connect(topology, nic, e1, nodeToInterconnect);
		mb.connect(topology, nodeToInterconnect, e2, nic);
	}

}
