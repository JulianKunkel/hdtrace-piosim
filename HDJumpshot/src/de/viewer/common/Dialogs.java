
 /** Version Control Information $Id: Dialogs.java 149 2009-03-27 13:55:56Z kunkel $
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

import javax.swing.*;
import java.awt.*;

public class Dialogs
{
    public static void error( Component p, String txt )
    {
       JOptionPane.showMessageDialog( p, txt, "Error",
                                      JOptionPane.ERROR_MESSAGE );
    }

    public static void warn( Component p, String txt )
    {
       JOptionPane.showMessageDialog( p, txt, "Warning",
                                      JOptionPane.WARNING_MESSAGE );
    }

    public static boolean confirm( Component p, String txt )
    {
        int ans = JOptionPane.showConfirmDialog( p, txt, "Confirmation",
                                                 JOptionPane.YES_NO_OPTION );
        return ( ans == JOptionPane.YES_OPTION );
    }   

    public static void info( Component p, String txt, ImageIcon icon )
    {
        if ( icon != null ) 
            JOptionPane.showMessageDialog( p, txt, "Information",
                                           JOptionPane.INFORMATION_MESSAGE,
                                           icon );
        else
            JOptionPane.showMessageDialog( p, txt, "Information",
                                           JOptionPane.INFORMATION_MESSAGE );
    }
}
