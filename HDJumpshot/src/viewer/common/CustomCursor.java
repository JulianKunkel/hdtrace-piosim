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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;

public class CustomCursor
{
    public  static Cursor   Normal       = null;
    public  static Cursor   Wait         = null;
    public  static Cursor   Hand         = null;
    public  static Cursor   HandOpen     = null;
    public  static Cursor   HandClose    = null;
    public  static Cursor   ZoomPlus     = null;
    public  static Cursor   ZoomMinus    = null;

    private static Toolkit  toolkit      = null;

    static {
        ( new CustomCursor() ).initCursors();
    }

    private Image getBestCursorImage( String filename )
    {
        Image          img;
        Dimension      opt_size;
        Graphics2D     g2d;
        int            iwidth, iheight;

        final File f = new File(Const.IMG_PATH + filename);        
        if (! f.canRead()){
        	throw new IllegalArgumentException("Image does not exist: " + f.getAbsolutePath() );
        }
        
        img      = new ImageIcon( Const.IMG_PATH + filename ).getImage();
        iwidth   = img.getWidth( null );
        iheight  = img.getHeight( null );
                
        opt_size = toolkit.getBestCursorSize( iwidth, iheight );
        if ( opt_size.width == iwidth && opt_size.height == iheight )
            return img;
        else {
            BufferedImage  buf_img;
            buf_img = new BufferedImage( opt_size.width, opt_size.height,
                                          BufferedImage.TYPE_INT_ARGB );
            System.out.println( filename
                              + ": (" + iwidth + "," + iheight + ") -> ("
                              + opt_size.width + "," + opt_size.height + ")" );
            g2d     = buf_img.createGraphics();
            g2d.drawImage( img, 0, 0, null );
            g2d.dispose();
            return buf_img;
        }
    }

    public void initCursors()
    {
        Image    img;
        Point    pt;

        Normal   = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
        Wait     = Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR );
        Hand     = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );

        toolkit   = Toolkit.getDefaultToolkit();
        pt        = new Point( 1, 1 );

        img       = this.getBestCursorImage( "HandOpenUpLeft25.gif" );
        HandOpen  = toolkit.createCustomCursor( img, pt, "Hand Open" );
        img       = this.getBestCursorImage( "HandCloseUpLeft25.gif" );
        HandClose = toolkit.createCustomCursor( img, pt, "Hand Close" );
        img       = this.getBestCursorImage( "ZoomPlusUpLeft25.gif" );
        ZoomPlus  = toolkit.createCustomCursor( img, pt, "Zoom Plus" );
        img       = this.getBestCursorImage( "ZoomMinusUpLeft25.gif" );
        ZoomMinus = toolkit.createCustomCursor( img, pt, "Zoom Minus" );
    }
}
