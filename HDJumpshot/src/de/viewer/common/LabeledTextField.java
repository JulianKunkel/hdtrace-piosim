
 /** Version Control Information $Id: LabeledTextField.java 304 2009-05-24 18:21:45Z kunkel $
  * @lastmodified    $Date: 2009-05-24 20:21:45 +0200 (So, 24. Mai 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 304 $ 
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class LabeledTextField extends JPanel
{
	private static final long serialVersionUID = 8486031160051542133L;
	
	private   static final   int     TEXT_HEIGHT = 20;
    protected static         Font    FONT        = null;

    private   JLabel                 tag;
    private   ActableTextField       fld;
    protected DecimalFormat          fmt;

    private FieldDocumentListener  self_listener;

    public LabeledTextField( String label, String format )
    {
        this( false, label, format );
    }

    public LabeledTextField( boolean isIndentedLabel,
                             String label, String format )
    {
        super();
        super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        tag = new JLabel( " " + label + " " ); // add " " to guarantee spacing
        if ( isIndentedLabel ) {
            JPanel tag_panel = new JPanel();
            tag_panel.setLayout( new BoxLayout( tag_panel, BoxLayout.X_AXIS ) );
            tag_panel.add( Box.createHorizontalStrut(
                               Const.LABEL_INDENTATION ) );
            tag_panel.add( tag );
            tag_panel.add( Box.createHorizontalGlue() );
            tag_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
            super.add( tag_panel );
        }
        else {
            tag.setAlignmentX( Component.LEFT_ALIGNMENT );
            super.add( tag );
        }

        fld = new ActableTextField();
        tag.setLabelFor( fld );
        fld.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( fld );

        // preferred_height = fld.getPreferredSize().height + this.TEXT_HEIGHT;
        if ( format != null ) {
            int  num_col;
            fmt = (DecimalFormat) NumberFormat.getInstance();
            fmt.applyPattern( format );
            num_col = Routines.getAdjNumOfTextColumns( fld, format.length() );
            fld.setColumns( num_col );
        }
        else
            fmt = null;

        /*  No self DocumentListener by default  */
        self_listener = null;

        // tag.setBorder( BorderFactory.createEtchedBorder() );
        fld.setBorder( BorderFactory.createEtchedBorder() );

        if ( FONT != null ) {
            tag.setFont( FONT );
            fld.setFont( FONT );
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
        fld.setFont( font );
    }

    public void setHorizontalAlignment( int alignment )
    {
        fld.setHorizontalAlignment( alignment );
    }

    public void setText( String str )
    {
        fld.setText( str );
    }

    public String getText()
    {
        if ( self_listener != null )
            return self_listener.getLastUpdatedText();
        else
            return fld.getText();
    }

    public void setBoolean( boolean bval )
    {
        fld.setText( String.valueOf( bval ) );
    }

    public boolean getBoolean()
    {
        String  bool_str = null;
        if ( self_listener != null )
            bool_str = self_listener.getLastUpdatedText();
        else
            bool_str = fld.getText();
        return    bool_str.equalsIgnoreCase( "true" )
               || bool_str.equalsIgnoreCase( "yes" );
    }

    public void setShort( short sval )
    {
        fld.setText( fmt.format( sval ) );
    }

    public short getShort()
    {
        String  short_str = null;
        if ( self_listener != null )
            short_str = self_listener.getLastUpdatedText();
        else
            short_str = fld.getText();

        try {
            return fmt.parse( short_str ).shortValue();
        } catch ( ParseException perr ) {
            perr.printStackTrace();
            return Short.MIN_VALUE;
        }
    }

    public void setInteger( int ival )
    {
        // fld.setText( Integer.toString( ival ) );
        fld.setText( fmt.format( ival ) );
    }
    
    public void setLong( long ival )
    {
        // fld.setText( Integer.toString( ival ) );
        fld.setText( fmt.format( ival ) );
    }

    public int getInteger()
    {
        String  int_str = null;
        if ( self_listener != null )
            int_str = self_listener.getLastUpdatedText();
        else
            int_str = fld.getText();

        try {
            return fmt.parse( int_str ).intValue();
        } catch ( ParseException perr ) {
            perr.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public void setFloat( float fval )
    {
        fld.setText( fmt.format( fval ) );
    }

    public float getFloat()
    {
        String  float_str = null;
        if ( self_listener != null )
            float_str = self_listener.getLastUpdatedText();
        else
            float_str = fld.getText();

        try {
            return fmt.parse( float_str ).floatValue();
        } catch ( ParseException perr ) {
            perr.printStackTrace();
            return Float.MIN_VALUE;
        }
    }

    public void setDouble( double dval )
    {
        fld.setText( fmt.format( dval ) );
    }
    
    public void addText(String txt){
    	fld.setText(fld.getText() + txt);
    }

    public double getDouble()
    {
        String  double_str = null;
        if ( self_listener != null )
            double_str = self_listener.getLastUpdatedText();
        else
            double_str = fld.getText();

        try {
            return fmt.parse( double_str ).doubleValue();
        } catch ( ParseException perr ) {
            perr.printStackTrace();
            return Double.NEGATIVE_INFINITY;
        }
    }

    public void setEditable( boolean flag )
    {
        fld.setEditable( flag );
    }

    public void setEnabled( boolean flag )
    {
        fld.setEnabled( flag );
    }

    @Override
    public void setToolTipText(String text) {    
    	super.setToolTipText(text);
    	fld.setToolTipText(text);
    }
    
    public void addActionListener( ActionListener listener )
    {
        fld.addActionListener( listener );
    }

    public void addSelfDocumentListener()
    {
        self_listener = new FieldDocumentListener();
        fld.getDocument().addDocumentListener( self_listener );
    }

    public Dimension getMaximumSize()
    {
        return new Dimension( Short.MAX_VALUE, fld.getPreferredSize().height + this.TEXT_HEIGHT );
    }

    public void setLabel(String label){
    	tag.setText(label);
    }

    public void fireActionPerformed()
    {
        fld.fireActionPerformed();
    }

    /*  DocumentListener Interface  */
    private class FieldDocumentListener implements DocumentListener
    {
        private Document            last_updated_document;

        public FieldDocumentListener()
        {
            last_updated_document  = null;
        }

        public void changedUpdate( DocumentEvent evt )
        {
            last_updated_document = evt.getDocument();
        }

        public void insertUpdate( DocumentEvent evt )
        {
            last_updated_document = evt.getDocument();
        }

        public void removeUpdate( DocumentEvent evt )
        {
            last_updated_document = evt.getDocument();
        }

        public String getLastUpdatedText()
        {
            if ( last_updated_document != null ) {
                try {
                    return last_updated_document.getText( 0,
                           last_updated_document.getLength() );
                } catch ( BadLocationException err ) {
                    return null;
                }
            }
            return null;
        }
    }
}
