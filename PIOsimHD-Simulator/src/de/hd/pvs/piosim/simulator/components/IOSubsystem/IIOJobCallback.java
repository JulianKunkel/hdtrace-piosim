package de.hd.pvs.piosim.simulator.components.IOSubsystem;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.event.IOJob;

public interface IIOJobCallback {
	public void IOJobCompleted(IOJob job, Epoch time);
}
