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

package viewer.profile;

import java.awt.Color;

import javax.swing.JPanel;

import viewer.common.Const;
import viewer.common.LabeledTextField;
import viewer.common.ModelInfoPanel;
import drawable.Category;

public class TraceProfileInfoPanel extends ModelInfoPanel<TraceCategoryStateProfile> {
	
	final private LabeledTextField  fld_category_name = new LabeledTextField( " ", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_metricPercent = new LabeledTextField( "Metric [%]", Const.FLOAT_FORMAT );
				
	final private LabeledTextField  fld_numberOfCalls = new LabeledTextField( "# calls", Const.INTEGER_FORMAT );	

	final private LabeledTextField  fld_exclusiveTime = new LabeledTextField( "Excl. [t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_exclTimePercent = new LabeledTextField( "Excl. Time [%]", Const.FLOAT_FORMAT );

	final private LabeledTextField  fld_inclusiveTime = new LabeledTextField( "Incl. [t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_inclTimePercent = new LabeledTextField( "Incl. Time [%]", Const.FLOAT_FORMAT );

	final private LabeledTextField  fld_maxExclusiveTime  = new LabeledTextField( "Max Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_minExclusiveTime = new LabeledTextField( "Min Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_maxInclusiveTime = new LabeledTextField( "Max Incl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_minInclusiveTime = new LabeledTextField( "Min Incl[t]", Const.PANEL_TIME_FORMAT );
	
	final private LabeledTextField  fld_avgExclusiveTime = new LabeledTextField( "Avg. Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_avgInclusiveTime = new LabeledTextField( "Avg. Incl[t]", Const.PANEL_TIME_FORMAT );
	
	@Override
	protected void addControlsToPanel(JPanel panel) {
		fld_category_name.setBackground( Color.black );

		addTextField(fld_category_name, "The category name the profile information belongs to");

		addTextField(fld_numberOfCalls, "Number of times a TraceObject of the category is visible");
		addTextField(fld_metricPercent, "Percent of the profile line the current TraceObject occupies with the selected Metric");
		
		addTextField(fld_exclusiveTime, "Sum of the exclusive time for all visible TraceObjects of this category");
		addTextField(fld_exclTimePercent, "(Sum exclusive time) / (Extend of time)");
		addTextField(fld_minExclusiveTime, "Minimum time needed for a single state of this category");
		addTextField(fld_maxExclusiveTime, "Maximum time needed for a single state of this category");
		addTextField(fld_avgExclusiveTime, "Average time needed for a single state of this category");
		
		addTextField(fld_inclusiveTime, "Sum of the inclusive time for all visible TraceObjects of this category");
		addTextField(fld_inclTimePercent, "(Sum inclusive time) / (Extend of time)");
		addTextField(fld_minInclusiveTime, "Minimum time needed for a single state of this category");
		addTextField(fld_maxInclusiveTime, "Maximum time needed for a single state of this category");
		addTextField(fld_avgInclusiveTime, "Average time needed for a single state of this category");
	}
	
	@Override
	public void showInfo(TraceCategoryStateProfile obj) {
		if(obj == null){
			return;
		}
		
		
		final Category cat = obj.getCategory();
		final TraceProfileFrame frame = obj.getProfileFrame();
		
		fld_category_name.setText(cat.getName() );    
		fld_category_name.setBackground( cat.getColor() );
		
		fld_metricPercent.setDouble( frame.getMetricHandler().getInterestingValue(obj) / frame.getMaxMetricValue() );
		
		fld_numberOfCalls.setInteger(obj.getNumberOfCalls());
		
		fld_inclusiveTime.setDouble(obj.getInclusiveTime());
		fld_exclusiveTime.setDouble(obj.getExclusiveTime());
		
		fld_inclTimePercent.setDouble(obj.getInclusiveTime() / frame.getRealModelTimeExtend());
		
		fld_exclTimePercent.setDouble(obj.getExclusiveTime() / frame.getRealModelTimeExtend());		

		
		fld_minExclusiveTime.setDouble(obj.getMinDurationExclusive());
		fld_minInclusiveTime.setDouble(obj.getMinDurationInclusive());
		fld_maxExclusiveTime.setDouble(obj.getMaxDurationExclusive());
		fld_maxInclusiveTime.setDouble(obj.getMaxDurationInclusive());
		
		fld_avgExclusiveTime.setDouble(obj.getExclusiveTime() / obj.getNumberOfCalls());
		fld_avgInclusiveTime.setDouble(obj.getInclusiveTime() / obj.getNumberOfCalls());
	}
}
