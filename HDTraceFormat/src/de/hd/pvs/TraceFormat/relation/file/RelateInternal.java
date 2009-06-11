package de.hd.pvs.TraceFormat.relation.file;


public class RelateInternal extends RelationFileEntry{
	private final long relatedToken;

	public RelateInternal(String token, String relatedToken) {
		super(token);
		this.relatedToken = Long.parseLong(relatedToken);
	}

	public long getRelatedToken() {
		return relatedToken;
	}	

	@Override
	public Type getType() {
		return Type.RELATE_INTERNAL;
	}

	/**
	 * Create the token str.
	 * @return
	 */
	public String getFullTokenID(){
		return Long.toString(relatedToken);
	}
}
