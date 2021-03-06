
/** Version Control Information $Id: LegendTable.java 469 2009-07-01 13:27:24Z kunkel $
 * @lastmodified    $Date: 2009-07-01 15:27:24 +0200 (Mi, 01. Jul 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 469 $ 
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

package de.viewer.legends;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class LegendTable extends JTable
{
	private static final long serialVersionUID = 996884107800805302L;

	private static final Insets EMPTY_INSETS  = new Insets( 0, 0, 0, 0 );

	final private LegendTableTraceModel    table_model;
	final private TableColumnModel    column_model;
	final private JTableHeader        table_header;

	private final UpdateTableModelListener myTableChangeListener = new UpdateTableModelListener();

	private class UpdateTableModelListener implements TableModelListener{
		@Override
		public void tableChanged(TableModelEvent e) {
			getTable().repaint();
		}
	}

	private LegendTable getTable(){
		return this;
	}

	public LegendTable(LegendTableTraceModel model)
	{
		super();

		this.table_model = model;
		super.setModel( table_model );

		super.setIntercellSpacing( new Dimension( 1, 1 ) );
		super.setDragEnabled(false);
		super.setShowHorizontalLines( false );
		super.setShowVerticalLines( true );

		column_model  = super.getColumnModel();
		table_header = this.getTableHeader();
		this.setColumnHeaderRenderers();
		this.initColumnSize();

		table_model.addTableModelListener(myTableChangeListener);
		super.setDefaultRenderer( CategoryIcon.class, new CategoryIconRenderer() );
		super.setDefaultEditor( CategoryIcon.class,  new CategoryIconEditor() );
	}

	private void setColumnHeaderRenderers()
	{
		TableColumn        column; 
		TableCellRenderer  renderer;
		JPopupMenu         pop_menu;
		MouseAdapter       handler;
		Class<?>              class_type;
		int                column_count;

		column_count  = table_model.getColumnCount();
		for ( int icol = 0; icol < column_count; icol++ ) {
			column     = column_model.getColumn( icol );
			renderer   = column.getHeaderRenderer();
			class_type = table_model.getColumnClass( icol );
			if ( class_type == Boolean.class ) {
				renderer = new GenericHeaderRenderer( this, icol );
				((GenericHeaderRenderer) renderer).initPressablePullDownTab();
				column.setHeaderRenderer( renderer );

				pop_menu = new OperationBooleanMenu( this, icol);
				handler  = new TableHeaderHandler( this, icol, pop_menu );
				table_header.addMouseListener( handler );
				handler  = new TableColumnHandler( this, icol, pop_menu );
				this.addMouseListener( handler );
			}
			
			if(class_type == TablePopupHandler.class){
				renderer = new GenericHeaderRenderer( this, icol );
				column.setHeaderRenderer( renderer );
				handler = new TablePopupHandler(this, icol);
				this.addMouseListener(handler);
				( (JComponent) renderer).setToolTipText(table_model.getColumnToolTip( icol ) );
			}

			if ( class_type == String.class ) {
				renderer = new GenericHeaderRenderer( this, icol );
				((GenericHeaderRenderer) renderer).initPressablePullDownTab();
				column.setHeaderRenderer( renderer );

				pop_menu = new OperationStringMenu( this, icol );
				handler  = new TableHeaderHandler( this, icol, pop_menu );
				table_header.addMouseListener( handler );
				
				handler  = new TableColumnHandler( this, icol, pop_menu );
				this.addMouseListener( handler );
			}else if ( renderer == null ) {
				renderer = new GenericHeaderRenderer( this, icol );
				column.setHeaderRenderer( renderer );
			}else{
				( (JComponent) renderer).setToolTipText(table_model.getColumnToolTip( icol ) );
			}
		}
	}

	private void initColumnSize()
	{
		TableCellRenderer  renderer;
		Component          component;
		TableColumn        column; 
		Dimension          intercell_gap;
		Dimension          header_size;
		Insets             header_insets;
		int                header_width;
		Dimension          cell_size;
		Insets             cell_insets;
		int                cell_width, cell_height, row_height;
		int                column_count, row_count;
		int                vport_width, vport_height;

		vport_width    = 0;
		vport_height   = 0;

		row_height     = 0;
		intercell_gap  = super.getIntercellSpacing();
		column_count   = table_model.getColumnCount();
		for ( int icol = 0; icol < column_count; icol++ ) {
			column        = column_model.getColumn( icol );
			// determine header renderer's size
			renderer      = column.getHeaderRenderer();
			if(renderer == null) 
				continue;
			component     = renderer.getTableCellRendererComponent( this,
					column.getHeaderValue(),
					false, false, -1, icol );
			header_size   = component.getPreferredSize();
			header_insets = ( (JComponent) component ).getInsets();
			header_width  = header_size.width + intercell_gap.width
			+ header_insets.left + header_insets.right;
			// determine cell renderer's size
			renderer     = column.getCellRenderer();
			if ( renderer == null )
				renderer = super.getDefaultRenderer(table_model.getColumnClass( icol ) );
			component   = renderer.getTableCellRendererComponent( this,
					null, 
					false, false, 0, icol );
			cell_size   = component.getPreferredSize();
			// cell_insets = ( (JComponent) component ).getInsets();
			if ( component instanceof CategoryIconRenderer )
				cell_insets = ( (JComponent) component ).getInsets();
			else
				cell_insets = EMPTY_INSETS;
			cell_width   = cell_size.width
			+ cell_insets.left + cell_insets.right;

			if ( cell_width > header_width ) {
				column.setPreferredWidth( cell_width );                
				vport_width  += cell_width;
			}
			else {
				column.setPreferredWidth( header_width );
				vport_width  += header_width;
			}

			// fixate column width of static columns
			if(icol != LegendTableTraceModel.NAME_COLUMN){
				column.setMaxWidth(column.getPreferredWidth());            	
			}
			column.setMinWidth(column.getPreferredWidth());         

			cell_height   = cell_size.height + cell_insets.top + cell_insets.bottom + 15;
			if ( cell_height > row_height )
				row_height  = cell_height;
		}
		super.setRowHeight( row_height );

		row_count     = table_model.getRowCount();
		if ( row_count > LegendConst.LIST_MAX_VISIBLE_ROW_COUNT )
			vport_height  = row_height * LegendConst.LIST_MAX_VISIBLE_ROW_COUNT;
		else
			vport_height  = row_height * row_count;
		super.setPreferredScrollableViewportSize(
				new Dimension( vport_width, vport_height ) );
	}
}
