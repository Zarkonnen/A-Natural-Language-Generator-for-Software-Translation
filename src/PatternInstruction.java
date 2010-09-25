/**
 * A single instruction in a path.
 * Moves either up or down Meaning Representation tree structure
 * and checks against the name of the field it moves to.
 * It can also check against its  type / tag if required.
 * If the matching fails, null is returned.
*/

public class PatternInstruction {

/**
 * This boolean determines if the we should
 * stay with the current node in the tree.
 * This allows for checking the type/tag of the
 * current node, and for checking multiple type/tags
 * by having multiple "stay" instructions in series.
*/
private boolean stay;

/**
 * This boolean determines if the pointer should
 * move up the tree or down. Down is more common
 * and is hence set to false.
*/
private boolean goUp;

/**
 * This indicates which field the instruction should
 * move down into. Or, if moving up, in what field
 * of the parent node the original node should be.
*/
private String fieldName;

/**
 * The language in which the fieldName is specified.
*/
private String fieldNameLanguage;

/**
 * The LanguageRuleServer this instruction
 * employs. It is needed for checking against
 * tags and supertypes.
 
*/
private LanguageRuleServer myLanguageRuleServer;

/**
 * Optionally, the type or tag the destination node
 * should have. If this is set to null, the check
 * is not made.
*/
private String typeOrTag;

/**
 * This is the line and file where this particular instruction comes from.
*/
private String myLineAndFile;

/**
 * Constructor.
 *
 * @param stay whether we just stay where we are
 * @param goUp whether the pointer should move to the parent, or move to a field
 * @param fieldName when going down, the name of the field to go to. When going up, used to check if we were in the parent's field of this name. Optional if going up. Ignored if staying.
 * @param fieldNameLanguage the language of above field name. Must be set if fieldName is set.
 * @param typeOrTag the new node is checked for having this type or tag. Check not made if this is set to null.
 * @param myLineAndFile the location where this instruction was read from. Necessary for error reporting.
*/
public PatternInstruction(boolean stay, boolean goUp, String fieldName, String fieldNameLanguage, String typeOrTag, LanguageRuleServer languageRuleServer, String myLineAndFile) {
	/*
	This is quite boring really - we literally just copy over all the data into local fields.
	*/
	this.stay = stay;
	myLanguageRuleServer = languageRuleServer;
	this.goUp = goUp;
	this.fieldName = fieldName;
	this.fieldNameLanguage = fieldNameLanguage;
	this.typeOrTag = typeOrTag;
	this.myLineAndFile = myLineAndFile;
}

/**
 * Executes this instruction.
 * If the instruction can be followed, returns the resulting node. If not, returns null.
 * The instruction can fail because the node does not exist, or because it failed to have the indicated type / tag.
 *
 * @param node the MRNode to execute this from
 * @return the MRNode arrived at, or null if the match fails
*/
public MRNode follow(MRNode node) {
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		if (stay) {
			System.out.println(DebugTracer.getIndent() + "=[" + typeOrTag + "] for '" + node.getType().getName("en") + "'");
		} else {
			if (goUp) {
				System.out.println(DebugTracer.getIndent() + "^" + fieldName + "[" + typeOrTag + "] for '" + node.getType().getName("en") + "'");
			} else {
				System.out.println(DebugTracer.getIndent() + "." + fieldName + "[" + typeOrTag + "] for '" + node.getType().getName("en") + "'");
			}
		}
	}

	/*
	We follow the instruction, by first setting the nextNode value to the current node value.
	*/
	MRNode nextNode = node;
	if (stay == false) {
		if (goUp) {
			/*
			In the case of "up", acquire the new node by calling getParent();
			*/
			nextNode = node.getParent();
			
			/*
			Optionally, compare against the field name. Note that this is the name of the parent's field in which the original
			node is in. NOT the name of the field the parent is in.
			*/
			if (fieldName.length() > 0) {
				if (nextNode.getChild(fieldName, fieldNameLanguage) != node) {
					return null;
				}
			}
		} else {
			/*
			In the case of "down", we get the node's child.
			*/
			nextNode = node.getChild(fieldName, fieldNameLanguage);
			if (nextNode == null) {
				return null;
			}
		}
	}
	
	
	/*
	Now, if the typeOrTag is not null, we have to check if our new node has the type/tag required.
	*/
	if (typeOrTag.length() > 0) {
	
		/*
		The first option: the new node is a string or int literal. In which case we acquire the
		literalvalue from it, and string-compare it against the typeOrTag field.
		If the compare fails, we return null immediately and don't bother to recurse up.
		(Is this good?)
		*/
		if (nextNode.isLiteral()) {
			if (nextNode.literalValue().equals(typeOrTag)) {
				return nextNode;
			} else {
				return null;
			}
		}
		
		/*
		The second option: We get the type of the new node, then the name of this type,
		and compare it against the typeOrTag field. If this fails we fall through
		to the next step, which is trying the tag.
		*/
		if (nextNode.getType().getName(fieldNameLanguage).equals(typeOrTag)) {
			return nextNode;
		}
		
		/*
		The third option: We get the ruleset for this node, and ask it whether its type
		or tag - or the type/tag of its superclass - matches. If not, or if there is no
		ruleset, we give up and return null.
		*/
		if (myLanguageRuleServer.hasRuleFor(nextNode.getType())) {
			MeaningRuleSet nextNodeRules = myLanguageRuleServer.getRule(nextNode.getType(), myLineAndFile);
			if (nextNodeRules.hasNameOrTag(typeOrTag)) {
				return nextNode;
			}
		}
		return null;
	} else {
		return nextNode;
	}
}

/**
 * Pretty-prints the instruction in a format
 * that can be read back in.
 *
 * @return the instruction in text form
*/
public String display() {
	String result = "";
	if (goUp) {
		result = "^";
	} else {
		result = ".";
	}
	result = result + fieldName;
	if (typeOrTag.length() > 0) {
		result = result + "[" + typeOrTag + "]";
	}
	return result;
}

}
