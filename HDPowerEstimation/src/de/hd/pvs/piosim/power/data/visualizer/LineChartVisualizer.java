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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Visualizes arrays of values
 * 
 * @author Timo Minartz
 * 
 */
public class LineChartVisualizer extends JFreeChartVisualizer {

	@Override
	protected void addPanels(JFrame frame) {

		frame.getContentPane().add(
				new JScrollPane(new ChartPanel(ChartFactory.createLineChart(
						null, xAxisTitle, "Utilization in percent",
						visualizationData.getUtilizationDataset(),
						PlotOrientation.VERTICAL, true, false, false))),
				BorderLayout.WEST);

		frame.getContentPane().add(
				new JScrollPane(new ChartPanel(ChartFactory.createLineChart(
						null, xAxisTitle, "Power consumption in Watt",
						visualizationData.getPowerConsumptionDataset(),
						PlotOrientation.VERTICAL, true, false, false))),
				BorderLayout.EAST);

	}


}
