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
