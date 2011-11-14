package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NIC.NICAnalytical;

/**
 * @author julian
 */
public class NICC implements HardwareComponents{
	static public NIC PVSNIC(){
		//NICAnalytical nic = new NICAnalytical();
		NIC nic = new NIC();
		nic.setName("NIC");
		nic.setTotalBandwidth(40 * GIB);
		return nic;
	}
	static public NIC NICAnalytical(){
		//NICAnalytical nic = new NICAnalytical();
		NIC nic = new NICAnalytical();
		nic.setName("NIC");
		nic.setTotalBandwidth(40 * GIB);
		return nic;
	}

}
