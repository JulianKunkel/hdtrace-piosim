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
package de.hd.pvs.piosim.power.data.visualizer;

import java.util.List;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.replay.ReplayItem;

public interface Visualizer {
	
	public Logger logger = Logger.getLogger(Visualizer.class);

	public void visualize() throws VisualizerException;
	public void setXAxisTitle(String string);
	public void copyReplayItems(List<ReplayItem> items);
	public void copyReplayItems(List<ReplayItem> items, String strategyName);
	public void reset();
	public void setChartTitle(String chartTitle);
	public void setUtilizationYAxisTitle(String utilizationYAxisTitle);
	public void setPowerConsumptionYAxisTitle(String powerConsumptionYAxisTitle);
	public void isPrintLegend(boolean printLegend);
	public void printDetails(boolean printDetails);

}
