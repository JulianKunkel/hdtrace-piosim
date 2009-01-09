
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

/**
 *
 */
package de.hd.pvs.piosim.model.inputOutput;

import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
import de.hd.pvs.piosim.model.interfaces.IXMLReader;
import de.hd.pvs.piosim.model.util.XMLutil;

/**
 * This class contains informations about an MPI_file.
 * Specific information like file size might be changed during runtime.
 *  
 * @author Julian M. Kunkel
 * 
 */
public class MPIFile implements IXMLReader{
	
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNull
	private String name = "";
	
	@Attribute(xmlName="InitialSize")
	@NotNegative
	private long size = 0l;
	
	@NotNull
	private Distribution distribution = null;
	/**
	 * unique ID set from outside this class.
	 */
	
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNegative
	private int id;
	
	/**
	 * @return the unique id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the file name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the file size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return the data distribution function.
	 */
	public Distribution getDistribution() {
		return distribution;
	}

	public void readXML(Element xmlnode) throws Exception {
		AttributeAnnotationHandler.readSimpleAttributes(xmlnode, this);
		
		distribution = Distribution.readDistributionFromXML(XMLutil
				.getFirstElementByTag(xmlnode, "Distribution"));		
	}
	
	public void writeXML(StringBuffer sb) {
		// TODO Auto-generated method stub		
	}

	/**
	 * Set the current file size (can be updated during runtime).
	 * @param size 
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	/**
	 * Set the distribution (should never be used during runtime).
	 * @param distribution
	 */
	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}
	
	/**
	 * Set the file id.
	 * @param id
	 */
	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * Set the file name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return "MPIFile " + getName() + " ID:" + getId() + " size: " + getSize();
	}
}
