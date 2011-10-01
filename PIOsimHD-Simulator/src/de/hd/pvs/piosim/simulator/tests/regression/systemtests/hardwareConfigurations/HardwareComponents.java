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

	public final long KBYTE = 1000;
	public final long MBYTE = 1000 * KBYTE;
	public final long GBYTE = 1000 * MBYTE;
	public final long TBYTE = 1000 * GBYTE;

	public final long KIB = 1024;
	public final long MIB = 1024 * KIB;
	public final long GIB = 1024 * MIB;
	public final long TIB = 1024 * GIB;
}
