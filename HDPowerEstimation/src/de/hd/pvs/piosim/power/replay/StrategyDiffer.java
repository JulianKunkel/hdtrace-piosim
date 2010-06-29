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
package de.hd.pvs.piosim.power.replay;

import java.util.List;

import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;

/**
 * This class starts the same Replay with different strategies and visualizes
 * the power consumption
 * 
 * @author Timo Minartz
 * 
 */
public class StrategyDiffer {

	private Replay replay;
	private List<PlayStrategy> strategyList;

	public Replay getReplay() {
		return replay;
	}

	public void setReplay(Replay replay) {
		this.replay = replay;
	}

	public List<PlayStrategy> getStrategyList() {
		return strategyList;
	}

	public void setStrategyList(List<PlayStrategy> strategyList) {
		this.strategyList = strategyList;
	}

	/**
	 * For each PlayStrategy in list a replay is started. The ergs are
	 * visualized. For this, one panel for the utilization data and one panel
	 * for the power consumption data is created. The devices in the power
	 * consumption panel are named by each PlayStrategy
	 * 
	 * @param countSteps
	 *            steps for each Replay to perform
	 * @param visualizer
	 *            Visualizer for showing the Replay data
	 * @throws ReplayException
	 *             if replaying failed
	 * @throws VisualizerException
	 *             if visualizing failed
	 */
	public void playAndVisualize(int countSteps, Visualizer visualizer)
			throws ReplayException, VisualizerException {

		
		// for each strategy, start a replay and add
		// power consumption to visualizer
		for (PlayStrategy playStrategy : strategyList) {

			// play...
			replay.setPlayStrategy(playStrategy);
			replay.setCountSteps(countSteps);
			replay.play();

			// add utilization and power consumption data to visualizer
			String strategyName = playStrategy.getClass().getSimpleName();
			
			visualizer.copyReplayItems(replay.getReplayItems(), strategyName);

			// reset all components defined in this replay and global time
			replay.reset();
			
		}

		visualizer.visualize();

	}
}
