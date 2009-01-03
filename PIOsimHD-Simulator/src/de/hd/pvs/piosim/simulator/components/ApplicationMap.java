
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
package de.hd.pvs.piosim.simulator.components;

import java.util.HashMap;

import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;

/**
 * Maps the application alias to the application and then to the simulated client which will 
 * run the program of the <alias, rank> tuple.
 * 
 * @author Julian M. Kunkel
 *
 */
public class ApplicationMap {	
	/* application "alias" => HashMap<rank, SClient> 
	 * note that the alias is different from the actual application name,  especially if a single application is started
	 * multiple times on different nodes */
	private HashMap<String, HashMap<Integer, GClientProcess>> map = new HashMap<String, HashMap<Integer, GClientProcess>>();
	
	/**
	 * Register a simulated client which will run the program of a particular rank for the application.  
	 * @param appAlias
	 * @param rank
	 * @param client
	 */
	public void put(String appAlias, int rank, GClientProcess client){
		HashMap<Integer, GClientProcess> appClients = map.get(appAlias);
		if(appClients == null){
			appClients = new HashMap<Integer, GClientProcess>();
			map.put(appAlias, appClients);
		}
		
		/* this rank should not be used twice */
		if(appClients.get(rank) != null){
			throw new IllegalArgumentException(client.getIdentifier() + " rank \""  + rank + "\" in  " +
					"application \"" + appAlias + "\" already used by " + appClients.get(rank).getIdentifier());
		}
		
		appClients.put(rank, client);
	}
	
	public GClientProcess getClient(String appAlias, int rank){
		return map.get(appAlias).get(rank);
	}
}
