
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

/**
 * 
 */
package de.hd.pvs.piosim.model.dynamicMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * The ClassMapper loads the available implementations and object types for the commands 
 * and allows the GUI and the
 * simulator to find the appropriate classes. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class DynamicCommandClassMapper extends DynamicMapper {
	
	/**
	 * Defines a command type which can have several implementations.
	 * 
	 * @author Julian M. Kunkel
	 */

	static public class CommandType implements Comparable<CommandType>{
		final String commandName;
		
		private CommandType(String commandName) {
			this.commandName = commandName;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() == this.getClass()) {
				CommandType cg = (CommandType) obj;
				return cg.commandName.equals(this.commandName);
			}else {
				return super.equals(obj);
			}
		}
		
		public int compareTo(CommandType group) {
			return this.commandName.compareTo(group.commandName);
		}
		
		@Override
		public int hashCode() {			
			return commandName.hashCode();
		}
		
		@Override
		public String toString() {
			return commandName;
		}
	}
	
	public static class CommandImplemenationMapping{
		final String commandClass;
		final String simulationClass;
		
		/** 
		 * If this command is useful only together with others, this list contains all of them:
		 */
		ArrayList<String> groupMembers = null; 
		
		public CommandImplemenationMapping(String commandClass, String simulationClass) {
			this.commandClass = commandClass;
			this.simulationClass = simulationClass;			
		}
		
		/**
		 * @return the groupMembers
		 */
		public ArrayList<String> getGroupMembers() {
			return groupMembers;
		}		
		
		/**
		 * @return the commandClass
		 */
		public String getCommandClass() {
			return commandClass;
		}
		
		/**
		 * @return the simulationClass
		 */
		public String getSimulationClass() {
			return simulationClass;
		}
	}	
	
	static private DynamicCommandClassMapper instance = new DynamicCommandClassMapper(); 
	
	/**
	 * Maps the command name to a list of implementations.
	 */
	HashMap<CommandType, ArrayList<CommandImplemenationMapping>> mapCommandImpl = new HashMap<CommandType, ArrayList<CommandImplemenationMapping>>();
	
	static public Collection<CommandType> getAvailableCommands(){
		return instance.mapCommandImpl.keySet();
	}
		
	/**
	 * Load the mapping.
	 */
	public DynamicCommandClassMapper(){
		// now read the command mapping:
		ArrayList<String> commandGroupContains = null;
		String [] commands = null;
		
		for(String line: readLines("CommandToSimulationMapper.txt")) {			
			
			if(line.charAt(0) == '+') {
				line = line.substring(1);
				
				commandGroupContains = new ArrayList<String>();
				
				// new set of commands:
				commands = line.split(",");
				
				if(commands.length > 1) {
					// all commands must coexist together
					for (String command: commands) {
						tryToLoadClass(command);
						
						String commandName = getImplementationClassName(command);
						commandGroupContains.add(commandName);
					}
				}
				
				// add them to the map if they are not part of it:
				for (String command: commands) {
					String commandName = getImplementationClassName(command);
					
					ArrayList<CommandImplemenationMapping> cmap = mapCommandImpl.get( new CommandType(commandName) );
					if (cmap == null) {
						cmap = new ArrayList<CommandImplemenationMapping>();
						mapCommandImpl.put( new CommandType(commandName), cmap);
					}
				}
				
				//System.out.println(getImplementationClassName(commands[0]));
			}else {
				// indeed a set of the implementations
				String [] implementations = line.split(",");
				if (implementations.length != commands.length) {
					throw new IllegalArgumentException(" Wrong number of implementations within group " + commands[0] + " total expected " + commands.length 
						+" but got " + implementations.length);
				}

				for (int i=0; i < commands.length; i++) {
					String command = commands[i];
					
					// search for implementations:
					String commandName = getImplementationClassName(command);					
					ArrayList<CommandImplemenationMapping> cmap = mapCommandImpl.get( new CommandType( commandName ));
					
					//System.out.println("Command " + command + " implemented by " + implementations[i]);
					
					tryToLoadClass(implementations[i]);
					CommandImplemenationMapping cmapping = new CommandImplemenationMapping(command, implementations[i]); 
					cmapping.groupMembers = commandGroupContains;
					
					cmap.add(cmapping);
				}
			}
			
		}
	}
	
	/**
	 * Return a set of possible implementations for a given command type.
	 * 
	 * @param commandType (as specified in the XML)
	 * @return
	 */
	static public ArrayList<CommandImplemenationMapping> getAvailableCommandImplementations(CommandType commandType){
		return instance.getCommandObjectMapFor(commandType);
	}
	
	static private ArrayList<CommandImplemenationMapping> getCommandObjectMapFor(CommandType commandType) {
		ArrayList<CommandImplemenationMapping> mop = instance.mapCommandImpl.get(commandType);
		if (mop == null) {
			throw new IllegalArgumentException("Command of type: " + commandType + " not mapped.");
		}
		return mop;
	}
	
	
	private CommandType parseCommandImplementationGroup(String commandName) {
		CommandType tmp = new CommandType(commandName);
		if(! mapCommandImpl.containsKey(tmp)) {
			throw new IllegalArgumentException("No implementation for type " + commandName + " available");
		}
		
		return tmp;
	}
	
	/**
	 * Lookup wether the name is a well known command type
	 * @param name
	 * @return
	 */
	static public CommandType getCommandImplementationGroup(String commandName) {
		return instance.parseCommandImplementationGroup(commandName);
	}
}
