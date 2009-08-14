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


import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.Enumeration;

import javax.swing.JPanel;

import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedStatisticsFileReader;
import de.viewer.common.Const;
import de.viewer.common.LabeledTextField;
import de.viewer.common.ModelTime;
import de.viewer.graph.HistogramDoubleData;

/**
 * Show a histogram i.e. details for a single statistic of a group.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticTimeHistogramFrame  extends StatisticHistogram<HistogramDoubleData>  {

	final LabeledTextField aggregatedTime = new LabeledTextField( "Aggregated [t]", Const.FLOAT_FORMAT );
	final LabeledTextField percentTime = new LabeledTextField( "% [t]", Const.FLOAT_FORMAT );

	
	public StatisticTimeHistogramFrame(BufferedStatisticsFileReader reader, StatisticsDescription description, ModelTime modelTime, CategoryStatistic category ) {
		super(reader, description, modelTime, category);
	}

	@Override
	protected void createToolbarOptions(JPanel panel) {
		aggregatedTime.setEditable( false );		
		panel.add(aggregatedTime);		

		percentTime.setEditable( false );		
		panel.add(percentTime);
	}

	@Override
	protected void histogramBinMouseOver(int bin, HistogramDoubleData data) {			
		double binval = data.getBins()[bin];
		aggregatedTime.setDouble(binval);

		percentTime.setDouble(binval / data.getAggregatedValue() * 100);
	}

	@Override
	protected void drawAdditionalInfoOnHistogramArea(Graphics2D g) {

	}

	private int determineBin(StatisticsGroupEntry entry, double min, double value, int numberOfBins, double deltaPerBin){
		int bin = (int)((value - min) / deltaPerBin);
		// out of range?
		return (bin >= numberOfBins) ? numberOfBins - 1 : bin;		
	}
	
	@Override
	protected HistogramDoubleData computeHistogram(int numberOfBins,
			double min, double max, double deltaPerBin, Epoch start, Epoch end) {

		final BigDecimal [] aggregatedTimes = new BigDecimal[numberOfBins];

		final int whichEntry = description.getNumberInGroup();

		final Enumeration<StatisticsGroupEntry> entries = reader.enumerateStatistics(start, end);

		for (int i=0; i < aggregatedTimes.length ; i++){
			aggregatedTimes[i] = new BigDecimal(0);
		}

		// store first and last entry to remove effect of overlapping events to the borders of the view extend
		StatisticsGroupEntry earliestEntry = null;
		StatisticsGroupEntry entry = null;
		
		BigDecimal valueSum = new BigDecimal(0);
		
		while(entries.hasMoreElements()){
			entry = entries.nextElement(); 				
			
			if(earliestEntry == null)
				earliestEntry = entry;

			final double value = entry.getNumeric(whichEntry);

			final int bin = determineBin(entry, min, value, numberOfBins, deltaPerBin);
			final BigDecimal duration = new BigDecimal(entry.getDurationTime().getDouble());
			
			aggregatedTimes[bin] = aggregatedTimes[bin].add(duration);
  
			valueSum = valueSum.add(duration.multiply(new BigDecimal(value)));		
		}
		
		// check if entry starts earlier or later than start, or ends later than end.
		if(entry != null){
			if(earliestEntry.getEarliestTime().compareTo(start) < 0){
				// starts earlier
				final int bin = determineBin(earliestEntry, min, whichEntry, numberOfBins, deltaPerBin);
				final BigDecimal overlappingDuration = new BigDecimal(start.subtract(earliestEntry.getEarliestTime()).getDouble());
				
				aggregatedTimes[bin] = aggregatedTimes[bin].subtract(overlappingDuration);
				final double value = entry.getNumeric(whichEntry);
				valueSum = valueSum.subtract(overlappingDuration.multiply(new BigDecimal(value)));
			}						
			if(entry.getLatestTime().compareTo(end) > 0){
				// ends later => subtract time.
				final int bin = determineBin(entry, min, whichEntry, numberOfBins, deltaPerBin);
				
				final BigDecimal overlappingDuration = new BigDecimal( entry.getLatestTime().subtract(end).getDouble() );
				
				aggregatedTimes[bin] = aggregatedTimes[bin].subtract(overlappingDuration);
				final double value = entry.getNumeric(whichEntry);
				valueSum = valueSum.subtract(overlappingDuration.multiply(new BigDecimal(value)));
			}	
		}

		// compute values:
		final double [] times = new double[aggregatedTimes.length];
		for (int i=0; i < aggregatedTimes.length ; i++){
			times[i] = aggregatedTimes[i].doubleValue();
		}

		return new HistogramDoubleData("", category.getColor(),  times, min, max - min, 
				valueSum.doubleValue() / end.subtract(start).getDouble(), valueSum.doubleValue());
	}
}
