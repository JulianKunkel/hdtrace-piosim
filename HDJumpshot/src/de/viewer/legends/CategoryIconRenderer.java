
 /** Version Control Information $Id: CategoryIconRenderer.java 177 2009-04-02 16:39:18Z kunkel $
  * @lastmodified    $Date: 2009-04-02 18:39:18 +0200 (Do, 02. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 177 $ 
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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class CategoryIconRenderer extends JLabel
                                  implements TableCellRenderer
{
	private static final long serialVersionUID = 1054236453753786546L;
	
	private static final Color  CELL_BACKCOLOR
                                = LegendConst.CELL_BACKCOLOR;
    private static final Color  CELL_FORECOLOR
                                = LegendConst.CELL_FORECOLOR;
    private static final Color  CELL_BACKCOLOR_SELECTED
                                = LegendConst.CELL_BACKCOLOR_SELECTED;
    private static final Color  CELL_FORECOLOR_SELECTED
                                = LegendConst.CELL_FORECOLOR_SELECTED;

    private Border  raised_border, lowered_border;

    public CategoryIconRenderer()
    {
        super();
        super.setOpaque( true );
        super.setHorizontalAlignment( SwingConstants.CENTER );
        super.setIconTextGap( LegendConst.CELL_ICON_TEXT_GAP );
        raised_border  = BorderFactory.createRaisedBevelBorder();
        lowered_border = BorderFactory.createLoweredBevelBorder();
    }

    public Component getTableCellRendererComponent( JTable   table,
                                                    Object   value,
                                                    boolean  isSelected,
                                                    boolean  hasFocus,
                                                    int      irow,
                                                    int      icolumn )
    {
        super.setIcon( (Icon) value );
        if ( isSelected ) {
            super.setForeground( CELL_FORECOLOR_SELECTED );
            super.setBackground( CELL_BACKCOLOR_SELECTED );
            super.setBorder( lowered_border );
        }
        else {
            super.setForeground( CELL_FORECOLOR );
            super.setBackground( CELL_BACKCOLOR );
            super.setBorder( raised_border );
        }
        // repaint();
        return this;
    }
}
