package de.hd.pvs.piosim.simulator.network.routing;

import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.simulator.IModelToSimulatorMapper;
import de.hd.pvs.piosim.simulator.base.IGDynamicImplementationObject;

public interface INetworkRouteCreator<ModelType extends PaketRoutingAlgorithm> extends IGDynamicImplementationObject<ModelType> {

	/**
	 * Allow the
	 * @param networkTopology
	 * @param sim
	 */
	public void buildRoutingTable(INetworkTopology networkTopology, IModelToSimulatorMapper objs);
}
