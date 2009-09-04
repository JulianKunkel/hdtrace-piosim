
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.simulator;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;

public class SimulationResults {

	// number of events processed:
	final private long eventCount;

	// epoch of the simulation
	final private Epoch virtualTime;

	final private double wallClockTime;

	final private HashMap<ComponentIdentifier, ComponentRuntimeInformation> idtoRuntimeInformationMap;

	final private HashMap<ComponentIdentifier, SPassiveComponent> components;

	public SimulationResults(HashMap<ComponentIdentifier, SPassiveComponent> components, long eventCount, Epoch virtualTime, double wallClockTime, HashMap<ComponentIdentifier, ComponentRuntimeInformation> idtoRuntimeInformationMap) {
		this.eventCount = eventCount;
		this.components = components;
		this.virtualTime = virtualTime;
		this.wallClockTime = wallClockTime;
		this.idtoRuntimeInformationMap = idtoRuntimeInformationMap;
	}

	public HashMap<ComponentIdentifier, SPassiveComponent> getComponents() {
		return components;
	}

	/**
	 * Returns the number of processed events.
	 * @return the eventCount
	 */
	public long getEventCount() {
		return eventCount;
	}

	/**
	 * Returns the current simulation time.
	 * @return
	 */
	public Epoch getVirtualTime() {
		return virtualTime;
	}

	public double getWallClockTime() {
		return wallClockTime;
	}

	public HashMap<ComponentIdentifier, ComponentRuntimeInformation> getComponentStatistics() {
		return idtoRuntimeInformationMap;
	}
}
