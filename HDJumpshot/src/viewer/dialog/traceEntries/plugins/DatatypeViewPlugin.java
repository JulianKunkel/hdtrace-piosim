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
 * @author Julian M. Kunkel
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
