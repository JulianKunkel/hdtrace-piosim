package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNetworkNode;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SBasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

/**
 * Simulates a unidirectional bus-system i.e. a single upstream channel to use one of
 * several possible channels at a time.
 *
 * @author julian
 */
public class GStoreUndForwardNode extends SBasicComponent<StoreForwardNetworkNode> implements IGNetworkNode<StoreForwardNetworkNode>{
	IPaketTopologyRouting topology;

	@Override
	public void setPaketRouting(IPaketTopologyRouting routing) {
		this.topology = routing;
	}

	@Override
	public ComponentRuntimeInformation getComponentInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processEvent(Event event, Epoch time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processInternalEvent(InternalEvent event, Epoch time) {
		// TODO Auto-generated method stub

	}
}
