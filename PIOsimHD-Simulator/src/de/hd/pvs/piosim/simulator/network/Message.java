
 /** Version Control Information $Id: Message.java 706 2009-10-06 13:52:54Z kunkel $
  * @lastmodified    $Date: 2009-10-06 15:52:54 +0200 (Di, 06. Okt 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 706 $
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

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

/**
 * A network message consists of several packets aka message parts.
 *
 * @author Julian M. Kunkel
 *
 */
public class Message<Data extends IMessageUserData> implements INetworkMessage {

	/**
	 * Flag if the MESSAGE_OVERHEAD is applied per message or per message part.
	 */
	final public static boolean overheadPerMessagePart = false;

	/**
	 * Amount of bytes used for the addressing.
	 * Currently, the minimum TCP header size (20) and IP header size (20) are used.
	 * Every packet created will add this overhead.
	 */
	final public static int MESSAGE_OVERHEAD_BYTES = 20+20;

	/**
	 * Receiver of the network message
	 */
	final private INetworkExit targetComponent;

	final private INetworkEntry sourceComponent;

	final RelationToken relationToken;

	/**
	 * The total size of this message.
	 */
	final private long payloadSize;


	/**
	 * How much data has been already packed into smaller packets.
	 */
	/* necessary to manage send and receive */
	private long createdPosition = 0;

	/**
	 * How much data got received from the target network component.
	 */
	private long receivedSize = 0;

	/** this is Data which is already available to transfer by the NIC, but
	 * not transferred so far. It can be transferred by the NIC as soon as possible.
	 */
	private long availableDataPosition = 0;

	final private Data containedData;

	/**
	 * Splits the message into a new smaller part.
	 * @param splitSize How much data can be contained at most in the MessagePart
	 */
	public MessagePart createNextMessagePart(long splitSize){
		long position = createdPosition;
		long remainingAvailBytes = (availableDataPosition - position);

		if ( remainingAvailBytes == 0){
			/* send complete OR data is not availabe, yet */
			return null;
		}

		long size     = (remainingAvailBytes > splitSize) ? splitSize : remainingAvailBytes;

		MessagePart part;

		if(overheadPerMessagePart){
			part = new MessagePart(this, size, MESSAGE_OVERHEAD_BYTES, position);
		}else if(! overheadPerMessagePart){
			if(createdPosition != 0){
				part = new MessagePart(this, size, 0, position);
			}else{ // first packet add the message part
				part = new MessagePart(this, size, MESSAGE_OVERHEAD_BYTES, position);
			}
		}

		createdPosition = position + size;
		return part;
	}

	public MessagePart createEmptyMessage(){
			return new MessagePart(this, 0, MESSAGE_OVERHEAD_BYTES, 0);
	}

	/**
	 * Receive a MessagePart.
	 * @param part
	 */
	public void receivePart(MessagePart part){
		receivedSize += part.getPayloadSize();
	}

	/**
	 * This constructor can be used to create messages which data is available upon call.
	 * @param size
	 * @param job
	 * @param flowPart
	 */
	public Message(long size, Data containedData, INetworkEntry sourceComponent, INetworkExit targetComponent, RelationToken parentToken) {
		assert(targetComponent != null);
		assert(sourceComponent != null);

		this.payloadSize = size;

		this.availableDataPosition = size;


		this.containedData = containedData;
		this.targetComponent = targetComponent;
		this.sourceComponent = sourceComponent;
		this.relationToken = parentToken;
	}

	public void resetMessage(){
		this.availableDataPosition = 0;
	}

	/**
	 * This function signals the availability of more data (which can be sent by a NIC)
	 * @param count Data created.
	 */
	public void appendAvailableDataToSend(long count){
		this.availableDataPosition += count;
		assert(this.availableDataPosition <= this.payloadSize);
	}


	/**
	 * Checks if the message got received completely.
	 * @return
	 */
	public boolean isReceivedCompletely(){
		return (receivedSize == payloadSize);
	}

	public long getRemainingBytesToReceive(){
		return payloadSize - receivedSize;
	}

	/**
	 * return the amounts of bytes which must be send.
	 * @return
	 */
	public long getRemainingBytesToSend(){
		return this.availableDataPosition - this.createdPosition;
	}

	/**
	 * Check if all data which should be transferred is available.
	 * @return
	 */
	public boolean isAllMessageDataAvailable(){
		return this.availableDataPosition == this.payloadSize;
	}

	@Override
	public INetworkExit getMessageTarget() {
		return targetComponent;
	}

	@Override
	public long getSize() {
		return payloadSize;
	}

	@Override
	public String toString() {
		return "Message from: " + sourceComponent.getIdentifier() + " to: " + targetComponent.getIdentifier() + " size " + getSize() + " data " + containedData ;
	}

	/**
	 * Get the user data transported with this message
	 * @return
	 */
	public Data getContainedUserData() {
		return containedData;
	}

	@Override
	public INetworkEntry getMessageSource() {
		return sourceComponent;
	}

	public RelationToken getRelationToken() {
		return relationToken;
	}
}
