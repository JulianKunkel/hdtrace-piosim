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

package de.viewer.dialog.traceEntries.plugins;

import java.awt.Color;

import javax.swing.JPanel;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyManager;
import de.topology.TopologyTreeNode;
import de.viewer.dialog.traceEntries.InfoTableData;
import de.viewer.dialog.traceEntries.ResizeListener;

/**
 * A plugin for the info dialog
 * @author Julian M. Kunkel
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
			ITraceEntry obj, 
			TopologyManager manager, 
			Epoch modelTimeOffsetToView,
			TopologyTreeNode topology,			
			ResizeListener resizeListener, 
			JPanel panel, InfoTableData textData);		
}
