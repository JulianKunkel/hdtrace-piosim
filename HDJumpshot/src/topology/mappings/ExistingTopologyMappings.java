package topology.mappings;


public enum ExistingTopologyMappings {
	TopologyDefault(new DefaultTopologyTreeMapping()),
	TopologyRanksFirst(new RankTopologyMapping())	
	; 	
	
	private final TopologyTreeMapping mapping;
	
	private ExistingTopologyMappings(TopologyTreeMapping mapping) {
		this.mapping = mapping;
	}
	
	public TopologyTreeMapping getInstance() {
		return mapping;		
	}
}
