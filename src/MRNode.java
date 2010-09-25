import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.io.PrintStream;

/**
 * A node in a meaning representation. It is an "instance" of a
 * VocabularyMeaning.
 *
 * @author David Stark
 * @version 2006-03-28
*/
public class MRNode {
	
	private VocabularyMeaning myType;
	private HashMap fields;
	private MRNode parent;
	
	private static int iDcounter = 0; //for unique IDs.
	public int iD;
	
	private String stringLiteral;
	private int intLiteral;
	private MRNode anyValue;
	private boolean isStringLiteral;
	private boolean isIntLiteral;
	
	/**
	 * The constructor. It needs to know the type of this node.
	 *
	 * @param type the type
	 * @param parent the node this node is contained in
	*/
	public MRNode(VocabularyMeaning type, MRNode parent) {
		myType = type;
		fields = new HashMap();
		this.parent = parent;
		
		iD = iDcounter;
		iDcounter++;
	}
	
	/**
	 * Constructor for a string literal. A string literal is a special
	 * kind of MRNode that has no type, and holds a string.
	 *
	 * @param stringLiteral the string this literal holds
	 * @param parent the node this node is contained in
	*/
	public MRNode(String stringLiteral, MRNode parent) {
		isStringLiteral = true;
		this.stringLiteral = stringLiteral.substring(1, stringLiteral.length() - 1);
		this.parent = parent;
		fields = new HashMap();
		
		iD = iDcounter;
		iDcounter++;
	}
	
	/**
	 * Constructor for an int literal. An int literal is a special
	 * kind of MRNode that has no type, and holds an integer.
	 *
	 * @param intLiteral the integer this literal holds
	 * @param parent the node this node is contained in	*/
	public MRNode(int intLiteral, MRNode parent) {
		isIntLiteral = true;
		this.intLiteral = intLiteral;
		this.parent = parent;
		fields = new HashMap();
		
		iD = iDcounter;
		iDcounter++;
	}
	
	/**
	 * Returns true if the node is a literal.
	*/
	public boolean isLiteral() {
		return (isStringLiteral || isIntLiteral);
	}
	
	/**
	 * Returns true if string literal.
	*/
	public boolean isStringLiteral() {
		return isStringLiteral;
	}
	
	/**
	 * Returns true if int literal.
	*/
	public boolean isIntLiteral() {
		return isIntLiteral;
	}
	
	/**
	 * Returns the literal value as a string.
	 *
	 * @return the literal value
	*/
	public String literalValue() {
		if (isStringLiteral) {
			return stringLiteral;
		}
		return Integer.toString(intLiteral);
	}
	
	/**
	 * Sets the literal value from a string. Will try to decode string to int if necessary.
	 *
	 * @param value a string that contains the literal value
	*/
	public void setLiteralValue(String value) {
		if (isIntLiteral) {
			intLiteral = Integer.parseInt(value);
		} else {
			stringLiteral = value;
		}
	}
	
	/**
	 * Another constructor, which can simply be passed
	 * the name and language of the type of this node.
	 *
	 * @param name the name of the type
	 * @param language the language the name is in
	 * @param parent the parent of this node
	 * @param errorLocation the line that caused this constructor to be called - needed for error reporting
	*/
	public MRNode(String name, String language, MRNode parent, String errorLocation) {
		myType = VocabularyServer.lookupMeaning(name, language, errorLocation);
		fields = new HashMap();
		this.parent = parent;
		
		iD = iDcounter;
		iDcounter++;
	}
	
	/**
	 * Adds a child, and returns its MRNode.
	 *
	 * @param label the label of the child
	 * @param type the type of the child
	 * @param language the language both are in
	 * @param errorLocation the line that caused this to be called - needed for error reporting
	 * @return the new child
	*/
	public MRNode addChild(String label, String type, String language, String errorLocation) {
		/*
		We need to retreive two pieces of information here. First off, we need the
		MeaningField object that describes the field this child shall be put into.
		Then we need the VocabularyMeaning of the child itself, to make a MRNode.
		And then put the newly created MRNode into this MRNode's field.
		*/
		
		MeaningField childField = myType.getField(label, language, errorLocation);
		
		//check for it being null due to language not found
		if (childField == null) {
				throw new VocabularyServerException(errorLocation + "The language '" + language + "' for the field called '" + label + "' could not be found.");
		}

		//the special cases of it being an int or a string
		if (childField.isString()) {
			MRNode child = new MRNode(type, this);
			fields.put(childField, child);
			return child;
		}
		if (childField.isInt()) {
			MRNode child = new MRNode(Integer.parseInt(type), this);
			fields.put(childField, child);
			return child;
		}
		

		VocabularyMeaning childType = VocabularyServer.lookupMeaning(type, language, errorLocation);
		MRNode child = new MRNode(childType, this);
		
		/*
		Now everything is ready - the new child, the field it should go into. But we should now check
		whether this type of child actually should go into this field.
		So here we ask the type of the child given whether the type of the field is the same type
		or a supertype. (We can put MRNodes into fields of their supertype.)
		*/
		if (!(childType.isMeOrSuper(childField.getType()))) {
			throw new TypingException(errorLocation + "'" + childType.getName("en") + "' is not the same type or a subtype of '" + childField.getType().getName("en") + "' and can hence not be put into the field '" + label + "'.");
		}
		
		fields.put(childField, child);
		return child;
	}
	
