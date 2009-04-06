
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 * ModelInfoPanel
 */

/*
 *  @author  Frank Panse
 *  @organization PVS University Heidelberg
 */

package viewer.timelines;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import viewer.common.Const;
import viewer.common.LabeledTextField;
import viewer.common.ModelInfoPanel;
import viewer.common.TimeFormat;
import viewer.pvfs2.PVFS2OpTypeParser;
import viewer.pvfs2.PVFS2SMParser;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import drawable.Category;


public class TimelineTraceObjectInfoPanel extends ModelInfoPanel<TraceObject>
{
	
	private LabeledTextField  fld_time_start;
	private LabeledTextField  fld_time_end;
	private LabeledTextField  fld_time_duration;
	private LabeledTextField  fld_category_name;
	private LabeledTextField  fld_operation_type;
	private LabeledTextField  fld_callID;
	private LabeledTextField  fld_jobID;
	private LabeledTextField  fld_value;

	static private final String PVFS2_sm_state = "SM-State";
	static private final String PVFS2_migration = "Migration";
	static private final String PVFS2_sm_field = "sm";
	static private final String PVFS2_sm_state_field = "st";
	static private final String PVFS2_operation = "op";
	static private final String PVFS2_callid = "cid";
	static private final String PVFS2_callid_rank = "rank";
	static private final String PVFS2_jobID = "jid";
	static private final String PVFS2_value = "vl";

	static private final String PVFS2_PC = "value";
	static private final String PVFS2_PERCENT = "percent";
	static private final String PROP_VALUE_MULTIPLIER = "multiplier";
	static private final String PROP_VALUE_PREFIX = "prefix";


	private static final String         FORMAT = Const.INFOBOX_TIME_FORMAT;
	private static       DecimalFormat  fmt    = null;
	private static       TimeFormat     tfmt   = null;

	/**
	 * contains the categories which could be mapped to PVFS2 operation types directly
	 */
	static final String decodeOperationType = "Request decode";
	
	final private TraceFormatBufferedFileReader reader;

	@Override
	public void showInfo(TraceObject obj) {
		if(obj == null)
			return;
		
		switch(obj.getType()){
		case EVENT:
			showInfo((EventTraceEntry) obj);
			return;
		case STATE:
			showInfo((StateTraceEntry) obj);
			return;
		case STATISTICENTRY:
			showInfo((StatisticEntry) obj);
			return;
		default:
			throw new IllegalArgumentException("Unexpected object of trace type: " + obj.getType());
		}
	}
	
