
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

import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.event.EventData;


/**
 * A MessagePart is a small packet from a Message.
 * @see Message
 *
 * @author Julian M. Kunkel
 *
 */
public class MessagePart implements INetworkMessage, EventData{

	/**
	 * Size of this MessagePart
	 */
	final private long partSize;

	final private long payloadSize;

	/**
	 * Position of this MessagePart inside the Message.
	 */
	final private long position;

	/**
	 * Parent Message
	 */
	final private Message msg;

	public MessagePart(Message msg, long payload, long header, long position) {
		this.partSize = payload + header;
		this.payloadSize = payload;

		this.msg = msg;
		this.position = position;
	}

	public long getPosition() {
		return position;
	}

	/**
	 * Return the size of this packet.
	 * @return
	 */
	public long getSize() {
		return partSize;
	}

	public long getPayloadSize(){
		return payloadSize;
	}

	@Override
	public String toString() {
		return "<" + partSize + "," + position + "," + msg + ">";
	}

	@Override
	public INetworkExit getMessageTarget() {
		return msg.getMessageTarget();
	}

	@Override
	public INetworkEntry getMessageSource(){
		return msg.getMessageSource();
	}

	/**
	 * Return the parent message which this object one is part of.
	 * @return
	 */
	public Message getMessage() {
		return msg;
	}
}
