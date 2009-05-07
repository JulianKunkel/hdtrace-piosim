
 /** Version Control Information $Id: TopologyLabels.java 242 2009-04-25 19:25:34Z kunkel $
  * @lastmodified    $Date: 2009-04-25 21:25:34 +0200 (Sa, 25. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 242 $ 
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


package de.hd.pvs.TraceFormat.topology;

import java.util.ArrayList;

public class TopologyTypes {
	private final ArrayList<String> types = new ArrayList<String>();

	/**
	 * Set all topology labels at once. (Additional could be added with addLabel...) 
	 * @param labels
	 */
	public void setTopologyTypes(String [] labels){
		this.types.clear();
		for(int i=0; i < labels.length; i++){
			addTypeForNextLevel(labels[i]);
		}
	}
	
	/**
	 * Add a label i.e. the next depth. 
	 * @param type
	 */
	public void addTypeForNextLevel(String type) {
		types.add(type);
	}
	
	public ArrayList<String> getTypes() {
		return types;
	}
	
	public String getTypeFor(int depth){
		return types.get(depth);
	}
}
