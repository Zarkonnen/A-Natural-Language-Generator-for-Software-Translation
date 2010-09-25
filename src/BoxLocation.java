/**
 * Defines the location of a box in the tree diagram.
*/

public class BoxLocation {

	public MRNode node;
	public MeaningField field;
	public int x;
	public int y;
	
	public BoxLocation(MRNode node, MeaningField field, int x, int y) {
		this.node = node;
		this.field = field;
		this.x = x;
		this.y = y;
	}
	
	public boolean clickedHere(int clickX, int clickY) {
		if (field == null) {
			return (
				(clickX >= x) &&
				(clickX <= x + 76) &&
				(clickY >= y) &&
				(clickY <= y + 30) 
				);
		} else {
			return (
				(clickX >= x) &&
				(clickX <= x + 76) &&
				(clickY >= y) &&
				(clickY <= y + 15) 
				);
		}
	}

}
