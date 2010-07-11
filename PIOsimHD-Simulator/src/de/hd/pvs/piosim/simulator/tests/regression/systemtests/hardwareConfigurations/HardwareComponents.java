package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;


/**
 * Contains archetypes for components.
 * Warning, each component should have an individual name, otherwise the template
 * management will not work.
 *
 * Once a component is defined here, its parameters shall never be modified!
 *
 * @author julian
 */
public interface HardwareComponents {

	public final long KBYTE = 1024;
	public final long MBYTE = 1024 * KBYTE;
	public final long GBYTE = 1024 * MBYTE;
	public final long TBYTE = 1024 * GBYTE;

	public final long K = 1000;
	public final long M = 1000 * K;
	public final long G = 1000 * M;
	public final long T = 1000 * G;
	public final long P = 1000 * T;

}
