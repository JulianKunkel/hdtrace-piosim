package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.piosim.model.util.Epoch;

public class ExternalStatisticsGroup{

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
	final HashMap<String, StatisticDescription> statisticTypeMap;
	final ArrayList<StatisticDescription>      statisticOrder;

	public ExternalStatisticsGroup() {
		statisticTypeMap = new HashMap<String, StatisticDescription>();
		statisticOrder = new ArrayList<StatisticDescription>();
	}

	public ExternalStatisticsGroup(ArrayList<StatisticDescription> statisticOrder,
			HashMap<String, StatisticDescription> statisticTypeMap){
		this.statisticOrder = statisticOrder;
		this.statisticTypeMap = statisticTypeMap;
	}

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

	public StatisticDescription getStatistic(String stat){
		return statisticTypeMap.get(stat);
	}

	public ArrayList<StatisticDescription> getStatisticsOrdered() {
		return statisticOrder;
	}
	
	public HashMap<String, StatisticDescription> getStatisticTypeMap() {
		return statisticTypeMap;
	}

	public void setName(String name) {
		this.groupName = name;
	}

	public String getName() {
		return groupName;
	}

	public int getSize(){
		return statisticOrder.size();
	}
	public void setTimeResolutionMultiplier(int timeResolutionMultiplier) {
		this.timeResolutionMultiplier = timeResolutionMultiplier;
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
