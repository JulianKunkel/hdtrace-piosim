
 /** Version Control Information $Id: LabeledComboBox.java 149 2009-03-27 13:55:56Z kunkel $
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

package de.viewer.common;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LabeledComboBox extends JPanel
{
    private static final   int     TEXT_HEIGHT = 20;
    private static         Font    FONT        = null;

    private JLabel                 tag;
    private JComboBox              lst;

    public LabeledComboBox( String label )
    {
        super();
        super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        JPanel tag_panel = new JPanel();
        tag_panel.setLayout( new BoxLayout( tag_panel, BoxLayout.X_AXIS ) );
            tag = new JLabel( label );
        tag_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        tag_panel.add( tag );
        tag_panel.add( Box.createHorizontalGlue() );

        lst = new JComboBox();
        tag.setLabelFor( lst );
        tag_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        lst.setAlignmentX( Component.LEFT_ALIGNMENT );

        super.add( tag_panel );
        super.add( lst );

        // tag.setBorder( BorderFactory.createEtchedBorder() );
        lst.setBorder( BorderFactory.createLoweredBevelBorder() );

        if ( FONT != null ) {
            tag.setFont( FONT );
            lst.setFont( FONT );
        }
    }

    public static void setDefaultFont( Font font )
    {
        FONT = font;
    }

    public void setLabelFont( Font font )
    {
        tag.setFont( font );
    }

    public void setFieldFont( Font font )
    {
        lst.setFont( font );
    }

    public void setEditable( boolean flag )
    {
        lst.setEditable( flag );
    }

    public void setEnabled( boolean flag )
    {
        lst.setEnabled( flag );
    }

    public void addItem( Object new_item )
    {
        lst.addItem( new_item );
    }

    public void setSelectedItem( Object an_object )
    {
        lst.setSelectedItem( an_object );
    }

    public void setSelectedBooleanItem( boolean bool_val )
    {
        if ( bool_val )
            lst.setSelectedItem( Boolean.TRUE );
        else
            lst.setSelectedItem( Boolean.FALSE );
    }

    public Object getSelectedItem()
    {
        return lst.getSelectedItem();
    }

    public boolean getSelectedBooleanItem()
    {
        return ((Boolean) lst.getSelectedItem()).booleanValue();
    }

    public void addActionListener( ActionListener listener )
    {
        lst.addActionListener( listener );
    }

    // BoxLayout respects component's maximum size
    public Dimension getMaximumSize()
    {
        return new Dimension( Short.MAX_VALUE,
                              lst.getPreferredSize().height
                            + this.TEXT_HEIGHT );
    }
}
