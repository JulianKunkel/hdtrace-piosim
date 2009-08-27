
 /** Version Control Information $Id: CategoryStatistic.java 325 2009-06-01 15:42:47Z kunkel $
  * @lastmodified    $Date: 2009-06-01 17:42:47 +0200 (Mo, 01. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 325 $ 
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


package de.drawable;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.viewer.legends.IPopupType;

public class CategoryStatistic extends Category {		
	public static enum Scaling implements IPopupType{
		DECIMAL('D'),
		LOGARITHMIC('L');
		
		final private char abreviationLetter;
		
		Scaling(char abreviationLetter){
			this.abreviationLetter = abreviationLetter;
		}

		@Override
		public char getAbbreviationChar() {
			return abreviationLetter;
		}
	}
	
	public static enum MaxAdjustment implements IPopupType{
		GLOBAL_MAX('G'),
		GLOBAL_GROUP_MAX('g'),
		ONE('1'),
		TIMELINE_MAX('L');
		
		final private char abreviationLetter;
		
		MaxAdjustment(char abreviationLetter){
			this.abreviationLetter = abreviationLetter;
		}

		@Override
		public char getAbbreviationChar() {
			return abreviationLetter;
		}
	}		
	
	public static enum MinAdjustment implements IPopupType{
		ZERO('-'),
		GLOBAL_MIN('G'),
		GLOBAL_GROUP_MIN('g'),
		TIMELINE_MIN('L');
		
		final private char abreviationLetter;
		
		MinAdjustment(char abreviationLetter){
			this.abreviationLetter = abreviationLetter;
		}

		@Override
		public char getAbbreviationChar() {
			return abreviationLetter;
		}
	}		
	
	private Scaling scaling = Scaling.DECIMAL;
	
	private MinAdjustment minAdjustment = MinAdjustment.ZERO;
	
	private MaxAdjustment maxAdjustment = MaxAdjustment.GLOBAL_GROUP_MAX;
	
	private boolean showAverageLine = false;
	
	final private StatisticsDescription statisticDescription;
	
	public CategoryStatistic( StatisticsDescription statistic, ColorAlpha in_color ) {
		super(statistic.getGroup().getName() + ":" + statistic.getName(), in_color);
		
		this.statisticDescription = statistic;
	}	

	public StatisticsDescription getStatisticDescription() {
		return statisticDescription;
	}
	
	public MaxAdjustment getMaxAdjustment() {
		return maxAdjustment;
	}
	
	public MinAdjustment getMinAdjustment() {
		return minAdjustment;
	}
	
	public void setMaxAdjustment(MaxAdjustment maxAdjustment) {
		this.maxAdjustment = maxAdjustment;
	}
	
	public void setMinAdjustment(MinAdjustment minAdjustment) {
		this.minAdjustment = minAdjustment;
	}
	
	public Scaling getScaling() {
		return scaling;
	}
	
	public void setScaling(Scaling scaling) {
		this.scaling = scaling;
	}
	
	@Override
	public boolean isSearchable() {
		return false;
	}
	
	@Override
	public void setSearchable(boolean new_value) {
	
	}
	
	@Override
	public VisualizedObjectType getTopologyType() {
		return VisualizedObjectType.STATISTIC;
	}
	
	public boolean isShowAverageLine() {
		return showAverageLine;
	}
	
	public void setShowAverageLine(boolean showAverageLine) {
		this.showAverageLine = showAverageLine;
	}
}
