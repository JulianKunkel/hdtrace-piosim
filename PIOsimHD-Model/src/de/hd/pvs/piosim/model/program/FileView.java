package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;

public class FileView {
	final private Datatype datatype;

	public FileView(Datatype datatype, int displacement) {
		StructDatatype dispDatatype = new StructDatatype();
		dispDatatype.appendType(datatype, displacement, Integer.MAX_VALUE);
		this.datatype = dispDatatype;
	}

	public Datatype getDatatype() {
		return datatype;
	}
}
