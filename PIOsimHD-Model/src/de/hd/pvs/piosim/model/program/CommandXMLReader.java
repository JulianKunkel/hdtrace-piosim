package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

public class CommandXMLReader {
	final private Application app;
	final private AttributeAnnotationHandler myCommonAttributeHandler;
	
	final static CommandFactory factory = new CommandFactory();


	class MyAttributeAnnotationHandler extends AttributeAnnotationHandler{
		public MyAttributeAnnotationHandler() {
			setDefaultXMLType(AttributeXMLType.ATTRIBUTE);
		}		

		// extend reader.
		public Object parseXMLString(java.lang.Class<?> type, String what) throws IllegalArgumentException {
			if (type == MPIFile.class) {
				return getFile(what);
			}else if (type == Communicator.class) {
				return getCommunicator(what);
			}
			return super.parseXMLString(type, what);
		};
	}

	public CommandXMLReader(Application app) {
		this.app = app;
		this.myCommonAttributeHandler = new MyAttributeAnnotationHandler();
	}

	/**
	 * Read the XML of a command from the DOM. Therefore Attribute annotated fields are filled with
	 * the XML content.
	 * 
	 * @param commandXMLElement
	 * @param cmd
	 * @param app
	 * @throws Exception
	 */
	public Command readCommandXML(XMLTag commandXMLElement, Program program) throws Exception {
		Command cmd = factory.createCommand(commandXMLElement.getName().toLowerCase());
		
		// read non-standard attributes:
		cmd.readXML(commandXMLElement);

		// now try to fill all command fields as specified by the Annotations.
		myCommonAttributeHandler.readSimpleAttributes(commandXMLElement, cmd);
		
		// read default parameters for all programs from XML:
		String aid = commandXMLElement.getAttribute("aid");
		if (aid != null){
			cmd.setAsynchronousID( Integer.parseInt( aid ) );
		}
		cmd.setProgram(program);
		return cmd;
	}


	/**
	 * Helper function, returns the communicator as specified in the string.
	 *  
	 * @return
	 */
	private Communicator getCommunicator(String which){
		Communicator communicator;		
		if(which != null){
			communicator = app.getCommunicator(which.toUpperCase());
			if (communicator == null){
				throw new IllegalArgumentException("Invalid Communicator with name " + which);
			}

		}else{
			communicator = app.getCommunicator("WORLD");
			if (communicator == null){
				throw new IllegalArgumentException("Invalid Communicator with name " + "WORLD");
			}
		}
		return communicator;
	}

	/**
	 * Helper function, returns the MPI_File as specified in the string.
	 * 
	 * @param which
	 * @return
	 */
	private MPIFile getFile(String which){		
		if (which == null){
			throw new IllegalArgumentException("No file given for command! But a file parameter is necessary!");
		}
		return app.getFile(Integer.parseInt(which));		
	}

}
