
 /** Version Control Information $Id: Debug.java 149 2009-03-27 13:55:56Z kunkel $
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


public class Debug
{
    private static Diagnosis msg = new Diagnosis();

    public static void initTextArea()
    {
        msg.initTextArea( "Debugging Output" );
    }

    public static void setActive( boolean is_active )
    {
        msg.setActive( is_active );
    }

    public static boolean isActive()
    {
        return msg.isActive();
    }

    public static void setFilename( String in_name )
    throws java.io.IOException
    {
        msg.setFilename( in_name );
    }

    public static void print( String str )
    {
        msg.print( str );
    }

    public static void println( String str )
    {
        msg.println( str );
    }
}
