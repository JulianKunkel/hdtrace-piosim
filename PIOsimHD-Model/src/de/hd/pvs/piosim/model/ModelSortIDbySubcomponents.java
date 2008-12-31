
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

import java.util.ArrayList;
import java.util.HashMap;

import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Switch.Switch;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;

/**
 * This model sorter sorts the IDs by the object hierarchy i.e. if a top level component gets
 * an ID then all the subcomponents get the next IDs. Then the next top level component gets the next ID ...   
 * @author Julian M. Kunkel
 *
 */
public class ModelSortIDbySubcomponents extends ModelComponentSorter {
	
	@Override
	public void sort(Model model) {
		// new Map.
		HashMap<Integer, BasicComponent> map = new HashMap<Integer, BasicComponent>();
		
		// sort first: nodes, then switches, all hierarchical
		int aktID = 0;
		
		for(Node n: model.getNodes()){
			aktID = aktID / 10 * 10 + 10; // assume max 10 children per node.

			map.put(aktID++, n);
			
			ArrayList<BasicComponent> list = n.getAllChildComponents();
			for(BasicComponent bc: list){
				map.put(aktID++, bc);
			}			
		}
		
		for(Switch sw: model.getSwitches()){
			aktID = aktID / 100 * 100 + 100; // assume max 10 children per node.

			map.put(aktID++, sw);
			
			ArrayList<BasicComponent> list = sw.getAllChildComponents();
			for(BasicComponent bc: list){
				map.put(aktID++, bc);
			}			
		}
		
		// set the new IDs.
		setComponentIDsByMap(model, map);
	}
}
