package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.util.Epoch;

public class ExternalStatisticsGroup{
	
	/**
	 * Data types a statistic value might have.
	 * @author julian
	 *
	 */
	public static enum StatisticType{
		FLOAT,
		INT,
		LONG,
		DOUBLE,
		STRING,
		EPOCH
	}
	
	/**
	 * Describes the format of the Timestamp.
	 */
	StatisticType timestampDatatype = StatisticType.EPOCH;
	
	/**
	 * Multiplies the read value with this value.
	 */
	int timeResolutionMultiplier = 1;
	
	/**
	 * Offset applied to all timestamps
	 */
	Epoch timeOffset = Epoch.ZERO;
	
	/**
	 * Name of this statistic group
	 */
	String groupName;
	
	// maps contained statistics to type
	final HashMap<String, StatisticDescription> statisticTypeMap = new HashMap<String, StatisticDescription>();
	final LinkedList<StatisticDescription>      statisticOrder = new LinkedList<StatisticDescription>();
	
	/**
	 * Add a statistic description, the order defines the order in which it is stored in the binary file.
	 */
	public void addStatistic(StatisticDescription desc){		
		statisticTypeMap.put(desc.getName(), desc);
		statisticOrder.add(desc);
	}
	
	public Collection<String> getStatistics() {
		return statisticTypeMap.keySet();
	}
	
	public StatisticType getType(String statistic){
		return statisticTypeMap.get(statistic).type;
	}
	
	public LinkedList<StatisticDescription> getStatisticsOrdered() {
		return statisticOrder;
	}
	
	public void setName(String name) {
		this.groupName = name;
	}
	
	public String getName() {
		return groupName;
	}
	
	public void setTimeResolutionMultiplier(int timeResulutionMultiplier) {
		this.timeResolutionMultiplier = timeResulutionMultiplier;
	}
	
	public void setTimestampDatatype(StatisticType timestampDatatype) {
		this.timestampDatatype = timestampDatatype;
	}
	
	public void setTimeOffset(Epoch timeOffset) {
		this.timeOffset = timeOffset;
	}
	
	public int getTimeResolutionMultiplier() {
		return timeResolutionMultiplier;
	}
	
	public StatisticType getTimestampDatatype() {
		return timestampDatatype;
	}
	
	public Epoch getTimeOffset() {
		return timeOffset;
	}
}
