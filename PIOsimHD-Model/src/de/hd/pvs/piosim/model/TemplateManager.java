
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
//
//	This file is part of PIOsimHD.
//
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model;

import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.interfaces.IDynamicModelComponent;
import de.hd.pvs.piosim.model.interfaces.ISerializableTemplateObject;


/**
 * Manages template components. These components can be instanciated several times
 * and allow a fast replication of useful (or standard) components.
 *
 * @author Julian M. Kunkel
 */
public class TemplateManager {

	private final XMLReaderToRAM reader = new XMLReaderToRAM();
	private final SerializationHandler serializationHandler = new SerializationHandler();

	/* String == Template-Name */
	private HashMap<String, IDynamicModelComponent> templates = new HashMap<String, IDynamicModelComponent>();

	/**
	 * Return all templates.
	 * @return the templates
	 */
	public Collection<IDynamicModelComponent> getTemplates() {
		return templates.values();
	}

	/**
	 * Returns the component for a given name
	 * @param search the template name
	 * @return the template with a given name or null
	 */
	public IDynamicModelComponent getTemplate(String search){
		return templates.get(search);
	}

	public void addTemplate(ISerializableTemplateObject template){
		addTemplate(template, template.getName());
	}

	/**
	 * Add a template. The Object is cloned via XML to ensure that modifications which are done
	 * after the component is added do not affect cloned components.
	 */
	public void addTemplate(IDynamicModelComponent template, String name){
		if(name == null || name.length() < 2){
			throw new IllegalArgumentException("Template name must be longer than 1 character");
		}

		if (templates.get(name) != null){
			throw new IllegalArgumentException("Template with name " + name + " already exists!");
		}

		try{
			IDynamicModelComponent clonedTemplate = serializationHandler.createDynamicObjectFromXML(getXMLRepresentation(template));

			templates.put(name, clonedTemplate);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create the XML from a component
	 * @param component of which XML should be created
	 * @return Root element node.
	 * @throws Exception
	 */
	private XMLTag getXMLRepresentation(IDynamicModelComponent component) throws Exception{
		StringBuffer sb = new StringBuffer();
		serializationHandler.createXMLFromInstance(component, sb);

		//System.out.println("XML: " + sb);

		return reader.convertXMLToXMLTag(sb.toString());
	}

	/**
	 * Clone a component. This is done by serializing the component to XML and then reading the XML
	 * back.
	 * @param <T> the component type to clone
	 * @param component the component to clone.
	 * @return The cloned component.
	 * @throws Exception
	 */
	public <T extends ISerializableTemplateObject> T cloneFromTemplate(T component) throws Exception{
		if(templates.get(component.getName()) == null){
			throw new IllegalArgumentException("Template is not registered!");
		}

		T clone = (T) serializationHandler.createDynamicObjectFromXML(getXMLRepresentation(component));

		return clone;
	}

	/**
	 * Clone a component. Invokes  cloneFromTemplate(T component).
	 * @param name The template name of the component to be cloned
	 * @return The cloned component
	 * @throws Exception
	 */
	public IDynamicModelComponent cloneFromTemplate(String name) throws Exception{
		IDynamicModelComponent component = templates.get(name);
		if(component == null){
			throw new IllegalArgumentException("Template with name " + name + " does not exists!");
		}

		return cloneFromTemplate((IBasicComponent)  component);
	}


	/**
	 * Remove a template.
	 * @param templateName The name of the template to remove.
	 */
	void deleteTemplate(String templateName){
		if ( templates.remove(templateName) == null){
			throw new IllegalArgumentException("Template with name " + templateName +
					" is not part of the TemplateManager");
		}
	}
}
