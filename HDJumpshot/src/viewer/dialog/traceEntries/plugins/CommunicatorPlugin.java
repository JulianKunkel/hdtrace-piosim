package viewer.dialog.traceEntries.plugins;

import java.awt.Color;

import javax.swing.JPanel;

import viewer.dialog.traceEntries.InfoTableData;
import viewer.dialog.traceEntries.ResizeListener;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import viewer.timelines.topologyPlugins.MPIThreadInputPlugin.MPIThreadObject;
import de.hd.pvs.TraceFormat.project.CommunicatorInformation;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class CommunicatorPlugin extends TopologyDependingPlugin<MPIThreadObject>{
	@Override
	public Color getColor() {
		return Color.LIGHT_GRAY;
	}	

	@Override
	protected Class<MPIThreadObject> getDependingTopologyPluginType() {	
		return MPIThreadObject.class;
	}

	@Override
	protected void ManufactureUI(TraceEntry obj, MPIThreadObject pluginData,
			ProjectDescription description, Epoch realModelTimeStart,
			ResizeListener resizeListener, JPanel panel, InfoTableData textData) {	
		// got a rank:
		final MPIRankObject rankObj = pluginData.getParentRankObject();
		final Integer rank = rankObj.getRank();

		final String cids = obj.getAttribute("cid");
		if(cids != null){			
			final CommunicatorInformation comm = description.getCommunicator(rank, Integer.parseInt(cids));
			textData.addSection("Communicator", getColor());
			textData.addData("Name", comm.getMPICommunicator().getName());
			textData.addData("Global rank" , rank);
			textData.addData("Local rank", comm.getLocalId());			
		}
	}
}
