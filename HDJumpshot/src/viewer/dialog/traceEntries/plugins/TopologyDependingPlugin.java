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
 * @author julian
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
