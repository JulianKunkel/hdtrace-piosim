package de.hd.pvs.piosim.simulator.network.routing;

import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

abstract public class AGPaketRouting<ModelType extends PaketRoutingAlgorithm>
	implements INetworkRouteCreator<ModelType>, IPaketTopologyRouting
{
	ModelType paketRoutingAlgorithm;

	@Override
	final public ModelType getModelComponent() {
		return paketRoutingAlgorithm;
	}

	@Override
	final public void setModelComponent(ModelType comp) throws Exception {
		this.paketRoutingAlgorithm = comp;
	}
}
