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