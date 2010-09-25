import java.util.ArrayList;
import java.util.Iterator;

/**
 * A path through a meaning representation tree.
 * Evaluating the path will return the node at the end of the path,
 * or null, if the path cannot be followed.
*/
public class TreePattern implements LogicNode {

/**
 * The string that defines this path.
*/
private String patternString;

/**
 * The language in which the path is formulated.
*/
private String myLanguage;

/**
 * An ArrayList of PatternInstructions that describes this path.
*/
private ArrayList instructions;

/**
 * The language rule server for this path's language.
*/
private LanguageRuleServer myLanguageRuleServer;

/**
 * The file and line this pattern is in.
*/
private String myFileAndLine;

/**
 * Constructor.
 *
 * @param pattern string definition of this path, parsed by the constructor
 * @param language the language in which the path is formulated
 * @param languageRuleServer the language rule server for the path's language
 * @param myFileAndLine the file and line this pattern is in
*/
public TreePattern(String pattern, String language, LanguageRuleServer languageRuleServer, String myFileAndLine) {
	myLanguageRuleServer = languageRuleServer;
	myLanguage = language;
	this.myFileAndLine = myFileAndLine;
	

	/*
		The structure of the string is as follows:
	It is composed out of a series of instructions with one or two parameters.
	The three instructions possible are:
	. for descending to a child-node
	^ for ascending to the parent node
	= for staying at the current node
	The first of the two possible parameters is the field name. It follows immediately
	after the instruction character. In the "." instruction, it indicates which field
	the destination is in. In the "^" instruction, it acts as a check as to whether the
	current node is in the parent node's field of that name. It is not allowed for the
	"=" instruction.
	The second of the possible parameters is the typeOrTag. This is a value that is
	matched against the name of the destination node's MRS and its tags. If no match
	is found, it is also matched against the types and tags of the destination's
	super-MRSs.
	*/
	
	/*
	The syntax is as follows:
	First: "^" or ".", followed by the currentField.
	Or "=".
	Second, optionally, currentTypeOrTag enclosed in square brackets. To put it in a greppish notation:
	
	(((^|.)currentField)|=)([currentTypeOrTag])?
	
	For example:
	.quantity[several]
	This is the instruction to go down to the quantity field, and to check that its type is "several".
	
	We now take it upon ourselves to parse this vile dragon.
	
	Specifically, what we are doing is that we are both lexing and parsing it at the same time.
	We examine each character in turn. If the character the beginning of an instruction (^|.|=), we
	add the just-completed instruction to the "instructions" ArrayList, and clear currentField and
	currentTypeOrTag. Subsequent normal characters are added to currentField.
	
	If a [ is encountered, future letters are added to currentTypeOrTag instead.
	*/
	this.patternString = pattern;
	
	/*
	The list of instructions.
	*/
	instructions = new ArrayList();
	
	/*
	The purpose of this boolean is to prevent the creation of an Instruction when the first (^|.|=) is
	encountered.
	*/
	boolean firstInstruction = true;
	
	/*
	This bit is switched on when a [ is encountered, and means that subsequent characters are added to
	currentTypeOrTag instead of currentField.
	*/
	boolean definingTypeOrTag = false;
	
	/*
	These two booleans define which of the three possible instructions we're currently reading in.
	*/
	boolean stay = false;
	boolean goUp = false;
	
	/*
	Variables for accumulating the field and typeOrTag values.
	*/
	String currentField = "";
	String currentTypeOrTag = "";
	
	/*
	Loop through all characters in the pattern string.
	*/
	for (int i = 0; i < pattern.length(); i++) {
		String currentChar = pattern.substring(i, i + 1);
		
		/*
		The purpose of this boolean is simply to remember if we've already processed
		the current character or not. This is set to true if the character is a "special" one
		(^|.|=|[|])
		Otherwise the character gets added to currentField or currentTypeOrTag at the end of the
		loop.
		*/
		boolean done = false;

		/*
		Encountering a "go up" instruction.
		*/
		if (currentChar.equals("^")) {
			definingTypeOrTag = false;
			if (firstInstruction) {
				/*
				If it's the first one, all we do is remember which kind of instruction we're now processing.
				*/
				firstInstruction = false;
				goUp = true;
				stay = false;
			} else {
				/*
				Since this is not the first instruction, we add the previous and now complete one to the instructions arraylist.
				Then we reset the fields.
				*/
				instructions.add(new PatternInstruction(stay, goUp, currentField, language, currentTypeOrTag, myLanguageRuleServer, myFileAndLine));
				goUp = true;
				stay = false;
				currentField = "";
				currentTypeOrTag = "";
			}
			done = true;
		}
		
		
		/*
		These next two cases are exactly analogous to the ^ case.
		*/
		if (currentChar.equals(".")) {
			definingTypeOrTag = false;
			if (firstInstruction) {
				firstInstruction = false;
				goUp = false;
				stay = false;
			} else {
				instructions.add(new PatternInstruction(stay, goUp, currentField, language, currentTypeOrTag, myLanguageRuleServer, myFileAndLine));
				goUp = false;
				stay = false;
				currentField = "";
				currentTypeOrTag = "";
			}
			done = true;
		}
		
		
		if (currentChar.equals("=")) {
			definingTypeOrTag = false;
			if (firstInstruction) {
				firstInstruction = false;
				goUp = false;
				stay = true;
			} else {
				instructions.add(new PatternInstruction(stay, goUp, currentField, language, currentTypeOrTag, myLanguageRuleServer, myFileAndLine));
				goUp = false;
				stay = false;
				currentField = "";
				currentTypeOrTag = "";
			}
			done = true;
		}
		
		
		if (currentChar.equals("[")) {
			definingTypeOrTag = true;
			done = true;
		}
		
		/*
		All this actually does is make sure "]" and " " are not added to field names or typeOrTags.
		Spaces are ignored here, and close brackets are technically unnecessary, as a typeOrTag
		definition is implicitly finished by ^|.|= or the end of the string.
		*/
		if ((currentChar.equals(" ")) || (currentChar.equals("]"))) {
			done = true;
		}
		
		/*
		All other characters are moved int to the currentTypeOrTag or currentField fields,
		depending on what we're defining at the moment.
		*/
		if (done == false) {
			if (definingTypeOrTag) {
				currentTypeOrTag = currentTypeOrTag + currentChar;
			} else {
				currentField = currentField + currentChar;
			}
		}
		
		
	} // end loop
	
	//write the final entry, unless there were never any entries in the first case
	if (firstInstruction == false) {
		instructions.add(new PatternInstruction(stay, goUp, currentField, language, currentTypeOrTag, myLanguageRuleServer, myFileAndLine));
	}
	
	//if firstInstruction on and there is something in the currentField or currentTypeOrTag fields
	//then something has gone quite wrong, as we here have some free-floating text with no instruction before it
	if ((firstInstruction) && ((currentField.length() > 0) || (currentTypeOrTag.length() > 0))) {
		throw new ConditionLogicParserException(myFileAndLine + "The path string is malformed.");
	}
}

/**
 * Fill function. Should NEVER EVER BE CALLED.
 * Hence, immediately throws exception if called.
 *
 * The reason why this is here is that TreePattern must conform to the LogicNode interface.
 *
 * @throws ConditionLogicParserException
*/
public void fill(LogicNode node) {
	throw new ConditionLogicParserException("Cannot fill a tree pattern!");
}

/**
 * Returns true if the pattern matches.
*/
public boolean evaluate(MRNode node) {
	if (instructions.size() == 0) {
		return true;
	}
	
	/*
	This works by invoking the instructions in sequence, feeding the result of one
	into the next. If an instruction returns null (meaning that it could not be followed),
	the function returns false.
	*/
	
	MRNode currentNode = node;
	Iterator iter = instructions.iterator();
	while (iter.hasNext()) {
		currentNode = ((PatternInstruction) iter.next()).follow(currentNode);
		if (currentNode == null) {
			return false;
		}
	}
	
	return true;
}

/**
 * Follows the path of the pattern. 
*/
public MRNode follow(MRNode node) {
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		System.out.println(DebugTracer.getIndent() + "Evaluating path '" + patternString + "' for node '" + node.getType().getName("en") + "'");
		DebugTracer.incRL();
	}

	MRNode currentNode = node;
	Iterator iter = instructions.iterator();
	while (iter.hasNext()) {
		currentNode = ((PatternInstruction) iter.next()).follow(currentNode);
		if (currentNode == null) {
			throw new ProductionPatternCannotBeResolvedException(patternString, myFileAndLine);
		}
	}
	
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		DebugTracer.decRL();
	}
		
	return currentNode;
}



//PRETTYPRINTING



/**
 * Writes out the pattern.
 * Should be readable back in.
*/
public String display() {
	/*
	This works by calling "display" on each instruction.
	*/
	String result = "";
	Iterator iter = instructions.iterator();
	while (iter.hasNext()) {
		result = result + ((PatternInstruction) iter.next()).display();
	}
	return result;
}
}
