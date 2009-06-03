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

import java.awt.Color;

import javax.swing.JPanel;

import viewer.dialog.traceEntries.InfoTableData;
import viewer.dialog.traceEntries.ResizeListener;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import viewer.timelines.topologyPlugins.MPIThreadInputPlugin.MPIThreadObject;
import de.hd.pvs.TraceFormat.project.CommunicatorInformation;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
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
	protected void ManufactureUI(ITraceEntry obj, MPIThreadObject pluginData,
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
