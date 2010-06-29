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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIComponentException;
import de.hd.pvs.piosim.power.acpi.ACPIStateChangeJob;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

/**
 * All acpi changes are wrapped in jobs, these jobs are managed here in a queue.
 * 
 * @author Timo Minartz
 * 
 */
public class JobScheduler {
	private Queue<ACPIStateChangeJob> jobQueue = new LinkedList<ACPIStateChangeJob>();
	private int lastStateInQueue = 0;

	/**
	 * 
	 * @return count scheduled, not finished jobs
	 */
	public int getCountScheduledJobs() {
		return jobQueue.size();
	}

	/**
	 * 
	 * @return first job in queue (always started)
	 */
	public ACPIStateChangeJob getActiveJob() {
		if (jobQueue.size() == 0)
			return null;

		return jobQueue.peek();
	}

	/**
	 * Creates a new job and adds it to intern queue. For every state change is
	 * a new job created (meaning changing from state 0 to state 2 creates 2
	 * jobs)
	 * 
	 * @param component
	 *            Component this job affected
	 * @param state
	 *            State for device to change to
	 * @throws JobException
	 *             if job not startable
	 */
	public void add(ACPIComponent component, int state) throws JobException {

		List<ACPIStateChangeJob> splittedJobs = new ArrayList<ACPIStateChangeJob>();

		for (int i = lastStateInQueue + 1; i < state; ++i) {
			splittedJobs.add(new ACPIStateChangeJob(component, i));
		}

		for (int i = lastStateInQueue - 1; i > state; --i) {
			splittedJobs.add(new ACPIStateChangeJob(component, i));
		}

		splittedJobs.add(new ACPIStateChangeJob(component, state));

		for (ACPIStateChangeJob job : splittedJobs) {

			job.setQueueTime(Time.getInstance());
			if (jobQueue.size() == 0) {

				job.start(Time.getInstance());
			}
			jobQueue.add(job);
		}
		walkThroughQueue();
		lastStateInQueue = state;
	}

	/**
	 * Finish all jobs in queue and returns resulting time overhead based on
	 * actual time
	 * 
	 * @return time for all jobs in queue to finish (serial)
	 * @throws JobException
	 *             if job not startable or not executable
	 */
	public BigDecimal finishAllJobsInQueue() throws JobException {
		BigDecimal duration = new BigDecimal("0");
		walkThroughQueue();

		Time now = Time.getInstance();

		while (!jobQueue.isEmpty()) {
			ACPIStateChangeJob job = jobQueue.poll();

			if (job.getStartTime().doubleValue() > 0) {
				// job was already started, but not finished
				// duration = duration + (job.duration - (now - job.starttime))
				// duration =
				// duration.add(job.getDuration().subtract(now.getCurrentTimeInMillis().subtract(job.getStartTime())));
				duration = BaseCalculation.sum(duration, BaseCalculation
						.substract(job.getDuration(), BaseCalculation
								.substract(now.getCurrentTimeInMillis(), job
										.getStartTime())));
			} else {

				job.start(job.getQueueTime().add(duration));

				duration = BaseCalculation.sum(duration, job.getDuration());
			}

			try {
				job.end();
			} catch (ACPIComponentException e) {
				throw new JobException("Job not executable", e.getStackTrace());
			}

		}

		return duration;
	}

	private void walkThroughQueue() throws JobException {
		Time now = Time.getInstance();

		while (!jobQueue.isEmpty()) {
			ACPIStateChangeJob job = jobQueue.peek();

			if (job.finished(now)) {
				try {
					job.end();
				} catch (ACPIComponentException e) {
					throw new JobException("Job not executable", e
							.getStackTrace());
				}
				jobQueue.poll();

				// BigDecimal jobFinishedTime =
				// job.getStartTime().add(job.getDuration());
				BigDecimal jobFinishedTime = BaseCalculation.sum(job
						.getStartTime(), job.getDuration());

				if (jobQueue.size() > 0) {
					ACPIStateChangeJob nextJob = jobQueue.peek();

					if (nextJob.getQueueTime().compareTo(jobFinishedTime) <= 0) {
						// next job was in queue and could be started just
						// after the last one (nextjob.queuetime <=
						// jobFinishedTime)
						nextJob.start(jobFinishedTime);
					} else if (nextJob.getQueueTime().compareTo(
							now.getCurrentTimeInMillis()) <= 0) {
						// next job was in queue and could be started just
						// when added to queue
						nextJob.start(nextJob.getQueueTime());
					}
				}

			} else {
				break;
			}
		}

	}

	/**
	 * Finish all jobs which are already done by actual time
	 * 
	 * @throws JobException
	 */
	public void refresh() throws JobException {
		walkThroughQueue();
	}
}
