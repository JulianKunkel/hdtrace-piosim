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
package de.hd.pvs.piosim.power.tests;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.JobException;
import de.hd.pvs.piosim.power.JobScheduler;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.devices.SimpleDisk;

public class JobSchedulerTest extends AbstractTestCase {

	JobScheduler scheduler;
	Time startTime;
	Logger logger = Logger.getLogger(JobSchedulerTest.class);

	@Test
	public void testSchedulerAddMethodSplitting() {
		JobScheduler scheduler = new JobScheduler();
		ACPIComponent component = new SimpleDisk().getACPIComponent();
		Time startTime = Time.getInstance();

		assertEquals(component.getDevicePowerState(),
				DevicePowerStates.DEVICE_POWER_STATE_0);

		assertEquals(0, scheduler.getCountScheduledJobs());

		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_2);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // 110 ms

		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // 220 ms

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

		startTime.timePassed(100);
		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // 10 ms

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

		startTime.timePassed(100);
		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_3);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // 1100 ms

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(5, scheduler.getCountScheduledJobs());

		startTime.timePassed(1000);
		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // 2220 ms
		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

	}

	@Test
	public void testSchedulerAddMethod() {
		JobScheduler scheduler = new JobScheduler();
		ACPIComponent component = new SimpleDisk().getACPIComponent();
		Time startTime = Time.getInstance();

		assertEquals(component.getDevicePowerState(),
				DevicePowerStates.DEVICE_POWER_STATE_0);

		assertEquals(0, scheduler.getCountScheduledJobs());

		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_2);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

		startTime.timePassed(100);
		try {
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

		try {
			startTime.timePassed(100);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_2);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_3);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(5, scheduler.getCountScheduledJobs());

		try {
			startTime.timePassed(1000);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_2);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1);
			scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0);
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(4, scheduler.getCountScheduledJobs());

	}

	@Test
	public void testSchedulerFinishAllJobsInQueue() {
		JobScheduler scheduler = new JobScheduler();
		ACPIComponent component = new SimpleDisk().getACPIComponent();
		Time startTime = Time.getInstance();

		logger.info("Time: " + startTime.getCurrentTimeInMillis());

		try {
		scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_2); // 110 ms
		scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0); // 220 ms
		scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_1); // 10 ms
		scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_3); // 1100 ms
		scheduler.add(component, DevicePowerStates.DEVICE_POWER_STATE_0); // 2220 ms
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		logger.info("Time: " + startTime.getCurrentTimeInMillis());
		logger.info("Count scheduled Jobs: "
				+ scheduler.getCountScheduledJobs());
		assertEquals(10, scheduler.getCountScheduledJobs());
		
		double count = 0;
		try {
			count = scheduler.finishAllJobsInQueue().doubleValue();
		} catch (JobException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(count == 3660);
	}

}
