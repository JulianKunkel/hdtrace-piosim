package de.hd.pvs.piosim.model.interfaces;

/**
 * A model object is one which can be simulated by the simulator.
 * The simulator allows to select one of multiple possible implementations for the
 * object, i.e. the user selects one implementation encapsulating the model object.
 *
 * @author julian
 */
public interface IDynamicImplementationObject extends IDynamicModelComponent{
	public String getObjectType();
}
