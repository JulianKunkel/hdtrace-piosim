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
import javax.swing.JLabel;
import javax.swing.JPanel;

import viewer.datatype.UnrolledDatatypeView;
import viewer.dialog.traceEntries.InfoTableData;
import viewer.dialog.traceEntries.ResizeListener;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import viewer.timelines.topologyPlugins.MPIThreadInputPlugin.MPIThreadObject;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Print information for datatypes used in (file) I/O
 * 
 * @author Julian M. Kunkel
 */
public class FileOperationPlugin extends DatatypeViewPlugin{
	@Override
	public Color getColor() {
		return Color.RED;
	}

	@Override
	protected void ManufactureUI(ITraceEntry obj, MPIThreadObject pluginData,
			ProjectDescription desc, Epoch modelTimeOffsetToView,
			ResizeListener resizeListener, JPanel panel, InfoTableData textData) {
		// got a rank:
		final MPIRankObject rankObj = pluginData.getParentRankObject();
		final Integer rank = rankObj.getRank();

		// parse type information:				
		addDatatypeView("File datatype", rank, desc, obj.getAttribute("filetid"), resizeListener, panel);
		addDatatypeView("Elementary file datatype", rank, desc, obj.getAttribute("etid"),resizeListener, panel);				

		final String fidStr = obj.getAttribute("fid");

		if(fidStr != null){
			// it might be a file command.					
			final ITraceEntry fopen = rankObj.getPreviousFileOpen(obj.getEarliestTime(), fidStr);

			if(fopen == null){
				System.err.println("Warning no previous open found for fid: " + fidStr +  " t: " + obj.getEarliestTime());

				return;
			}

			textData.addSection("File operation", getColor());
			textData.addData("file name", fopen.getAttribute("name"));			

			final String sizeStr = obj.getAttribute("size");			
			if(sizeStr != null){
				textData.addData("size: ", sizeStr);
				final long size = Long.parseLong(sizeStr);
				final String offsetStr = obj.getAttribute("offset");
				
				final long offset = Long.parseLong(offsetStr);
				
				final ITraceEntry fview = rankObj.getPreviousFileSetView(obj.getEarliestTime(), fidStr);
				if(fview != null){
					addDatatypeView("File datatype", rank, desc, fview.getAttribute("filetid"), resizeListener, panel);
					
				}

				if(offsetStr != null){					
					final JLabel label = new JLabel("Accessed bytes:");
					label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
					panel.add(label);
					
					UnrolledDatatypeView unrolledView;
					
					if(fview != null){
						final HashMap<Long, Datatype> typeMap = desc.getDatatypeMap(rank);  
						if(typeMap == null){
							System.err.println("Type map not available for rank: " + rank);
							return;
						}
						final long tid = Long.parseLong(fview.getAttribute("filetid"));			

						final Datatype ftype = typeMap.get(tid);
						final Datatype etype = typeMap.get(Long.parseLong(fview.getAttribute("etid")));
												
						final long viewOffset = Long.parseLong(fview.getAttribute("offset"));

						unrolledView = new UnrolledDatatypeView(ftype, size,	offset * etype.getExtend()); 
						
						textData.addData("offset (after view)", offset * etype.getExtend() + viewOffset);				
						textData.addData("etype size, extend", etype.getSize() + ", " + etype.getExtend());
					}else{
						// no view set, therefore use null						
						unrolledView = new UnrolledDatatypeView(null, size, offset);
						textData.addData("offset", offset);						
					}
					panel.add(unrolledView.getScrollPane());
				}
			}
		}
	}
}
