
 /** Version Control Information $Id: TableHeaderHandler.java 149 2009-03-27 13:55:56Z kunkel $
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

package de.viewer.legends;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/*
   Class to simulate a JMenuBar header editor for a JTable with boolean value
*/
public class TableHeaderHandler extends MouseAdapter
{
    private JPopupMenu        pop_menu;

    private JTable            table_view;
    private JTableHeader      table_header;
    private int               the_column;
                              // Model column index that this listens to 

    public TableHeaderHandler( JTable in_table, int in_column,
                               JPopupMenu in_popup_menu )
    {
        super();
        table_view    = in_table;
        table_header  = table_view.getTableHeader();
        the_column    = in_column;
        pop_menu      = in_popup_menu;
    }

    /*
        MouseAdapter interface
    */
    public void mousePressed( MouseEvent evt )
    {
        Rectangle  header_rect;
        Point      click;
        int        click_column, model_column;
        int        pt_x, pt_y;

        click        = evt.getPoint();
        click_column = table_header.columnAtPoint( click );
        model_column = table_view.convertColumnIndexToModel( click_column );
        if ( model_column == the_column ) {
            header_rect  =  table_header.getHeaderRect( click_column );
            // System.out.println( "Mouse Clicked with " + header_rect );
            pt_x         = header_rect.x;
            pt_y         = header_rect.y + header_rect.height;
            pop_menu.show( evt.getComponent(), pt_x, pt_y );
            // pop_menu.show( evt.getComponent(), evt.getX(), evt.getY() );
        }
    }
}
