
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


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

package de.hd.pvs.piosim.model;

import java.util.ArrayList;
import java.util.HashMap;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.AttributeList;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper.CommandImplemenationMapping;

/**
 * This Class contains common attributes used in several components.
 *
 * @author Julian M. Kunkel
 */
public class GlobalSettings {

	/**
	 * The maximum amount of data which can be shipped per network paket.
	 */
	@Attribute
	private long transferGranularity = 1024 * 10;

	/**
	 * The maximum amount of data which should be read or written at once from the I/O subsystem.
	 */
	@Attribute
	private long IOGranularity = 1024 * 1024 * 10;

	/**
	 * Maximum amount of data transferred in the eager protocol.
	 */
	@Attribute
	private long maxEagerSendSize = 1024 * 100; // 100 KByte

	/**
	 * This object contains the actual chosen implementation for a function group.
	 */
	private HashMap<CommandType, String>  clientFunctionImplementation =
		new HashMap<CommandType, String>();

	/**
	 * Default constructor, sets the latest implementation for the client operations.
	 */
	public GlobalSettings(){
		// set default computation module:
		for(CommandType cm: DynamicCommandClassMapper.getAvailableCommands()){
			if(getClientFunctionImplementation(cm) == null){
				ArrayList<CommandImplemenationMapping> impl = DynamicCommandClassMapper.getAvailableCommandImplementations(cm);

				setClientFunctionImplementation(cm, impl.get(impl.size()-1).getSimulationClass() );
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" flowBufferSize "	+ IOGranularity + " transferGranularity " + transferGranularity +
				" MaxEagerSendSize " + getMaxEagerSendSize());

		for(CommandType cm: clientFunctionImplementation.keySet()){
			sb.append("\n\tmethod " + cm.toString()  +
					clientFunctionImplementation.get(cm));
		}

		return sb.toString();
	}


	/**
	 * Maximum amount of data transferred in the eager protocol.
	 */

	/**
	 * Get the maximum amount of data which should be read or written at once from the I/O subsystem.
	 */
	@AttributeGetters
	public long getTransferGranularity() {
		return transferGranularity;
	}

	/**
	 * Set the maximum amount of data which should be read or written at once from the I/O subsystem.
	 */
	public void setTransferGranularity(long transferGranularity) {
		this.transferGranularity = transferGranularity;
	}


	/**
	 * Get the maximum amount of data which should be read or written at once from the I/O subsystem.
	 * @return
	 */
	@AttributeGetters
	public long getIOGranularity() {
		return IOGranularity;
	}

	/**
	 * Set the maximum amount of data which should be read or written at once from the I/O subsystem.
	 * @param flowBufferSize
	 */
	public void setIOGranularity(long flowBufferSize) {
		this.IOGranularity = flowBufferSize;
	}

	/**
	 * Return the maximum amount of data transferred in the eager protocol.
	 *
	 * @return the maxEagerSendSize
	 */
	@AttributeGetters
	public long getMaxEagerSendSize() {
		return maxEagerSendSize;
	}

	/**
	 * Set the maximum amount of data transferred in the eager protocol.
	 * @param maxEagerSendSize
	 */
	public void setMaxEagerSendSize(long maxEagerSendSize) {
		this.maxEagerSendSize = maxEagerSendSize;
	}

	@AttributeList(
			listAnnotationCreatorMethod = "getAvailableMethodImplementations",
			classImplementingListMethod = "de.hd.pvs.piosim.model.ClientMethods"
	)

	@AttributeGetters
	public String getClientFunctionImplementation(CommandType method) {
		return clientFunctionImplementation.get(method);
	}

	/**
	 * Set the implementation for a specific function group.
	 *
	 * @param method
	 * @param impl Valid implementation.
	 */
	public void setClientFunctionImplementation(CommandType method, String impl){
		clientFunctionImplementation.put(method, impl);
	}

}
