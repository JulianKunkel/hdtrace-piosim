
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

package de.hd;

/**
 * @hidden
 * @opt nodefontname luxisr
 * @opt nodefontabstractname luxisri
 * @opt edgefontname luxisr
 * @opt nodefontsize 8
 * @opt edgefontsize 8
 * @opts inferdep #creates too many arcs
 * @opts useimports
 
 */
class UMLOptions {}

/**
 * @view
 
 * @opt postfixpackage
 * @opt qualify
 * @opt types
 
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 
 * @match class de.hd.pvs.piosim.model.components.*
 * @opt nodefillcolor PaleGreen
 * @opt !hide
 
 */
class ModelComponentOverview {}

/**
 * @view
 
 * @opt postfixpackage
 * @opt qualify
 * @opt types
 
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 * 
 * @match class de.hd.pvs.piosim.simulator.component.*
 * @opt nodefillcolor LemonChiffon
 * @opt !hide  
 */
class SimulatorComponentOverview {}



/**
 * @view
 
 * @opt postfixpackage
 * @opt qualify
 * @opt types
 * @opt attributes
 * @opt operations
 * @opt visibility
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 
 * @match class de.hd.pvs.piosim.model.components.*
 * @opt nodefillcolor PaleGreen
 * @opt !hide
 
 */
class ModelComponentDetails {}

/**
 * @view
 * @opt attributes
 * @opt operations
 * @opt postfixpackage
 * @opt qualify
 * @opt types
 * @opt visibility
 
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 * 
 * @match class de.hd.pvs.piosim.simulator.component.*
 * @opt nodefillcolor LemonChiffon
 * @opt !hide  
 */
class SimulatorComponentDetails {}

/**
 * @view
 * @opt attributes
 * @opt operations
 * @opt postfixpackage
 * @opt types
 * @opt visibility
 
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 * 
 * @match class de.hd.pvs.piosim.simulator.program.*
 * @opt nodefillcolor LemonChiffon
 * @opt !hide  
 */
class SimulatorCommandsDetails {}

/**
 * @view
 * @opt qualify
 * @opt postfixpackage
  
 * @match class .*
 * @opt nodefillcolor LightGray
 * @opt hide
 * 
 * @match class de.hd.pvs.piosim.simulator.program.*
 * @opt nodefillcolor LemonChiffon
 * @opt !hide  
 */
class SimulatorCommandsOverview {}

/**
 * @view
 
 * @match class .*
 * @opt nodefillcolor LightGray
 
 * @match class de.hd.pvs.piosim.simulator.*
 * @opt nodefillcolor LemonChiffon
 
 * @match class de.hd.pvs.piosim.model.*
 * @opt nodefillcolor PaleGreen
 
 */
class Overview {}

/**
 * @view
 * @opt visibility
 * @opt operations
 
 * @match class .*
 * @opt nodefillcolor LightGray
 
 * @match class de.hd.pvs.piosim.simulator.*
 * @opt nodefillcolor LemonChiffon
 
 * @match class de.hd.pvs.piosim.model.*
 * @opt nodefillcolor PaleGreen
 
 * @match class java.*|org.xml.*
 * @opt !attributes
 * @opt !operations
 */
class DetailedView {}


/*
 * @view
 * @opt inferrel
 * @opt collpackages java.util.*
 * @opt inferdep
 * @opt hide java.*

 * @match class .*
 * @opt nodefillcolor LightGray
 
 * @match class de.hd.pvs.piosim.simulator.*
 * @opt nodefillcolor LemonChiffon
 
 * @match class de.hd.pvs.piosim.model.*
 * @opt nodefillcolor PaleGreen
 
 */
class RelationsAndDependencies {}
