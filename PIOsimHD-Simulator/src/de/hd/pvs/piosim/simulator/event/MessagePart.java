
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

package de.hd.pvs.piosim.simulator.event;

import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;


/**
 * A MessagePart is a small packet from a Message.
 * @see Message
 * 
 * @author Julian M. Kunkel
 *
 */
public class MessagePart extends NetworkEventType{
	/**
	 * Size of this MessagePart
	 */
	final private long size;
	
	/**
	 * Position of this MessagePart inside the Message.
	 */
	final private long position;
	
	/**
	 * Parent Message
	 */
	final private Message msg;

	public MessagePart(Message msg, long size, long position) {
		this.size = size;
		this.msg = msg;
		this.position = position;
	}

	public long getPosition() {
		return position;
	}
	
	/**
	 * Check if this MessagePart represents the last packet of a message.
	 * 
	 * @return
	 */
	public boolean isLastPart(){
		return (position + size) == msg.getTotalSize(); 
	}

	/**
	 * Return the size of this packet.
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Return the parent message of which this package is a part.
	 * @return
	 */
	public Message getMessage() {
		return msg;
	}

	@Override
	public String toString() {
		return "msg-part: <" + size + "," + position + "> " + msg;
	}

	/**
	 * Return the network job which created the parent message.
	 */
	public SingleNetworkJob getNetworkJob() {
		return msg.getNetworkJob();
	}
}
