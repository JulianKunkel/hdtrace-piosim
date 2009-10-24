package de.hd.pvs.piosim.model.dynamicMapper;

import java.util.HashMap;

public class DynamicTraceEntryToCommandMapper extends DynamicMapper {
	static private DynamicTraceEntryToCommandMapper instance = new DynamicTraceEntryToCommandMapper();

	final private HashMap<String, CommandType> traceEntryMap = new HashMap<String, CommandType>();

	private DynamicTraceEntryToCommandMapper(){
		for(String line: readLines("TraceEntryNameToCommandMapping.txt")) {
			if(line.contains(":")){
				final String [] split =  line.split(":");
				if(split.length != 2){
					throw new IllegalArgumentException("Error in TraceEntryNameToCommandMapping.txt in line: " + line);
				}

				final CommandType newCommandType = new CommandType(split[1].trim());
				for(String str: split[0].split(",")){
					if(str.trim().length() == 0){
						continue;
					}
					if(traceEntryMap.put(str.trim(), newCommandType) != null){
						throw new IllegalArgumentException("TraceEntry mapping already defined for entry: " + str.trim() + ", error while parsing command " + newCommandType.getCommandName());
					}
				}

				// default 1:1 mapping:
				traceEntryMap.put(newCommandType.getCommandName(), newCommandType);
			}
		}
	}

	static public CommandType getCommandForTraceEntryName(String name){
		CommandType type = instance.traceEntryMap.get(name);
		return type;
	}

	static public boolean isCommandAvailable(String commandName){
		return instance.traceEntryMap.containsKey(commandName);
	}

	static public HashMap<String, CommandType> getTraceEntryMapping(){
		return instance.traceEntryMap;
	}
}
