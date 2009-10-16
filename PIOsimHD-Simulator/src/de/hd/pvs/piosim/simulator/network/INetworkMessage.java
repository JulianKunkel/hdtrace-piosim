
 /** Version Control Information $Id: INetworkMessage.java 708 2009-10-10 12:09:14Z kunkel $
  * @lastmodified    $Date: 2009-10-10 14:09:14 +0200 (Sa, 10. Okt 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 708 $
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

/**
 *
 */
package de.hd.pvs.piosim.simulator.network;

import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;


/**
 * A network message transfers a message which has a well defined size.
 * Depending on the implementing class it can contain further information required to process
 * or interprete the message.
 *
 * @author Julian M. Kunkel
 *
 */
public interface INetworkMessage {
	/**
	 * size of the job to be transferred over the network
	 * if <= 0 the size is treated as unknown (for RECV messages).
	 * @return
	 */
	public long getSize();


	/**
	 * The destination of this message (part) inside the network
	 * @return
	 */
	public INetworkExit getMessageTarget();

	/**
	 * The source of this message (part)
	 * @return
	 */
	public INetworkEntry getMessageSource();
}
