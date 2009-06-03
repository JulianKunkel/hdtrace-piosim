//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package topology;

import plugins.FilePlugin;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
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

	abstract public boolean tryToActivate(TopologyTypes labels); 
	
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
