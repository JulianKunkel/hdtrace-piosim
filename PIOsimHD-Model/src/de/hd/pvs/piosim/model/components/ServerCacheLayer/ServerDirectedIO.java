/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2009 Michael Kuhn
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
package de.hd.pvs.piosim.model.components.ServerCacheLayer;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;

/**
 * This cache tries to aggregate/combine read and write requests into larger
 * requests.
 *
 * @author Michael Kuhn
 *
 */
public class ServerDirectedIO extends NoCache {

	/**
	 * Define the maximum hole between two adjacent read requests to combine them into one big
	 * request.
	 */
	@Attribute
	@NotNegativeOrZero
	int readDataSievingMaxHoleSizeToCombine = 0;

	public void setReadDataSievingMaxHoleSizeToCombine(
			int readDataSievingMaxHoleSizeToCombine) {
		this.readDataSievingMaxHoleSizeToCombine = readDataSievingMaxHoleSizeToCombine;
	}

	public int getReadDataSievingMaxHoleSizeToCombine() {
		return readDataSievingMaxHoleSizeToCombine;
	}
}