	public void showInfo( StatisticEntry statistic ) {
		reset();		
		
		StatisticGroupEntry groupEntry = statistic.getParentGroupEntry();
		StatisticDescription desc = statistic.getDescription();
		final double time = reader.subtractGlobalMinTimeOffset(groupEntry.getEarliestTime());
		
		this.setStartTime( "" );
		this.setEndTime( "" + time);

		
		this.setDuration( fmt.format( 0 ) );
		final Category cat = reader.getCategory(groupEntry.getGroup(), desc.getName()); 
		this.setCategoryName( desc.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
		
		if(desc.getUnit() != null)
			this.setInfoString(statistic.getValue().toString() + " " + desc.getUnit());
		else
			this.setInfoString(statistic.getValue().toString());
	}


	public void showInfo( StateTraceEntry state ) {
		reset();
		
		this.setStartTime( "" + reader.subtractGlobalMinTimeOffset(state.getEarliestTime())  );
		this.setEndTime( "" + reader.subtractGlobalMinTimeOffset(state.getLatestTime()) );

		this.setDuration( fmt.format( state.getDurationTimeDouble() ) );
		final Category cat = reader.getCategory(state); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}

	public void showInfo( EventTraceEntry event ) {
		reset();
		
		final double time = reader.subtractGlobalMinTimeOffset(event.getEarliestTime());
		
		this.setStartTime( "" + time  );
		this.setEndTime( "" + time );

		this.setDuration( fmt.format( 0 ) );
		final Category cat = reader.getCategory(event); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}

	
	
	@Override
	protected void addControlsToPanel(JPanel panel) {
		if ( fmt == null ) {
			fmt = (DecimalFormat) NumberFormat.getInstance();
			fmt.applyPattern( FORMAT );
		}
		if ( tfmt == null )
			tfmt = new TimeFormat();
		
		fld_category_name    = new LabeledTextField( " ", Const.PANEL_TIME_FORMAT );
		fld_category_name.setEditable( false );
		fld_category_name.setBackground( Color.black );

		fld_time_start    = new LabeledTextField( "Start time", Const.PANEL_TIME_FORMAT );
		fld_time_start.setEditable( false );

		fld_time_end    = new LabeledTextField( "End time", Const.PANEL_TIME_FORMAT );
		fld_time_end.setEditable( false );

		fld_time_duration     = new LabeledTextField( "Duration", Const.PANEL_TIME_FORMAT );
		fld_time_duration.setEditable( false );

		fld_callID = new LabeledTextField( "CallID", Const.PANEL_TIME_FORMAT );
		fld_callID.setEditable( false );

		fld_operation_type = new LabeledTextField( "Operation Type", Const.PANEL_TIME_FORMAT );
		fld_operation_type.setEditable( false );

		fld_jobID = new LabeledTextField( "Job ID", Const.PANEL_TIME_FORMAT );
		fld_jobID.setEditable( false );

		fld_value = new LabeledTextField( "Value", Const.PANEL_TIME_FORMAT );
		fld_value.setEditable( false );
		
		panel.add( fld_category_name );
		panel.add( fld_time_start );
		panel.add( fld_time_end );
		panel.add( fld_time_duration );
		panel.add( fld_callID );
		panel.add( fld_operation_type );
		panel.add( fld_jobID );		
		panel.add( fld_value );

	}
	
	public TimelineTraceObjectInfoPanel( TraceFormatBufferedFileReader reader )
	{
		this.reader = reader;
	}


	private void reset() {
		fld_time_start.setText("");
		fld_time_end.setText("");
		fld_time_duration.setText("");
		fld_category_name.setText("");
		fld_category_name.setBackground( Color.black );
		setInfoString("");
	}

	private void setStartTime(final String starttime)
	{
		fld_time_start.setText(starttime);
	}

	private void setEndTime(final String endtime)
	{
		fld_time_end.setText(endtime);
	}

	private void setDuration(final String duration)
	{
		fld_time_duration.setText(duration);
	}

	private void setCategoryColor(final Color color)
	{
		fld_category_name.setBackground( color );
	}

	private void setCategoryName(final String type)
	{
		fld_category_name.setText(type);
	}

	private void setInfoString(final String info)
	{
		if(info != null){
			fld_value.setText(info);
			return;
		}
		
		fld_callID.setLabel("CallID");
		fld_operation_type.setLabel("Operation type");
		fld_value.setLabel("Value");

		fld_operation_type.setText("");
		fld_callID.setText("");
		fld_jobID.setText("");
		fld_value.setText("");
		fld_callID.setVisible(true);
		fld_jobID.setVisible(true);
		String param = null;

		String category = fld_category_name.getText();
		String category_plain = category.replaceAll("[ :\n\t]", "");

		/* figure out if this is object is a special PVFS2 object */
		if( category.equals(decodeOperationType) ){
			param =  getParam(info, PVFS2_operation);
			if(param != null){
				Integer op = Integer.parseInt(param);
				String type = PVFS2OpTypeParser.getInstance().getOperationType( op );
				if( type != null){
					fld_operation_type.setText(type);
				}else{
					System.err.println("PVFS Type " + type + " is unknown !");
				}
			}
		}else{
			param =  getParam(info, PVFS2_operation);
			if(param != null){
				Integer op = Integer.parseInt(param);
				String type = PVFS2OpTypeParser.getInstance().getEventType( op );
				if( type != null){
					fld_operation_type.setText(type);
				}else{
					System.err.println("PVFS Event Type " + type + " is unknown !");
				}
			}
		}

		param = getParam(info, PVFS2_PC);
		if ( param != null && param.length() > 0 ){
			String percent = getFloatParam(info, PVFS2_PERCENT);
			String value = param;
			Double dbl_val = 0.0;
			String propMultiplier = getPVFS2_PC_modifier(category_plain, PROP_VALUE_MULTIPLIER);
			String propPrefix = getPVFS2_PC_modifier(category_plain, PROP_VALUE_PREFIX);

			if ( propMultiplier != null ){
				dbl_val = Double.parseDouble(value) * Double.parseDouble(propMultiplier);
				value = "" + dbl_val;
				if( value.length() > 10){
					value = value.substring(0, 10);
				}
			}else{
				dbl_val = Double.parseDouble(value);
			}

			if( propPrefix == null){
				propPrefix = "";
			}else{
				propPrefix = " " + propPrefix;
			}

			fld_value.setText(value + propPrefix);

			fld_operation_type.setLabel("Maximum value");
			if (dbl_val != 0.0){
				fld_operation_type.setText("" + dbl_val / Float.valueOf(percent));
			}else{
				fld_operation_type.setText("NAN");
			}

			fld_callID.setVisible(false);
			fld_jobID.setVisible(false);
			return;
		}

		if (category.startsWith(PVFS2_migration)){
			String src_handle,target_handle, parent_handle;
			/* display special migration informations */
			src_handle = getParam(info, "src");
			target_handle = getParam(info, "tgt");
			parent_handle = getParam(info, "parent");

			fld_jobID.setVisible(false);

			fld_callID.setLabel("Src handle");
			fld_callID.setText(src_handle);
			fld_operation_type.setLabel("Tgt handle");
			fld_operation_type.setText(target_handle);
			fld_value.setLabel("Parent handle");
			fld_value.setText(parent_handle);
			return;
		}

		if (category.equals(PVFS2_sm_state)){
			/* display special SM informations */
			Integer sm = Integer.parseInt( getParam(info, PVFS2_sm_field));
			Integer state = Integer.parseInt( getParam(info, PVFS2_sm_state_field));
			String sm_name = PVFS2SMParser.getInstance().getStateMaschineName(sm);
			String state_name = PVFS2SMParser.getInstance().getStateName(
					sm, state);

			fld_callID.setVisible(false);
			fld_jobID.setVisible(false);
			fld_operation_type.setLabel("SM name");
			fld_operation_type.setText(sm_name);
			fld_value.setText(state_name);
			return;
		}

		param =  getParam(info, PVFS2_callid);
		if(param != null && param.length() > 0){
			String rank =  getParam(info, PVFS2_callid_rank);
			fld_callID.setText("rank:" + rank + ":" + param);
		}
		param =  getParam(info, PVFS2_jobID);
		if(param != null){
			fld_jobID.setText(param);
		}
		param =  getParam(info, PVFS2_value);
		if(param != null){
			fld_value.setText(param);
		}
	}

	static private Properties pvfs2_pc_modifier = new Properties();
	static {
		try{
			pvfs2_pc_modifier.load(new FileInputStream("jumpshot-modelInfoPanel.property"));
		}catch(IOException e){
			System.err.println("Error during accessing jumpshot-modelInfoPanel.property: " + e.getMessage());
		}
	}

	private String getPVFS2_PC_modifier(String category, String what){
		return pvfs2_pc_modifier.getProperty(category + "_" + what);
	}


	private String getParam(String paramstring, String param) {
		Pattern pattern =  Pattern.compile(param + "=(-?[0-9]*)", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = pattern.matcher( paramstring );
		if(m.find()){
			return m.group(1);
		}
		return null;
	}

	private String getFloatParam(String paramstring, String param) {
		Pattern pattern =  Pattern.compile(param + "=(-?[0-9]*[,.][0-9]*)", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = pattern.matcher( paramstring );
		if(m.find()){
			return m.group(1);
		}
		return null;
	}
}
