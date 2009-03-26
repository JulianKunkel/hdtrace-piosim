package drawable;

public class CategoryState extends CategoryEvent{
	public CategoryState(String in_name, ColorAlpha in_color ) {
		super(in_name, in_color);
	}
	
	@Override
	public TopologyType getTopologyType() {
		return TopologyType.STATE;
	}
}
