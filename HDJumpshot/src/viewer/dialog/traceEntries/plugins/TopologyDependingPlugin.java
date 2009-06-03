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

package viewer.dialog.traceEntries.plugins;

import javax.swing.JPanel;

import topology.ITopologyInputPluginObject;
import topology.TopologyManager;
import topology.TopologyTreeNode;
import viewer.dialog.traceEntries.InfoTableData;
import viewer.dialog.traceEntries.ResizeListener;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Such a plugin depends on exactly one input topology plugin.
 * If such a plugin is set then the plugin gets executed. 
 *  
 * @author Julian M. Kunkel
 */
abstract public class TopologyDependingPlugin<TYPE extends ITopologyInputPluginObject> implements IInfoDialogPlugin{
	
	abstract protected Class<TYPE> getDependingTopologyPluginType();
	
	abstract protected void ManufactureUI(ITraceEntry obj, TYPE pluginData,
			ProjectDescription description,
			Epoch modelTimeOffsetToView, 
			ResizeListener resizeListener, 
			JPanel panel,
			InfoTableData textData);
	
	@Override
	final public void ManufactureUI(ITraceEntry obj, TopologyManager manager,
			Epoch modelTimeOffsetToView, TopologyTreeNode topology, ResizeListener resizeListener,
			JPanel panel, InfoTableData textData) 
	{
		final TYPE pluginObj = manager.getPluginObjectForTopology(topology.getTopology(), getDependingTopologyPluginType());
		if(pluginObj != null){
			// plugin shall be activated:
			ManufactureUI(obj, pluginObj, topology.getFile().getProjectDescription(), 
					modelTimeOffsetToView, resizeListener, panel, textData);
		}
	}
}
