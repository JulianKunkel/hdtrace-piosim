
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

package viewer.zoomable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import viewer.common.Const;

public class ScrollbarTime extends JScrollBar
{
    private ModelTime   model;
    private Dimension   min_size;

    public ScrollbarTime( ModelTime model )
    {
        super( JScrollBar.HORIZONTAL );
        this.model = model;

        setModel( model );
        this.addAdjustmentListener( model );

        super.setUnitIncrement( Const.TIME_SCROLL_UNIT_INIT );

        min_size = super.getMinimumSize();
        if ( min_size.height <= 0 )
            min_size.height = 20;
    }

    public void setBlockIncrementToModelExtent()
    {
        if ( model != null ) {
            int model_extent = model.getExtent();
            if ( model_extent > 1 )
                super.setBlockIncrement( model_extent );
        }
    }

    public Dimension getMinimumSize()
    {
        return min_size;
    }

}
