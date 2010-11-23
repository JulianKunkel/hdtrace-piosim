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

import java.math.BigDecimal;

import org.junit.Test;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChange;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.devices.SimpleDisk;

public class ACPIStateChangesHistoryTest extends AbstractTestCase {
	
	@Test
	public void testBasicFeatures() {
		ACPIStateChangesHistory history = ACPIStateChangesHistory.getInstance();
		
		assertEquals(0,history.getSize());
		
		// test adding
		
		ACPIComponent disk = new SimpleDisk().getACPIComponent();
		
		assertEquals(1,history.getSize()); // initial state
		
		history.add(new BigDecimal("0"), disk, 0, new BigDecimal("10"));
		
		assertEquals(2,history.getSize()); // above created entry
		
		ACPIStateChange entry = history.get(1);
		
		assertEquals(0.0,entry.getTime().doubleValue());
		assertEquals(disk,entry.getACPIComponent());
		assertEquals(0,entry.getState());
		

		// test singleton
		assertEquals(2,ACPIStateChangesHistory.getInstance().getSize());
		
		entry = ACPIStateChangesHistory.getInstance().get(1);
		
		assertEquals(0.0,entry.getTime().doubleValue());
		assertEquals(disk,entry.getACPIComponent());
		assertEquals(0,entry.getState());
		
		// test copy
		ACPIStateChangesHistory historyCopy = ACPIStateChangesHistory.getCopy();
		
		assertEquals(2,historyCopy.getSize());
		
		entry = historyCopy.get(1);
		
		assertEquals(0.0,entry.getTime().doubleValue());
		assertEquals(disk,entry.getACPIComponent());
		assertEquals(0,entry.getState());
		
		// test reseting
		ACPIStateChangesHistory.getInstance().reset();
		
		assertEquals(0,ACPIStateChangesHistory.getInstance().getSize());
		assertEquals(0,history.getSize());
		assertEquals(2,historyCopy.getSize());
		
		entry = historyCopy.get(1);
		
		assertEquals(0.0,entry.getTime().doubleValue());
		assertEquals(disk,entry.getACPIComponent());
		assertEquals(0,entry.getState());
		
	}
	
	@Test
	public void testHistoryEntries() {
		MockDevice mock = new MockDevice();
		mock.setName("mock");
		
		mock.setStateEnergyConsumption(0, 100);
		mock.setStateEnergyConsumption(1, 10);
		
		mock.setIncStateDuration(0, 1000);
		mock.setIncStateEnergyConsumption(0, 50);
		
		assertEquals(0.0, mock.getEnergyConsumption().doubleValue());
		assertEquals(0, mock.getDevicePowerState());
		
		Time.getInstance().timePassed(3600000);
		
		assertEquals(100.0, mock.getEnergyConsumption().doubleValue());
		assertEquals(0, mock.getDevicePowerState());
		
		try {
			mock.toACPIState(1);
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		Time.getInstance().timePassed(1000);
		
		assertEquals(150.0, mock.getEnergyConsumption().doubleValue());
		assertEquals(1, mock.getDevicePowerState());
		
		Time.getInstance().timePassed(3600000);
		
		assertEquals(160.0, mock.getEnergyConsumption().doubleValue());
		assertEquals(1, mock.getDevicePowerState());
		
		mock.getACPIComponent().finalizeComponent();
		
		assertEquals(160.0, mock.getEnergyConsumption().doubleValue());
		assertEquals(1, mock.getDevicePowerState());
		
		ACPIStateChangesHistory.getInstance().print();
		
		for(int i=0; i<ACPIStateChangesHistory.getInstance().getSize(); ++i) {
			ACPIStateChange entry = ACPIStateChangesHistory.getInstance().get(i);
			
			assertEquals(mock.getACPIComponent(),entry.getACPIComponent());
			
			if(i == 0) {
				assertEquals(0.0, entry.getTime().doubleValue());
				assertEquals(0, entry.getState());
			}
			
			if(i == 1) {
				assertEquals(3600000.0, entry.getTime().doubleValue());
				assertEquals(ACPIStateChangesHistory.STATE_END, entry.getState());
			}
			
			if(i == 2) {
				assertEquals(3600000.0, entry.getTime().doubleValue());
				assertEquals(ACPIStateChangesHistory.STATE_CHANGE, entry.getState());
			}
			
			if(i == 3) {
				assertEquals(3601000.0, entry.getTime().doubleValue());
				assertEquals(ACPIStateChangesHistory.STATE_END, entry.getState());
			}
			
			if(i == 4) {
				assertEquals(3601000.0, entry.getTime().doubleValue());
				assertEquals(1, entry.getState());
			}
			
			if(i == 5) {
				assertEquals(7201000.0, entry.getTime().doubleValue());
				assertEquals(ACPIStateChangesHistory.STATE_END, entry.getState());
			}
				
		}
		
	}

}