	/**
	 * Returns a the contents of a field. If it cannot be found, returns null.
	 * It will not throw an error if the field referred to does not exist in
	 * the type of this term. (in the VM of this MRNode)
	 *
	 * @param label the name of the field the child is in
	 * @param language the name of the data
	 * @return the MRNode of the child
	*/
	public MRNode getChild(String label, String language) {
		/*
		The way this works is that it requests the given field from its type,
		then it uses that field as an index into its fields hashmap to retreive
		the child looked for.
		For purposes of error reporting, we assume that the type would complain
		if we were asking for a nonexistent field.
		*/
		return (MRNode) fields.get(myType.getFieldNullOnFail(label, language));
	}
	
	/**
	 * Returns the contents of a field. If it cannot be found, returns null.
	 *
	 * @param field the field
	 * @return the MRNode of the child
	*/
	public MRNode getChild(MeaningField field) {
		return (MRNode) fields.get(field);
	}
	
	/**
	 * Returns a list of all used fields.
	 *
	 * @return a hashset of all used fields
	*/
	public HashSet getUsedFields() {
		return new HashSet(fields.keySet());
	}
	
	/**
	 * Gives the width of this bit of the meaning-representation tree.
	*/
	public int treeWidth(MRNode selected, String language) {
		if (fields.size() == 0) {
			return 1;
		}
		Iterator childIter = fields.values().iterator();
		int count = 0;
		while (childIter.hasNext()) {
			count += ((MRNode) childIter.next()).treeWidth(selected, language);
		}
		if (this == selected) {
			count++;
			/*
			HashSet emptyFields = myType.allFieldsSet(language);
			emptyFields.removeAll(fields.values());
			count += emptyFields.size();
			*/
		}
		return count;
	}
	
	/**
	 * Returns the parent's field in which this node is in. May be null if at root.
	*/
	public MeaningField getParentField() {
		if (parent == null) {
			return null;
		}
		return parent.getContainingField(this);
	}
	
	/**
	 * Returns the field (if any) which the passed node is in. May be null.
	*/
	public MeaningField getContainingField(MRNode n) {
		//This is slow because we need to look up a hashmap the wrong way around.
		Iterator fieldIter = fields.keySet().iterator();
		while (fieldIter.hasNext()) {
			MeaningField f = (MeaningField) fieldIter.next();
			if (fields.get(f) == n) {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * Deletes a child.
	*/
	public void deleteChild(MeaningField field) {
		fields.remove(field);
	}

	
	/**
	 * Returns the parent.
	*/
	public MRNode getParent() {
		return parent;
	}
	
	/**
	 * Returns the type of this node.
	*/
	public VocabularyMeaning getType() {
		return myType;
	}
	
	/**
	 * Prints the fields of this node to the given stream.
	 *
	 * @param stream a printstream to print to
	 * @param language the language to print in
	*/
	public void writeFieldsToStream(PrintStream stream, String language, String tabs) {
		Iterator fieldIter = fields.keySet().iterator();
		while (fieldIter.hasNext()) {
			MeaningField field = (MeaningField) fieldIter.next();
			MRNode child = (MRNode) fields.get(field);
			if (child.isLiteral()) {
				if (child.isIntLiteral()) {
					stream.println(tabs + field.getName(language) + " " + child.literalValue());
				} else {
					stream.println(tabs + field.getName(language) + " \"" + child.literalValue() + "\"");
				}
			} else {
				stream.println(tabs + field.getName(language) + " " + child.getType().getName(language));
				child.writeFieldsToStream(stream, language, tabs + "\t");
			}
		}
	}
	
	
	//PRETTYPRINTING
	
	
	/**
	 * Writes out this node and its children for a .dot file, 
	 * including the field relations.
	 *
	 * Use this version for the root node.
	 *
	 * @param language the language to display the graph in
	 * @return a list of graphviz nodes of this and its children
	*/
	public String graphNodes(String language) {
		String result = "";
		if (isStringLiteral) {
			result = (new Integer(iD)).toString() + " [label=\"'" + stringLiteral + "'\"];\n";
		} else {
			if (isIntLiteral) {
				result = (new Integer(iD)).toString() + " [label=\"#" + Integer.toString(intLiteral) + "\"];\n";
			} else {
				result = (new Integer(iD)).toString() + " [label=\"" + myType.getName(language) + "\"];\n";
			}
		}
		
		Iterator fieldIter = fields.keySet().iterator();
		while (fieldIter.hasNext()) {
			MeaningField field = (MeaningField) fieldIter.next();
			MRNode child = (MRNode) fields.get(field);
			result = result + child.graphNodes(iD, field.getName(language), language);
		}
		return result;
	}
	
	/**
	 * Writes out this node and its children for a .dot file, 
	 * including the field relations.
	 *
	 * @param parentID the ID of the parent node
	 * @param fieldName the name of the field of the parent node this node is contained in
	 * @param language the language to display the graph in
	 * @return a list of graphviz nodes of this and its children
	*/
	public String graphNodes(int parentID, String fieldName, String language) {
		String result = graphNodes(language);
		return result + (new Integer(parentID)).toString() + " -- " + (new Integer(iD)).toString() + " [label=\"" + fieldName + "\" fontsize=10];\n";
	}
	
}
