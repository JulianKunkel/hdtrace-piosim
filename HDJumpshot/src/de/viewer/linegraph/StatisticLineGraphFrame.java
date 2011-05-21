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

package de.viewer.linegraph;


import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hdTraceInput.IBufferedStatisticsReader;
import de.hdTraceInput.StatisticStatistics;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyStatisticTreeNode;
import de.viewer.common.ModelTime;
import de.viewer.common.TimeEvent;
import de.viewer.common.TimeListener;
import de.viewer.graph.ElementEnumeration;
import de.viewer.graph.GraphData;
import de.viewer.graph.LineGraph2DStatic;

public class StatisticLineGraphFrame {
	private final JFrame frame;

	private final LineGraph2DStatic graph = new LineGraph2DStatic();
	private final MyTimeModifiedListener timeModifiedListener = new MyTimeModifiedListener();
	private final ModelTime modelTime;

	private static class StatisticData extends GraphData{
		final IBufferedStatisticsReader source;
		final StatisticsDescription desc;
		final StatisticStatistics  stats;
		
		public StatisticData(IBufferedStatisticsReader statSource, CategoryStatistic category) {
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
