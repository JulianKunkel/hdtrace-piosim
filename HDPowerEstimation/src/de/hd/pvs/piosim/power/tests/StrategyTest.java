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

import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class StrategyTest extends AbstractTestCase {
	
	private Replay replay;
	private ExtendedNode node;

	@Test
	public void testDiffStrategies() {
		try {
			replay = TestObjectCreator.createTestReplayWith3DevicesAndDifferentUtilization(new SimplePlayStrategy());
			node = NodeFactory.createEmptyExtendedNode("extNode");
			
			for(ReplayItem item : replay.getReplayItems())
				node.add(item.getReplayDevice().getACPIDevice());
			
			BigDecimal simple = getSimpleStrategyConsumption();
			BigDecimal optimal = getOptimalStrategyConsumption();
			BigDecimal approach = getApproachStrategyConsumption();
			
			System.out.println(simple + " " + optimal + " " + approach);
			
			assertEquals(true,simple.doubleValue() >= optimal.doubleValue());
			assertEquals(true,optimal.doubleValue() >= approach.doubleValue());
			
			assertGreaterOrEqual(simple, optimal);
			assertGreaterOrEqual(optimal, approach);
			
			PowerSupply powerSupply = new PowerSupply();
			powerSupply.setProcentualOverhead(new BigDecimal("0.35"));
			
			node.setPowerSupply(powerSupply);
			
			BigDecimal simpleWithOverhead = getSimpleStrategyConsumption();
			
			assertGreaterOrEqual(simpleWithOverhead, simple);
			
			BigDecimal optimalWithOverhead = getOptimalStrategyConsumption();
			
			assertGreaterOrEqual(optimalWithOverhead, optimal);
			
			BigDecimal approachWithOverhead = getApproachStrategyConsumption();
			assertGreaterOrEqual(approachWithOverhead, approach);
			
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReplayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private BigDecimal getSimpleStrategyConsumption() throws ReplayException {
		replay.reset();
		replay.setPlayStrategy(new SimplePlayStrategy());
		replay.play();
		return node.getPowerConsumption();
	}

	private BigDecimal getOptimalStrategyConsumption() throws ReplayException {
		replay.reset();
		replay.setPlayStrategy(new OptimalPlayStrategy());
		replay.play();
		return node.getPowerConsumption();
	}

	private BigDecimal getApproachStrategyConsumption() throws ReplayException {
		replay.reset();
		replay.setPlayStrategy(new ApproachPlayStrategy());
		replay.play();
		return node.getPowerConsumption();
	}

}
