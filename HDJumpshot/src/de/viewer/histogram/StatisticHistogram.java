//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package de.viewer.histogram;


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedStatisticsFileReader;
import de.hdTraceInput.StatisticStatistics;
import de.viewer.common.ButtonAutoRefresh;
import de.viewer.common.Const;
import de.viewer.common.IAutoRefreshable;
import de.viewer.common.LabeledSpinner;
import de.viewer.common.LabeledTextField;
import de.viewer.common.ModelTime;
import de.viewer.common.TimeEvent;
import de.viewer.common.TimeListener;
import de.viewer.graph.GraphAxis;
import de.viewer.graph.GraphData;
import de.viewer.graph.Histogram2D;
import de.viewer.graph.HistogramData;

/**
 * Show a histogram i.e. details for a single statistic of a group.
 * 
 * @author Julian M. Kunkel
 */
abstract public class StatisticHistogram<DATATYPE extends HistogramData> {

	final ModelTime modelTime;
	final JFrame frame;

	final BufferedStatisticsFileReader reader;
	final StatisticsDescription description;

	final HistogramGraph histogramGraph;

	/**
	 * The number of bins of the histogram to put the elements in.
	 */
	private int numberOfBins = 20;
	
	final LabeledSpinner binNumberSpinner = new LabeledSpinner("Number of bins", new SpinnerNumberModel(numberOfBins, 10, 1000, 10), 
			new ChangeListener(){
		@Override
		public void stateChanged(ChangeEvent e) {
			numberOfBins = (Integer) binNumberSpinner.getValue();								
			triggerRefreshHistogramData();		
		}
	});

	
	final LabeledTextField labelBin = new LabeledTextField( "Bin", Const.INTEGER_FORMAT );
	final LabeledTextField labelMinValue = new LabeledTextField( "Bin min", Const.FLOAT_FORMAT );;
	final LabeledTextField labelMaxValue = new LabeledTextField( "Bin max", Const.FLOAT_FORMAT );;

	final StatisticStatistics statistics;
	final CategoryStatistic category;

	/**
	 * Is the datatype for this histogram integer
	 */
	final boolean isInteger;

	// real data of the histogram:
	private DATATYPE histogramData;

	// value of bin increases per bin
	private double deltaPerBin;
	

	/**
	 * Create the toolbar menu and controls: 
	 * @param panel
	 */
	abstract protected void createToolbarOptions(JPanel panel);

	/**
	 * Compute the histogram.
	 * @return
	 */
	abstract protected DATATYPE computeHistogram(int numberOfBins, double min, double max, double deltaPerBin, Epoch start, Epoch end);
	
	/**
	 * The mouse is moved over the bin.
	 * @param bin
	 */
	abstract protected void histogramBinMouseOver(int bin, DATATYPE data);
	
	/**
	 * Draw additional information on the histogram drawing area
	 * @param g
	 */
	abstract protected void drawAdditionalInfoOnHistogramArea(Graphics2D g);
	
	/**
	 * Automatically refresh histogram if time changed (i.e. scrolled)
	 */
	private class MyTimeModifiedListener implements TimeListener{
		@Override
		public void timeChanged(TimeEvent evt) {
			triggerRefreshHistogramData();
		}
	}

	private MyTimeModifiedListener timeModifiedListener = new MyTimeModifiedListener();

	private class MyWindowClosedListener extends WindowAdapter{
		@Override
		public void windowClosed(WindowEvent e) {
			// don't forget to remove modelTime listener (if autoupdate), otherwise ressources are wasted
			modelTime.removeTimeListener(timeModifiedListener);
			super.windowClosed(e);
		}		
	}
	
	public StatisticHistogram(BufferedStatisticsFileReader reader, StatisticsDescription description, ModelTime modelTime, CategoryStatistic category ) {
		this.modelTime = modelTime;
		this.reader = reader;
		this.description = description;
		this.statistics = reader.getStatisticsFor(description.getNumberInGroup());
		this.category = category;
		// now all important fields are set:
		this.histogramGraph = new HistogramGraph();
		
		this.histogramGraph.getYAxis().setIntegerType(true);
		
		// set the X axis value as an integer type if appropriate
		if(description.getDatatype() == StatisticsEntryType.INT32 || description.getDatatype() == StatisticsEntryType.INT64){
			this.histogramGraph.getXAxis().setIntegerType(true);
			this.isInteger = true;
		}else{
			this.isInteger = false;
		}

		frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
		frame.setPreferredSize(new Dimension(400, 250));
		frame.setResizable(true);

		labelBin.setEditable( false );		
		
		labelMinValue.setEditable( false );		
		
		labelMaxValue.setEditable( false );		

		// default on close operation:
		frame.addWindowListener(new MyWindowClosedListener());
	}

