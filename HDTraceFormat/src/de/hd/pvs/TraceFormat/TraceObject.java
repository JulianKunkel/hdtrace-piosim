
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.


package de.hd.pvs.TraceFormat;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Basic class for all managed trace objects.
 * 
 * @author Julian M. Kunkel
 *
 */
public interface TraceObject {
	/**
	 * What kind of trace object is it?
	 * @return
	 */
	public TraceObjectType getType();
	
	/**
	 * When does this trace object start
	 * @return
	 */
	public Epoch getEarliestTime();
	
	/**
	 * When is this trace object finished
	 * @return
	 */
	public Epoch getLatestTime();
	
	/**
	 * Duration of the trace object
	 * @return
	 */
	public Epoch getDurationTime();	
}
