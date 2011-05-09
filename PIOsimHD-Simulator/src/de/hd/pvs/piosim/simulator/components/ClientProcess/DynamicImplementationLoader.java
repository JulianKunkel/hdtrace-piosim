package de.hd.pvs.piosim.simulator.components.ClientProcess;

import java.util.HashMap;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * This class maintains maps to translate Commands to their corresponding implementation.
 * The GlobalSettings define which implementation if it is not enforced.
 *
 * @author julian
 */
public class DynamicImplementationLoader {

	final GlobalSettings settings;


	private static DynamicImplementationLoader instance;

	public static DynamicImplementationLoader getInstance() {
		return instance;
	}

	public DynamicImplementationLoader(GlobalSettings settings) {
		this.settings = settings;
	}

	/**
	 * Maps the commands to the command's implementation. For all clients the
	 * same implementation must be picked.
	 */
	private final HashMap<Class<? extends Command>, CommandImplementation> commandMap =
		new HashMap<Class<? extends Command>, CommandImplementation>();

	/**
	 * if a command implementation is enforced then a instance get added to this set here, to allow
	 * a consistent view.
	 */
	private final HashMap<Class<? extends CommandImplementation>, CommandImplementation> enforcedCommandImplementations =
		new HashMap<Class<? extends CommandImplementation>, CommandImplementation>();



	/**
	 * Pick a <code>CommandImplementation</code> for a command of a particular type..
	 * @param what
	 * @return
	 */
	private CommandImplementation instantiateCommandImplementation(Class<? extends Command>  what){
		CommandImplementation imp = null;

		//determine global setting value
		final CommandType cMethodMapping = DynamicCommandClassMapper.getCommandImplementationGroup(what);
		assert(cMethodMapping != null);

		String implChoosen = settings.getClientFunctionImplementation(cMethodMapping);

		assert(implChoosen != null);

		// instantiate an object for the command implementation.
		try{
			Class<?> implClass = Class.forName(implChoosen);

			assert(implClass != null);

			if(enforcedCommandImplementations.containsKey(implClass)){
				// class is loaded already, therefore use it.
				imp = enforcedCommandImplementations.get(implClass);
			}else{

				Object obj = implClass.newInstance();
				//singular implementation.
				imp = (CommandImplementation) obj;
			}
		}catch(Exception e){
			System.err.println("Problem in class: " + implChoosen + " does not implement the command: " +
					what.getCanonicalName());
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Methods for command: " + what.getSimpleName() + " in class: " +
				imp.getClass().getCanonicalName());

		commandMap.put(what, imp);
		enforcedCommandImplementations.put(imp.getClass(), imp);
		return imp;
	}

	/**
	 * Load the CommandImplementation from the given Class.
	 * @param forcedImpl
	 * @return
	 */
	public CommandImplementation getInstanceForClass(Class<? extends CommandImplementation> forcedImpl){
		// check if a instance exists
		CommandImplementation cme = enforcedCommandImplementations.get(forcedImpl);
		if(cme == null){
			// instantiate now:
			try{
				cme = forcedImpl.newInstance();
			}catch(Exception e){
				System.err.println("Problem in enforced class: " + forcedImpl.getCanonicalName());
				e.printStackTrace();
				System.exit(1);
			}

			enforcedCommandImplementations.put(forcedImpl, cme);
		}
		return cme;
	}

	/**
	 * Load the command implementation from the global settings.
	 * @param cmd
	 * @return
	 */
	public CommandImplementation getCommandInstanceForCommand(Class<? extends Command> cmd){
		CommandImplementation cme = commandMap.get(cmd);

		if(cme == null){
			cme = instantiateCommandImplementation(cmd);
		}
		return cme;
	}

	/**
	 * Return the default class as specified in the GlobalSettings if not overridden in forcedImpl
	 *
	 * @param forcedImpl
	 * @param cmd
	 * @return
	 */
	public CommandImplementation getCommandInstanceIfNotForced(Class<? extends CommandImplementation> forcedImpl, Class<? extends Command> cmd){
		return (forcedImpl == null ) ? getCommandInstanceForCommand(cmd) : getInstanceForClass(forcedImpl);
	}


	public final static void initalize(GlobalSettings settings){
		instance = new DynamicImplementationLoader(settings);
	}
}
