
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


import java.lang.reflect.Field;
import java.util.HashMap;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Router.Router;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.interfaces.ISerializableObject;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.NetworkTopology;
import de.hd.pvs.piosim.model.program.Application;

/**
 * Lightweight Mediator (and Facade) class which eases creation of new Models.
 * All created objects are added to the model.
 *
 * @author Julian M. Kunkel
 *
 */
public class ModelBuilder {
	/**
	 * The actual model which is modified.
	 */
	Model model = null;

	/**
	 * Basic Constructur, use this one to create a new model.
	 */
	public ModelBuilder() {
		// initialize mappings without loading the classes explicitly
		DynamicCommandClassMapper.loadConfiguration(false);
		DynamicModelClassMapper.loadConfiguration(false);

		model = new Model();
	}

	/**
	 * Use this constructor to extend an already existing model.
	 *
	 * @param modelToExtend
	 */
	public ModelBuilder(Model modelToExtend) {
		model = modelToExtend;
	}

	/**
	 * Return the GlobalSettings, to be modified directly.
	 *
	 * @return
	 */
	public GlobalSettings getGlobalSettings() {
		return model.getGlobalSettings();
	}

	/**
	 * Modify the model in a consistent way that a router is part of the node.
	 *
	 * @param node
	 * @param client
	 */
	public void addRouter(Node node, Router router) {
		node.getHostedComponents().add(router);
		router.setParentComponent(node);

		if (model.isComponentInModel(node)){
			model.addComponent(router);
		}
	}

	/**
	 * Modify the model in a consistent way that the client is part of the node.
	 *
	 * @param node
	 * @param client
	 */
	public void addClient(Node node, ClientProcess client) {
		node.getHostedComponents().add(client);
		client.setParentComponent(node);

		client.getNetworkInterface().setParentComponent(client);

		if (model.isComponentInModel(node)){
			model.addComponent(client);
		}
	}

	/**
	 * Modify the model in a consistent way that the server is part of the node.
	 *
	 * @param node
	 * @param server
	 */
	public void addServer(Node node, Server server) {
		node.getHostedComponents().add(server);
		server.setParentComponent(node);

		server.getNetworkInterface().setParentComponent(server);

		if (model.isComponentInModel(node)){
			model.addComponent(server);
		}
	}

	/**
	 * Add a node and all subcomponents to the model.
	 *
	 * @param node
	 */
	public void addNode(Node node) {
		model.addComponent(node);
	}

	/**
	 * Link two entities together.
	 *
	 * @param nic
	 * @param port
	 */
	public void connect(INetworkTopology topology, INetworkNode src, INetworkEdge via, INetworkNode tgt) {
		if(! model.isComponentInModel(via)){
			model.addComponent(via);
		}
		if(! model.isComponentInModel(src)){
			throw new IllegalArgumentException("Src component not part of the model, add it!");
		}
		if(! model.isComponentInModel(tgt)){
			throw new IllegalArgumentException("Tgt component not part of the model, add it!");
		}
		((NetworkTopology) topology).addEdge(src, via, tgt);
	}

	public INetworkTopology createTopology(String name){
		NetworkTopology topology = new NetworkTopology();
		model.addTopology(topology);
		topology.setName(name);
		return topology;
	}


	/**
	 * Set the Application of a particular alias.
	 *
	 * @param alias
	 * @param app
	 */
	public void setApplication(String alias, Application app){
		HashMap<String, Application> map= model.applicationNameMap;
		map.put(alias, app);
		app.setAlias(alias);
	}

	/**
	 * Add a Template to the model if it is not contained, yet.
	 */
	public void addTemplateIf(IBasicComponent template){
		if(model.getTemplateManager().getTemplate(template.getName()) != null){
			return;
		}
		model.getTemplateManager().addTemplate(template);
	}



	/**
	 * Add a Template to the Model.
	 *
	 * @param template
	 */
	public void addTemplate(IBasicComponent template){
		model.getTemplateManager().addTemplate(template);
	}

