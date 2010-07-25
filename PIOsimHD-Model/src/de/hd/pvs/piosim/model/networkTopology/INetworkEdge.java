package de.hd.pvs.piosim.model.networkTopology;


public interface INetworkEdge  extends INetworkFlowComponent{
	public void setTopology(INetworkTopology topology);
	public INetworkTopology getTopology();
}
