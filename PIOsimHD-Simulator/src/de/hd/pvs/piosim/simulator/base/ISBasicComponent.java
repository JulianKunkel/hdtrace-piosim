package de.hd.pvs.piosim.simulator.base;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;

public interface ISBasicComponent<ModelComp extends IBasicComponent>
	extends ISPassiveComponent<ModelComp>
{

	/**
	 * The simulator calls this function to allow the component to actually run its next event.
	 * This means the particular component must have an event which must be scheduled next.
	 */
	abstract public void processEvent(Event event, Epoch time);

	/**
	 * Process an event the component started for itself
	 * @param event
	 */
	abstract public void processInternalEvent(InternalEvent event, Epoch time);

}