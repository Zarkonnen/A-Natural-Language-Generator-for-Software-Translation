import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;

/**
 * A panel for rendering MRs.
*/
public class MRPanel extends JPanel {
	private MeaningRepresentation myMR;
	private static final Color boxBGColor = new Color(150, 200, 255);
	private static final Color boxSelColor = new Color(150, 255, 200);
	private static final Color fieldBGColor = new Color(255, 200, 150);
	private static final Color fieldSelColor = new Color(200, 255, 150);
	private static final Color boxFGColor = Color.BLACK;
	
	public static final MeaningField ROOT_FIELD = new MeaningField();
	
	private ArrayList boxLocations;
	
	public MRNode currentNode;
	public MeaningField currentField;
	
	//The language to display everything in.
	private String displayLanguage;

	/**
	 * Constructor.
	*/
	public MRPanel(MeaningRepresentation mr) {
		myMR = mr;
		boxLocations = new ArrayList();
		displayLanguage = "en";
	}
	
	/**
	 * Handling Mouse Clicks
	*/
	public void handleMouseClick(int x, int y) {
		currentNode = null;
		currentField = null;
		Iterator boxIter = boxLocations.iterator();
		while (boxIter.hasNext()) {
			BoxLocation l = (BoxLocation) boxIter.next();
			if (l.clickedHere(x, y)) {
				currentNode = l.node;
				currentField = l.field;
				repaint(0, 0, getWidth(), getHeight());
				return;
			}
		}
		repaint(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Painting.
	*/
	protected void paintComponent(Graphics g) {
		//clear
		g.clearRect(0, 0, getWidth(), getHeight());
		boxLocations.clear();
		
		g.setFont(new Font("Helvetica", Font.PLAIN, 11));
		
		//drawing a node
		
		//1. find out how wide the node's children are.
		//2. draw the node at the appropriate place as determined by
			//x: children width / 2
			//y: recursion depth
		//3. find out who the immediate children are, and how wide they are
		//4. recursively tell them to draw themselves
		//5. draw connecting lines
		if (myMR.getRootNode() != null) {
			paintNode(null, myMR.getRootNode(), g, 0, 0);
		} else {
			paintRootNode(g);
		}
	}
	
	/**
	 * Paints the root node.
	*/
	private void paintRootNode(Graphics g) {
		int x = getWidth() / 2 - 40;
		int y = 5;
		
		if (currentField == ROOT_FIELD) {
			g.setColor(fieldSelColor);
		} else {
			g.setColor(fieldBGColor);
		}
		
		g.fillRect(x, y, 76, 15);
		g.setColor(boxFGColor);
		g.drawRect(x, y, 76, 15);
		
		g.drawString("click me!", x + 4, y + 12);
		
		boxLocations.add(new BoxLocation(null, ROOT_FIELD, x, y));
	}
	
	/**
	 * Paints an unfilled field.
	*/
	private void paintField(MeaningField f, MRNode n, Graphics g, int xOffset, int y) {
		//Draw the field.
		int x = xOffset + getWidth() / 2 - 40;
		
		if ((n == currentNode) && (f == currentField)) {
			g.setColor(fieldSelColor);
		} else {
			g.setColor(fieldBGColor);
		}
		
		g.fillRect(x, y, 76, 15);
		g.setColor(boxFGColor);
		g.drawRect(x, y, 76, 15);
		
		g.drawString(makeUTF8(f.getName(displayLanguage)) + ":", x + 4, y + 12);
		
		//Enter it into the location array
		boxLocations.add(new BoxLocation(n, f, x, y));
		
		//That's all. The field's not gonna have any children now is it.
	}
	
	
	/**
	 * Paints a node.
	*/
	private void paintNode(MeaningField f, MRNode n, Graphics g, int xOffset, int recursion) {
		//Draw the actual node.
		int x = xOffset + getWidth() / 2 - 40;
		int y = recursion * 50 + 5;
	
		if ((n == currentNode) && (currentField == null)) {
			g.setColor(boxSelColor);
		} else {
			g.setColor(boxBGColor);
		}
		g.fillRect(x, y, 76, 30);
		g.setColor(boxFGColor);
		g.drawRect(x, y, 76, 30);
		if (f != null) {
			g.drawString(makeUTF8(f.getName(displayLanguage)) + ":", x + 4, y + 12);
		}
		if (n.isLiteral()) {
			g.drawString("\"" + n.literalValue() + "\"", x + 4, y + 26);
		} else {
			g.drawString(makeUTF8(n.getType().getName(displayLanguage)), x + 4, y + 26);
		}
		
		//Enter it into the location array.
		boxLocations.add(new BoxLocation(n, null, x, y));
		
		if (n.isLiteral()) {
			//end now, no children to be drawn anyway
			return;
		}
		
		//Find out my width in units.
		int treeWidth = n.treeWidth(currentNode, displayLanguage);
		HashSet allFields = null;
		
		//Get a list of children - a HashSet of MeaningFields.
		HashSet childFields = n.getUsedFields();
		
		//If this one's selected, we need a list of all its potential children.
		if (n == currentNode) {
			VocabularyMeaning type = n.getType();
			allFields = type.allFieldsSet(displayLanguage);
			allFields.removeAll(childFields);
			if (allFields.size() == 0) {
				treeWidth--;
			}
		}
		
		//Iterate over the children and draw them.
		Iterator cFIter = childFields.iterator();
		int offsetAlreadyUsed = 0;
		while (cFIter.hasNext()) {
			MeaningField field = (MeaningField) cFIter.next();
			MRNode node = n.getChild(field);
			int nodeWidth = node.treeWidth(currentNode, displayLanguage);
			int newXOffset = xOffset - treeWidth * 40 + offsetAlreadyUsed * 80 + nodeWidth * 40;
			offsetAlreadyUsed += nodeWidth;
			g.drawLine(x + 38, y + 30, newXOffset + getWidth() / 2 - 2, y + 50);
			paintNode(field, node, g, newXOffset, recursion + 1);
		}
		
		//If necessary, iterate over the fields and draw them.
		if ((n == currentNode) && (allFields.size() > 0)) {
			//offsetAlreadyUsed++;
			int yShift = 0;
			int newXOffset = xOffset - treeWidth * 40 + offsetAlreadyUsed * 80 + 40;
			g.drawLine(x + 38, y + 30, newXOffset + getWidth() / 2 - 2, y + 50);
			//First, get the set of empty fields.
			cFIter = allFields.iterator();
			while (cFIter.hasNext()) {
				MeaningField field = (MeaningField) cFIter.next();
				paintField(field, n, g, newXOffset, y + 50 + yShift * 14);
				yShift++;
			}
		}
	}
	
	public String getDisplayLanguage() {
		return displayLanguage;
	}
	
	private String makeUTF8(String s) {
		try {
			return new String(s.getBytes(), "UTF-8");
		}
		catch (java.io.UnsupportedEncodingException e) {
			return s;
		}
	}
	
	public void setMR(MeaningRepresentation newMR) {
		myMR = newMR;
		currentNode = null;
		currentField = null;
	}	
}
