
 /** Version Control Information $Id: LabeledFloatSlider.java 149 2009-03-27 13:55:56Z kunkel $
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Hashtable;


public class LabeledFloatSlider extends LabeledTextField
                                implements ChangeListener,
                                           ActionListener
{
    private static final  int   SLIDER_MIN    = 0;
    private static final  int   SLIDER_MAX    = 10000;
    private static final  int   SLIDER_EXTENT = SLIDER_MAX - SLIDER_MIN;

    private JSlider  slider;
    private float    fmin;
    private float    fmax;
    private float    fextent;

    public LabeledFloatSlider( String label, float min_label, float max_label )
    {
        super( true, label, Const.FLOAT_FORMAT );
        fmin     = min_label;
        fmax     = max_label;
        fextent  = fmax - fmin;

        JLabel    tick_mark;
        Hashtable label_table;
        int       ival;
        float     fval;

        ival    = (SLIDER_MIN + SLIDER_MAX) / 2;
        slider  = new JSlider( JSlider.HORIZONTAL,
                               SLIDER_MIN, SLIDER_MAX, ival );       
        slider.setPaintLabels( true );
        fval    = ivalue2flabel( ival );
        label_table = new Hashtable();
            tick_mark = new JLabel( fmt.format(fmin) );
            if ( FONT != null )
                tick_mark.setFont( FONT );
        label_table.put( new Integer( SLIDER_MIN ), tick_mark );
            tick_mark = new JLabel( fmt.format(fval) );
            if ( FONT != null )
                tick_mark.setFont( FONT );
        label_table.put( new Integer( ival ), tick_mark );
            tick_mark = new JLabel( fmt.format(fmax) );
            if ( FONT != null )
                tick_mark.setFont( FONT );
        label_table.put( new Integer( SLIDER_MAX ), tick_mark );
        slider.setLabelTable( label_table );
        slider.setBorder( BorderFactory.createLoweredBevelBorder() );
        slider.setAlignmentX( Component.LEFT_ALIGNMENT );
        slider.addChangeListener( this );
        super.addActionListener( this );
        super.add( slider );
    }

    private float  ivalue2flabel( int ival )
    {
        return fextent / SLIDER_EXTENT * ((float)( ival - SLIDER_MIN )) + fmin;
    }

    private int    flabel2ivalue( float fval )
    {
        return  (int) Math.round( (float) SLIDER_EXTENT / fextent
                                * ( fval - fmin ) ) + SLIDER_MIN;
    }

    public void setFloat( float fval )
    {
        int   ival;
        ival  = flabel2ivalue( fval );
        if ( ival <= SLIDER_MIN ) {
            ival = SLIDER_MIN + 1;
            fval = ivalue2flabel( ival );
        }
        if ( ival >= SLIDER_MAX ) {
            ival = SLIDER_MAX - 1;
            fval = ivalue2flabel( ival );
        }
        super.setFloat( fval );
        slider.setValue( ival );
    }

    public void stateChanged( ChangeEvent evt )
    {
        int   ival;
        float fval;
        ival  = slider.getValue();
        if ( ival <= SLIDER_MIN ) {
            ival = SLIDER_MIN + 1;
            slider.setValue( ival );
        }
        if ( ival >= SLIDER_MAX ) {
            ival = SLIDER_MAX - 1;
            slider.setValue( ival );
        }
        fval  = ivalue2flabel( ival );
        super.setFloat( fval );
    }

    public void actionPerformed( ActionEvent evt )
    {
        int   ival;
        float fval;
        fval  = super.getFloat();
        ival  = flabel2ivalue( fval );
        if ( ival <= SLIDER_MIN ) {
            ival = SLIDER_MIN + 1;
            super.setFloat( ivalue2flabel( ival ) );
        }
        if ( ival >= SLIDER_MAX ) {
            ival = SLIDER_MAX - 1;
            super.setFloat( ivalue2flabel( ival ) );
        }
        slider.setValue( ival );
    }
}
