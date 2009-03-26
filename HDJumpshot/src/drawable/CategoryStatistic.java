package drawable;

public class CategoryStatistic extends Category {
	public CategoryStatistic(String in_name, ColorAlpha in_color ) {
		super(in_name, in_color);
	}
	
	@Override
	public boolean isSearchable() {
		return false;
	}
	
	@Override
	public void setSearchable(boolean new_value) {
	
	}
	
	@Override
	public TopologyType getTopologyType() {
		return TopologyType.STATISTIC;
	}
}