	public void triggerRefreshHistogramData(){
		histogramGraph.triggerRefreshHistogramData();
	}

	protected class HistogramGraph extends Histogram2D implements IAutoRefreshable{
		private static final long serialVersionUID = 1L;
		
		// position of the first real histogram pixel:
		private int   xOffsetByLabels;
		
		// automatically redraw on time modification:
		boolean isAutoRefresh;

		BackgroundThread backgroundThread = null;

		// background thread computing the histogram data:
		/**
		 * At most one of this thread is executed.  
		 * @author julian
		 */
		class BackgroundThread extends Thread{

			@Override
			public void run() {			
				double min = statistics.getMinValue();
				double max = statistics.getMaxValue();
				
				if(min == max){
					min = min * 0.5;
					max = max * 2 + 2;
				}
				
				deltaPerBin = (max - min) / numberOfBins;
				
				histogramData = computeHistogram(numberOfBins, min, max, deltaPerBin, 
						modelTime.getViewPositionAdjusted(), modelTime.getViewEndAdjusted());

				histogramGraph.removeAllLines();
				histogramGraph.addLine(histogramData);
				
				reloadData();				
			}
		}

		@Override
		protected void binMouseOver(int bin) {			
			double min = bin * deltaPerBin + statistics.getMinValue();
			double max = (bin +1) * deltaPerBin + statistics.getMinValue()  ;
			
			if(isInteger){
				long imin = (long) Math.ceil(min);
				long imax = (long) max;
				if(imin > imax)
					return;
				labelMinValue.setLong( imin );
				labelMaxValue.setLong( imax );
			}else{
				labelMinValue.setDouble( min );
				labelMaxValue.setDouble( max );				
			}
			
			labelBin.setInteger(bin + 1);
			
			histogramBinMouseOver(bin, histogramData);			
		}

		public HistogramGraph() {			
			setAutoRefresh(de.viewer.common.Parameters.ACTIVE_REFRESH);
		}

		/**
		 * Call it when the number of bins change or the time interval.
		 */
		public void triggerRefreshHistogramData(){			
			// automatically adapt the title.
			frame.setTitle( reader.getGroup().getName() + ":" + description.getName() + " (" +
					String.format("%.4f", modelTime.getViewPosition()) + "-" + 
					String.format("%.4f",(modelTime.getViewEnd()))
					+ ")"
			);
			
			backgroundThread = new BackgroundThread();
			backgroundThread.start();
		}

		@Override
		protected void drawGraph(Graphics2D g, GraphData line, GraphAxis xaxis, GraphAxis yaxis, int curGraph,
				int graphCount) 
		{			
			if(getNumberOfLines() == 0) // due to some background computation, data might not be available
				return;

			final int width = xaxis.getDrawSize();

			final double widthPerBin = getBarWidth() * xaxis.getPixelPerValue();
			if(widthPerBin < 1.0){
				binNumberSpinner.setValue(width);
				return;
			}

			super.drawGraph(g, line, xaxis, yaxis, curGraph,
					graphCount);
			
			drawAdditionalInfoOnHistogramArea(g);
		}

		@Override
		public boolean isAutoRefresh() {			
			return isAutoRefresh;
		}

		@Override
		public void setAutoRefresh(boolean autoRefresh) {
			if(autoRefresh == true){
				modelTime.addTimeListener(timeModifiedListener);
			}else{
				modelTime.removeTimeListener(timeModifiedListener);
			}

			isAutoRefresh = autoRefresh;
		}
	}

	public void show(){
		JPanel xPanel = new JPanel();
		xPanel.setLayout(new BoxLayout( xPanel, BoxLayout.X_AXIS));

		xPanel.add(binNumberSpinner);

		xPanel.add(labelBin);		

		xPanel.add(labelMinValue);				

		xPanel.add(labelMaxValue);		
		
		createToolbarOptions(xPanel);

		JButton autoRefresh_btn = new ButtonAutoRefresh(histogramGraph);
		xPanel.add( autoRefresh_btn );        


		final JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));		
		yPanel.setMinimumSize(frame.getPreferredSize());


		yPanel.add(xPanel);
		yPanel.add(histogramGraph.getDrawingArea());

		frame.setContentPane(yPanel);
		this.histogramGraph.triggerRefreshHistogramData();
		
		
		
		frame.setVisible(true);
	}	
	
	protected HistogramGraph getHistogramGraph() {
		return histogramGraph;
	}
	
	protected DATATYPE getHistogramData() {
		return histogramData;
	}	
}
