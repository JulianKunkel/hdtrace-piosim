
/** Version Control Information $Id: TimelineTraceObjectInfoPanel.java 418 2009-06-18 13:43:00Z kunkel $
 * @lastmodified    $Date: 2009-06-18 15:43:00 +0200 (Do, 18. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 418 $ 
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

package de.viewer.timelines;


import java.awt.Color;
import java.math.BigDecimal;

import javax.swing.JPanel;

import de.drawable.Category;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.IBufferedStatisticsReader;
import de.hdTraceInput.StatisticStatistics;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyStatisticTreeNode;
import de.viewer.common.Const;
import de.viewer.common.LabeledTextField;
import de.viewer.common.ModelInfoPanel;


public class TimelineTraceObjectInfoPanel extends ModelInfoPanel<TraceObjectInformation>
{

	// general purpose fields
	private LabeledTextField  fld_time_start;
	private LabeledTextField  fld_time_end;
	private LabeledTextField  fld_time_duration;
	private LabeledTextField  fld_category_name;

	// for the statistics:
	private LabeledTextField  fld_stat_sum;
	private LabeledTextField  fld_stat_average;

	// integrated means integrated over the time:
	private LabeledTextField  fld_stat_integrated_sum;
	private LabeledTextField  fld_stat_integrated_avg;

	private LabeledTextField  fld_operation_type;
	private LabeledTextField  fld_callID;
	private LabeledTextField  fld_jobID;
	private LabeledTextField  fld_value;

	/**
	 * contains the categories which could be mapped to PVFS2 operation types directly
	 */
	static final String decodeOperationType = "Request decode";

	final private TraceFormatBufferedFileReader reader;

	@Override
	public void showInfo(TraceObjectInformation infoObj) {
		if(infoObj == null)
			return;

		final ITracableObject obj = infoObj.getObject();
		
		assert(obj != null);
		
		final TopologyNode node =  infoObj.getTopologyTreeNode().getTopology();

		switch(obj.getType()){
		case EVENT:
			showInfo((IEventTraceEntry) obj, node);
			return;
		case STATE:
			showInfo((IStateTraceEntry) obj, node);
			return;
		case STATISTICENTRY:
			showInfo((StatisticsEntry) obj, node, 
					((TopologyStatisticTreeNode) infoObj.getTopologyTreeNode()).getStatisticSource());
			return;
		case RELATION:
			showInfo((RelationEntry) obj, infoObj.getTime(), node);
			return;
		default:
			throw new IllegalArgumentException("Unexpected object of trace type: " + obj.getType());
		}
	}
	
	public void showInfo( RelationEntry entry,
			Epoch time,
			TopologyNode topology) 
	{
		
		// decide which contained element got selected.		
		for(IStateTraceEntry state: entry.getStates()){
			if(state.getEarliestTime().compareTo(time) <= 0){
				if(state.getLatestTime().compareTo(time) >= 0){
					// we are inside the state
					showInfo(state, topology);
					return;
				}
			}else{
				break;
			}
		}
		
		// if none got selected directly, then choose relation
		setVisibleControls(TracableObjectType.RELATION);

		this.setCategoryName("Relation");
		this.setStartTime(reader.subtractGlobalMinTimeOffset(entry.getEarliestTime()));
		this.setEndTime( reader.subtractGlobalMinTimeOffset(entry.getLatestTime()));
		this.setDuration( entry.getDurationTime() );
	}
	
	public void showInfo( StatisticsEntry statistic, 
			TopologyNode topology, 
			IBufferedStatisticsReader sReader) {

		setVisibleControls(TracableObjectType.STATISTICENTRY);		

		final StatisticsGroupEntry groupEntry = statistic.getParentGroupEntry();
		final StatisticsDescription desc = statistic.getDescription();
		final Category cat = reader.getCategory(desc); 

		this.setStartTime(  reader.subtractGlobalMinTimeOffset(groupEntry.getEarliestTime()));
		this.setEndTime( reader.subtractGlobalMinTimeOffset(groupEntry.getLatestTime()));

		this.setCategoryName( desc.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
		
		final String descUnit;

		if(desc.getUnit() != null){
			descUnit = " " + statistic.getDescription().getUnit();
		}else{
			descUnit = "";
		}
		
		Number number = statistic.getNumericValue();
		
		if(number.getClass() == Integer.class){
			fld_value.setInteger((Integer) number);	
		}else if(number.getClass() == Long.class){
			fld_value.setLong((Long) number);	
		}else{
			fld_value.setDouble(number.doubleValue());	
		}

		
		fld_value.addText(descUnit);
		
		final StatisticStatistics stat = sReader.getStatisticsFor(desc.getNumberInGroup());

		fld_stat_average.setDouble(stat.getAverageValue());
		fld_stat_average.addText(descUnit);
		
		fld_stat_sum.setDouble(stat.getSum().doubleValue());		
		fld_stat_integrated_sum.setDouble(stat.getIntegratedSum().doubleValue());
		
		fld_stat_integrated_avg.setDouble(stat.getIntegratedSum().divide( 
			sReader.getMaxTime().subtract(sReader.getMinTime()).getBigDecimal(), BigDecimal.ROUND_HALF_UP	
			).doubleValue());
		fld_stat_integrated_avg.addText(descUnit);
	}


	public void showInfo( IStateTraceEntry state, TopologyNode topology ) {
		setVisibleControls(TracableObjectType.STATE);

		this.setStartTime(  reader.subtractGlobalMinTimeOffset(state.getEarliestTime())  );
		this.setEndTime( reader.subtractGlobalMinTimeOffset(state.getLatestTime()) );

		this.setDuration(  state.getDurationTime() );
		final Category cat = reader.getCategory(state); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}

	public void showInfo( IEventTraceEntry event, TopologyNode topology) {
		setVisibleControls(TracableObjectType.EVENT);

		final Epoch time = reader.subtractGlobalMinTimeOffset(event.getEarliestTime());

		this.setStartTime( time  );
		this.setEndTime( time );

		final Category cat = reader.getCategory(event); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}



	@Override
	protected void addControlsToPanel(JPanel panel) {
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

		fld_value = new LabeledTextField( "Value", Const.PANEL_TIME_FORMAT);
		fld_value.setEditable( false );

		// special fields for statistics:
		fld_stat_integrated_sum = new LabeledTextField( "Integrated Sum", Const.FLOAT_FORMAT );
		fld_stat_integrated_sum.setEditable( false );
		fld_stat_integrated_sum.setToolTipText("Show the sum of the statistic entries multiplied by their length");
		fld_stat_integrated_sum.setBackground(Color.LIGHT_GRAY);
		
		fld_stat_integrated_avg = new LabeledTextField( "Integrated Avg", Const.FLOAT_FORMAT );
		fld_stat_integrated_avg.setEditable( false );
		fld_stat_integrated_avg.setToolTipText("Show the sum of the statistic entries multiplied by their " +
				"length divided by the total time duration");
		fld_stat_integrated_avg.setToolTipText("Show the numeric average of the statistic values");
		fld_stat_integrated_avg.setBackground(Color.LIGHT_GRAY);
		
		fld_stat_sum = new LabeledTextField( "Sum", Const.FLOAT_FORMAT );
		fld_stat_sum.setEditable( false );
		fld_stat_sum.setBackground(Color.LIGHT_GRAY);
				

		fld_stat_average = new LabeledTextField( "Average", Const.FLOAT_FORMAT );
		fld_stat_average.setEditable( false );
		fld_stat_average.setBackground(Color.LIGHT_GRAY);
		
				
		
		//

		panel.add( fld_category_name );

		panel.add( fld_stat_sum );		
		panel.add( fld_stat_average );
		panel.add( fld_stat_integrated_sum);
		panel.add( fld_stat_integrated_avg);

		panel.add( fld_time_start );
		panel.add( fld_time_end );
		panel.add( fld_time_duration );
		panel.add( fld_callID );
		panel.add( fld_operation_type );
		panel.add( fld_jobID );		
		panel.add( fld_value );
		
		setVisibleControls(TracableObjectType.EVENT);
	}

	public TimelineTraceObjectInfoPanel( TraceFormatBufferedFileReader reader )
	{
		this.reader = reader;
	}

	/**
	 * Reset visible fields and 
	 * load the visible fields (and further controls if necessary) for a given object type.
	 * 
	 * @param forType
	 */
	private void setVisibleControls(TracableObjectType forType) {
		fld_time_start.setVisible(false);
		fld_time_duration.setVisible(false);
		fld_stat_average.setVisible(false);
		fld_stat_sum.setVisible(false);
		fld_stat_integrated_sum.setVisible(false);
		fld_stat_integrated_avg.setVisible(false);
		fld_callID.setVisible(false);
		fld_jobID.setVisible(false);
		fld_operation_type.setVisible(false);
		fld_value.setVisible(false);

		switch(forType){
		case STATE:
			fld_time_start.setVisible(true);
			fld_time_duration.setVisible(true);
			break;
		case EVENT:
			break;
		case STATISTICENTRY:
			fld_time_start.setVisible(true);
			fld_value.setVisible(true);
			fld_stat_integrated_sum.setVisible(true);
			fld_stat_integrated_avg.setVisible(true);
			fld_stat_average.setVisible(true);
			fld_stat_sum.setVisible(true);		
		case RELATION:
			fld_time_start.setVisible(true);
			fld_time_duration.setVisible(true);
		}

		fld_category_name.setText("");
		fld_category_name.setBackground( Color.black );
	}

	private void setStartTime(final Epoch starttime)
	{
		fld_time_start.setDouble(starttime.getDouble());
	}

	private void setEndTime(final Epoch endtime)
	{
		fld_time_end.setDouble(endtime.getDouble());
	}

	private void setDuration(final Epoch duration)
	{
		fld_time_duration.setDouble(duration.getDouble());
	}

	private void setCategoryColor(final Color color)
	{
		fld_category_name.setBackground( color );
	}

	private void setCategoryName(final String type)
	{
		fld_category_name.setText(type);
	}
}
