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

package viewer.histogram;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import base.statistics.BufForTimeAveBoxes;
import viewer.common.Const;
import viewer.common.LabeledTextField;

public class StatBoxStatusPanel extends JPanel
{
    private static final String  DRAW_STATES = "States Only";
    private static final String  DRAW_ARROWS = "Arrows Only";
    private static final String  DRAW_ALL    = "All";

    private JComboBox            combobox;
    private BufForTimeAveBoxes   buf4statboxes;
    private JButton              canvas_redraw_btn;

    public StatBoxStatusPanel( final BufForTimeAveBoxes statboxes )
    {
        super();
        buf4statboxes  = statboxes;
        super.setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

        combobox  = new JComboBox();
        combobox.setFont( Const.FONT );
        combobox.setEditable( false );
        combobox.addItem( DRAW_STATES );
        combobox.addItem( DRAW_ARROWS );
        combobox.addItem( DRAW_ALL );
        combobox.setToolTipText( "Display options for the Histogram" );
        canvas_redraw_btn = null;

        // super.add( new LabeledTextField( " ", null ) ); // determin size
        super.add( combobox );
    }

    public void addRedrawListener( JButton  btn )
    {
        canvas_redraw_btn = btn;
        combobox.addActionListener( new DisplayModeActionListener() );
    }

    public void init()
    {
        /*
            Since JComboBox.setSelectedItem() invokes ActionListener which
            call canvas_redraw_btn.doClick(), i.e. JComboBox.setSelectedItem()
            redraws the Statline window.  There init() needs to be called
            after RowAdjustment.initSlidersAndTextFields().
        */
        combobox.setSelectedItem( DRAW_STATES );
    }

    private class DisplayModeActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent evt )
        {
            String display_str;
            display_str = (String) combobox.getSelectedItem();
            if ( display_str.equals( DRAW_STATES ) ) {
                buf4statboxes.setDrawingStates( true );
                buf4statboxes.setDrawingArrows( false );
            }
            else if ( display_str.equals( DRAW_ARROWS ) ) {
                buf4statboxes.setDrawingStates( false );
                buf4statboxes.setDrawingArrows( true );
            }
            else if ( display_str.equals( DRAW_ALL ) ) {
                buf4statboxes.setDrawingStates( true );
                buf4statboxes.setDrawingArrows( true );
            }
            else {
                buf4statboxes.setDrawingStates( true );
                buf4statboxes.setDrawingArrows( false );
            }
            canvas_redraw_btn.doClick();
        }
    }
}
