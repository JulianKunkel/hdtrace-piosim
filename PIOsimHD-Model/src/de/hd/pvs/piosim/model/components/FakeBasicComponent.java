
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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

package de.hd.pvs.piosim.model.components;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;

/**
 * A FakeComponent does not keep any private model information but might be used to
 * create new Components in the Simulator on the Fly.  
 * 
 * @author Julian M. Kunkel
 */
public class FakeBasicComponent extends BasicComponent{
	
	/**
	 * Add the FakeBasicComponent with a given name
	 * 
	 * @param name
	 * @param model
	 */
	public FakeBasicComponent(String name, Model model, BasicComponent<?> parentComponent) {
		getIdentifier().setName(name);
		setParentComponent(parentComponent);
	}
	
	@Override
	public String getComponentType() {		
		return FakeBasicComponent.class.getSimpleName();
	}
}
