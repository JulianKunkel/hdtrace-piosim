
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

package de.hd.pvs.piosim.simulator.base;
import de.hd.pvs.piosim.model.components.FakeBasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.output.ComponentLogger;

/**
 * Basic component which contains a particular "type" of ModelComponents.
 * This component never gets events from the simulator and thus is considered passive.
 * It can be used to create new events or as a container delegating events.
 * 
 * @author Julian M. Kunkel
 *
 * @param <ModelComp> The type of components in this passive component 
 */
public class SPassiveComponent<ModelComp extends BasicComponent> {

	static private ComponentLogger logger = new ComponentLogger();
	
	/**
	 * Which model component is represented by this object
	 * Even if a component is a virtual component it needs to connect to a "fake" or 
	 * duplicated Model component. Otherwise the Simulator is error-prone ! 
	 */
	private ModelComp modelComponent = null;
	
	/**
	 * back reference to the simulator.
	 */
	private Simulator simulator = null;

	/**
	 * @return the simulator
	 */
	final public Simulator getSimulator() {
		assert(simulator != null);
		return simulator;
	}


	final public ModelComp getModelComponent(){
		assert(modelComponent != null);
		return modelComponent;
	}

	final public ComponentIdentifier getIdentifier() {
		assert(modelComponent != null);
		return modelComponent.getIdentifier();
	}

	final public String getName(){
		return getIdentifier().getName();
	}

	@Override
	final public int hashCode() {
		return getIdentifier().hashCode();
	}

	/**
	 * This method gets invoked by the simulator to signal completion of the simulation. 
	 */
	public void simulationFinished() {

	}

	/**
	 * Set the component this object tries to simulate. Even if a component is a virtual component
	 * it needs to connect to a "fake" Model component.
	 * The components should be attached after the model build completed.
	 * @param comp The actual Model component
	 */
	public void setSimulatedModelComponent(ModelComp comp, Simulator sim) throws Exception{
		if (this.modelComponent != null){
			throw new IllegalArgumentException("BasicComponent already set to: " + this.modelComponent);
		}


		if (FakeBasicComponent.class.isInstance( comp )){
			int lastID = sim.getModel().getMaxComponentID() + sim.getExistingSimulationObjects().size() + 1;
			comp.getIdentifier().setID(lastID);

		}else if( ! sim.getModel().isComponentInModel(comp) ){
			// FakeBasicComponent is ignored here, because it should not be in the model.
			throw new IllegalArgumentException("BasicComponent not in model: " + comp);
		}

		this.modelComponent = comp;
		this.simulator = sim;

		// add the Component to the Simulator to allow lookup later.
		sim.addSimulatedComponent(this);

		assert(this.modelComponent != null);
		assert(this.simulator != null);
	}

	/**
	 * Provided for the IGComponent interface.
	 * @return
	 */
	public final SPassiveComponent<ModelComp> getSimulatorObject(){
		return this;
	}

	/**
	 * Print some debugging information if the configurations allows it.
	 * 
	 * @param what
	 */
	final public boolean debug(String what){
		return logger.debug(this, what);
	}
	
	final public boolean debugFollowUpLine(String what){
		return logger.debugFollowUpline(this, what);
	}
	
	final public void info(String what){
		logger.info(this, what);
	}
	
	final public void warn(String what){
		logger.warn(this, what);
	}	
	
	final public void infoFollowUpLine(String what){		
		System.out.println(" -> " + what);
	}
}
