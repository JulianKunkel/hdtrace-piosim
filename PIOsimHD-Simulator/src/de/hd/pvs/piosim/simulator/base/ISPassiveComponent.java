
 /** Version Control Information $Id: SPassiveComponent.java 670 2009-09-04 18:47:24Z kunkel $
  * @lastmodified    $Date: 2009-09-04 20:47:24 +0200 (Fr, 04. Sep 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 670 $
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

package de.hd.pvs.piosim.simulator.base;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.Simulator;

/**
 * Basic component which contains a particular "type" of ModelComponents.
 * This component never gets events from the simulator and thus is considered passive.
 * It can be used to create new events or as a container delegating events.
 *
 * @author Julian M. Kunkel
 *
 * @param <ModelComp> The type of components in this passive component
 */
public interface ISPassiveComponent<ModelComp extends IBasicComponent>
	extends IGDynamicImplementationObject<ModelComp>
{
	/**
	 * @return the simulator
	 */
	 public Simulator getSimulator();

	 public ComponentIdentifier getIdentifier();

	 public String getName();

	/**
	 * This method gets invoked by the simulator to signal that the model is now completely
	 * build.
	 */
	public void simulationModelIsBuild();
	/**
	 * This method gets invoked by the simulator to signal completion of the simulation.
	 * Arbitrary data could be appended to the results.
	 */
	public void simulationFinished() ;

	/**
	 * Return information about the simulation.
	 * Override this method to really provide information
	 *
	 * @return
	 */
	public ComponentRuntimeInformation getComponentInformation();

	public void setSimulator(Simulator sim);

	/**
	 * Provided for the IGComponent interface.
	 * @return
	 */
	public ISPassiveComponent<ModelComp> getSimulatorObject();
}
