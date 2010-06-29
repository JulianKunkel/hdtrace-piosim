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

import org.junit.Test;

import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.acpi.history.CommandLineHistoryExporter;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class CommandLineHistoryExporterTest extends AbstractTestCase {

	
	@Test
	public void testBasics() {
		playOptimal();
		
		CommandLineHistoryExporter.export(ACPIStateChangesHistory.getInstance());	
	}
	
	private void playOptimal() {
		
		try {
			Replay replay = TestObjectCreator.createTestReplayWith3DevicesAndDifferentUtilizationAndOptimalStrategy();
			
			replay.play();
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
