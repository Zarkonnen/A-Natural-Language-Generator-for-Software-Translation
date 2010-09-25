import java.util.Stack;

/**
 * Describes a specific production rule. Provides the ability to check whether the rule is applicable
 * for a given MRNode, and will recursively call the generation rules of other nodes when invoked.
 *
 * Remember:
 * LanguageServer contains LanguageRuleServers contains MeaningRuleSets contains MeaningRules.
*/
public class MeaningRule {

/**
 * The name of the rule. This is used for matching against - only if the name of the rule
 * is the correct one should the rule be invoked.
*/
public String name;

/**
 * The production string. Contains the text to be output when this rule is invoked,
 * and may contain references to other rules ( {rulename} ) or other MRNodes ( <path_to_node:rulename> ).
*/
public String production;

/** 
 * The head of a LogicNode tree that specifies the conditions under which this rule should be invoked.
*/
private TreeHead condition;

/**
 * The code of the language this rule is for.
*/
private String myLanguage;

/**
 * The LanguageRuleServer for this rule's language. The rule needs this so it can invoke other rules.
*/
private LanguageRuleServer myLanguageRuleServer;

/**
 * The line and file this rule is in. Used for error reporting.
*/
private String myLineAndFile;


/**
 * Reads in the rule from a definition string.
 *
 * @param def definition string
 * @param language the language this rule is for
 * @param languageRuleServer the rule server for this rule's language
 * @param myLineAndFile the line and file this rule is in
*/
public MeaningRule(String def, String language, LanguageRuleServer languageRuleServer, String myLineAndFile) {
	myLanguageRuleServer = languageRuleServer;
	myLanguage = language;
	this.myLineAndFile = myLineAndFile;
	
	/*
	The definition string consists of three comma-separated values:
	name
	condition
	production
	
	The first act hence is to split the string into three.
	
	The name and production strings are just stored, but the condition needs to be parsed
	into a LogicNode tree.
	*/
	
	/*
	We split things apart by finding the two commas that separate the three values
	and then using substring() to isolate the values.
	*/
	int firstComma = def.indexOf(",");
	int secondComma = def.indexOf(",", firstComma + 2);
	
	name = def.substring(0, firstComma);
	production = def.substring(secondComma + 1).trim();
	production = production.substring(1, production.length() - 1); //get rid of quotes
	String conditionString = "*";
	try {
		conditionString = def.substring(firstComma + 1, secondComma);
	}
	catch (StringIndexOutOfBoundsException e) {
		throw new ConditionLogicParserException(myLineAndFile + e.toString() + "\nLine was: " + def);
	}
	
	
	/*
	Now comes the hard part: parsing the condition string.
	The condition string is a propositional logic expression with paths as its terminals.
	We don't have to worry about the paths here, as they are parsed by the constructor
	of the TreePattern objects we create for them.
	However, we need to parse the boolean logic. The following operators are allowed:
	& conjunction
	| disjunction
	! negation
	Also, brackets are allowed.
	
	How do we then lex and parse this?
	We go through it one character at a time.
	
	The basic idea is this: we maintain a stack of all the expressions we are currently
	in. So for example in the expression a | (b & X), when at X we have the conjunction with b
	on top of the stack, and then the disjunction with a.
	
	The LogicTrees only implement conjunctions and disjunctions with two parameters, so if we
	encounter something like a & b & c, we need to split it into (a & b) & c.
	*/
	
	/* // Activate this and the other print statements of its kind to get debugging information.
	   // The lexer will then output each token as it is recognised.
	System.out.println("\n-------------------------------------");
	System.out.println("NAME = \"" + name + "\"");
	System.out.println("PRODUCTION = \"" + production + "\"");
	System.out.println("CONDITION = \"" + conditionString + "\"");
	*/
	
	if(conditionString.trim().equals("*") == false) {
	
		/*
		We set up the head of the logicnode tree and push it onto the stack.
		*/
		condition = new TreeHead();
		LogicNode currentLN = condition;
		Stack s = new Stack();
		s.push(condition);
		
		/*
		The buffer in which the current terminal is accumulated. Any letter that is not
		&, |, !, (, ) or a space gets added to this.
		*/
		String currentTerminal = "";
		
		/*
		The structure of propositional logic expression is an alternation in between
		expressions like A or (A & (B | !C)), and operators like &. This boolean
		variable keeps track of whether we are expecting an operator ( & or | ), or an
		expression.
		*/
		boolean expectingExpression = true;
		
		/*
		If this is on, it means that successive operators are attached to the next created
		expression.
		*/
		boolean appendToSubNode = false;
		
		boolean firstElementNot = false;
		
		int bracketsLevel = 0;
		
		
		for (int i = 0; i < conditionString.length(); i++) {
			boolean done = false;
			String currentChar = conditionString.substring(i, i + 1);
			if (currentChar.equals(" ")) {
				if (currentTerminal.length() > 0) {
					/*
					//comment in the following line for debug information
					System.out.print(" " + currentTerminal + " ");
					*/
					
					TreePattern tp = new TreePattern(currentTerminal, myLanguage, myLanguageRuleServer, myLineAndFile);
					currentLN.fill(tp);
					if (appendToSubNode) {
						s.push(currentLN);
						currentLN = tp;
						appendToSubNode = false;
					} else {
						if (s.peek().getClass() == TreeNot.class) {
							currentLN = (LogicNode) s.pop();
						}
					}
					
					currentTerminal = "";
					expectingExpression = false;
				}
				done = true;
			}
			if (currentChar.equals("(")) {
				if (expectingExpression == false) {
					throw new ConditionLogicParserException("Found unexpected (.");
				} else {
					//System.out.print(" ( ");
					bracketsLevel++;
					appendToSubNode = true;
				}
				done = true;
			}
			if (currentChar.equals("!")) {
				if (expectingExpression == false) {
					throw new ConditionLogicParserException("Found unexpected !.");
				} else {
					//System.out.print(" ! ");
					s.push(currentLN);
					TreeNot tn = new TreeNot();
					currentLN.fill(tn);
					currentLN = tn;
					if (appendToSubNode) {
						firstElementNot = true;
					}
					appendToSubNode = false;
					//automagically go down one level, I *think* this is right
				}
				done = true;
			}
			if (currentChar.equals("&")) {
				if (expectingExpression) {
					throw new ConditionLogicParserException("Found unexpected &.");
				} else {
					expectingExpression = true;
					//System.out.print(" & ");
					if ((currentLN.getClass() == TreeNot.class) && (firstElementNot == false)) {
						TreeAnd ta = new TreeAnd((LogicNode) s.pop());
						((LogicNode) s.peek()).fill(ta);
						currentLN = ta;
					} else {
						TreeAnd ta = new TreeAnd(currentLN);
						((LogicNode) s.peek()).fill(ta);
						currentLN = ta;
					}
					firstElementNot = false;
				}
				done = true;
			}
			if (currentChar.equals("|")) {
				if (expectingExpression) {
					throw new ConditionLogicParserException("Found unexpected |.");
				} else {
					expectingExpression = true;
					//System.out.print(" & ");
					if ((currentLN.getClass() == TreeNot.class) && (firstElementNot == false)) {
						TreeOr to = new TreeOr((LogicNode) s.pop());
						((LogicNode) s.peek()).fill(to);
						currentLN = to;
					} else {
						TreeOr to = new TreeOr(currentLN);
						((LogicNode) s.peek()).fill(to);
						currentLN = to;
					}
					firstElementNot = false;
				}
				done = true;
			}
			
			if (currentChar.equals(")")) {
				if (expectingExpression) {
					if (currentTerminal.length() > 0) {
						//System.out.print(" " + currentTerminal + " ");
						
						TreePattern tp = new TreePattern(currentTerminal, myLanguage, myLanguageRuleServer, myLineAndFile);
						currentLN.fill(tp);
						if (appendToSubNode) {
							s.push(currentLN);
							currentLN = tp;
							appendToSubNode = false;
						} else {
							if (s.peek().getClass() == TreeNot.class) {
								currentLN = (LogicNode) s.pop();
							}
						}
						
						currentLN = (LogicNode) s.pop();
						
						currentTerminal = "";
						expectingExpression = false;
						bracketsLevel--;
						//System.out.print(" ) ");
						appendToSubNode = false;
					} else {
						throw new ConditionLogicParserException("Found unexpected ).");
					}
				} else {
					if (bracketsLevel == 0) {
						throw new ConditionLogicParserException("Found ) without matching (.");
					} else {
						bracketsLevel--;
						currentLN = (LogicNode) s.pop();
						//System.out.print(" ) ");
						appendToSubNode = false;
					}
				}
				done = true;
			}
			if (done == false) {
				currentTerminal = currentTerminal + currentChar;
			}
		} // end loop
		if (currentTerminal.length() > 0) {
			//System.out.println(" " + currentTerminal + " ");
			TreePattern tp = new TreePattern(currentTerminal, myLanguage, myLanguageRuleServer, myLineAndFile);
			currentLN.fill(tp);
		}
		
	} //end check for *
} //end method

/**
 * Pretty-print the condition of this generation rule.
 *
 * @return a string of the logic condition, should be readable back in.
*/
public String displayCondition() {
	if (condition == null) {
		return "*";
	} else {
		return condition.display();
	}
}

/**
 * Tests whether this rule applies, given a name, and the MRNode this rule is used for.
 *
 * The rule must both have the name supplied, and its condition must evaluate to true
 * for the given MRNode.
 *
 * @param match the rule name to match against
 * @param node the MRNode to use in evaluation of the rule
 * @return whether the rule applies
*/
public boolean applies(String match, MRNode node) {
	return (name.equals(match) && ((condition == null) || condition.evaluate(node)));
}

/**
 * Capitalises the first letter of the given string.
 *
 * @param s the string
 * @return a capitalised version
*/
private String capitalise(String s) {
	return s.substring(0, 1).toUpperCase() + s.substring(1);
}

/**
 * Invokes the rule. The rule may in turn invoke other rules, and will return a text string of natural language.
 *
 * @param node the MRNode to invoke this rule for
 * @param originatingRuleSet the ruleset this invokation originated in. This is needed for invoking other rules for the same meaning.
 * @return a natural language string
*/
public String produce(MRNode node, MeaningRuleSet originatingRuleSet) {
	/*
	Here we need to deal with the generation string. There are two special commands in generation strings:
	{localrule}
	and
	<path:rulename>
	
	So we need to scan the string for
	{
	and
	<
	
	In order to do this we keep three pointers up to date:
	endOfLastBracket, nextCurlyBracket and nextAngleBracket
	whichever of nextCurlyBracket/nextAngleBracket is closer we evaluate
	but first we print the text in-between
	*/
	
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		System.out.println(DebugTracer.getIndent() + "RULE '" + name + "' with production string '" + production + "'.");
		DebugTracer.incRL();
	}
	
