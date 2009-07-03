
/** Version Control Information $Id: InfoDialogForTraceEntries.java 406 2009-06-16 14:18:45Z kunkel $
 * @lastmodified    $Date: 2009-06-16 16:18:45 +0200 (Di, 16. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 406 $ 
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


package de.viewer.dialog.traceEntries;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.topology.TopologyManager;
import de.topology.TopologyTreeNode;
import de.viewer.dialog.InfoDialog;
import de.viewer.dialog.traceEntries.InfoTableData.InfoTableListData;
import de.viewer.dialog.traceEntries.plugins.IInfoDialogPlugin;

public class InfoDialogForTraceEntries extends InfoDialog implements ResizeListener
{
	private static final long serialVersionUID = 1L;
	
	public InfoDialogForTraceEntries( final Frame     frame,
			final Epoch clickedTime,			
			final Epoch modelTimeDiff,
			TopologyTreeNode topologyTreeNode,
			TopologyManager manager,
			final ITraceEntry obj)
	{
		super( frame, "TraceEntry Info Box", clickedTime, modelTimeDiff);

		final JPanel panel = (JPanel) this.getContentPane();
		panel.setLayout( new BoxLayout( panel,  BoxLayout.Y_AXIS ) );

		final JTable table = new JTable();
		panel.add(table); 

		final InfoTableData tableData = new InfoTableData();

		tableData.addSection("Trace entry", Color.WHITE);
		tableData.addData("Name", obj.getName());
		tableData.addData("Start [t]", obj.getEarliestTime().subtract(modelTimeDiff).toString());
		tableData.addData("Duration [t]", obj.getLatestTime().subtract(obj.getEarliestTime()).toString());

		setMinimumSize(new Dimension(300, 250));		

		// scan and activate plugins:
		for(IInfoDialogPlugin plugin: IInfoDialogPlugin.availablePlugins){
			plugin.ManufactureUI(obj, manager, modelTimeDiff, topologyTreeNode, this, panel, tableData);
		}

		if(tableData.getTableData().size() > 0){
			// create an array list!
			final ArrayList<InfoTableData.InfoTableListData> tableDataArray = new ArrayList<InfoTableListData>(tableData.getTableData());

			table.setModel(new AbstractTableModel() {
				private static final long serialVersionUID = -3419029365708534895L;

				public String getColumnName(int column) { return null; }
				public int getRowCount() { return tableDataArray.size(); }
				public int getColumnCount() { return 2; }

				public Object getValueAt(int row, int col) { 
					if(col == 0)
						return tableDataArray.get(row).key;
					else
						return tableDataArray.get(row).value;
				}

				public boolean isCellEditable(int row, int column) { return false; }
				public void setValueAt(Object value, int row, int col) {}
			});
			// set the color renderer:
			table.setDefaultRenderer(Object.class, new TableCellRenderer(){
				@Override
				public JLabel getTableCellRendererComponent(JTable table,
						Object string, boolean isSelected, boolean hasFocus,
						int row, int column) {

					final JLabel label = new JLabel(string.toString());

					label.setBackground(tableDataArray.get(row).color);
					label.setOpaque(true);

					return label;
				}
			});
		}

		JLabel label = new JLabel("Contained XML data:");
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		panel.add(label);

		// add XML formatted
		final StyleContext sc = new StyleContext();
		final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
		final JTextPane textPane = new JTextPane(doc);
		
		createXMLStyles(sc);

		addXMLTextRecursivly(textPane, sc, (XMLTag) obj, 0);        

		textPane.setEditable(false);
		final JScrollPane scroller = new JScrollPane(textPane);
		scroller.setMinimumSize(new Dimension(150, 50));
		panel.add(scroller);

		panel.add( super.getCloseButtonPanel() );		
	}

	@Override
	public void layoutRefreshed() {
		if(this.isVisible()){
			this.setSize(getPreferredSize());
		}
	}
	
	private static void addXMLTextRecursivly(JTextPane pane, StyleContext sc,
			XMLTag tag, int nesting) 
	{
		// recurisvly add tag:		
		pane.setCharacterAttributes(sc.getStyle("tag"), true);		
		for(int i=0; i < nesting; i++){
			pane.replaceSelection("  ");
		}
		pane.replaceSelection("<" + tag.getName());
		// add attributes
		for (String key: tag.getAttributes().keySet()){
			pane.setCharacterAttributes(sc.getStyle("key"), true);
			pane.replaceSelection(" " + key);
			
			pane.setCharacterAttributes(sc.getStyle("neutral"), true);
			pane.replaceSelection("=\"");
			
			pane.setCharacterAttributes(sc.getStyle("value"), true);
			pane.replaceSelection(tag.getAttribute(key));
			
			pane.setCharacterAttributes(sc.getStyle("neutral"), true);
			pane.replaceSelection("\"");
		}
		pane.setCharacterAttributes(sc.getStyle("tag"), true);		
		
		if(tag.getNestedXMLTags() != null ){
			pane.replaceSelection(">\n");
			
			for (XMLTag child: tag.getNestedXMLTags()){
				addXMLTextRecursivly(pane, sc, child, nesting + 1);
				pane.replaceSelection("\n");
			}
			
			pane.setCharacterAttributes(sc.getStyle("tag"), true);		
			for(int i=0; i < nesting; i++){
				pane.replaceSelection("  ");
			}
			pane.replaceSelection("</" + tag.getName() + ">\n");	
		}else{
			pane.replaceSelection("/>");
		}
	}

	private static void createXMLStyles(StyleContext sc) {  
		// Create and add the constant width style
		Style cwStyle = sc.addStyle("value", null);
		StyleConstants.setFontFamily(cwStyle, "monospaced");
		StyleConstants.setForeground(cwStyle, Color.GREEN.darker());

		cwStyle = sc.addStyle("key", null);
		StyleConstants.setFontFamily(cwStyle, "monospaced");
		StyleConstants.setForeground(cwStyle, Color.BLUE);

		Style heading2Style = sc.addStyle("neutral", null);
		StyleConstants.setForeground(heading2Style, Color.BLACK);
		StyleConstants.setFontFamily(heading2Style, "monospaced");
		
		// Create and add the heading style
		heading2Style = sc.addStyle("tag", null);
		StyleConstants.setForeground(heading2Style, Color.BLACK);
		//StyleConstants.setFontSize(heading2Style, 16);
		StyleConstants.setFontFamily(heading2Style, "monospaced");
		StyleConstants.setBold(heading2Style, true);
		//StyleConstants.setLeftIndent(heading2Style, 8);
		//StyleConstants.setFirstLineIndent(heading2Style, 0);
	}

}
