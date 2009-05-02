package viewer.dialog.traceEntries.plugins;

import java.awt.Color;

import javax.swing.JPanel;

import topology.TopologyManager;
import topology.TopologyTreeNode;
import viewer.dialog.traceEntries.InfoTableData;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A plugin for the info dialog
 * @author julian
 */
public interface IInfoDialogPlugin {
	/**
	 * The list of known and instantiated plugins:
	 */	
	public static IInfoDialogPlugin [] availablePlugins = new IInfoDialogPlugin[] { 
		new CommunicatorPlugin(),
		new DatatypeViewPlugin(),
		new FileOperationPlugin()
		};
	
	/**
	 * Plugin color (to recognize)
	 */
	public Color getColor();
	
	public void ManufactureUI(
			TraceEntry obj, 
			TopologyManager manager, 
			Epoch modelTimeOffset,
			TopologyTreeNode topology,			
			JPanel panel, 
			InfoTableData textData);		
}
