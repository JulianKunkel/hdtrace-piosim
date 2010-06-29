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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.replay.ReplayItem;

/**
 * This class implements a visualizer for the commandline for arrays
 * of values
 * @author Timo Minartz
 *
 */
public class CommandlineVisualizer implements Visualizer {
	private int maxXValue;
	private int minXValue;
	private double maxYValue = Double.MIN_VALUE;
	private double minYValue = Double.MAX_VALUE;
	private List<double[]> valuesList = new ArrayList<double[]>();
	private List<String> namesList = new ArrayList<String>();
	private int countRows = 10;

	public void addPanelValues(String name, BigDecimal[] values) {
		double[] valuesArray = new double[values.length];

		for (int i = 0; i < values.length; ++i) {
			valuesArray[i] = values[i].doubleValue();
			if (valuesArray[i] < minYValue)
				minYValue = valuesArray[i];
			if (valuesArray[i] > maxYValue)
				maxYValue = valuesArray[i];
		}
		valuesList.add(valuesArray);
		namesList.add(name);
	}

	private void setConstraints() throws InvalidValueException {
		minXValue = 0;
		maxXValue = valuesList.get(0).length - 1;

		if (minYValue < 0)
			throw new InvalidValueException("values < 0 not specified");

		minYValue = 0;
	}

	@Override
	public void visualize() {

		try {
			setConstraints();
		} catch (Exception ex) {
			System.err.println(ex);
			return;
		}

		double blocksize = maxYValue / countRows;
		
		int i = 0;

		for (double[] values : valuesList) {
			System.out.println(namesList.get(i++));
			for (int row = 0; row < countRows; ++row) {

				for (int j = minXValue; j <= maxXValue; ++j) {
					if (values[j] < (maxYValue - row * blocksize)
							&& values[j] >= (maxYValue - (row + 1) * blocksize))
						System.out.print("x");
					else if (row == 0 && values[j] == maxYValue)
						System.out.print("x");
					else
						System.out.print(" ");
				}
				System.out.println();
			}
		}
	}

	@Override
	public void copyReplayItems(List<ReplayItem> items) {
		for(ReplayItem item : items) {
			addPanelValues(item.getReplayDevice().getACPIDevice().getName(), item.getReplayDevice().getUtilization());
			addPanelValues(item.getReplayDevice().getACPIDevice().getName(), item.getReplayDevice().getPowerConsumption());
		}
	}

	@Override
	public void setXAxisTitle(String string) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void reset() {
		
	}

	@Override
	public void copyReplayItems(List<ReplayItem> items, String strategyName) {
		copyReplayItems(items);
		
	}

	@Override
	public void setChartTitle(String chartTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPowerConsumptionYAxisTitle(String powerConsumptionYAxisTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUtilizationYAxisTitle(String utilizationYAxisTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isPrintLegend(boolean printLegend) {
		// TODO Auto-generated method stub
		
	}

}
