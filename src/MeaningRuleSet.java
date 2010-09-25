import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An ordered list of generation rules associated with a given VocabularyMeaning.
 * It is invoked by the generation string of a rule, or on the top MRNode of
 * a MeaningRepresentation.
 *
 * The rules' conditions are tested one after the other, and the first one to
 * have its condition satisfied is invoked.
 *
 * Remember:
 * LanguageServer contains LanguageRuleServers contains MeaningRuleSets contains MeaningRules.
*/

public class MeaningRuleSet {

/*
The VocabularyMeaning this is associated with. This may be null, as some
super-MRSs are not associated with any VM.
For example, "verb" is a super-MRS of "open". "open" is a VM, but "verb" is not.
*/
private VocabularyMeaning myMeaning;

/*
The LanguageRuleServer this MRS is served by.
*/
private LanguageRuleServer myLanguageRuleServer;

/*
The set of tags associated with this MRS. Can be tested by the condition of a MeaningRule.
*/
private HashSet tags;

/*
The super-MRS. This can be null if there is no super-MRS.
*/
public MeaningRuleSet mySuper;

/*
The name of this MRS - if an associated VocabularyMeaning exists, it has the same
name in the language this MRS is about.
*/
public String name;

/*
The name of the super-MRS. Used only temporarily to record the name before linking.
*/
private String mySuperName;

/*
An ordered list of the generation rules this MRS contains.
*/
private ArrayList rules;

/*
The line and file where this MRS is defined.
*/
private String myLineAndFile;

/**
 * Constructor.
 *
 * @param name the name of this MRS
 * @param myMeaning the meaning this rule is about
 * @param myLanguageRuleServer the server this rule is in
 * @param myLineAndFile the line and file where this MRS is defined
*/
public MeaningRuleSet(String name, VocabularyMeaning myMeaning, LanguageRuleServer myLanguageRuleServer, String myLineAndFile) {
	this.name = name;
	this.myMeaning = myMeaning;
	this.myLanguageRuleServer = myLanguageRuleServer;
	tags = new HashSet();
	rules = new ArrayList();
	this.myLineAndFile = myLineAndFile;
}

/**
 * @return true if the given name is correct or a matching tag exists.
*/
public boolean hasNameOrTag(String match) {
	if (name.equals(match) || tags.contains(match)) {
		return true;
	} else {
		if (mySuper != null) {
			return mySuper.hasNameOrTag(match);
		}
	}
	return false;
}

/**
 * Adds a tag.
 *
 * @param tag the tag
*/
public void addTag(String tag) {
	tags.add(tag);
}

/**
 * Sets the name of the super-MRS. Note that link() has to be called to actually make the connection.
 *
 * @param mySuperName the name of the super-MRS of this MRS
*/
public void setSuperName(String mySuperName) {
	this.mySuperName = mySuperName;
}

/**
 * Adds a MeaningRule.
 *
 * @param ruleDefinition a string defining the rule
*/
public void addRule(String ruleDefinition, String myLineAndFile) {
	MeaningRule mr = new MeaningRule(ruleDefinition, myLanguageRuleServer.languageName, myLanguageRuleServer, myLineAndFile);
	rules.add(mr);
}

/**
 * Links the super-ruleset. This must be called after all MeaningRuleSets have been loaded.
*/
public void link() {
	if (mySuperName != null) {
		mySuper = myLanguageRuleServer.getRule(mySuperName, myLineAndFile + "While linking:\n");
	}
}

/**
 * This function outputs the string generated for the given node.
 * It calls a private "produce" function that has this ruleset as a parameter. This indirection
 * is necessary because the generation process needs to keep track of at which MRS a given call
 * to produce() started. Specifically, the super-MRS needs to know which sub-MRS the call originated
 * from.
 *
 * @param match the name of the generation rule to use
 * @param node the MRNode to apply this MRS to
 * @return the result of applying this MRS to the given MRNode: natural language
*/
public String produce (String match, MRNode node) {
	return produce(match, node, this);
}

/**
 * The private generation function.
 * It works by iterating over the set of rules, testing the name & condition of each,
 * and invoking the first one it matches. If no rule matches, it invokes produce() for
 * its super-MRS. If no super-MRS exists, generation has failed, and an exception is thrown.
*/
private String produce(String match, MRNode node, MeaningRuleSet originatingRuleSet) {
	/* DebugTrace */
	if (DebugTracer.doDebug()) {
		System.out.println(DebugTracer.getIndent() + "Invoking RuleSet '" + name + "' using match string '" + match + "'.");
		DebugTracer.incRL();
	}

	Iterator iter = rules.iterator();
	while (iter.hasNext()) {
		MeaningRule r = (MeaningRule) iter.next();
		/* DebugTrace */
		if (DebugTracer.doDebug()) {
			//System.out.println(DebugTracer.getIndent() + "Testing rule " + r.name + ", " + r.displayCondition() + ".");
			DebugTracer.incRL();
		}
		if (r.applies(match, node)) {
			/* DebugTrace */
			if (DebugTracer.doDebug()) {
				System.out.println(DebugTracer.getIndent() + "Invoking rule '" + r.name + "'.");
				DebugTracer.decRL();
				DebugTracer.decRL();
			}
			return r.produce(node, originatingRuleSet);
		}
		/* DebugTrace */
		if (DebugTracer.doDebug()) {
			DebugTracer.decRL();
		}
	}
	

	if (mySuper != null) {
		/* DebugTrace */
		if (DebugTracer.doDebug()) {
			System.out.println(DebugTracer.getIndent() + "No match found, invoking super-ruleset: '" + mySuper.name + "'.");
			DebugTracer.decRL();
		}
		return mySuper.produce(match, node, originatingRuleSet);
	} else {
		throw new NoMatchingProductionRuleFoundException(match, name, myLineAndFile);
	}
}

}
