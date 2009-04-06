package viewer.profile;

import java.awt.Color;

import javax.swing.JPanel;

import viewer.common.Const;
import viewer.common.LabeledTextField;
import viewer.common.ModelInfoPanel;
import drawable.Category;

public class TraceProfileInfoPanel extends ModelInfoPanel<TraceCategoryStateProfile> {
	
	final private LabeledTextField  fld_category_name = new LabeledTextField( " ", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_numberOfCalls = new LabeledTextField( "# calls", Const.INTEGER_FORMAT );	
	final private LabeledTextField  fld_exclusiveTime = new LabeledTextField( "Excl. [t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_inclusiveTime = new LabeledTextField( "Incl. [t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_maxExclusiveTime  = new LabeledTextField( "Max Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_minExclusiveTime = new LabeledTextField( "Min Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_maxInclusiveTime = new LabeledTextField( "Max Incl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_minInclusiveTime = new LabeledTextField( "Min Incl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_avgExclusiveTime = new LabeledTextField( "Avg. Excl[t]", Const.PANEL_TIME_FORMAT );
	final private LabeledTextField  fld_avgInclusiveTime = new LabeledTextField( "Avg. Incl[t]", Const.PANEL_TIME_FORMAT );
	
	@Override
	protected void addControlsToPanel(JPanel panel) {
		fld_category_name.setEditable( false );
		fld_category_name.setBackground( Color.black );
		
		panel.add(fld_category_name);
		
		fld_numberOfCalls.setEditable( false );
		panel.add(fld_numberOfCalls);
		
		fld_exclusiveTime.setEditable( false );
		panel.add(fld_exclusiveTime);
		
		fld_inclusiveTime.setEditable( false );
		panel.add(fld_inclusiveTime);
		
		fld_minExclusiveTime.setEditable( false );
		panel.add(fld_minExclusiveTime);
		
		fld_maxExclusiveTime.setEditable( false );
		panel.add(fld_maxExclusiveTime);
		
		fld_minInclusiveTime.setEditable( false );
		panel.add(fld_minInclusiveTime);
		
		fld_maxInclusiveTime.setEditable( false );
		panel.add(fld_maxInclusiveTime);
		
		fld_avgInclusiveTime.setEditable( false );
		panel.add(fld_avgInclusiveTime);
		
		fld_avgInclusiveTime.setEditable( false );
		panel.add(fld_avgInclusiveTime);
	}
	
	@Override
	public void showInfo(TraceCategoryStateProfile obj) {
		if(obj == null){
			return;
		}
		
		
		final Category cat = obj.getCategory(); 
		fld_category_name.setText(cat.getName() );    
		fld_category_name.setBackground( cat.getColor() );
		
		fld_numberOfCalls.setInteger(obj.getNumberOfCalls());
		
		fld_inclusiveTime.setDouble(obj.getInclusiveTime());
		fld_exclusiveTime.setDouble(obj.getExclusiveTime());
		
		fld_minExclusiveTime.setDouble(obj.getMinDurationExclusive());
		fld_minInclusiveTime.setDouble(obj.getMinDurationInclusive());
		fld_maxExclusiveTime.setDouble(obj.getMaxDurationExclusive());
		fld_maxInclusiveTime.setDouble(obj.getMaxDurationInclusive());
		
		fld_avgExclusiveTime.setDouble(obj.getExclusiveTime() / obj.getNumberOfCalls());
		fld_avgInclusiveTime.setDouble(obj.getInclusiveTime() / obj.getNumberOfCalls());
	}
}
