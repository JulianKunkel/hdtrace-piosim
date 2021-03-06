
 /** Version Control Information $Id: GenericHeaderRenderer.java 166 2009-04-01 18:36:07Z kunkel $
  * @lastmodified    $Date: 2009-04-01 20:36:07 +0200 (Mi, 01. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 166 $ 
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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class GenericHeaderRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -1929338988672843913L;
	
	private LegendTable   table_view;
    private int                renderer_column;

    private LegendTableTraceModel   table_model;
    private JTableHeader       table_header;
    private Color              released_bg_color;
    private Color              pressed_bg_color;
    private Icon               released_tab_icon;
    private Icon               pressed_tab_icon;

 
    public GenericHeaderRenderer( LegendTable in_table, int icolumn )
    {
        super();
        table_view       = in_table;
        renderer_column  = icolumn;

        table_model = (LegendTableTraceModel) table_view.getModel();
        super.setText( table_model.getColumnName( icolumn ) );
        super.setToolTipText( table_model.getColumnToolTip( icolumn ) );
        super.setHorizontalAlignment( SwingConstants.CENTER );

        super.setForeground( table_model.getColumnNameForeground( icolumn ) );
        released_bg_color  = table_model.getColumnNameBackground( icolumn );
        pressed_bg_color   = released_bg_color.darker();
        super.setBackground( released_bg_color );
    }

    /*
        If this header renderer needs to have a pulldown tab,
        this.initPressablePullDownTab() has to be called to set up the
        simulated pulldown tab.
    */
    public void initPressablePullDownTab()
    {
        boolean  is_raised_tab;
        is_raised_tab  = table_model.isRaisedColumnNameIcon( renderer_column );
        released_tab_icon  = new Triangular3DIcon( Triangular3DIcon.DOWN,
                                                   true, is_raised_tab );
        pressed_tab_icon   = new Triangular3DIcon( Triangular3DIcon.DOWN,
                                                   false, is_raised_tab );
        table_header  = table_view.getTableHeader();
        super.setHorizontalTextPosition( SwingConstants.LEFT );
        super.setIcon( released_tab_icon );
        //  Renderer is a RubberStamp class, it does not get any MouseEvent
        table_header.addMouseListener( new RendererMouseHandler() );
        // super.setBorder( BORDER );
    }

    public void setPressed( boolean isPressed )
    {
        if ( isPressed ) {
            super.setIcon( pressed_tab_icon );
            super.setBackground( pressed_bg_color );
        }
        else {
            super.setIcon( released_tab_icon );
            super.setBackground( released_bg_color );
        }
        super.revalidate();
        super.repaint();
        table_header.repaint(); 
    }


    private class RendererMouseHandler extends MouseAdapter
    {
        private GenericHeaderRenderer  renderer;

        public RendererMouseHandler()
        {
            renderer  = GenericHeaderRenderer.this;
        }

        private boolean isMouseEventAtMyColumn( MouseEvent evt )
        {
            Point      click;
            int        click_column, model_column;

            click        = evt.getPoint();
            click_column = table_header.columnAtPoint( click );
            model_column = table_view.convertColumnIndexToModel( click_column );
            return model_column == renderer_column;
        }

        public void mousePressed( MouseEvent evt )
        {
            if ( isMouseEventAtMyColumn( evt ) )
                renderer.setPressed( true );
        }

        public void mouseReleased( MouseEvent evt )
        {
            if ( isMouseEventAtMyColumn( evt ) )
                renderer.setPressed( false );
        }
    }
}
