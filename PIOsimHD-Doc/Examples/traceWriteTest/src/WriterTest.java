import java.io.IOException;

import de.hd.pvs.TraceFormat.SimpleTraceWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * This project is an example how to use the java trace writer.
 * @author julian
 *
 */
public class WriterTest {		
	public static void main(String[] args) throws IOException{				
		SimpleTraceWriter writer = new SimpleTraceWriter("/tmp/test", "test descrpition", "test application",  
				new String []{"Host"});
		
		final TopologyNode host1 = writer.initializeTopology(new String[]{"host01"});
		final TopologyNode host2 = writer.initializeTopology(new String[]{"host02"});			
		
		StateTraceEntry s1 = writer.StateStart(host1, "Simple", new Epoch(0.0));
		
		StateTraceEntry s2 = writer.StateStart(host2, "ACPI1", new Epoch(1.0));
		StateTraceEntry s3 = writer.StateStart(host2, "ACPI2", new Epoch(2.0));
		
		writer.StateEnd(host2, new Epoch(3.0), s3);
		writer.StateEnd(host2, new Epoch(4.0), s2);
		writer.StateEnd(host1, new Epoch(4.1), s1);
		
		// now try to write some statistics:
		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy");
		
		StatisticDescription statEstimatedSimple = new StatisticDescription(estimatedEnergy, "Simple", StatisticsEntryType.FLOAT, 0, "Watt", 1, "Energy");
		StatisticDescription statEstimatedLookAhead = new StatisticDescription(estimatedEnergy, "Look Ahead", StatisticsEntryType.FLOAT, 0, "Watt", 1, "Energy");
		
		estimatedEnergy.addStatistic(statEstimatedLookAhead);
		estimatedEnergy.addStatistic(statEstimatedSimple);
			
		
		writer.Statistics(host1, new Epoch(2.0), statEstimatedLookAhead, new Float(2.0));
		writer.Statistics(host1, new Epoch(2.0), statEstimatedSimple, new Float(2.0));
		
		writer.Statistics(host1, new Epoch(3.0), statEstimatedLookAhead, new Float(3.0));
		writer.Statistics(host1, new Epoch(3.0), statEstimatedSimple, new Float(1.0));
		
		writer.Statistics(host1, new Epoch(4.0), statEstimatedLookAhead, new Float(2.0));
		writer.Statistics(host1, new Epoch(4.0), statEstimatedSimple, new Float(2.6));
		
		writer.finalizeTrace();
	}
}
