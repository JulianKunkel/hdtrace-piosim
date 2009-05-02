/**
 * 
 */
package viewer.dialog.traceEntries;

import java.awt.Color;
import java.util.LinkedList;

public class InfoTableData{
	static class InfoTableListData{
		final String key;
		final Object value;
		final Color color;

		public InfoTableListData(String key, Object value, Color color) {
			this.key = key;
			this.value = value;
			this.color = color;
		}
	}
	
	final LinkedList<InfoTableListData> tableData = new LinkedList<InfoTableListData>();

	public void addSection(String name, Color color){
		tableData.add(new InfoTableListData(name, "", color));
	}
	
	public void addData(String key, Object value){
		tableData.add(new InfoTableListData("  " + key, value, null));
	}

	public void addNestedData(String key, Object value){
		addData(" +" + key, value);
	}	
	
	public LinkedList<InfoTableListData> getTableData() {
		return tableData;
	}
}