/**
 * Represents an OR operator for use in a TreePattern.
*/

public class TreeOr implements LogicNode {

private LogicNode contentA;
private LogicNode contentB;

/**
 * The Constructor. Requires one LogicNode tree.
 *
 * @param node one of two LogicNodes to be ORed
*/
public TreeOr(LogicNode node) {
	contentA = node;
}

/**
 * Used to set the other LogicNode to be ORed.
 * The first one is set by the constructor.
 *
 * @param node the other LogicNode to be ORed
*/
public void fill(LogicNode node) {
	contentB = node;
}

/**
 * Pretty-prints the OR in a way that can be parsed back in.
 *
 * @return a pretty-printed string of this OR node
*/
public String display() {
	return "(" + contentA.display() + " | " + contentB.display() + ")";
}

/**
 * Evaluates this OR by recursing down to its contents.
 *
 * @return contentA OR contentB
*/
public boolean evaluate(MRNode startNode) {
	return ((contentA.evaluate(startNode)) || (contentB.evaluate(startNode)));
}

}
