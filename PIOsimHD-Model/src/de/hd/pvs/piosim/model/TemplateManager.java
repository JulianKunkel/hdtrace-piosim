
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

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;


/**
 * Manages template components. These components can be instanciated several times
 * and allow a fast replication of useful (or standard) components.
 *  
 * @author Julian M. Kunkel
 */
public class TemplateManager {
	
	/* String == Template-Name */
	private HashMap<String, BasicComponent> templates = new HashMap<String,BasicComponent>();

	/**
	 * Return all templates.
	 * @return the templates
	 */
	public Collection<BasicComponent> getTemplates() {
		return templates.values();
	}
	
	/**
	 * Returns the component for a given name
	 * @param search the template name
	 * @return the template with a given name or null
	 */
	public BasicComponent getTemplate(String search){
		return templates.get(search);
	}
	
	/**
	 * Add a template. The Object is cloned via XML to ensure that modifications which are done 
	 * after the component is added do not affect cloned components.
	 */
	public void addTemplate(BasicComponent template){
		String name = template.getName();
		
		if(name == null || name.length() < 2){
			throw new IllegalArgumentException("Template name must be longer than 1 character");
		}
		
		if (templates.get(name) != null){
			throw new IllegalArgumentException("Template with name " + name + " already exists!");
		}
		
		try{
		ModelXMLReader mreader = new ModelXMLReader();
		BasicComponent clonedTemplate = mreader.createComponentFromXML(getXML(template), false);
		
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
	private Element getXML(BasicComponent component) throws Exception{
		ModelXMLWriter mwriter = new ModelXMLWriter();
			
		StringBuffer sb = new StringBuffer();
		mwriter.createXMLFromComponent(component, sb);
		
		//System.out.println("XML: " + sb);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(  new ByteArrayInputStream(sb.toString().getBytes() ));
		return document.getDocumentElement();
	}
	
	/**
	 * Clone a component. This is done by serializing the component to XML and then reading the XML
	 * back.
	 * @param <T> the component type to clone
	 * @param component the component to clone.
	 * @return The cloned component.
	 * @throws Exception
	 */
	public <T extends BasicComponent> T cloneFromTemplate(T component) throws Exception{
		if(templates.get(component.getName()) == null){
			throw new IllegalArgumentException("Template is not registered!");
		}				
		
		ModelXMLReader mreader = new ModelXMLReader();
				
		T clone = (T) mreader.createComponentFromXML(getXML(component), true);
		
		return clone;
	}
	
	/**
	 * Clone a component. Invokes  cloneFromTemplate(T component).
	 * @param name The template name of the component to be cloned
	 * @return The cloned component
	 * @throws Exception
	 */
	public BasicComponent cloneFromTemplate(String name) throws Exception{
		BasicComponent component = templates.get(name);
		if(component == null){
			throw new IllegalArgumentException("Template with name " + name + " does not exists!");
		}
		
		return cloneFromTemplate(component);
	}
	
	/**
	 * Remove a template.
	 * @param template to remove.
	 */
	void deleteTemplate(BasicComponent template){
		deleteTemplate(template.getName());
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
