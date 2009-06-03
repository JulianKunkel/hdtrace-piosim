
 /** Version Control Information $Id: StateTraceEntry.java 325 2009-06-01 15:42:47Z kunkel $
  * @lastmodified    $Date: 2009-06-01 17:42:47 +0200 (Mo, 01 Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 325 $ 
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

package de.hd.pvs.TraceFormat.trace;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * State, contains a start and end time and might be nested.
 * I.e. it is allowed to contain child entries. 
 * 
 * @author julian
 */
public interface IStateTraceEntry extends ITraceEntry{
	/**
	 * Walk through the children in correct time order, (Depth First Search)
	 * This includes not the parent state.
	 * @return
	 */
	public ForwardStateEnumeration childForwardEnumeration();
	
	/**
	 * Walk through the children in correct time order, (Depth First Search)
	 * The least startTime the child might have is the startTime.
	 * This includes not the parent state. 
	 * @return
	 */
	public ForwardStateEnumerationStartTime childForwardEnumeration(Epoch startTime);


	/**
	 * Walk through the children in reversed time order
	 * @return
	 */
	public Enumeration<ITraceEntry> childBackwardEnumeration();

	/**
	 * Return true if this state has nested trace entries
	 * @return
	 */
	public boolean hasNestedTraceChildren();

	/**
	 * Might return null if this state has no nested trace entries, otherwise return the nested children.
	 * @return
	 */
	public ArrayList<ITraceEntry> getNestedTraceChildren();

	/**
	 * Create an informative string for the state and all contained children. 
	 * @return
	 */
	public String toStringWithChildren();	
}
