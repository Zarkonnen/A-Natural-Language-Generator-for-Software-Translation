/**
 * Represents a NOT operator for use in a LogicNode tree.
*/

public class TreeNot implements LogicNode {

private LogicNode content;

/*
This class uses the default constructor because it needs no
initial values.
*/

/**
 * Used to set the LogicNode to be NOTed.
 *
 * @param node the node to be negated
*/
public void fill(LogicNode node) {
	content = node;
}

/**
 * Pretty-prints the NOT in a way that can be parsed back in.
 *
 * @return a pretty-printed string of this NOT node
*/
public String display() {
	return "!(" + content.display() + ")";
}

/**
 * Evaluates this NOT by recursing down to its contained node.
 *
 * @return NOT(content)
*/
public boolean evaluate(MRNode startNode) {
	return !(content.evaluate(startNode));
}

}
