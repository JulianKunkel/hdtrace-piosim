
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

package de.hd.pvs.piosim.model.components.ClientProcess;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.components.superclasses.NodeHostedComponent;

/**
 * A ClientProcess is a process with a defined rank within an application.  
 * 
 * @author Julian M. Kunkel
 */

public final class ClientProcess extends NodeHostedComponent {
  /** The client rank in the program. */
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNegative
	int rank = -1;
	
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNegative
	int thread = 0;
	
	/**
	 * The application the client process belongs to
	 */
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNull
	String application;
  
  public int getRank() {
    return rank;
  }
  
  public void setRank(int rank) {
		this.rank = rank;
	}
  
  public int getThread() {
		return thread;
	}
  
  public void setThread(int thread) {
		this.thread = thread;
	}
  
  public void setApplication(String application) {
		this.application = application;
	}
  
  public String getApplication() {
		return application;
	}
  	
	@Override
	public String getComponentType() {		
		return ClientProcess.class.getSimpleName();
	}
}
