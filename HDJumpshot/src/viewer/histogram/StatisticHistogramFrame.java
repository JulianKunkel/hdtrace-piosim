package viewer.histogram;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.StatisticStatistics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import viewer.common.LabeledSpinner;
import viewer.zoomable.ModelTime;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import drawable.CategoryStatistic;

/**
 * Show a histogram i.e. details for a single statistic of a group.
 * 
 * @author julian
 */
public class StatisticHistogramFrame {

	final ModelTime modelTime;
	final JFrame frame;
	
	final BufferedStatisticFileReader reader;
	final StatisticDescription description;
	
	final HistogramImagePanel histogramPanel = new HistogramImagePanel();
	final LabeledSpinner binNumberSpinner;
	
	final StatisticStatistics statistics;
	final CategoryStatistic category;
	
	int numberOfBins = 20;
	
	public StatisticHistogramFrame(BufferedStatisticFileReader reader, StatisticDescription description, ModelTime modelTime, CategoryStatistic category ) {
		this.modelTime = modelTime;
		this.reader = reader;
		this.description = description;
		this.statistics = reader.getStatisticsFor(description.getNumberInGroup());
		this.category = category;

		frame = new JFrame(reader.getGroup().getName() + " : " + description.getName());
        frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
		frame.setResizable(true);
				
		JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));		
		yPanel.setMinimumSize(frame.getPreferredSize());
		yPanel.add(histogramPanel);
		
		JPanel xPanel = new JPanel();
		xPanel.setLayout(new BoxLayout( xPanel, BoxLayout.X_AXIS));
		
		binNumberSpinner = new LabeledSpinner("Number of bins", new SpinnerNumberModel(numberOfBins, 10, 1000, 10), 
				new ChangeListener(){
		@Override
			public void stateChanged(ChangeEvent e) {
				numberOfBins = (Integer) binNumberSpinner.getValue();
				
				histogramPanel.repaint();
			}
		});
		
		
		xPanel.add(binNumberSpinner);			
		
		yPanel.add(xPanel);
		
		frame.add(yPanel);
	}
	
	private class HistogramImagePanel extends JPanel{
		private static final long serialVersionUID = 1L;

		public HistogramImagePanel() {
			// double buffering.
			super(true);
			
			setFont(new Font(getFont().getName(), 0, 10));
		}
		
		private class HistogramData{
			final int maxNumber;
			final int [] values;
			
			public HistogramData(int maxValue, int[] values) {
				this.values = values;
				this.maxNumber = maxValue;
			}
		}
		
		private HistogramData computeHistogram(){			
			final int [] values = new int[numberOfBins];
			int maxNumber = 0;

			final double min = statistics.getMinValue();
			final double max = statistics.getMaxValue();
			final double diff = max - min;
			
						
			for(int i=0; i < numberOfBins; i++){
				final double minValBin = diff / numberOfBins * i;
				final double maxValBin = diff / numberOfBins * (i+1);
			}								
			
			return new HistogramData(maxNumber, values);
		}
		
		@Override
		public void paint(Graphics g) {
			final Rectangle vis = getVisibleRect(); 
			
			final FontMetrics fontMetric = g.getFontMetrics();

			final int width = vis.width - fontMetric.getMaxAdvance() - 5;
			final int height = vis.height - fontMetric.getHeight() - 5;
			
			g.clearRect(vis.x, vis.y, width, height);
						
			final double widthPerBin = width / numberOfBins;
			
			final double min = statistics.getMinValue();
			final double max = statistics.getMaxValue();

			final HistogramData histoData = computeHistogram();
			
			String str;
			
			// draw Y axis
			final int fontHeight = fontMetric.getHeight();
			final int yLabelCount = (vis.height / fontHeight);
			
			for(int i=1 ; i < yLabelCount; i++){
				str = String.format("%.1f", ((float) histoData.maxNumber/ yLabelCount * i));
				g.drawChars( str.toCharArray(), 0, str.length(), vis.x , vis.y + vis.height - i * fontHeight);
			}
			
			vis.x = vis.x +  fontMetric.getMaxAdvance() * 3;
			
			g.drawLine(vis.x, vis.y, vis.x , vis.y + vis.height);
		
			
			// draw X axis
			str = String.format("%.4f", min).toString();
			
			g.drawChars( str.toCharArray(), 0, str.length(), vis.x , vis.y + vis.height - fontMetric.getDescent());
			
			str =  String.format("%.4f",max).toString();
			
			g.drawChars( str.toCharArray(), 0, str.length(), vis.x + 
					width - fontMetric.charsWidth(str.toCharArray(), 0, str.length()), vis.y + vis.height - fontMetric.getDescent());
			
			g.drawLine(vis.x, vis.y + vis.height - fontMetric.getHeight(), vis.x + vis.width, vis.y + vis.height - fontMetric.getHeight());
			
						
			g.setColor(category.getColor());			
			
			for(int i=0; i < numberOfBins; i++){
				final int xOffset = (int) (widthPerBin * i) + vis.x;
				
				g.fillRect(xOffset, vis.y + (int) ((float) height * histoData.values[i] / histoData.maxNumber), 
						(int) widthPerBin, vis.y + height);
			}
		}
	}
	
	
	public void show(){
        frame.setVisible(true);
	}	
}
