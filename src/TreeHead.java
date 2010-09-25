/**
 * The head of a LogicNode tree.
 * A LogicNode tree is a tree structure made from LogicNodes
 * that represents a boolean logic expression of TreePatterns.
 * Calling evaluate(MRNode) on it evaluates its logic in terms
 * of the results of evaluating the TreePatterns for the given
 * MRNode.
*/

public class TreeHead implements LogicNode {

LogicNode content;

/*
Does not use a constructor. The contents are filled in with fill().
*/

/**
 * Used to fill in the first node of the tree.
 *
 * @param node the first node of the tree
*/
public void fill(LogicNode node) {
	content = node;
}

/**
 * Pretty-prints the entire tree.
 * The result can be read in by the parser.
 *
 * @return a pretty-printed version of the tree
*/
public String display() {
	return content.display();
}

/**
 * Evaluates the entire logic tree.
 * It recurses down its boolean logic, and evaluates the TreePatterns for its node.
 *
 * @param startNode the MR node for which the tree should be evaluated
 * @return whether this tree evaluates to true
*/
public boolean evaluate(MRNode startNode) {
	return content.evaluate(startNode);
}

}