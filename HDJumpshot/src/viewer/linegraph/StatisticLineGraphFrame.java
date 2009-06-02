package viewer.linegraph;

import hdTraceInput.BufferedStatisticsFileReader;
import hdTraceInput.StatisticStatistics;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import topology.TopologyStatisticTreeNode;
import viewer.common.ModelTime;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.graph.ElementEnumeration;
import viewer.graph.GraphData;
import viewer.graph.LineGraph2DStatic;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import drawable.CategoryStatistic;

public class StatisticLineGraphFrame {
	private final JFrame frame;

	private final LineGraph2DStatic graph = new LineGraph2DStatic();
	private final MyTimeModifiedListener timeModifiedListener = new MyTimeModifiedListener();
	private final ModelTime modelTime;

	private static class StatisticData extends GraphData{
		final BufferedStatisticsFileReader source;
		final StatisticsDescription desc;
		final StatisticStatistics  stats;
		
		public StatisticData(BufferedStatisticsFileReader statSource, CategoryStatistic category) {
			super(category.getName(), category.getColor());
			source = statSource;
			desc = category.getStatisticDescription();
			stats = source.getStatisticsFor(desc.getNumberInGroup());
		}

		@Override
		public double getMinX() {
			return source.getMinTime().getDouble();
		}

		@Override
		public double getMaxX() {
			return source.getMaxTime().getDouble();
		}

		@Override
		public double getMinY() {
			return stats.getMinValue();
		}

		@Override
		public double getMaxY() {
			return stats.getMaxValue(); 
		}

		@Override
		public ElementEnumeration getXValues() {
			return new StatXValEnum(source.getStatEntries());
		}

		@Override
		public ElementEnumeration getYValues() {
			return new StatYValEnum(source.getStatEntries(), desc.getNumberInGroup());
		}
		
		static private class StatYValEnum implements ElementEnumeration{
			private int pos = 0;
			private final StatisticsGroupEntry [] entries;
			private final int statNum;
			
			public StatYValEnum(StatisticsGroupEntry [] entries, int statNum) {
				this.entries = entries;
				this.statNum = statNum;
			}
			
			@Override
			public boolean hasMoreElements() {
				return pos < entries.length;
			}
			
			@Override
			public double nextElement() {
				return entries[pos++].getNumeric(statNum);
			}
		}
		

		static private class StatXValEnum implements ElementEnumeration{
			private int pos = 0;
			private final StatisticsGroupEntry [] entries;
			
			public StatXValEnum(StatisticsGroupEntry [] entries) {
				this.entries = entries;
			}
			
			@Override
			public boolean hasMoreElements() {
				return pos < entries.length;
			}
			
			@Override
			public double nextElement() {
				return entries[pos++].getLatestTime().getDouble();
			}
		}
		
	}
	
	private void setTitle(){
		frame.setTitle("Statistics line graph" + " (" +
				String.format("%.4f", modelTime.getViewPosition()) + "-" + 
				String.format("%.4f",(modelTime.getViewEnd()))
				+ ")");
	}
	
	/**
	 * Automatically refresh histogram if time changed (i.e. scrolled)
	 */
	private class MyTimeModifiedListener implements TimeListener{
		@Override
		public void timeChanged(TimeEvent evt) {
			graph.reloadData();
			setTitle();
		}
	}
	
	private class MyWindowClosedListener extends WindowAdapter{
		@Override
		public void windowClosed(WindowEvent e) {
			// don't forget to remove modelTime listener (if autoupdate), otherwise ressources are wasted
			modelTime.removeTimeListener(timeModifiedListener);
			super.windowClosed(e);
		}		
	}
	
	public StatisticLineGraphFrame(ArrayList<TopologyStatisticTreeNode> selectedStatNodes, TraceFormatBufferedFileReader reader, ModelTime modelTime) {		
		this.modelTime = modelTime;
		
		frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
		frame.setPreferredSize(new Dimension(400, 250));
		frame.setResizable(true);
		
		setTitle();


		final JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));
		
		for(TopologyStatisticTreeNode node: selectedStatNodes){
			if( node.getStatisticDescription().isNumeric())
				graph.addLine(new StatisticData(node.getStatisticSource(), reader.getCategory(node.getStatisticDescription())));	
		}		
		
		yPanel.add(graph.getDrawingArea());
		
		frame.setContentPane(yPanel);

		modelTime.addTimeListener(timeModifiedListener);
		// default on close operation:
		frame.addWindowListener(new MyWindowClosedListener());
	}

	public void show(){
		frame.setVisible(true);
	}	
}
