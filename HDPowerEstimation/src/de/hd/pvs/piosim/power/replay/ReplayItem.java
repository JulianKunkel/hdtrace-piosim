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

import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;

public class ReplayItem {
	
	private PlayStrategy playStrategy;
	private ReplayDevice replayDevice;
	
	public PlayStrategy getPlayStrategy() {
		return playStrategy;
	}
	
	public void setPlayStrategy(PlayStrategy playStrategy) {
		this.playStrategy = playStrategy;
	}
	
	public ReplayDevice getReplayDevice() {
		return replayDevice;
	}

	public void setReplayDevice(ReplayDevice replayDevice) {
		this.replayDevice = replayDevice;
	}

	public void step(int countSteps, int stepsize) throws ReplayException {
		replayDevice.setStepsize(stepsize);
		playStrategy.step(replayDevice, countSteps);
	}
	
	public void reset() {
		replayDevice.reset();
	}
}
