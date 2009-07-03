
 /** Version Control Information $Id: StateBorder.java 149 2009-03-27 13:55:56Z kunkel $
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

package de.drawable;

import java.awt.Graphics2D;
import java.awt.Color;

//  A place holder for State's BorderType.  It is meant to be extended
public abstract class StateBorder
{
    public  static final StateBorder EMPTY_BORDER
                                     = new EmptyBorder();
    public  static final StateBorder WHITE_PLAIN_BORDER
                                     = new WhitePlainBorder();
    public  static final StateBorder WHITE_LOWERED_BORDER
                                     = new WhiteLoweredBorder();
    public  static final StateBorder WHITE_RAISED_BORDER
                                     = new WhiteRaisedBorder();
    public  static final StateBorder COLOR_LOWERED_BORDER
                                     = new ColorLoweredBorder();
    public  static final StateBorder COLOR_RAISED_BORDER
                                     = new ColorRaisedBorder();
    public  static final StateBorder COLOR_XOR_BORDER
                                     = new ColorXORBorder();

    public static StateBorder parseString( String str )
    {
        if ( str.equalsIgnoreCase( COLOR_XOR_BORDER.toString() ) )
            return COLOR_XOR_BORDER;
        else if ( str.equalsIgnoreCase( COLOR_RAISED_BORDER.toString() ) )
            return COLOR_RAISED_BORDER;
        else if ( str.equalsIgnoreCase( COLOR_LOWERED_BORDER.toString() ) )
            return COLOR_LOWERED_BORDER;
        else if ( str.equalsIgnoreCase( WHITE_RAISED_BORDER.toString() ) )
            return WHITE_RAISED_BORDER;
        else if ( str.equalsIgnoreCase( WHITE_LOWERED_BORDER.toString() ) )
            return WHITE_LOWERED_BORDER;
        else if ( str.equalsIgnoreCase( WHITE_PLAIN_BORDER.toString() ) )
            return WHITE_PLAIN_BORDER;
        else if ( str.equalsIgnoreCase( EMPTY_BORDER.toString() ) )
            return EMPTY_BORDER;
        else 
            return null;
    }

    public abstract void paintStateBorder( Graphics2D g, Color color,
                                           int iHead, int jHead,
                                           boolean isStartVtxIn,
                                           int iTail, int jTail,
                                           boolean isFinalVtxIn );

    public abstract String toString();



    public static class EmptyBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {}

        public String toString() { return "Empty"; }
    }



    public static class WhitePlainBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setColor( Color.white );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            g.setColor( Color.white );
            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
        }

        public String toString() { return "WhitePlain"; }
    }



    public static class WhiteLoweredBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setColor( Color.white );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            g.setColor( Color.gray );
            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
        }

        public String toString() { return "WhiteLowered"; }
    }    



    public static class WhiteRaisedBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setColor( Color.gray );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            g.setColor( Color.white );
            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
        }

        public String toString() { return "WhiteRaised"; }
    }



    public static class ColorLoweredBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setColor( color.brighter() );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            g.setColor( color.darker() );
            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
        }

        public String toString() { return "ColorLowered"; }
    }



    public static class ColorRaisedBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setColor( color.darker() );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            g.setColor( color.brighter() );
            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
        }

        public String toString() { return "ColorRaised"; }
    }   // Endof public class ColorRaisedBorder

    public static class ColorXORBorder extends StateBorder
    {
        public void paintStateBorder( Graphics2D g, Color color,
                                      int iHead, int jHead,
                                      boolean isStartVtxIn,
                                      int iTail, int jTail,
                                      boolean isFinalVtxIn )
        {
            int    iwidth  = iTail - iHead + 1;
            int    jheight = jTail - jHead + 1;
            // Color  color  = Color.white;
            /*
               Draw the shaded lower right first then upper left,
               so tight packed states are shown as white.
            */
            g.setXORMode( color );
            // g.drawLine( iHead, jTail, iTail, jTail );
            g.fillRect( iHead, jTail, iwidth, 1 );
            if ( isFinalVtxIn )
                // g.drawLine( iTail, jHead, iTail, jTail );
                g.fillRect( iTail, jHead, 1, jheight );

            // g.drawLine( iHead, jHead, iTail, jHead );
            g.fillRect( iHead, jHead, iwidth, 1 );
            if ( isStartVtxIn )
                // g.drawLine( iHead, jHead, iHead, jTail );
                g.fillRect( iHead, jHead, 1, jheight );
            g.setPaintMode();
        }

        public String toString() { return "ColorXOR"; }
    }   // Endof public class ColorRaisedBorder
}
