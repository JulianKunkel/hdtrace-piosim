package de.hd.pvs.piosim.simulator;

import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;

/**
 * Encapsulates an interface to map model components to instanciated simulated
 * objects.
 *
 * @author julian
 */
public interface IModelToSimulatorMapper<ModelType extends ISPassiveComponent> {
	public ModelType getSimulatedComponent(ComponentIdentifier cid);

	/**
	 * Get the SimulationComponent for a particular model component.
	 * @param mComponent
	 * @return
	 */
	public ModelType getSimulatedComponent(IBasicComponent mComponent);
}
