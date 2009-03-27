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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import viewer.common.Const;
/*
   Class to simulate a JMenuBar header editor for a JTable with String value
 */
public class OperationStringMenu extends JPopupMenu
{
	private static String     index_order_icon_path	= Const.IMG_PATH	+ "checkbox/IndexOrder.gif";
	private static String     case_sensitive_topo_order_icon_path	= Const.IMG_PATH	+ "checkbox/CaseSensitiveTopoOrder.gif";
	private static String     case_insensitive_topo_order_icon_path	= Const.IMG_PATH	+ "checkbox/CaseInsensitiveTopoOrder.gif";
	private static String     case_sensitive_order_icon_path	= Const.IMG_PATH	+ "checkbox/CaseSensitiveOrder.gif";
	private static String     case_insensitive_order_icon_path	= Const.IMG_PATH	+ "checkbox/CaseInsensitiveOrder.gif";
	private static String     reverse_order_icon_path	= Const.IMG_PATH	+ "checkbox/ReverseOrder.gif";

	private JTable            table_view;
	private LegendTableModel  table_model;
	private int               str_column;  // index where String.class is

	public OperationStringMenu( JTable in_table, int in_column )
	{
		super();
		table_view  = in_table;
		table_model = (LegendTableModel) table_view.getModel();
		str_column  = in_column;

		super.setLabel( table_model.getColumnName( str_column ) );
		super.setToolTipText( table_model.getColumnToolTip( str_column ) );
		this.addMenuItems();
	}

	private void addMenuItems()
	{
		JMenuItem  menu_item;
		Icon       icon;

		icon = new ImageIcon( case_sensitive_topo_order_icon_path );
		
		menu_item = new JMenuItem( "A...Z a...z", icon );
		menu_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{ table_model.arrangeOrder(
					LegendComparators.CASE_SENSITIVE_ORDER ); }
		} );

		menu_item = new JMenuItem( "z...a Z...A", icon );
		menu_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{ table_model.reverseOrder(
					LegendComparators.CASE_SENSITIVE_ORDER );
			}
		} );
		super.add( menu_item );
		
		menu_item = new JMenuItem( "Aa...Zz", icon );
		menu_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{ table_model.arrangeOrder(
					LegendComparators.CASE_INSENSITIVE_ORDER ); }
		} );
		super.add( menu_item );

		menu_item = new JMenuItem( "zZ...aA", icon );
		menu_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{ table_model.reverseOrder(
					LegendComparators.CASE_INSENSITIVE_ORDER );
			}
		} );
		super.add( menu_item );

		icon = new ImageIcon( index_order_icon_path );

		menu_item   = new JMenuItem( "Topology aA..zZ", icon );
		menu_item.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent evt )
					{ table_model.arrangeOrder( LegendComparators.TOPOLOGY_NAME_ORDER ); }
				} );
		super.add( menu_item );

		menu_item   = new JMenuItem( "Topology zZ..aA", icon );
		menu_item.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent evt )
					{ table_model.reverseOrder( LegendComparators.TOPOLOGY_NAME_ORDER ); }
				} );
		super.add( menu_item );
	}
}
