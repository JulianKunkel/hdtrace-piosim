package viewer.dialog.traceEntries.plugins;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import viewer.datatype.DatatypeView;
import viewer.dialog.traceEntries.InfoTableData;
import viewer.dialog.traceEntries.ResizeListener;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import viewer.timelines.topologyPlugins.MPIThreadInputPlugin.MPIThreadObject;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Print information for datatypes (i.e. recursivly unroll datatype)
 * @author julian
 *
 */
public class DatatypeViewPlugin extends TopologyDependingPlugin<MPIThreadObject>{
	@Override
	public Color getColor() {
		return Color.BLUE;
	}

	@Override
	protected Class<MPIThreadObject> getDependingTopologyPluginType() {	
		return MPIThreadObject.class;
	}

	@Override
	protected void ManufactureUI(ITraceEntry obj, MPIThreadObject pluginData,
			ProjectDescription desc, Epoch modelTimeOffsetToView,
			ResizeListener resizeListener, JPanel panel, InfoTableData textData) {
		// got a rank:
		final MPIRankObject rankObj = pluginData.getParentRankObject();
		final Integer rank = rankObj.getRank();

		// parse type information:				
		addDatatypeView("Memory Datatype", rank, desc, obj.getAttribute("tid"), resizeListener, panel);
	}


	protected void addDatatypeView(String forWhat, int rank, ProjectDescription desc, 
			String xmlStr, ResizeListener resizeListener, JPanel panel){
		if(xmlStr != null){
			final JLabel label = new JLabel(forWhat);
			label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			panel.add(label);

			final HashMap<Long, Datatype> typeMap = desc.getDatatypeMap(rank);  
			if(typeMap == null){
				System.err.println("Type map not available for rank: " + rank);
				return;
			}
			final long tid = Long.parseLong(xmlStr);			

			final Datatype type = typeMap.get(tid);

			if(type == null){
				System.err.println("Warning: type: " + tid + " not found for rank: " + rank);
				return;
			}
			final DatatypeView view = new DatatypeView();
			view.setRootDatatype(type);

			view.setDatatypeViewChangeListener(resizeListener);
			
			panel.add(view.getRootComponent());
		}
	}
}
