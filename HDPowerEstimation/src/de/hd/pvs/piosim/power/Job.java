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
package de.hd.pvs.piosim.power;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.calculation.BaseCalculation;

/**
 * A job to be performed with startTime, endTime and queueTime
 * @author Timo Minartz
 *
 */
public class Job {

	private BigDecimal startTime = new BigDecimal("-1");
	private BigDecimal duration = new BigDecimal("-1");
	private BigDecimal queueTime = new BigDecimal("-1");
	
	/**
	 * @return time in ms when this job was queued
	 */
	public BigDecimal getQueueTime() {
		return queueTime;
	}

	/**
	 * @param queueTime time when this job was queued
	 */
	public void setQueueTime(Time queueTime) {
		this.queueTime = queueTime.getCurrentTimeInMillis();
	}

	/**
	 * @return time in ms when job was started 
	 */
	public BigDecimal getStartTime() {
		return startTime;
	}

	/**
	 * @return duration in ms for this job
	 */
	public BigDecimal getDuration() {
		return duration;
	}
	
	/**
	 * @param duration duration in ms for this job
	 */
	public void setDuration(BigDecimal duration) {
		this.duration = duration;
	}
	
	/**
	 * Starts this job with the given start time
	 * @param startTime time in ms when this job is started
	 * @throws JobException if job duration is not set
	 */
	public void start(BigDecimal startTime) throws JobException {
		if(duration.doubleValue() < 0) 
			throw new JobException("Job not initialized");
		this.startTime = startTime;
	}

	/**
	 * Calls <code>start(Double startTime)</code> with the time in ms
	 * @param startTime
	 * @throws JobException if job duration is not set
	 */
	public void start(Time startTime) throws JobException {
		start(startTime.getCurrentTimeInMillis());
	}
	
	/**
	 * @param now Time to check against
	 * @return True if startTime + duration < now.getCurrentTimeInMillis();
	 * @throws JobException if job not started
	 */
	public boolean finished(Time now) throws JobException {
		if(startTime.doubleValue() < 0 || duration.doubleValue() < 0)
			throw new JobException("Job not started!");
		
		return BaseCalculation.sum(startTime,duration).compareTo(now.getCurrentTimeInMillis()) <= 0;
	}
}