	String result = "";
	int endOfLastBracket = 0;
	int nextCurlyBracket = production.indexOf("{");
	int nextAngleBracket = production.indexOf("<");
	while ((nextCurlyBracket > -1) || (nextAngleBracket > -1)) {
		if ((nextCurlyBracket > -1) && ((nextCurlyBracket < nextAngleBracket) || (nextAngleBracket < 0))) {
			/*
			curly bracket time:
			isolate the contents of the bracket
			*/
			String bracketContents = production.substring(nextCurlyBracket + 1, production.indexOf("}", nextCurlyBracket));
			
			if (nextCurlyBracket > 0) { //check if there is anything before the bracket
				result = result + production.substring(endOfLastBracket, nextCurlyBracket);
			}
			
			/*
			invoke originating rule set, with capitalisation if needed
			*/
			if (bracketContents.substring(0, 1).equals("+")) {
				bracketContents = bracketContents.substring(1);
				result = result + capitalise(result = result + originatingRuleSet.produce(bracketContents, node));
			} else {
				result = result + originatingRuleSet.produce(bracketContents, node);
			}
			
			/*
			update bracket pointers
			*/
			endOfLastBracket = production.indexOf("}", nextCurlyBracket) + 1;
			nextCurlyBracket = production.indexOf("{", endOfLastBracket);
		} else {
			if (nextAngleBracket > -1) {
				/*
				angle bracket time.
				
				which is composed of a path to a node whose ruleset gets invoked, and optionally
				a rule name after a colon
				*/
				String bracketContents = production.substring(nextAngleBracket + 1, production.indexOf(">", nextAngleBracket));
				
				//capitalisation: if yes, set capitalise, chop off + symbol
				boolean capitalise = bracketContents.substring(0, 1).equals("+");
				if (capitalise) {
					bracketContents = bracketContents.substring(1);
				}
				
				/*
				now find out if there is a rule name
				*/
				String path = bracketContents;
				String ruleName = "*";
				int colonIndex = bracketContents.indexOf(":");
				if (colonIndex > -1) {
					path = bracketContents.substring(0, colonIndex);
					ruleName = bracketContents.substring(colonIndex + 1);
				}
				
				
				if (nextAngleBracket > 0) { //check if there is anything before the angle bracket, and if yes insert it
					result = result + production.substring(endOfLastBracket, nextAngleBracket);
				}
				
				/*
				now find the node the path refers to
				*/
				MRNode referredToNode = (new TreePattern(path, myLanguage, myLanguageRuleServer, myLineAndFile + "(in the generation string)\n")).follow(node);
				
				/*
				now, invoke the ruleset of the MRNode, or print the literal value of the node if it is a literal
				*/
				String tempResult = "";
				if (referredToNode.isLiteral()) {
					tempResult = referredToNode.literalValue();
				} else {
					MeaningRuleSet nodeMRS = myLanguageRuleServer.getRule(referredToNode.getType(), myLineAndFile);
					tempResult = nodeMRS.produce(ruleName, referredToNode);
				}
				
				/*
				capitalise the result if necessary
				*/
				if (capitalise) {
					result = result + capitalise(tempResult);
				} else {
					result = result + tempResult;
				}
				
				/*
				update bracket pointers
				*/
				endOfLastBracket = production.indexOf(">", nextAngleBracket) + 1;
				nextAngleBracket = production.indexOf("<", endOfLastBracket);
			}
		}
	}
	
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		DebugTracer.decRL();
	}
	
	/*
	Finally, add the trailing text and return the string produced.
	*/
	return result + production.substring(endOfLastBracket);
}

}
