
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

import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;

/**
 * A network message consists of several packets.
 *
 * @author Julian M. Kunkel
 *
 */
public class Message<Data extends IMessageUserData> implements INetworkMessage {
	/**
	 * Receiver of the network message
	 */
	final private INetworkExit targetComponent;

	final private INetworkEntry sourceComponent;

	/**
	 * The total size of this message.
	 */
	final private long totalSize;

	/**
	 * How much data has been already packed into smaller packets.
	 */
	/* necessary to manage send and receive */
	private long createdPosition = 0;

	/**
	 * How much data got received from the target network component.
	 */
	private long receivedPosition = 0;

	/** this is Data which is already available to transfer by the NIC, but
	 * not transferred so far. It can be transferred by the NIC as soon as possible.
	 */
	private long availableDataPosition = 0;

	final private Data containedData;

	/**
	 * Return the size of this message
	 */
	public long getTotalSize() {
		return totalSize;
	}

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
		MessagePart part = new MessagePart(this, size, position);
		createdPosition = position + size;
		return part;
	}

	/**
	 * Undo the creation of the message part i.e. a call to createNextMessagePart
	 * and then undo does not change the internal state of a message
	 *
	 * @param part
	 */
	public void undoCreationOfMessagePart(MessagePart part){
		assert(part.getPosition() == createdPosition - part.getSize());
		createdPosition -= part.getSize();
	}

	/**
	 * Receive a MessagePart.
	 * @param part
	 */
	public void receivePart(MessagePart part){
		receivedPosition += part.getSize();
	}

	/**
	 * This constructor can be used to create messages which data is available upon call.
	 * @param size
	 * @param job
	 * @param flowPart
	 */
	public Message(long size, Data containedData, INetworkEntry sourceComponent, INetworkExit targetComponent) {
		this.totalSize = size;
		this.availableDataPosition = size;
		this.containedData = containedData;
		this.targetComponent = targetComponent;
		this.sourceComponent = sourceComponent;
	}

	/**
	 * This constructor is used to create messages which data is only
	 * partially available upon call. However, transfer should start immediately.
	 * @param size
	 * @param job
	 * @param currentPosition How much data is already available from the Message?
	 */
	public Message(long size, long currentPosition, Data containedData, INetworkEntry sourceComponent, INetworkExit targetComponent) {
		this.totalSize = size;
		this.availableDataPosition = currentPosition;
		this.containedData = containedData;
		this.targetComponent = targetComponent;
		this.sourceComponent = sourceComponent;
	}

	/**
	 * This function signals the availability of more data (which can be sent by a NIC)
	 * @param count Data created.
	 */
	public void appendAvailableDataToSend(long count){
		this.availableDataPosition += count;
		assert(this.availableDataPosition <= this.totalSize);
	}


	/**
	 * @return the availableDataPosition
	 */
	public long getAvailableDataPosition() {
		return availableDataPosition;
	}

	/**
	 * Checks if the message got received completely.
	 * @return
	 */
	public boolean isReceivedCompletely(){
		return (receivedPosition == totalSize);
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
		return this.availableDataPosition == this.totalSize;
	}

	@Override
	public INetworkExit getMessageTarget() {
		return targetComponent;
	}

	@Override
	public long getSize() {
		return totalSize;
	}

	@Override
	public String toString() {
		return " to: " + targetComponent.getIdentifier();
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
}
