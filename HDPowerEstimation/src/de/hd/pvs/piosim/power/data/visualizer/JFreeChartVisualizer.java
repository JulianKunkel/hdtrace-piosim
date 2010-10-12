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
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.hd.pvs.piosim.power.replay.ReplayItem;

public abstract class JFreeChartVisualizer implements Visualizer {

	private Thread thread;

	protected String xAxisTitle;
	protected String chartTitle;
	protected String utilizationYAxisTitle;
	protected String powerConsumptionYAxisTitle;
	protected VisualizationData visualizationData;
	protected boolean printLegend = true;
	protected boolean printDetails = true;
	
	@Override
	public void isPrintLegend(boolean printLegend) {
		this.printLegend = printLegend;
	}
	
	public void setDetails(Map<String,String> details) {
		visualizationData.setDetails(details);
	}
	
	public Map<String,String> getDetails() {
		return visualizationData.getDetails();
	}
	
	private void createFrame() {
		JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());

		JPanel summaryPanel = new JPanel();

		summaryPanel.setLayout(new GridLayout(visualizationData.getCountRows(),
				2));

		for (String label : visualizationData.getSummary()) {
			summaryPanel.add(new JLabel(label));
		}

		addPanels(frame);

		frame.getContentPane().add(new JScrollPane(summaryPanel),
				BorderLayout.SOUTH);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	protected abstract void addPanels(JFrame frame);

	@Override
	public void visualize() throws VisualizerException {

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createFrame();
			}
		});

		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					thread = Thread.currentThread();
				}
			});

			if (thread != null) {
				thread.join();
			}
		} catch (InterruptedException e) {
			throw new VisualizerException(e);
		} catch (InvocationTargetException e) {
			throw new VisualizerException(e);
		}

	}

	@Override
	public void copyReplayItems(List<ReplayItem> items) {		
		copyReplayItems(items, null);
	}
	
	@Override
	public void copyReplayItems(List<ReplayItem> items, String name) {
		if(visualizationData == null)
			visualizationData = new VisualizationData();
		
		visualizationData.addReplayItems(items,name);
	}

	@Override
	public void setXAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}
	
	@Override
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}
	
	@Override
	public void setUtilizationYAxisTitle(String utilizationYAxisTitle) {
		this.utilizationYAxisTitle = utilizationYAxisTitle;
	}

	@Override
	public void setPowerConsumptionYAxisTitle(String powerConsumptionYAxisTitle) {
		this.powerConsumptionYAxisTitle = powerConsumptionYAxisTitle;
	}
	
	@Override
	public void reset() {
		visualizationData = null;
	}
	

	@Override
	public void printDetails(boolean printDetails) {
		this.printDetails = printDetails;
	}
	
}
