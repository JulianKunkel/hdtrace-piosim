
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

package de.hd.pvs.piosim.model.components.superclasses;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;

/**
 * Identifies a component.
 * 
 * @author Julian M. Kunkel
 */
public class ComponentIdentifier implements Comparable<ComponentIdentifier> {
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	private String name = null;
	
	@NotNegativeOrZero
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	private Integer id = null;
	
	public ComponentIdentifier() {
	}
	
	/**
	 * Create a ComponentIdentifier with an specific ID.
	 * @param id
	 */
	public ComponentIdentifier(Integer id){
		this.id = id;
	}
	

	/**
	 * @return the Componenent name
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name.length() == 0){
			name = null;
		}

		this.name = name;
	}
	
	public void setID(Integer id) {
		this.id = id;
	}

	@AttributeGetters public Integer getID() {
		return id;
	}

	@Override
	public String toString() {
		return "\"" + name + "\"" + " id="+ id;
	}

	@Override
	public int hashCode() {
		if(id == null){
			//return Integer.MIN_VALUE;
			// this should never happen!
			throw new IllegalArgumentException("Component has an invalid identifier" + this);
		}
		return id;
	}

	public int compareTo(ComponentIdentifier o) {
		return (o.id == this.id) ? 0 :
			((o.id < this.id) ? -1: +1);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if( obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		if ( ((ComponentIdentifier) obj).id == this.id){			
			return true;
		}
		return false;
	}
}
