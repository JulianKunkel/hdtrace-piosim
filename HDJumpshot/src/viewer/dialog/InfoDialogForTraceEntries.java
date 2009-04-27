
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of HDJumpshot.

//HDJumpshot is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//HDJumpshot is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


package viewer.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import topology.TopologyTraceTreeNode;
import topology.TopologyTreeNode;
import viewer.datatype.DatatypeView;
import de.hd.pvs.TraceFormat.project.CommunicatorInformation;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class InfoDialogForTraceEntries extends InfoDialog
{
	private static final long serialVersionUID = 1L;


	static private class InfoTableData{
		final String key;
		final Object value;

		public InfoTableData(String key, Object value) {
			this.key = key;
			this.value = value;
		}
	}

	final ArrayList<InfoTableData> tableData = new ArrayList<InfoTableData>();

	private void addTableData(String key, Object value){
		tableData.add(new InfoTableData(key, value));
	}
	
	private void addTableDataNested(String key, Object value){
		addTableData("  " + key, value);
	}

	public InfoDialogForTraceEntries( final Frame     frame, 
			final Epoch    clicked_time,
			final Epoch realModelTimeStart,
			TopologyTraceTreeNode topologyTreeNode, 
			TraceEntry obj)
	{
		super( frame, "TraceEntry Info Box", clicked_time, realModelTimeStart);

		final Container panel = this.getContentPane();
		panel.setLayout( new BoxLayout( panel,  BoxLayout.Y_AXIS ) );

		JLabel label = new JLabel(obj.getType().toString());
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		panel.add(label);        

		final JTable table = new JTable();
		panel.add(table); 


		setMinimumSize(new Dimension(300, 250));

		final Font notBold = new Font("SansSerif", Font.PLAIN, label.getFont().getSize());

		switch (obj.getType()){
		case STATE:
		case EVENT:
			break;
		}

		// try to parse communicator:
		final TopologyTreeNode rankNode = topologyTreeNode.getParentTreeNodeWithTopologyLabel("rank");
		if(rankNode != null){
			// got a rank:
			final Integer rank = Integer.parseInt(rankNode.getTopology().getText());						
			if (rank != null){
				addTableData("Rank" , rank);
				final ProjectDescription desc = rankNode.getFile().getProjectDescription();

				final String cids = obj.getAttribute("cid");
				if(cids != null){			
					final CommunicatorInformation comm = desc.getCommunicator(rank, Integer.parseInt(cids));

					addTableData("Communicator", comm.getMPICommunicator().getName());
					addTableDataNested("Local rank", comm.getLocalId());
				}

				// parse type information:				
				addDatatypeView("Datatype", rank, desc, obj.getAttribute("tid"), panel);
				addDatatypeView("File datatype", rank, desc, obj.getAttribute("filetid"), panel);
				addDatatypeView("Elementary file datatype", rank, desc, obj.getAttribute("etid"), panel);
			}
		}

		if(tableData.size() > 0){
			table.setModel(new AbstractTableModel() {
				private static final long serialVersionUID = -3419029365708534895L;
				
				public String getColumnName(int column) { return null; }
				public int getRowCount() { return tableData.size(); }
				public int getColumnCount() { return 2; }
				
				public Object getValueAt(int row, int col) { 
					if(col == 0)
						return tableData.get(row).key;
					else
						return tableData.get(row).value;
				}
				
				public boolean isCellEditable(int row, int column) { return false; }
				public void setValueAt(Object value, int row, int col) {}
			});
		}

		label = new JLabel("Contained XML data:");
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		panel.add(label);

		String text = obj.toString();

		JTextArea jtxt = new JTextArea(text);
		jtxt.setLineWrap(true);
		jtxt.setEditable(false);
		jtxt.setFont(notBold);
		jtxt.setBackground(Color.LIGHT_GRAY);

		panel.add(jtxt);

		panel.add( super.getCloseButtonPanel() );
	}

	private void addDatatypeView(String forWhat, int rank, ProjectDescription desc, String xmlStr, Container panel){
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

			Datatype type = typeMap.get(tid);
			
			if(type == null){
				System.err.println("Warning: type: " + tid + " not found for rank: " + rank);
				return;
			}
			DatatypeView view = new DatatypeView();
			view.setRootDatatype(type);

			panel.add(view.getRootComponent());
		}
	}
}
