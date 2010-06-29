//	Copyright (C) 2010 Timo Minartz
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
package de.hd.pvs.piosim.power.acpi;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.Job;
import de.hd.pvs.piosim.power.JobException;

public class ACPIStateChangeJob extends Job {
	
	private ACPIComponent component; 
	private int targetACPIState;
	
	public ACPIStateChangeJob(ACPIComponent component, int targetACPIState) {
		this.component = component;
		this.targetACPIState = targetACPIState;
	}
	
	/**
	 * 
	 * @return targetACPIState of this job
	 */
	public int getTargetACPIState() {
		return targetACPIState;
	}

	/**
	 * Ends this job. Calls <code>startDevicePowerState(state, time)</code> on the acpi component to start the new state.
	 * Time is the startTime of this job + duration of this job
	 * @throws ACPIComponentException if no valid acpi state
	 */
	public void end() throws ACPIComponentException {
		component.startDevicePowerState(targetACPIState, this.getStartTime().add(this.getDuration()));
	}
	
	/**
	 * Sets duration for this job (from <code>getDurationForACPIStateChange(state)</code> method)
	 * and starts job. Calls <code>endDevicePowerState(time)</code> on the acpi component to end the last state.
	 * @param startTime Time this job should be started (possibly in the past)
	 * @throws JobException if duration not valid
	 */
	@Override
	public void start(BigDecimal startTime) throws JobException {
		this.setDuration(component.getDurationForDevicePowerStateChange(targetACPIState));
		component.endDevicePowerState(startTime);
		super.start(startTime);
	}
	
	/**
	 * Returns duration for this job. If duration not initialized, method
	 * <code>getDurationForACPIStateChange(state)</code> is called and returned
	 * @return duration for this job
	 */
	@Override
	public BigDecimal getDuration() {
		BigDecimal duration = super.getDuration();
		
		if(duration.doubleValue() < 0)
			duration = component.getDurationForDevicePowerStateChange(targetACPIState);
		
		return duration;
	}
	
	@Override
	public String toString() {
		return component + ": change to ACPI State " + targetACPIState + " " + this.getStartTime() + " - " + (this.getStartTime().add(this.getDuration()));
	}
	
}
