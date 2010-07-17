package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;

/**
 * Create a single I/O server node and interconnect it with the given network.
 * @author julian
 */
public class IOServerCreator {
	final Server serverTemplate;
	final IOSubsystem ioTemplate;
	final ServerCacheLayer cacheLayerTemplate;

	public IOServerCreator(Server serverTemplate,
			IOSubsystem ioTemplate,
			ServerCacheLayer cacheLayerTemplate)
	{
		this.serverTemplate = serverTemplate;
		this.ioTemplate = ioTemplate;
		this.cacheLayerTemplate = cacheLayerTemplate;
	}

	public Server createServer(String prefix, ModelBuilder mb) throws Exception{
		mb.addTemplateIf(serverTemplate);
		mb.addTemplateIf(ioTemplate);
		mb.addTemplateIf(cacheLayerTemplate);

		final Server s = mb.cloneFromTemplate(serverTemplate);
		final IOSubsystem io = mb.cloneFromTemplate(ioTemplate);
		final ServerCacheLayer cache = mb.cloneFromTemplate(cacheLayerTemplate);

		s.setName(prefix + "S" + serverTemplate.getName());
		cache.setName(prefix + "CACHE" + cacheLayerTemplate.getName());
		io.setName(prefix + "IOSUB" + ioTemplate.getName());

		s.setCacheImplementation(cache);
		s.setIOsubsystem(io);
		io.setParentComponent(s);
		cache.setParentComponent(s);

		return s;
	}

}
