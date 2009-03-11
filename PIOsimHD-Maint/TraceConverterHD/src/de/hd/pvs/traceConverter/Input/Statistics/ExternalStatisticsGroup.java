package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
	
	String name;
	
	// maps contained statistics to type
	final HashMap<String, StatisticType> statisticTypeMap = new HashMap<String, StatisticType>();
	
	public void addStatistic(String name, StatisticType type){
		statisticTypeMap.put(name, type);
	}
	
	public Collection<String> getStatistics() {
		return statisticTypeMap.keySet();
	}
	
	public StatisticType getType(String statistic){
		return statisticTypeMap.get(statistic);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
