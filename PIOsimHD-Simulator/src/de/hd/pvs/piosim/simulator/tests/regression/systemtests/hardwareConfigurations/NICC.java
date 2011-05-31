package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.NIC.NIC;

/**
 * @author julian
 */
public class NICC implements HardwareComponents{
	static public NIC PVSNIC(){
		//NICAnalytical nic = new NICAnalytical();
		NIC nic = new NIC();
		nic.setName("NIC");
		nic.setTotalBandwidth(100000 * MBYTE);
		return nic;
	}

}
