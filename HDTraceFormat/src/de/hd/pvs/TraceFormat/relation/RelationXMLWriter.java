package de.hd.pvs.TraceFormat.relation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;

public class RelationXMLWriter {
	private final int version = 1;
	/**
	 * Output file
	 */
	private final FileWriter file;

	/**
	 * time to write = time - timeAdjustment
	 */
	private final Epoch timeAdjustment;

	private final String localToken;
	private final String hostID;
	private final int topologyNumber;
	private final String filename;
	
	private final TopologyNode topologyNode; 

	private long curToken = 0;
	
	/**
	 * The combined key of host, localtoken and topologyNumber must be unique.
	 *  
	 * @param filename
	 * @param hostID
	 * @param localToken
	 * @param topologyNumber
	 * @param timeAdjustment
	 * @throws IOException
	 */
	public RelationXMLWriter(String filename, String hostID, String localToken, int topologyNumber,  TopologyNode topologyNode, Epoch timeAdjustment) throws IOException {		
		file = new FileWriter(filename);
		file.write("<relation version=\"" + version + "\" hostID=\"" + hostID + "\" localToken=\"" + localToken + "\" topologyNumber=\"" + topologyNumber + "\" timeAdjustment=\"" + timeAdjustment + "\">\n");			

		this.topologyNode = topologyNode;
		this.timeAdjustment = timeAdjustment;
		this.topologyNumber = topologyNumber;
		this.hostID = hostID;
		this.localToken = localToken;
		this.filename = filename;
	}
	
	public TopologyNode getTopologyNode() {
		return topologyNode;
	}

	public RelationToken createTopLevelRelation(Epoch time){
		long token = curToken++;

		try{
			file.write("<rel");
			writeTokenAndTime(token, time);
			file.write("/>");
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}

		return new RelationToken(token, this);
	}


	private void writeTokenAndTime(long token, Epoch time) throws IOException{
		file.write(" t=\"" + token + "\" time=\"" + time.subtract(timeAdjustment) + "\"");
	}

	
	public RelationToken relateProcessLocalToken(RelationToken parent, Epoch time){
		long token = curToken++;

		try{
			file.write("<rel");
			writeTokenAndTime(token, time);
			file.write(" p=\"" + parent.parent.topologyNumber + ":" + parent.id + "\"/>");			
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}				
		
		return new RelationToken(token, this);
	}
	
	public RelationToken relateMultipleProcessLocalTokens(RelationToken [] parents, Epoch time){
		long token = curToken++;

		try{
			file.write("<rel");
			writeTokenAndTime(token, time);
			file.write(">");
			for(RelationToken parent: parents){
				file.write("<p p=\"" + parent.parent.topologyNumber + ":" + parent.id +"\"/>");				
			}
			
			file.write("<rel/>");
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}				
		
		return new RelationToken(token, this);
	}
	
	public void destroyRelation(RelationToken relation, Epoch time){
		try{
			file.write("<un");
			writeTokenAndTime(relation.id, time);
			file.write("/>");			
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}				
		
		if(relation.startedStates != 0){
			throw new IllegalArgumentException("Error, open states in relation, not closed!");			
		}
	}
	
	
	public void startState(RelationToken relation, Epoch time, String name){
		startStateInternal(relation, time, name, null, null);
	}

	/**
	 * 
	 * @param relation
	 * @param time
	 * @param name
	 * @param childTags
	 * @param attrNameValues, vector with name, value pairs.
	 */
	public void startState(RelationToken relation, Epoch time,  String name, String childTags, String [] attrNameValues){
		if(attrNameValues == null){
			startStateInternal(relation, time, name, childTags, null);
			return;
		}
		
		if(attrNameValues.length % 2 != 0){
			throw new IllegalArgumentException("The attributes must be pairs of name and values!");
		}
		
		final StringBuffer buff = new StringBuffer();
		
		for(int i=0; i < attrNameValues.length; i+=2){
			buff.append(" " + attrNameValues[i] + "=\"" + attrNameValues[i+1] + "\"");
		}
		
		startStateInternal(relation, time, name, childTags, buff.toString());	
	}

	private void startStateInternal(RelationToken relation, Epoch time, String name, String childTags, String attributes){
		relation.startedStates++;
		
		try{
			file.write("<s name=\"" + name + "\"");
			writeTokenAndTime(relation.id, time);
			
			if(attributes != null){
				file.write(" " + attributes);
			}
			
			if(childTags != null){
				file.write(">" + childTags + "</s>");
			}else{
				file.write("/>");
			}			
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}				
	}
	
	public void endState(RelationToken relation, Epoch time){
		endState(relation, time, null, null, true);
	}
	
	public void endState(RelationToken relation, Epoch time, String childTags, String [] attrNameValues){
		if(attrNameValues.length % 2 != 0){
			throw new IllegalArgumentException("The attributes must be pairs of name and values!");
		}
		
		final StringBuffer buff = new StringBuffer();
		
		for(int i=0; i < attrNameValues.length; i+=2){
			buff.append(attrNameValues[i] + "=\"" + attrNameValues[i+1] + "\" ");
		}
		
		endState(relation, time, childTags, buff.toString(), true);	
	}
	 
	private void endState(RelationToken relation, Epoch time, String childTags, String attributes, boolean f){
		try{
			file.write("<e");
			writeTokenAndTime(relation.id, time);
			
			if(attributes != null){
				file.write(" " + attributes);
			}
			
			if(childTags != null){
				file.write(">" + childTags + "</e>");
			}else{
				file.write("/>");
			}
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}				
		
		relation.startedStates--;
		
		if(relation.startedStates < 0){
			throw new IllegalArgumentException("Error, more relation states ended, than started!");
		}
	}



	public void finalize(){
		try {
			file.write("</relation>\n");
			file.close();
			
			if(curToken == 0){
				// nothing done, delete the file
				(new File(filename)).delete();
			}
		} catch (IOException e) { 		
			throw new IllegalArgumentException(e);
		}
	}
}
