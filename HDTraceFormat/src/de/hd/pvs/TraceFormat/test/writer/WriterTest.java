package de.hd.pvs.TraceFormat.test.writer;

import java.io.IOException;

import de.hd.pvs.TraceFormat.SimpleTraceFormatWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * This project is an example how to use the (central) java trace writer API.
 * @author julian
 *
 */
public class WriterTest {		
	public static void main(String[] args) throws IOException{				
		SimpleTraceFormatWriter writer = new SimpleTraceFormatWriter("/tmp/test", "test descrpition", "test application",  
				new String []{"Host"});
		
		final TopologyNode host1 = writer.createInitalizeTopology(new String[]{"host01"});
		final TopologyNode host2 = writer.createInitalizeTopology(new String[]{"host02"});			
		
		StateTraceEntry s1 = writer.StateStart(host1, "Simple", new Epoch(0.0));
		StateTraceEntry s2 = writer.StateStart(host2, "ACPI1", new Epoch(1.0));
		StateTraceEntry s3 = writer.StateStart(host2, "ACPI2", new Epoch(2.0));
		
		writer.StateEnd(host2, new Epoch(3.0), s3);
		writer.StateEnd(host2, new Epoch(4.0), s2);
		writer.StateEnd(host1, new Epoch(4.1), s1);
		
		// now try to write some statistics:
		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy");
		
		StatisticsDescription statEstimatedSimple = new StatisticsDescription(estimatedEnergy, "Simple", StatisticsEntryType.FLOAT, 0, "Watt", "Energy");
		StatisticsDescription statEstimatedLookAhead = new StatisticsDescription(estimatedEnergy, "Look Ahead", StatisticsEntryType.FLOAT, 0, "Watt", "Energy");
		
		estimatedEnergy.addStatistic(statEstimatedLookAhead);
		estimatedEnergy.addStatistic(statEstimatedSimple);
			
		
		writer.writeStatisticsTimestamp(host1, estimatedEnergy, new Epoch(2.0));
		writer.writeStatisticValue(host1, statEstimatedLookAhead, new Float(2.0));
		writer.writeStatisticValue(host1, statEstimatedSimple, new Float(2.0));

		writer.writeStatisticsTimestamp(host1, estimatedEnergy, new Epoch(3.0));
		writer.writeStatisticValue(host1, statEstimatedLookAhead, new Float(3.0));
		writer.writeStatisticValue(host1, statEstimatedSimple, new Float(1.0));
		
		writer.writeStatisticsTimestamp(host1, estimatedEnergy, new Epoch(4.0));
		writer.writeStatisticValue(host1, statEstimatedLookAhead, new Float(2.0));
		writer.writeStatisticValue(host1, statEstimatedSimple, new Float(2.6));
		
		writer.finalizeTrace();
	}
}
