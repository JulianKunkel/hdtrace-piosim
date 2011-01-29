package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.NIC.NIC;

/**
 * @author julian
 */
public class NICC implements HardwareComponents{
	static public NIC PVSNIC(){
		NIC nic = new NIC();
		nic.setName("NIC");
		// determined on west1 by using memory-bandwidth.c 1000 iter, 104857600
		// 1000 iterations, time:13.106618s MB/s:7629.733218
		nic.setTotalBandwidth(7629 * MBYTE);
		return nic;
	}

}
