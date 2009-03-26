package drawable;

public class CategoryEvent extends Category {

    private boolean         isSearchable = true;
    
	public CategoryEvent(String in_name, ColorAlpha in_color ) {
		super(in_name, in_color);
	}

    public void setSearchable( boolean new_value )
    {
        isSearchable = new_value;
    }

    public boolean isSearchable()
    {
        return isSearchable;
    }
    
    @Override
    public TopologyType getTopologyType() {
    	return TopologyType.EVENT;
    }
}
