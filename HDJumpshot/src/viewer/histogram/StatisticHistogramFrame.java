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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.zoomable.ModelTime;
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

	final HistogramImagePanel histogramPanel;
	final LabeledSpinner binNumberSpinner;
	final LabeledTextField labelBin;
	final LabeledTextField labelNumberOfElements;
	final LabeledTextField labelMinValue;
	final LabeledTextField labelMaxValue;

	final StatisticStatistics statistics;
	final CategoryStatistic category;

	final Font              drawFont = new Font( "Monospaced", Font.PLAIN, 10 );

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
		this.histogramPanel = new HistogramImagePanel();

		frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
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
		
    JButton autoRefresh_btn = new ButtonAutoRefresh(histogramPanel);
    xPanel.add( autoRefresh_btn );        
		

		final JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));		
		yPanel.setMinimumSize(frame.getPreferredSize());
		

		yPanel.add(xPanel);
		yPanel.add(histogramPanel);

		frame.add(yPanel);
		
		// default on close operation:
		frame.addWindowListener(new MyWindowClosedListener());
	}

	public void triggerRefreshHistogramData(){
		histogramPanel.triggerRefreshHistogramData();
	}

	private class HistogramImagePanel extends JPanel implements IAutoRefreshable{
		private static final long serialVersionUID = 1L;

		// real data of the histogram:
		private HistogramData histogramData;
		// position of the first real histogram pixel:
		private int           xOffsetByLabels;
		// pixel width per bin:
		private double        widthPerBin;		
		// value of bin increases per bin
		private double deltaPerBin;

		// # bin which was mouse over the last time
		private int oldMouseOverBin = -1;

		// automatically redraw on time modification:
		boolean isAutoRefresh = false;
		
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
				
				repaint();
				
				return null;
			}
		}

		
		public HistogramImagePanel() {						
			// double buffering.
			super(true);
			triggerRefreshHistogramData();

			// add a mouse listener which shows information about the selected statistic bin:
			this.addMouseMotionListener(new MouseMotionAdapter(){
				@Override
				public void mouseMoved(MouseEvent e) {
					if(histogramData == null) // maybe computed in background
						return;
					
					if(e.getX() > xOffsetByLabels){
						int bin =(int) ((e.getX() - xOffsetByLabels) / widthPerBin);		

						if (bin > numberOfBins){
							bin = numberOfBins - 1;
						}

						if ( bin == oldMouseOverBin)
							return;

						labelBin.setInteger(bin + 1);						
						labelMaxValue.setDouble( (bin +1) * deltaPerBin + statistics.getMinValue()  );
						labelMinValue.setDouble(bin * deltaPerBin + statistics.getMinValue());
						labelNumberOfElements.setInteger(histogramData.values[bin]);

						oldMouseOverBin = bin;
					}
				}
			});
		}

		/**
		 * Contains the input data for the histogram 
		 * @author Julian M. Kunkel
		 */
		private class HistogramData{
			final int maxNumber;
			final int [] values;

			public HistogramData(int maxValue, int[] values) {
				this.values = values;
				this.maxNumber = maxValue;
			}
		}

		/**
		 * Call it when the number of bins change or the time interval.
		 */
		public void triggerRefreshHistogramData(){
			oldMouseOverBin = -1;
			
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

			final Enumeration<StatisticGroupEntry> entries = reader.enumerateStatistics(	
					new Epoch(modelTime.getTimeViewPosition()), 
					new Epoch(modelTime.getTimeViewExtent() + modelTime.getTimeViewPosition()));

			while(entries.hasMoreElements()){
				double value = entries.nextElement().getNumeric(whichEntry);
				int bin = (int)((value - min) / deltaPerBin);
				// out of range?
				bin = (bin >= numberOfBins) ? numberOfBins - 1 : bin;

				values[bin] ++;
				// adapt max number of entries
				maxNumber = (values[bin] > maxNumber) ? values[bin] : maxNumber;  
			}

			return new HistogramData(maxNumber, values);
		}

		@Override
		public void paint(Graphics graphics) {
			// automatically adapt the title.
			frame.setTitle(reader.getGroup().getName() + ":" + description.getName() + " (" +
					String.format("%.4f", modelTime.getTimeViewPosition()) + "-" + 
					String.format("%.4f",(modelTime.getTimeViewExtent() + modelTime.getTimeViewPosition()))
					+ ")"
					);
			
			
			final Graphics2D g = (Graphics2D) graphics;

			g.setFont(drawFont);

			final Rectangle vis = getVisibleRect(); 
			// clear screen:
			g.clearRect(vis.x, vis.y, vis.width, vis.height);

			final int fontSize = drawFont.getSize();

			String str;
			str = numberOfBins + "."; 

			xOffsetByLabels = fontSize * str.length();

			final int width = vis.width - xOffsetByLabels;
			final int height = vis.height - fontSize * 2;
			
			vis.y += fontSize;

			widthPerBin = (double) width / numberOfBins;
			if(widthPerBin < 1.0){
				binNumberSpinner.setValue(width);
				return;
			}

			final double min = statistics.getMinValue();
			final double max = statistics.getMaxValue();

			// draw Y axis
			final int yLabelCount = (vis.height / fontSize) - 1;

			for(int i=0 ; i <= yLabelCount; i++){
				str = String.format("%d", Math.round((float) histogramData.maxNumber/ yLabelCount * i));
				g.drawChars( str.toCharArray(), 0, str.length(), vis.x , vis.y + vis.height - fontSize - i * fontSize);
			}

			g.drawLine(vis.x + xOffsetByLabels, vis.y, vis.x + xOffsetByLabels , vis.y + vis.height - fontSize);


			// draw X axis
			str = String.format("%.4f", min).toString();

			g.drawChars( str.toCharArray(), 0, str.length(), 
					vis.x + xOffsetByLabels + 2, vis.y + vis.height - fontSize);

			str =  String.format("%.4f",max).toString();

			g.drawChars( str.toCharArray(), 0, str.length(), vis.x + 
					vis.width - str.length() * fontSize, vis.y + vis.height - fontSize);

			g.drawLine(vis.x, vis.y + vis.height - 2 *  fontSize, vis.x + vis.width, vis.y + vis.height - 2 * fontSize);

			// draw bins:			
			g.setColor(category.getColor());			

			vis.x = vis.x + xOffsetByLabels;

			for(int i=0; i < numberOfBins; i++){
				final int xOffset = (int) (widthPerBin * i) + vis.x;

				final int binHeight = (int) ((float) height * histogramData.values[i] / histogramData.maxNumber);
				g.fillRect(xOffset, vis.y + height - binHeight, 	(int) widthPerBin, binHeight);
			}

			// draw darker border
			g.setColor(category.getColor().darker());						
			for(int i=0; i < numberOfBins; i++){
				final int xOffset = (int) (widthPerBin * i) + vis.x;

				final int binHeight = (int) ((float) height * histogramData.values[i] / histogramData.maxNumber);
				g.drawRect(xOffset, vis.y + height - binHeight, 	(int) widthPerBin, binHeight);
			}


			// draw additional helper lines with a dashed line
			float dash[] = { 5.0f };
			g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 5.0f, dash, 0.0f));

			// one fifth of the screen.

			g.setColor(Color.DARK_GRAY);

			for(int i=1; i < 5 ; i++){
				int y = (int) (0.2 * i * height) + vis.y;
				g.drawLine(vis.x + 1,  y,	vis.x + vis.width, y);
			}

			// draw average:
			float [] newdash = { 2.0f };
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 2.0f, newdash, 0.0f));

			g.setColor(Color.PINK);
			final double widthPerValue =  width / (max - min) ;
			int x = vis.x + (int) ((statistics.getAverageValue() - min) * widthPerValue) ;
			
			drawVerticalLine(g, vis, x, vis.y, height);

			// draw stddevs
			g.setColor(Color.WHITE);
			for (int i=1; i <= 3; i++){
				g.setColor(g.getColor().darker());
				x = vis.x + (int) ((i * statistics.getStddevValue() + statistics.getAverageValue() - min) * widthPerValue) ;
				drawVerticalLine(g, vis, x, vis.y, height);

				x = vis.x + (int) ((-i * statistics.getStddevValue() + statistics.getAverageValue() - min) * widthPerValue) ;
				drawVerticalLine(g, vis, x, vis.y, height);
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
	
	private void drawVerticalLine(Graphics g, Rectangle vis, int x, int y, int height){
		if (x <= vis.x || x >= vis.x + vis.width)
			return;
		g.drawLine(x, vis.y, x, vis.y + height);
	}


	public void show(){
		frame.setVisible(true);
	}	
}
