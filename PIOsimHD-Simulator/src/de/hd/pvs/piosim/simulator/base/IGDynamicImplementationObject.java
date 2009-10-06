package de.hd.pvs.piosim.simulator.base;

import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;

/**
 * This interface encapsulates a model object linked to the object simulating its behavior.
 *
 * @author julian
 */
public interface IGDynamicImplementationObject<ModelComp extends IDynamicImplementationObject> {
	public ModelComp getModelComponent();

	/**
	 * Set the component this object tries to simulate. Even if a component is a virtual component
	 * it needs to connect to a "fake" Model component.
	 * The components should be attached after the model build completed.
	 * @param comp The actual Model component
	 */
	public void setModelComponent(ModelComp comp) throws Exception;
}
