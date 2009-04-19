package arrow;

/**
 * A group serves a purpose for instance MPI individual communication or 
 * client/server I/O communication. 
 * 
 * @author julian
 */
public class ManagedArrowGroup{
	private ArrowsOrdered arrowsOrdered = null;
	private final ArrowCategory category;		
	
	private final ArrowComputer computer;
	private boolean wasComputed = false;
	
	public ManagedArrowGroup(ArrowComputer computer) {
		this.computer = computer;
		this.category = computer.getResponsibleCategory();
	}

	public boolean isComputed() {
		return wasComputed;
	}
	
	public ArrowCategory getCategory() {
		return category;
	}
	
	public ArrowsOrdered getArrowsOrdered() {
		return arrowsOrdered;
	}
	
	ArrowComputer getComputer() {
		return computer;
	}
	
	void setComputeResults(ArrowsOrdered arrowsOrdered){
		wasComputed = true;
		this.arrowsOrdered = arrowsOrdered;
	}
	
	void clearComputedState(){
		this.arrowsOrdered = null;
		wasComputed = false;
	}
}