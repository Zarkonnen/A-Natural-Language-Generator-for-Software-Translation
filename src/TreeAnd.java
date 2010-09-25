/**
 * Represents an AND operator for use in a LogicNode tree.
*/

public class TreeAnd implements LogicNode {

private LogicNode contentA;
private LogicNode contentB;

/**
 * The Constructor. Requires one LogicNode.
 *
 * @param node one of two LogicNodes to be ANDed
*/
public TreeAnd(LogicNode node) {
	contentA = node;
}

/**
 * Used to set the other LogicNode to be ANDed.
 * The first one is set by the constructor.
 *
 * @param node the other LogicNode to be ANDed
*/
public void fill(LogicNode node) {
	contentB = node;
}

/**
 * Pretty-prints the AND in a way that can be parsed back in.
 *
 * @return a pretty-printed string of this AND node
*/
public String display() {
	return "(" + contentA.display() + " & " + contentB.display() + ")";
}

/**
 * Evaluates this AND by recursing down to its contents.
 *
 * @return contentA AND contentB
*/
public boolean evaluate(MRNode startNode) {
	return ((contentA.evaluate(startNode)) && (contentB.evaluate(startNode)));
}

}
