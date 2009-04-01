
/** Version Control Information $Id: TableColumnHandler.java 149 2009-03-27 13:55:56Z kunkel $
 * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 149 $ 
 */

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


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package viewer.legends;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;



public class TablePopupHandler extends MouseAdapter implements ActionListener
{
	final private JPopupMenu        pop_menu;

	final private JTable            table_view;
	final private JTableHeader      table_header;
	final private int               the_column;
	final LegendTableTraceModel     model;
	
	/**
	 * The last row the mouse was clicked
	 */
	int lastClickedRow = -1;
	
	public TablePopupHandler( LegendTable in_table, int in_column )
	{
		super();
		table_view    = in_table;
		table_header  = table_view.getTableHeader();
		the_column    = in_column;
		pop_menu      = new JPopupMenu();


		model = (LegendTableTraceModel) table_view.getModel();;

		final Object[] values = model.getColumnAlternatives(in_column);

		if(values != null){
			for(Object val: values){
				final JMenuItem item = new JMenuItem(val.toString());
				pop_menu.add(item);
				item.addActionListener(this);
			}
		}
		
	}
	
	/** when an option gets selected */
	@Override
	public void actionPerformed(ActionEvent e) {
		model.setValueAt(e.getActionCommand(), lastClickedRow, the_column);
	}

	/*
        MouseAdapter interface
	 */
	 public void mousePressed( MouseEvent evt )
	{
		Point      click;
		int        click_column, model_column;

		if ( ! SwingUtilities.isLeftMouseButton( evt ) )
			return;

		click        = evt.getPoint();
		click_column = table_header.columnAtPoint( click );
		model_column = table_view.convertColumnIndexToModel( click_column );
		
		int row = table_view.rowAtPoint(evt.getPoint());
		lastClickedRow = table_view.convertRowIndexToModel(row);
		
		if ( model_column == the_column ) {
			pop_menu.show( evt.getComponent(), evt.getX(), evt.getY() );
		}
	}
}
