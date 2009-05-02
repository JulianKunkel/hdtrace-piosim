package viewer.dialog.traceEntries.plugins;

import java.awt.Color;

import javax.swing.JPanel;

import viewer.dialog.traceEntries.InfoTableData;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import viewer.timelines.topologyPlugins.MPIThreadInputPlugin.MPIThreadObject;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
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
			ProjectDescription desc, Epoch realModelTimeStart,
			JPanel panel, InfoTableData textData) {
		// got a rank:
		final MPIRankObject rankObj = pluginData.getParentRankObject();
		final Integer rank = rankObj.getRank();

		// parse type information:				
		addDatatypeView("File datatype", rank, desc, obj.getAttribute("filetid"), panel);
		addDatatypeView("Elementary file datatype", rank, desc, obj.getAttribute("etid"), panel);				

		final String fidStr = obj.getAttribute("fid");
		if(fidStr != null){
			// it might be a file command.					
			final TraceEntry fopen = rankObj.getPreviousFileOpen(realModelTimeStart, fidStr);
			if(fopen == null){
				return;
			}
			
			textData.addSection("File operation", getColor());
			textData.addData("file name", fopen.getAttribute("name"));			

			final String sizeStr = obj.getAttribute("size");
			if(sizeStr != null){
				textData.addData("size: ", sizeStr);

				final TraceEntry fview = rankObj.getPreviousFileSetView(realModelTimeStart, fidStr);
				if(fview != null){
					addDatatypeView("File datatype", rank, desc, fview.getAttribute("filetid"), panel);	
				}
			}
		}
	}
}
