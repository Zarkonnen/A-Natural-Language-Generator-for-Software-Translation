/**
 * An interface for nodes used to construct trees that represent boolean expressions.
 *
 * Upon creation, such a node always has a slot available for a sub-node,
 * which can then be filled with fill().
*/
public interface LogicNode {

/**
 * This method fills in this node's empty field with a node.
 *
 * @param node the node with which to fill the field
*/
public void fill(LogicNode node);

/**
 * Displays this node and its sub-nodes if any.
*/
public String display();

/**
 * Evaluates the contained pattern matches for the given
 * MRNode as a starting point.
 * 
 * @param startNode the node the patterns should start matching from
 * @return true if the tree matches
*/
public boolean evaluate(MRNode startNode);

}
