
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

package de.hd.pvs.piosim.simulator.output;

import de.hd.pvs.piosim.model.logging.ConsoleLogger;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;

/**
 * This class 
 * 
 * @author Julian M. Kunkel
 *
 */
public class ComponentLogger extends ConsoleLogger {
	/**
	 * Print the message with the stack trace.
	 * @param what
	 */
	public void info(SPassiveComponent component, String what){		
		StringBuffer buff = new StringBuffer();
		buff.append(component.getSimulator().getVirtualTime().getFullDigitString() 
				+ " " + component.getClass().getSimpleName() +  " <" +  component.getIdentifier() + ">: ");
		buff.append(what); 
		getStackTrace(buff, 3);
		
		System.out.print(buff.toString());
	}
	
	/**
	 * Print the message with the stack trace.
	 * @param what
	 */
	public void warn(SPassiveComponent component, String what){		
		StringBuffer buff = new StringBuffer();
		buff.append(component.getSimulator().getVirtualTime().getFullDigitString() + " [WARN] " 
				+ component.getClass().getSimpleName() +  " <" +  component.getIdentifier() + ">: ");
		buff.append(what);
		getStackTrace(buff, 3);
		
		System.out.print(buff.toString());
	}
	
	public boolean isDebuggable(SPassiveComponent component){
		if(debugAll){
			return true;
		}
		
		return basicComponentIDsToTrace.contains(component.getIdentifier().getID()) ||
			canonicalClassNamesToTrace.contains(component.getClass().getCanonicalName());
	}

	/**
	 * Print the message with the stack trace, depending on the component log or not.
	 *  
	 * @param object the calling object.
	 * @param what
	 */
	public boolean debug(SPassiveComponent component, String what){
		if (! isDebuggable(component)){
			return true;
		}
		
		StringBuffer buff = new StringBuffer();
		buff.append(component.getSimulator().getVirtualTime().getFullDigitString() 
				+ " " + component.getClass().getSimpleName() +  " <" +  component.getIdentifier() + ">: ");
		buff.append(what);
		getStackTrace(buff, 3);
		
		System.out.print(buff.toString());
		
		return true;
	}
	
	/**
	 * Print another line, but without stack trace.
	 * 
	 * @param object the calling object
	 * @param what
	 */
	public boolean debugFollowUpline(SPassiveComponent component, String what){
		if (! isDebuggable(component)){
			return true;
		}

		System.out.println(" -> " + what);
		return true;
	}
}
