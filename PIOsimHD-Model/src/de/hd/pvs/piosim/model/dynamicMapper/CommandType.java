package de.hd.pvs.piosim.model.dynamicMapper;

/**
 * Defines a command type which can have several implementations.
 *
 * @author Julian M. Kunkel
 */

public class CommandType implements Comparable<CommandType>{
	final private String commandName;

	static final public CommandType NoOperation = new CommandType("NoOperation");

	public CommandType(String commandName) {
		this.commandName = commandName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass()) {
			CommandType cg = (CommandType) obj;
			return cg.commandName.equals(this.commandName);
		}else {
			return super.equals(obj);
		}
	}

	public int compareTo(CommandType group) {
		return this.commandName.compareTo(group.commandName);
	}

	@Override
	public int hashCode() {
		return commandName.hashCode();
	}

	@Override
	public String toString() {
		return commandName;
	}

	public String getCommandName() {
		return commandName;
	}
}