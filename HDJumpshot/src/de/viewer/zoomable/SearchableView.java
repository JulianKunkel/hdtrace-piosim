
 /** Version Control Information $Id: SearchableView.java 419 2009-06-18 14:42:07Z kunkel $
  * @lastmodified    $Date: 2009-06-18 16:42:07 +0200 (Do, 18. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 419 $ 
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


package de.viewer.zoomable;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * This interface allows to search through the contained TraceObjects
 * 
 * @author Julian M. Kunkel
 */
public interface SearchableView
{	
    public SearchResults searchPreviousTraceable(Epoch earlierThan);
    public SearchResults searchNextTracable(Epoch laterThan);
}
