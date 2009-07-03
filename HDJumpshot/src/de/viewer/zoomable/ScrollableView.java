
 /** Version Control Information $Id: ScrollableView.java 206 2009-04-12 17:40:09Z kunkel $
  * @lastmodified    $Date: 2009-04-12 19:40:09 +0200 (So, 12. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 206 $ 
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

package de.viewer.zoomable;


/**
   Define the interface to be implemented by the view object within
   the viewport.  The interface defines the operations of view object
   to be used by the viewport.
*/
public interface ScrollableView
{
	/**
	 * Check and redraw the view after a zoom (if necessary).
	 * Returns whether it is redrawn or not.
	 * @return
	 */
    public boolean checkToZoomView();

  	/**
  	 * Check and redraw the view after the view has been scrolled.
  	 * It is likely that only a subset of the image must be redrawn.
  	 * Returns whether it is redrawn or not.
  	 * @return
  	 */
    public boolean checkToScrollView();

    /**
     * Get the XaxisViewPosition relative with the time.
     * @return
     */
    public int  getXaxisViewPosition();

    /**
     * Enforce to redraw the view
     */
    public void forceRedraw();
    
    /**
     * Notifies the view that it got resized
     */
    public void resized();
}
