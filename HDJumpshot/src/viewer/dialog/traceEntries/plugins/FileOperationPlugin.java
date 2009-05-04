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
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Print information for datatypes used in (file) I/O
 * 
 * @author julian
 */
public class FileOperationPlugin extends DatatypeViewPlugin{
	@Override
	public Color getColor() {
		return Color.RED;
	}

	@Override
	protected void ManufactureUI(TraceEntry obj, MPIThreadObject pluginData,
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
			final TraceEntry fopen = rankObj.getPreviousFileOpen(obj.getEarliestTime(), fidStr);

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
				
				final TraceEntry fview = rankObj.getPreviousFileSetView(obj.getEarliestTime(), fidStr);
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
