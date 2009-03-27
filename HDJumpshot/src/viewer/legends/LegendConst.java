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

package viewer.legends;

import java.awt.Color;

public class LegendConst
{
    /*
       * ICON_WIDTH & ICON_HEIGHT need to be ODD numbers, so a center line
         in the icon is really centered in the middle of the icon. 
       * The ratio of ICON_HEIGHT to ICON_WIDTH should be no more than 0.80
         for appealing reason.
    */
           static final int    ICON_WIDTH                  = 35;
           static final int    ICON_HEIGHT                 = 40;

           static final int    CELL_HEIGHT                 = ICON_HEIGHT + 10;
           static final int    CELL_ICON_TEXT_GAP          = 8;
           static final Color  CELL_BACKCOLOR              = Color.lightGray;
           static final Color  CELL_FORECOLOR              = Color.black;
           static final Color  CELL_BACKCOLOR_SELECTED     = Color.gray;
           static final Color  CELL_FORECOLOR_SELECTED     = Color.yellow;

           static final int    LIST_MAX_VISIBLE_ROW_COUNT  = 25;
}
