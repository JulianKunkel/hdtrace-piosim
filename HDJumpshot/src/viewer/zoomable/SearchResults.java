
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


package viewer.zoomable;

import de.hd.pvs.TraceFormat.ITracableObject;


/**
 * Encapsulates results of a search
 * @author Julian M. Kunkel
 *
 */
public class SearchResults {
	
	final int timeline;
	final ITracableObject object;
	
	public SearchResults(int timeline, ITracableObject object) {
		this.timeline = timeline;
		this.object = object;
	}
	
	public ITracableObject getObject() {
		return object;
	}
	
	public int getTimeline() {
		return timeline;
	}
	
	public boolean wasSucessfull(){
		return object != null;
	}
}
