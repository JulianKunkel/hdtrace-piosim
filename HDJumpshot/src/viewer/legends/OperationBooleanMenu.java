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
 Class to simulate a JMenuBar header editor for a JTable with boolean value
 */
public class OperationBooleanMenu extends JPopupMenu {
	private static String toggle_selected_icon_path = Const.IMG_PATH
			+ "checkbox/ToggleSelected.gif";
	private static String enable_selected_icon_path = Const.IMG_PATH
			+ "checkbox/EnableSelected.gif";
	private static String disable_selected_icon_path = Const.IMG_PATH
			+ "checkbox/DisableSelected.gif";
	private static String toggle_all_icon_path = Const.IMG_PATH
			+ "checkbox/ToggleAll.gif";
	private static String enable_all_icon_path = Const.IMG_PATH
			+ "checkbox/EnableAll.gif";
	private static String disable_all_icon_path = Const.IMG_PATH
			+ "checkbox/DisableAll.gif";

	private JTable table_view;
	private LegendTableModel table_model;
	private int bool_column; // index where Boolean.class is

	public OperationBooleanMenu(JTable in_table, int in_column) {
		super();
		table_view = in_table;
		table_model = (LegendTableModel) table_view.getModel();
		bool_column = in_column;

		super.setLabel(table_model.getColumnName(bool_column));
		super.setToolTipText(table_model.getColumnToolTip(bool_column));
		this.addMenuItems();
	}

	private void addMenuItems() {
		JMenuItem menu_item;
		Icon icon;
		icon = new ImageIcon(toggle_selected_icon_path);
		menu_item = new JMenuItem("Toggle Selected", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				toggleSelectedAtColumn(bool_column);
			}
		});
		super.add(menu_item);


		icon = new ImageIcon(enable_selected_icon_path);
		
		menu_item = new JMenuItem("Enable Selected", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setSelectedAtColumn(bool_column, Boolean.TRUE);
			}
		});
		super.add(menu_item);

		icon = new ImageIcon(disable_selected_icon_path);
		menu_item = new JMenuItem("Disable Selected", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setSelectedAtColumn(bool_column, Boolean.FALSE);
			}
		});
		super.add(menu_item);

		icon = new ImageIcon(disable_selected_icon_path);
		
		menu_item = new JMenuItem("Toggle All", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				toggleAllAtColumn(bool_column);
			}
		});
		super.add(menu_item);

		icon = new ImageIcon(enable_all_icon_path);
		menu_item = new JMenuItem("Enable All", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setAllAtColumn(bool_column, Boolean.TRUE);
			}
		});
		super.add(menu_item);

		icon = new ImageIcon(disable_all_icon_path);
		menu_item = new JMenuItem("Disable All", icon);
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setAllAtColumn(bool_column, Boolean.FALSE);
			}
		});
		super.add(menu_item);
	}

	/*
	 * 4 private methods for addMenuItem()
	 */
	private void toggleSelectedAtColumn(int icolumn) {
		int[] irows;
		int irows_length, irow, idx;
		Boolean bval;

		irows = table_view.getSelectedRows();
		irows_length = irows.length;
		for (idx = 0; idx < irows_length; idx++) {
			irow = irows[idx];
			bval = (Boolean) table_model.getValueAt(irow, icolumn);
			if (bval.booleanValue())
				table_model.setValueAt(Boolean.FALSE, irow, icolumn);
			else
				table_model.setValueAt(Boolean.TRUE, irow, icolumn);
		}
	}

	private void setSelectedAtColumn(int icolumn, Boolean bval) {
		int[] irows;
		int irows_length, irow, idx;

		irows = table_view.getSelectedRows();
		irows_length = irows.length;
		for (idx = 0; idx < irows_length; idx++) {
			irow = irows[idx];
			table_model.setValueAt(bval, irow, icolumn);
		}
	}

	private void toggleAllAtColumn(int icolumn) {
		int irows_length, irow;
		Boolean bval;

		irows_length = table_model.getRowCount();
		for (irow = 0; irow < irows_length; irow++) {
			bval = (Boolean) table_model.getValueAt(irow, icolumn);
			if (bval.booleanValue())
				table_model.setValueAt(Boolean.FALSE, irow, icolumn);
			else
				table_model.setValueAt(Boolean.TRUE, irow, icolumn);
		}
	}

	private void setAllAtColumn(int icolumn, Boolean bval) {
		int irows_length, irow;

		irows_length = table_model.getRowCount();
		for (irow = 0; irow < irows_length; irow++) {
			table_model.setValueAt(bval, irow, icolumn);
		}
	}
}