	/**
	 * Remove the component from the model and add it as a template.
	 *
	 * @param component
	 */
	public void changeComponentToTemplate(IBasicComponent component){
		// remove component from the model.
		model.removeComponent(component);
		model.getTemplateManager().addTemplate(component);
	}

	/**
	 * Clone the component from a particular template.
	 *
	 * @param <T>
	 * @param component
	 * @return
	 * @throws Exception
	 */
	public <T extends IBasicComponent> T cloneFromTemplate(T component) throws Exception{
		return model.getTemplateManager().cloneFromTemplate(component);
	}

	/**
	 * Clone the template with a specific name.
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public IBasicComponent cloneFromTemplate(String name) throws Exception{
		return (IBasicComponent) model.getTemplateManager().cloneFromTemplate(name);
	}

	/**
	 * The modifications made in the newTemplate are propagated in all model components using
	 * this Template as follows:
	 * - The template name is modified.
	 * - Attributes are set to the new template values if the old template value equals the current
	 *   attribute value of the object. Therefore attributes which were changed after the template
	 *   values have been set are preserved.	 *
	 *
	 * @param oldTemplate The Name of the old template which should be replaced by the newTemplate
	 * @param newTemplate
	 */
	public void modifyTemplateAndDerivedObjects(String oldTemplateName, BasicComponent newTemplate){
		TemplateManager templateManager = model.getTemplateManager();

		ISerializableObject oldTemplate = templateManager.getTemplate(oldTemplateName);
		if(oldTemplate == null){
			throw new IllegalArgumentException("Template with name " + oldTemplateName + " does not exist!");
		}

		if(newTemplate == null || newTemplate.getName() == null || newTemplate.getName().length() < 2){
			throw new IllegalArgumentException("New template is invalid, it needs a name");
		}

		if( newTemplate.getClass() != oldTemplate.getClass()){
			throw new IllegalArgumentException("The new template has a different class than the old: " +
					oldTemplate.getClass().getCanonicalName() + " new: " + newTemplate.getClass().getCanonicalName());
		}

		templateManager.deleteTemplate(oldTemplateName);
		templateManager.addTemplate(newTemplate, newTemplate.getName());

		for(IBasicComponent component: model.getCidCMap().values()){
			if(component.getTemplate() == null || ! component.getTemplate().equals(oldTemplateName) )
				continue;

			// this object is instantiated from the template

			final BasicComponent compImpl = (BasicComponent) component;

			if(newTemplate.getName() != oldTemplateName){
				compImpl.setTemplate(newTemplate.getName());

				//maybe we should rename instantiated components in case their name was not changed
				if(compImpl.getName().startsWith(oldTemplateName + "_") ){
					model.renameComponent( compImpl, newTemplate.getName() + "_01" );
				}
			}

			// set basic values based on templated values

			Class classIterate = component.getClass();

			while(classIterate != Object.class) {
				Field [] fields = classIterate.getDeclaredFields();
				for (Field field : fields) {
					if( ! field.isAnnotationPresent(Attribute.class))
						continue;

					try{
						field.setAccessible(true);
						Object oldvalue = field.get(component);
						Object newValue = field.get(newTemplate);
						Object oldTemplateValue =  field.get(oldTemplate);

						//System.out.println(field.getName() + " - " + oldvalue + " tmpl: " + oldTemplateValue + " new: " + newValue);

						if(oldvalue.equals(oldTemplateValue)){
							field.set(component, newValue);
						}
						field.setAccessible(false);
					}catch(Exception e){
						e.printStackTrace();
					}
				}

				classIterate = classIterate.getSuperclass();
			}
		}
	}


	// TODO same for delete, move rename etc. from model to this location !

	/**
	 * Get the (final) Model from the Model Builder.
	 *
	 * @return The model
	 */
	public Model getModel() {
		return model;
	}

	public void addNetworkNode(INetworkNode interNode) {
		model.addComponent(interNode);
	}
}
