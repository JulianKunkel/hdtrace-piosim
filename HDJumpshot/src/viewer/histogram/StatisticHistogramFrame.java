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

package viewer.histogram;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.StatisticStatistics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import viewer.common.ButtonAutoRefresh;
import viewer.common.Const;
import viewer.common.IAutoRefreshable;
import viewer.common.LabeledSpinner;
import viewer.common.LabeledTextField;
import viewer.common.ModelTime;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.graph.GraphAxis;
import viewer.graph.GraphData;
import viewer.graph.Histogram2D;
import viewer.graph.HistogramData;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.CategoryStatistic;

/**
 * Show a histogram i.e. details for a single statistic of a group.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticHistogramFrame {

	final ModelTime modelTime;
	final JFrame frame;

	final BufferedStatisticFileReader reader;
	final StatisticDescription description;

	final HistogramGraph histogramGraph;
	final LabeledSpinner binNumberSpinner;
	final LabeledTextField labelBin;
	final LabeledTextField labelNumberOfElements;
	final LabeledTextField labelMinValue;
	final LabeledTextField labelMaxValue;

	final StatisticStatistics statistics;
	final CategoryStatistic category;

	/**
	 * The number of bins of the histogram to put the elements in.
	 */
	int numberOfBins = 20;

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

	public StatisticHistogramFrame(BufferedStatisticFileReader reader, StatisticDescription description, ModelTime modelTime, CategoryStatistic category ) {
		this.modelTime = modelTime;
		this.reader = reader;
		this.description = description;
		this.statistics = reader.getStatisticsFor(description.getNumberInGroup());
		this.category = category;
		// now all important fields are set:
		this.histogramGraph = new HistogramGraph();

		frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
		frame.setPreferredSize(new Dimension(400, 250));
		frame.setResizable(true);

		JPanel xPanel = new JPanel();
		xPanel.setLayout(new BoxLayout( xPanel, BoxLayout.X_AXIS));

		binNumberSpinner = new LabeledSpinner("Number of bins", new SpinnerNumberModel(numberOfBins, 10, 1000, 10), 
				new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				numberOfBins = (Integer) binNumberSpinner.getValue();								
				triggerRefreshHistogramData();		
			}
		});

		xPanel.add(binNumberSpinner);

		labelBin    = new LabeledTextField( "Bin", Const.INTEGER_FORMAT );
		labelBin.setEditable( false );		
		xPanel.add(labelBin);		

		labelNumberOfElements    = new LabeledTextField( "# Elements", Const.INTEGER_FORMAT );
		labelNumberOfElements.setEditable( false );		
		xPanel.add(labelNumberOfElements);		

		labelMinValue    = new LabeledTextField( "MinValue", Const.FLOAT_FORMAT );
		labelMinValue.setEditable( false );		
		xPanel.add(labelMinValue);				

		labelMaxValue    = new LabeledTextField( "MaxValue", Const.FLOAT_FORMAT );
		labelMaxValue.setEditable( false );		
		xPanel.add(labelMaxValue);		

		JButton autoRefresh_btn = new ButtonAutoRefresh(histogramGraph);
		xPanel.add( autoRefresh_btn );        


		final JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));		
		yPanel.setMinimumSize(frame.getPreferredSize());


		yPanel.add(xPanel);
		yPanel.add(histogramGraph.getDrawingArea());

		frame.setContentPane(yPanel);

		// default on close operation:
		frame.addWindowListener(new MyWindowClosedListener());
	}

	public void triggerRefreshHistogramData(){
		histogramGraph.triggerRefreshHistogramData();
	}

	private class HistogramGraph extends Histogram2D implements IAutoRefreshable{
		private static final long serialVersionUID = 1L;

		// real data of the histogram:
		private HistogramData histogramData;
		// position of the first real histogram pixel:
		private int   xOffsetByLabels;
		
		// value of bin increases per bin
		private double deltaPerBin;

		// automatically redraw on time modification:
		boolean isAutoRefresh;

		BackgroundThread backgroundThread = null;

		// background thread computing the histogram data:
		/**
		 * At most one of this thread is executed.  
		 * @author julian
		 */
		class BackgroundThread extends SwingWorker<Void, Void>{

			@Override
			protected Void doInBackground() throws Exception {
				histogramData = computeHistogram();
				if(isCancelled()){
					return null;
				}

				// automatically adapt the title.
				frame.setTitle(reader.getGroup().getName() + ":" + description.getName() + " (" +
						String.format("%.4f", modelTime.getViewPosition()) + "-" + 
						String.format("%.4f",(modelTime.getViewExtent() + modelTime.getViewPosition()))
						+ ")"
				);

				histogramGraph.removeAllLines();
				histogramGraph.addLine(histogramData);

				reloadData();

				return null;
			}
		}

		@Override
		protected void binMouseOver(int bin) {
			labelBin.setInteger(bin + 1);						
			labelMaxValue.setDouble( (bin +1) * deltaPerBin + statistics.getMinValue()  );
			labelMinValue.setDouble(bin * deltaPerBin + statistics.getMinValue());
			labelNumberOfElements.setInteger(histogramData.getBins()[bin]);
		}

		public HistogramGraph() {						
			triggerRefreshHistogramData();

			setAutoRefresh(viewer.common.Parameters.ACTIVE_REFRESH);
		}

		/**
		 * Call it when the number of bins change or the time interval.
		 */
		public void triggerRefreshHistogramData(){
			if( backgroundThread != null ){
				backgroundThread.cancel(true);
			}

			backgroundThread = new BackgroundThread();
			backgroundThread.execute();
		}

		/**
		 * Compute the input data for the histogram
		 * @return
		 */
		private HistogramData computeHistogram(){			
			final int [] values = new int[numberOfBins];
			int maxNumber = 0;

			final double min = statistics.getMinValue();
			final double max = statistics.getMaxValue();
			
			deltaPerBin = (max - min) / numberOfBins;
			final int whichEntry = description.getNumberInGroup();

			final Epoch startTime = new Epoch(modelTime.getViewPosition());
			final Epoch endTime = new Epoch(modelTime.getViewExtent() + modelTime.getViewPosition());
			
			final Enumeration<StatisticGroupEntry> entries = reader.enumerateStatistics(	
					startTime, endTime);

			while(entries.hasMoreElements()){
				double value = entries.nextElement().getNumeric(whichEntry);
				int bin = (int)((value - min) / deltaPerBin);
				// out of range?
				bin = (bin >= numberOfBins) ? numberOfBins - 1 : bin;

				values[bin] ++;
				// adapt max number of entries
				maxNumber = (values[bin] > maxNumber) ? values[bin] : maxNumber;  
			}

			return new HistogramData("", category.getColor(),  values, min, max - min);
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

			// draw average:
			float [] newdash = { 2.0f };
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 2.0f, newdash, 0.0f));

			g.setColor(Color.PINK);
			int x = getXAxis().convertValueToPixel(statistics.getAverageValue()) ;

			g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());

			// draw stddevs
			g.setColor(Color.WHITE);
			for (int i=1; i <= 3; i++){
				g.setColor(g.getColor().darker());
				x = getXAxis().convertValueToPixel(i * statistics.getStddevValue() + statistics.getAverageValue()) ;
				g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());

				x = getXAxis().convertValueToPixel(i * statistics.getStddevValue() + statistics.getAverageValue()) ;

				g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());
			}
		}

		@Override
		public boolean isAutoRefresh() {			
			return isAutoRefresh;
		}

		@Override
		public void setAutoRefresh(boolean autoRefresh) {
			isAutoRefresh = autoRefresh;

			if(autoRefresh == true){
				modelTime.addTimeListener(timeModifiedListener);
				triggerRefreshHistogramData();
			}else{
				modelTime.removeTimeListener(timeModifiedListener);
			}
		}
	}

	public void show(){
		frame.setVisible(true);
	}	
}
