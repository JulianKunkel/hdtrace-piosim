package de.hd.pvs.piosim.simulator.inputOutput;


import java.util.List;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.inputOutput.IIOTarget;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;

public class IORedirectionHelper {
	static public IORedirection getIORedirectionLayerFor(List<IORedirection> layers, int id){
		IORedirection ioRedirection = null;

		// now setup IORedirection Layer if applicable.
		for(IORedirection layer: layers){
			if(layer.getModifyingComponentIDs().contains(id)){
				if(ioRedirection != null){
					throw new IllegalArgumentException("Error, IORedirection Layer is already set");
				}
				ioRedirection = layer;
			}
		}
		return ioRedirection;
	}

	static public INodeHostedComponent getNextHopFor(INodeHostedComponent target, IORedirection ioRedirection, Model model){
		if(ioRedirection == null){
			return target;
		}

		final int id = ((IIOTarget) target).getIdentifier().getID();
		final Integer redirectVia = ioRedirection.getRedirectFor(id);

		if(redirectVia != null){
			// is set!
			return (INodeHostedComponent) model.getCidCMap().get(redirectVia);
		}else if(ioRedirection.getDefaultRouteID() != IORedirection.ROUTE_DIRECTLY){
			// default route is set
			return (INodeHostedComponent) model.getCidCMap().get(ioRedirection.getDefaultRouteID());
		}else{
			// route directly to server
			return target;
		}
	}
}
