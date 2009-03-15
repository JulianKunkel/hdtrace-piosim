package de.hd.pvs.piosim.model.program;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * The full program is always in memory.
 * 
 * @author julian
 */
public class ProgramInMemory extends Program {

	/**
	 * Sequence of commands to run.
	 */
	private final ArrayList<Command> commands = new ArrayList<Command>();
	
	/**
	 * The next command number to be read.
	 */
	private int currentCommandPosition = 0;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Command c : commands) {
			b.append(c + "\n");
		}
		return b.toString();
	}
		
	/**
	 * Return the number of commands in this program
	 * @return
	 */
	public int getSize() {
		return commands.size();
	}
	
	/**
	 * Return the list of commands to run.
	 * @return
	 */
	public ArrayList<Command> getCommands() {
		return commands;
	}
	
	@Override
	public Command getNextCommand() {
		if(currentCommandPosition < commands.size())
			return commands.get(currentCommandPosition++);
		else
			return null;
	}
	
	@Override
	public boolean isFinished() {		
		return currentCommandPosition + 1 == commands.size();
	}
	
	@Override
	public void restartWithFirstCommand() {
		currentCommandPosition = 0;
	}
}
