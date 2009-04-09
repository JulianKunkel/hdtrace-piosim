
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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

package viewer.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;

public class Routines
{
    private static final String UnitIndentStr = "   ";

    public static Dimension getScreenSize()
    {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    //  JTextField.getColumnWidth() uses char('m') defines column width
    //  getAdjNumOfTextColumns() computes the effective char column number
    //  that is needed by the JTextField's setColumns().
    //  This routine should be good for both JTextField and JTextArea
    public static int getAdjNumOfTextColumns( Component textcomp,
                                              int num_numeric_columns )
    {
        FontMetrics metrics;
        int         num_char_columns;

        metrics = textcomp.getFontMetrics( textcomp.getFont() );
        num_char_columns = (int) Math.ceil( (double) num_numeric_columns
                                          * metrics.charWidth( '1' )
                                          / metrics.charWidth( 'm' ) );
        // System.out.println( "num_char_columns = " + num_char_columns );
        return num_char_columns;
    }

    private static final  double  MATH_LOG_10 = Math.log( 10.0 );
    /*
       getRulerIncrement() takes in an estimated time increment and
       returns a more appropriate time increment
    */
    public static double getTimeRulerIncrement( double t_incre )
    {
        double incre, incre_expo, incre_ftr, tmp_mant, incre_mant;
        incre      = t_incre;
        incre_expo = Math.ceil( Math.log( incre ) / MATH_LOG_10 );
        incre_ftr  = Math.pow( 10.0, incre_expo );
        tmp_mant   = incre / incre_ftr;
        if ( tmp_mant < 0.1125 )
            incre_mant = 0.1;
        else if ( tmp_mant < 0.1625 )
            incre_mant = 0.125;
        else if ( tmp_mant < 0.225 )
            incre_mant = 0.2;
        else if ( tmp_mant < 0.325 )
            incre_mant = 0.25;
        else if ( tmp_mant < 0.45 )
            incre_mant = 0.4;
        else if ( tmp_mant < 0.75 )
            incre_mant = 0.5;
        else
            incre_mant = 1.0;

        // system.err.println( "Routines.getTimeRulerIncrement("
        //                   + t_incre + ") = " + incre_mant * incre_ftr );
        return incre_mant * incre_ftr;
    }

    public static double getTimeRulerFirstMark( double t_init, double t_incre )
    {
        double quotient;
        // quotient = Math.ceil( t_init / t_incre );
        quotient = Math.floor( t_init / t_incre );
        return quotient * t_incre;
    }



    private static final double COLOR_FACTOR = 0.85;

    public static Color getSlightBrighterColor( Color color )
    {
        int red   = color.getRed();
        int green = color.getGreen();
        int blue  = color.getBlue();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int ii = (int) ( 1.0 / (1.0-COLOR_FACTOR) );
        if ( red == 0 && green == 0 && blue == 0)
           return new Color( ii, ii, ii );

        if ( red > 0 && red < ii )
            red = ii;
        if ( green > 0 && green < ii )
            green = ii;
        if ( blue > 0 && blue < ii )
            blue = ii;

        return new Color( Math.min( (int)(red  /COLOR_FACTOR), 255 ),
                          Math.min( (int)(green/COLOR_FACTOR), 255 ),
                          Math.min( (int)(blue /COLOR_FACTOR), 255 ) );
    }

    public static Color getSlightDarkerColor( Color color )
    {
        return new Color( Math.max( (int)(color.getRed()  *COLOR_FACTOR), 0),
                          Math.max( (int)(color.getGreen()*COLOR_FACTOR), 0),
                          Math.max( (int)(color.getBlue() *COLOR_FACTOR), 0) );
    }
}
