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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.Enumeration;

import javax.swing.JPanel;

import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.IBufferedStatisticsReader;
import de.viewer.common.Const;
import de.viewer.common.LabeledTextField;
import de.viewer.common.ModelTime;
import de.viewer.graph.GraphAxis;
import de.viewer.graph.HistogramIntData;

/**
 * Show a histogram i.e. details for a single statistic of a group.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticHistogramFrame extends StatisticHistogram<HistogramIntData> {

	final LabeledTextField labelNumberOfElements = new LabeledTextField( "# Elements", Const.INTEGER_FORMAT );

	public StatisticHistogramFrame(IBufferedStatisticsReader reader, StatisticsDescription description, ModelTime modelTime, CategoryStatistic category ) {
		super(reader, description, modelTime, category);

		getHistogramGraph().getYAxis().setIntegerType(true);	
	}

	@Override
	protected void createToolbarOptions(JPanel panel) {
		labelNumberOfElements.setEditable( false );

		panel.add(labelNumberOfElements);		
	}

	@Override
	protected void histogramBinMouseOver(int bin, HistogramIntData data) {
		labelNumberOfElements.setInteger(data.getBins()[bin]);	
	}

	@Override
	protected HistogramIntData computeHistogram(int numberOfBins, double min,
			double max, double deltaPerBin, Epoch start, Epoch end) {			

		final int [] values = new int[numberOfBins];

		int maxNumber = 0;

		final int whichEntry = description.getNumberInGroup();

		final Enumeration<StatisticsGroupEntry> entries = reader.enumerateStatistics(start, end);
		
		int numberOfElements = 0;
		BigDecimal valueSum = new BigDecimal(0);

		while(entries.hasMoreElements()){
			final StatisticsGroupEntry entry = entries.nextElement(); 
			
			numberOfElements++;

			final double value = entry.getNumeric(whichEntry);

			int bin = (int)((value - min) / deltaPerBin);
			// out of range?
			bin = (bin >= numberOfBins) ? numberOfBins - 1 : bin;

			values[bin] ++;
			// adapt max number of entries
			maxNumber = (values[bin] > maxNumber) ? values[bin] : maxNumber;
			
			valueSum = valueSum.add(new BigDecimal(value));
		}

		return new HistogramIntData("", category.getColor(),  values, min, max - min, valueSum.doubleValue() / numberOfElements, 
				valueSum.doubleValue());
	}

	@Override
	protected void drawAdditionalInfoOnHistogramArea(Graphics2D g) {
		final HistogramGraph graph = getHistogramGraph();
		final GraphAxis xaxis = graph.getXAxis();
		final GraphAxis yaxis = graph.getYAxis();
		
		// draw average:
		float [] newdash = { 2.0f };
		g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 2.0f, newdash, 0.0f));

		g.setColor(Color.PINK);
		int x = xaxis.convertValueToPixel(statistics.getAverageValue()) ;

		g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());

		// draw stddevs
		g.setColor(Color.BLACK);
		for (int i=1; i <= 3; i++){
			g.setColor(g.getColor().brighter());
			
			x = xaxis.convertValueToPixel( i * statistics.getStddevValue() + statistics.getAverageValue()) ;
			g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());

			x = xaxis.convertValueToPixel(-i * statistics.getStddevValue() + statistics.getAverageValue()) ;

			g.drawLine(x, yaxis.getDrawOffset() + yaxis.getDrawSize(), x, yaxis.getDrawOffset());		
		}
	}
}
