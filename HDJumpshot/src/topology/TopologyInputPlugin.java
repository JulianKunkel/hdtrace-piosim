package topology;

import plugins.FilePlugin;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

abstract public class TopologyInputPlugin extends FilePlugin{
	
	private TopologyManager topologyManager; 
	
	/**
	 * Try to instantiate a object of the given type for a topology.
	 * Method shall be called exactly once for each (trace) topology.
	 * 
	 * @param topology
	 * @return null if not applicable to the given topology
	 */
	abstract public ITopologyInputPluginObject tryToInstantiateObjectFor(TopologyNode topo);
	
	/**
	 * The type the objects instantiated will have
	 * @return
	 */
	abstract public Class<? extends ITopologyInputPluginObject> getInstanciatedObjectsType();

	abstract public boolean tryToActivate(TopologyLabels labels); 
	
	@Override
	final public boolean tryToActivate(TraceFormatFileOpener file) {
		return tryToActivate(file.getTopologyLabels());
	}
	
	public TopologyManager getTopologyManager() {
		return topologyManager;
	}
	
	void setTopologyManager(TopologyManager topologyManager) {
		this.topologyManager = topologyManager;
	}
}
