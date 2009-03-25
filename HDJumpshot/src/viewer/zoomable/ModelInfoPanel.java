/*
 * ModelInfoPanel
 */

/*
 *  @author  Frank Panse
 *  @organization PVS University Heidelberg
 */

package viewer.zoomable;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import viewer.common.Const;
import viewer.common.LabeledTextField;


public class ModelInfoPanel extends JPanel
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


	/**
	 * contains the categories which could be mapped to PVFS2 operation types directly
	 */
	static final String decodeOperationType = "Request decode";

	private List              vport_list;

	public ModelInfoPanel( ModelInfo model )
	{
		super();
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

		vport_list         = new ArrayList();

		fld_category_name    = new LabeledTextField( " ", Const.PANEL_TIME_FORMAT );
		fld_category_name.setEditable( false );
		fld_category_name.setBackground( Color.black );
		add( fld_category_name );


		fld_time_start    = new LabeledTextField( "Start time", Const.PANEL_TIME_FORMAT );
		fld_time_start.setEditable( false );
		add( fld_time_start );

		fld_time_end    = new LabeledTextField( "End time", Const.PANEL_TIME_FORMAT );
		fld_time_end.setEditable( false );
		add( fld_time_end );


		fld_time_duration     = new LabeledTextField( "Duration", Const.PANEL_TIME_FORMAT );
		fld_time_duration.setEditable( false );
		add( fld_time_duration );

		fld_callID = new LabeledTextField( "CallID", Const.PANEL_TIME_FORMAT );
		fld_callID.setEditable( false );
		add( fld_callID );

		fld_operation_type = new LabeledTextField( "Operation Type", Const.PANEL_TIME_FORMAT );
		fld_operation_type.setEditable( false );
		add( fld_operation_type );

		fld_jobID = new LabeledTextField( "Job ID", Const.PANEL_TIME_FORMAT );
		fld_jobID.setEditable( false );
		add( fld_jobID );

		fld_value = new LabeledTextField( "Value", Const.PANEL_TIME_FORMAT );
		fld_value.setEditable( false );
		add( fld_value );


		super.setBorder( BorderFactory.createEtchedBorder() );
	}


	public void reset() {
		fld_time_start.setText("");
		fld_time_end.setText("");
		fld_time_duration.setText("");
		fld_category_name.setText("");
		fld_category_name.setBackground( Color.black );
		setInfoString("");
	}

	public void setStartTime(final String starttime)
	{
		fld_time_start.setText(starttime);
	}

	public void setEndTime(final String endtime)
	{
		fld_time_end.setText(endtime);
	}

	public void setDuration(final String duration)
	{
		fld_time_duration.setText(duration);
	}

	public void setCategoryColor(final Color color)
	{
		fld_category_name.setBackground( color );
	}

	public void setCategoryName(final String type)
	{
		fld_category_name.setText(type);
	}

	public void setInfoString(final String info)
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


	public void addViewportTime( final ViewportTime  vport )
	{
		if ( vport != null )
			vport_list.add( vport );
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
