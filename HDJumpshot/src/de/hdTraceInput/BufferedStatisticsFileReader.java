//	Copyright (C) 2009, 2011 Julian M. Kunkel
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


package de.hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Buffers all statistics of a given file.
 * 
 * @author julian
 *
 */
public class BufferedStatisticsFileReader extends BufferedMemoryReader {
	final Epoch additionalTimeAdjustment;
	final String filename;
	
	public BufferedStatisticsFileReader(String filename, String expectedGroupName, Epoch additionalTimeOffset) throws Exception{
		final StatisticsReader reader = new StatisticsReader(filename, expectedGroupName, additionalTimeOffset);

		setGroup(reader.getGroup());
		
		// read the data from the file
		StatisticsGroupEntry current = reader.getNextInputEntry();
		
		final ArrayList<StatisticsGroupEntry> statEntries = new ArrayList<StatisticsGroupEntry>();
		
		while(current != null){
			statEntries.add(current);
			current = reader.getNextInputEntry();
		}

		setEntries(statEntries.toArray(new StatisticsGroupEntry[]{}));
		
		this.additionalTimeAdjustment = additionalTimeOffset;
		this.filename = filename;
	}
	
	public Epoch getAdditionalTimeAdjustment() {
		return additionalTimeAdjustment;
	}
	
	public String getFilename() {
		return filename;
	}
}
