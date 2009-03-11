package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
		STRING
	}
	
	String groupName;
	
	// maps contained statistics to type
	final HashMap<String, StatisticDescription> statisticTypeMap = new HashMap<String, StatisticDescription>();
	final LinkedList<StatisticDescription>      statisticOrder = new LinkedList<StatisticDescription>();
	
	/**
	 * Add a statistic description, the order defines the order in which it is stored in the binary file.
	 */
	public void addStatistic(String name, StatisticType type){
		StatisticDescription desc = new StatisticDescription(name, type);
		
		statisticTypeMap.put(name, desc);
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
}
