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
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.JobException;
import de.hd.pvs.piosim.power.JobScheduler;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public abstract class ACPIDevice extends ACPIComponentFacade {

	public static final int RUNNING = 0;
	public static final int IDLE = 1;

	private static List<ACPIDevice> devices = new ArrayList<ACPIDevice>();
	private int deviceState = IDLE;
	private BigDecimal devicePowerStateChangesOverhead = new BigDecimal("0");
	private JobScheduler scheduler = new JobScheduler();

	/**
	 * must be implemented from all extending devices with device specific power
	 * settings (i.e. device power state 1 -> x watt)
	 */
	public abstract void initComponentPowerConsumption();

	/**
	 * must be implemented from all extending devices for device specific
	 * converting (i.e. memory given in mbyte -> convert to percent)
	 */
	public abstract BigDecimal convertToPercentualUtilization(double value,
			String unit) throws ConvertingException;

	protected ACPIDevice() {
		register(this);
		initComponentPowerConsumption();
	}

	/* registering stuff */

	public static void register(ACPIDevice device) {
		devices.add(device);
	}

	public static boolean deregister(ACPIDevice device) {
		return devices.remove(device);
	}

	public static void deregisterAllDevices() {
		devices.clear();
	}

	/**
	 * 
	 * @return List of all instantiated ACPIDevices
	 */
	public static List<ACPIDevice> getDevices() {
		return devices;
	}

	public static ACPIDevice getACPIDevice(String nodeName, String deviceName) {

		for (ACPIDevice device : devices) {
			if (device.getName().equals(deviceName)
					&& device.getNode().getName().equals(nodeName))
				return device;
		}

		return null;

	}

	/**
	 * Calls a refresh on the scheduler for the acpiChanges and finish all
	 * incomplete jobs in scheduler. If resulting state in not
	 * ACPI_DEVICE_STATE_0, a new job is added to scheduler and finished. So
	 * after method call, device is in ACPI_DEVICE_STATE_0 and
	 * ACPIDevice.RUNNING. A time overhead is possible.
	 * 
	 * @throws ComponentException
	 */
	public void run() throws ComponentException {

		try {
			scheduler.refresh();

			if (this.deviceState == RUNNING)
				return;

			if (scheduler.getCountScheduledJobs() > 0) {

				devicePowerStateChangesOverhead = BaseCalculation.sum(
						devicePowerStateChangesOverhead,
						scheduler.finishAllJobsInQueue());
			}

			if (this.getDevicePowerState() != DevicePowerStates.DEVICE_POWER_STATE_0) {
				scheduler.add(this.getACPIComponent(),
						DevicePowerStates.DEVICE_POWER_STATE_0);

				devicePowerStateChangesOverhead = BaseCalculation.sum(
						devicePowerStateChangesOverhead,
						scheduler.finishAllJobsInQueue());
			}

			this.deviceState = RUNNING;

		} catch (JobException e) {
			throw new ComponentException("Scheduler refresh failed",
					e.getStackTrace());
		}
	}

	/**
	 * Changes device state to ACPIDevice.IDLE
	 */
	public void stop() {
		this.deviceState = IDLE;
	}

	/**
	 * 
	 * @return true if this device is running
	 */
	public boolean isRunning() {
		return this.deviceState == RUNNING;
	}

	/**
	 * Schedules a change to ACPI_DEVICE_STATE_3 if not running
	 * 
	 * @throws ACPIDeviceException
	 */
	public void toSleep() throws ACPIDeviceException {
		if (this.isRunning()) {
			throw new ACPIDeviceException("Device is running");
		}

		try {
			scheduler.add(this.getACPIComponent(),
					DevicePowerStates.DEVICE_POWER_STATE_3);
		} catch (JobException e) {
			throw new ACPIDeviceException("Adding to scheduler failed",
					e.getStackTrace());
		}
	}

	/**
	 * Schedules a change to ACPI_DEVICE_STATE_0
	 * 
	 * @throws ACPIDeviceException
	 */
	public void toWork() throws ACPIDeviceException {
		try {
			scheduler.add(getACPIComponent(),
					DevicePowerStates.DEVICE_POWER_STATE_0);
		} catch (JobException e) {
			throw new ACPIDeviceException("Adding to scheduler failed",
					e.getStackTrace());
		}
	}

	/**
	 * Schedules a change to ACPI_DEVICE_STATE_2 if not running
	 * 
	 * @throws ACPIDeviceException
	 */
	public void toSuspend() throws ACPIDeviceException {
		if (this.isRunning())
			throw new ACPIDeviceException("Device is running");

		try {
			scheduler.add(getACPIComponent(),
					DevicePowerStates.DEVICE_POWER_STATE_2);
		} catch (JobException e) {
			throw new ACPIDeviceException("Adding to scheduler failed",
					e.getStackTrace());
		}

	}

	/**
	 * Schedules a change to <code>state</code>, if device is running.
	 * Otherwise, call run() first.
	 * 
	 * @throws ACPIDeviceException
	 */
	public void toACPIState(int state) throws ACPIDeviceException {
		if (state != DevicePowerStates.DEVICE_POWER_STATE_0 && this.isRunning())
			throw new ACPIDeviceException("Device is running");

		try {
			scheduler.add(getACPIComponent(), state);
		} catch (JobException e) {
			throw new ACPIDeviceException("Adding to scheduler failed",
					e.getStackTrace());
		}

	}

	/**
	 * 
	 * Overwrites the acpiComponents getPowerConsumption() method to calculate
	 * the accurate power consumption till now. If a device state change is
	 * running, the (partial) power consumption for the change is included
	 * 
	 * @return Power consumption for this device till now in watt-h
	 */
	@Override
	public BigDecimal getPowerConsumption() {
		refresh();
		BigDecimal currentPowerConsumption = super.getCurrentPowerConsumption();
		BigDecimal powerConsumption = super.getPowerConsumption();

		if (scheduler.getCountScheduledJobs() > 0) {
			ACPIStateChangeJob job = scheduler.getActiveJob();

			if (!job.getStartTime().equals(
					Time.getInstance().getCurrentTimeInMillis())) {

				// only if job is really running

				BigDecimal duration = job.getDuration(); // in ms
				int targetACPIState = job.getTargetACPIState();

				// in watt-h
				BigDecimal powerConsumptionForChange = getPowerConsumptionForDevicePowerStateChange(targetACPIState);

				currentPowerConsumption = ACPICalculation.calculateInWatt(
						powerConsumptionForChange, duration);

			}
		}

		BigDecimal now = Time.getInstance().getCurrentTimeInMillis();

		BigDecimal lastChangeTime = getLastChangeTime();

		powerConsumption = ACPICalculation.sumPowerConsumptionTillNow(
				powerConsumption, currentPowerConsumption, now, lastChangeTime);

		return powerConsumption;
	}

	/**
	 * Overhead means the time, if device is not in device state 0 and a call to
	 * run() is performed, means device has to be powered on before doing any
	 * work. This overhead has to be minimized, to avoid changes of the
	 * resulting calculating time
	 * 
	 * @return the overhead for acpi state changes
	 */
	@Override
	public BigDecimal getACPIStateChangesTimeOverhead() {
		return devicePowerStateChangesOverhead;
	}

	/**
	 * Refresh this device. This means, all scheduled acpi state changes till
	 * now are performed and device is up to date. Should be called when
	 * implementing any calculation method.
	 */
	@Override
	public void refresh() {
		try {
			scheduler.refresh();
		} catch (JobException e) {
			System.err.println("Refreshing of scheduler failed:");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return count scheduled jobs greater than zero
	 */
	public boolean hasEnqueuedStateChanges() {
		refresh();
		return scheduler.getCountScheduledJobs() > 0;
	}

	@Override
	public BigDecimal getMaxPowerConsumption() {
		return this.getComponentPowerSchema().getStatePowerConsumption(0,
				new BigDecimal("1"));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getName();
	}

	/**
	 * Refreshed devices and changes utilization
	 * 
	 * @param utilization
	 *            future utilization for this device
	 * @throws ComponentException
	 */
	@Override
	public void changeUtilization(BigDecimal utilization)
			throws ComponentException {
		refresh();
		super.changeUtilization(utilization);
	}

}
