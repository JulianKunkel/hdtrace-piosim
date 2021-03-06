
 /** Version Control Information $Id: ColorAlpha.java 177 2009-04-02 16:39:18Z kunkel $
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

package de.drawable;

import java.awt.Color;
import java.util.Arrays;

public class ColorAlpha extends Color
                        implements Comparable<ColorAlpha>
{
	private static final long serialVersionUID = -4694529781755510600L;

    public static final int         OPAQUE           = 255;
    public static final int         NEAR_OPAQUE      = 191;
    public static final int         HALF_OPAQUE      = 127;
    public static final int         NEAR_TRANSPARENT = 63;
    public static final int         TRANSPARENT      = 0;

    public static final ColorAlpha  WHITE_NEAR_OPAQUE
                                    = new ColorAlpha( ColorAlpha.white,
                                                      ColorAlpha.NEAR_OPAQUE );
    public static final ColorAlpha  WHITE_OPAQUE
                                    = new ColorAlpha( ColorAlpha.white,
                                                      ColorAlpha.OPAQUE );
    public static final ColorAlpha  YELLOW_OPAQUE
                                    = new ColorAlpha( ColorAlpha.yellow,
                                                      ColorAlpha.OPAQUE );

    public ColorAlpha()
    {
        super( 255, 192, 203, ColorAlpha.OPAQUE );
    }

    public ColorAlpha( int red, int green, int blue )
    {
        super( red, green, blue, ColorAlpha.OPAQUE );
    }

    public ColorAlpha( Color color, int alpha )
    {
        super( color.getRed(), color.getGreen(), color.getBlue(), alpha );
    }

    public ColorAlpha( Color color )
    {
        super( color.getRed(), color.getGreen(), color.getBlue(),
               color.getAlpha() );
    }

    public ColorAlpha( int red, int green, int blue, int alpha,
                       boolean in_isModifiable )
    {
        super( red, green, blue, alpha );
    }

    public String toString()
    {
        return "(" + getRed() + "," + getGreen() +  "," + getBlue()
             + "," + getAlpha() + ")";
    }

    public int getLengthSq()
    {
        return super.getRed() * super.getRed()
             + super.getGreen() * super.getGreen()
             + super.getBlue() * super.getBlue();
    }

    public boolean equals( final ColorAlpha clr )
    {
        return    ( super.getRed()   == clr.getRed() )
               && ( super.getGreen() == clr.getGreen() )
               && ( super.getBlue()  == clr.getBlue() );
    }

    public int compareTo( ColorAlpha clr )
    {
        if ( ! this.equals( clr ) )
            // return this.getLengthSq() - clr.getLengthSq();
            return clr.getLengthSq() - this.getLengthSq();
        else
            return 0;
    }

    private static ColorAlpha colors[];
    private static int        next_color_index;

    /*
       possible RGB values are based on 6x6x6 Color Cube defined in
       http://world.std.com/~wij/color/index.html
    */
    private static void initDefaultColors()
    {
        int         ired, igreen, iblue;
        int         vals_length, colors_length, idx;
        int         vals[] = { 0x0, 0x33, 0x66, 0x99, 0xCC, 0xFF };

        vals_length    = vals.length;
        colors_length  = vals_length * vals_length * vals_length;
        colors         = new ColorAlpha[ colors_length ];
        idx = 0;
        for ( ired = 0; ired < vals_length; ired++ ) {
            for ( igreen = 0; igreen < vals_length; igreen++ ) {
                for ( iblue = 0; iblue < vals_length; iblue++ ) {
                    colors[ idx ] = new ColorAlpha( vals[ ired ],
                                                    vals[ igreen ],
                                                    vals[ iblue ] );
                    idx++;
                }
            }
        }

        /*
           Sort the colors[] into accending natural order,
           This sorting guarantees the nearest neighbor colors in colors[]
           are always distinguishable, or are contrasting to each other.
        */
        Arrays.sort( colors );

        // Initialize the next available color index, to avoid white;
        next_color_index = 1;
    }

    public static ColorAlpha getNextDefaultColor()
    {
        int returning_color_index;

        if ( colors == null )
            ColorAlpha.initDefaultColors();

        // "%(colors.lenth-1)" ignores the last color in colors[], black.
        returning_color_index = next_color_index % ( colors.length - 1 );
        next_color_index++;
        return colors[ returning_color_index ];
    }

}
