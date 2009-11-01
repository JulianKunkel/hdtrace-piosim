package de.hd.pvs.piosim.model.inputOutput;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * This layer allows to redirect I/O calls through intermediate nodes.
 * A client (or route) can specify for each I/O server a router over which all
 * operations will be redirected.
 *
 * @author julian
 *
 */
public class IORedirection{
	/**
	 * If the defaultRouteID is equal to this value,
	 * then the servers are contacted directly, i.e. without redirection.
	 */
	final public static int ROUTE_DIRECTLY = -1 ;

	/**
	 * the component id which shall be used as a default route.
	 */
	//@Attribute(type=AttributeXMLType.ATTRIBUTE)
	private int defaultRouteID = ROUTE_DIRECTLY;

	/**
	 * Maps the server ID to the intermediate router.
	 */
	HashMap<Integer, Integer> redirects = new HashMap<Integer, Integer>();

	/**
	 * Components which shall use this redirection layer
	 */
	LinkedList<Integer> modifiedComponentIDs = new LinkedList<Integer>();

	public HashMap<Integer, Integer> getRedirects() {
		return redirects;
	}

	public Integer getRedirectFor(int serverID){
		return redirects.get(serverID);
	}

	public void addRedirect(int serverID, int via){
		redirects.put(serverID, via);
	}

	public void addModifyingComponent(int id){
		modifiedComponentIDs.add(id);
	}

	public List<Integer> getModifyingComponentIDs() {
		return modifiedComponentIDs;
	}

	public int getDefaultRouteID() {
		return defaultRouteID;
	}

	/**
	 * Set to ROUTE_DIRECTLY if the servers shall be contacted directly.
	 * @param defaultRouteID
	 */
	public void setDefaultRouteID(int defaultRouteID) {
		this.defaultRouteID = defaultRouteID;
	}
}
