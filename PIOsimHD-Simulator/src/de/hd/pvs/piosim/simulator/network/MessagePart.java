
 /** Version Control Information $Id: MessagePart.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $
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

package de.hd.pvs.piosim.simulator.network;

import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;


/**
 * A MessagePart is a small packet from a Message.
 * @see Message
 *
 * @author Julian M. Kunkel
 *
 */
public class MessagePart implements INetworkMessage{
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

	@Override
	public String toString() {
		return "msg-part: <" + size + "," + position + "> " + msg;
	}

	@Override
	public INetworkExit getNetworkTarget() {
		return msg.getNetworkTarget();
	}

	/**
	 * Return the parent message which this object one is part of.
	 * @return
	 */
	public Message getMessage() {
		return msg;
	}
}
