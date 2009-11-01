package de.hd.pvs.piosim.simulator.components.Router;

import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.IGComponent;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;

public interface IGRouter<Type extends SPassiveComponent>
	extends IGComponent<Type>, ISNodeHostedComponent<Type>
{

}
