
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

package de.hd.pvs.piosim.model;

import java.util.HashMap;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;

/**
 * This basic class allows to reorder the IDs of model components depending on criteria defined
 * in subclasses.
 *  
 * @author Julian M. Kunkel
 *
 */
abstract public class ModelComponentSorter {
	
	/**
	 * Sort the component IDs based on the subclass criteria.
	 * 
	 * @param model
	 */
	abstract public void sort(Model model);
	
	/**
	 * Adapt the IDs in the model.
	 * 
	 * @param newMapIDToComp
	 */
	final protected void setComponentIDsByMap(
			Model model, 
			HashMap<Integer, BasicComponent> newMapIDToComp)
	{
		if (newMapIDToComp.size() != model.getCidCMap().size()){
			throw new IllegalArgumentException("Number of entries in the new component map differs from old map." +
					" This should never happen in a sorter.");
		}
		
		// adapt IDs in all components.
		for(Integer newID : newMapIDToComp.keySet()){
			BasicComponent comp = newMapIDToComp.get(newID);
			comp.getIdentifier().setID(newID);
		}
		
		// now set the new map.
		model.setCidCMap(newMapIDToComp);
	}
}
