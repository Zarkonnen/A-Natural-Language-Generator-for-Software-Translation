import java.util.HashMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

/**
 * A server for mapping VocabularyMeanings to MeaningRuleSets for a given language.
 *
 * Remember:
 * LanguageServer contains LanguageRuleServers contains MeaningRuleSets contains MeaningRules.
 *
 * @author David Stark
 * @version 2006-03-27
*/
public class LanguageRuleServer {

/*
The name of this language. It is not the full name but rather a 2-3 letter code.
"en", for example, stands for "English", and "de" for "Deutsch".
*/
public String languageName;

/*
A HashMap mapping VocabularyMeanings to MeaningRuleSets.
*/
private HashMap vocabularyMeaningToMeaningRuleSet;

/*
A HashMap for looking up the names of MeaningRuleSets.
*/
private HashMap nameToMeaningRuleSet;

/**
 * The constructor. It loads the data for said language from file.
 *
 * @param language the language this server is for.
*/
public LanguageRuleServer(String language) {
	languageName = language;
	vocabularyMeaningToMeaningRuleSet = new HashMap();
	nameToMeaningRuleSet = new HashMap();
	
	/*
	We find the data file relative to the jar file - at data/languages/<language>.txt
	*/
	File languageRulesFile = new File(
		new File(
			new File(
				new File(
					ClassLoader.getSystemResource("LanguageRuleServer.class").getPath().substring(5).replaceAll("\\%20", "\\ "))
				.getParentFile().getParentFile()
			, "data")
		, "languages")
	, language + ".txt");
	
	if (languageRulesFile.exists() == false) {
		throw new LanguageNotImplementedException("The language " + language + " has no rules file.");
	}
	
	/*
	The method of data loading adapted here is one of reading in line by
	line and passing the lines down	to the current object they contain for further processing.
	
	Whenever a "rules" line is encountered, we know that a new MRS definition has started.
	*/
	
	PeekingLineReader r = null;
	try {
		r = new PeekingLineReader(
			new BufferedReader(
				new FileReader(languageRulesFile)
			)
		, true, false);
	}
	catch (java.io.FileNotFoundException e) {
		throw new LanguageNotImplementedException("The language " + language + " has no rules file.");
	}
	
	/*
	Now we loop through. Each line consists of two parts: a keyword, and some parameters for it.
	Allowed keywords are: 
	rules - for starting a new MRS definition
	tag - for specifying a tag
	rule - for specifying a tag
	super - for specifying the super-MRS
	
	Comments in the // style are also possible.
	*/
	
	MeaningRuleSet currentRuleSet = null;
		
	String line = r.readLine();
	while (line != null) {
		/*
		We split apart each line into two parts. The first part is everything leading up to the first space,
		the second part is everything after that space.
		*/
		String lineparts[] = StringFunctions.splitLineAlongSpace(line);
		boolean processed = false;
		
		/*
		If the line begins with "rules", a new MeaningRuleSet is created. The VocabularyServer is queried for
		the associated VocabularyMeaning, but there may not be one, as in the case of "verb". In that case,
		the VS will simply return null, and this is handled by the addMeaningRuleSet function.
		*/
		if (lineparts[0].equals("rules")) {
			String lineAndFile = "Rules file for " + language + ", line " + Integer.toString(r.getLineNumber()) + ":\n";
			currentRuleSet = addMeaningRuleSet(VocabularyServer.lookupMeaningNullOnFail(lineparts[1], languageName), lineparts[1], lineAndFile);
			processed = true;
		}
		
		/*
		These next three cases simply deal with storing tag/rule/super-MRS information in the current MRS.
		*/
		if (lineparts[0].equals("tag")) {
			currentRuleSet.addTag(lineparts[1]);
			processed = true;
		}
		if (lineparts[0].equals("rule"))  {
			String lineAndFile = "Rules file for " + language + ", line " + Integer.toString(r.getLineNumber()) + ":\n";
			currentRuleSet.addRule(lineparts[1], lineAndFile);
			processed = true;
		}
		if (lineparts[0].equals("super"))  {
			currentRuleSet.setSuperName(lineparts[1]);
			processed = true;
		}
		/*
		Allowing for possible comments, we raise an exception if the line does not start properly.
		*/
		if (processed == false) {
			if (lineparts[0].startsWith("//") == false) {
				throw new UnexpectedLineInLanguageRulesException(languageName, line, "rules, tag, rule, super, or a comment (//)", r.getLineNumber());
			}
		}
		line = r.readLine();
	}
	
	/*
	Then we iterate through all the MRSs, and tell them to link() to their super-MRS.
	*/
	Iterator ruleIter = nameToMeaningRuleSet.values().iterator();
	while (ruleIter.hasNext()) {
		((MeaningRuleSet) ruleIter.next()).link();
	}
}

/**
 * Returns a meaning rule set for the given VocabularyMeaning.
 *
 * @param meaning the VocabularyMeaning to look for.
 * @param errorLocation which line and file caused the request for this MRS
 * @return the rule set
 * @throws RuleSetNotFoundException
*/
public MeaningRuleSet getRule(VocabularyMeaning meaning, String errorLocation) {
	if (vocabularyMeaningToMeaningRuleSet.containsKey(meaning)) {
		return (MeaningRuleSet) vocabularyMeaningToMeaningRuleSet.get(meaning);
	} else {
		throw new RuleSetNotFoundException(languageName, meaning.getName(languageName), errorLocation);
	}
}

/**
 * Returns a meaning rule set for the given name.
 *
 * @param name the name to look for
 * @param errorLocation which line and file caused this call
 * @return the rule set
 * @throws RuleSetNotFoundException
*/
public MeaningRuleSet getRule(String name, String errorLocation) {
	if (nameToMeaningRuleSet.containsKey(name)) {
		return (MeaningRuleSet) nameToMeaningRuleSet.get(name);
	} else {
		throw new RuleSetNotFoundException(languageName, name, errorLocation);
	}
}


/**
 * Returns true if there is a meaning rule for the given meaning.
 *
 * @param meaning the meaning to look for
 * @return whether there is a rule for the meaning
*/
public boolean hasRuleFor(VocabularyMeaning meaning) {
	return vocabularyMeaningToMeaningRuleSet.containsKey(meaning);
}

/**
 * Add a new MeaningRuleSet. This is then returned so its values can be filled in.
 * It is also added to the nameToMeaningRuleSet HashMap, and if there is an associated
 * VocabularyMeaning, added to the vocabularyMeaningToMeaningRuleSet HashMap.
 *
 * @param meaning the VocabularyMeaning this ruleSet is for. This can be null if there is no matching VM.
 * @param name the name of the MRS to be created.
 * @param myLineAndFile the line and file this MRS is in
 * @return the MeaningRuleSet
*/
public MeaningRuleSet addMeaningRuleSet(VocabularyMeaning meaning, String name, String myLineAndFile) {
	MeaningRuleSet newRuleSet = new MeaningRuleSet(name, meaning, this, myLineAndFile);
	if (meaning != null) {
		vocabularyMeaningToMeaningRuleSet.put(meaning, newRuleSet);
	}
	nameToMeaningRuleSet.put(name, newRuleSet);
	return newRuleSet;
}



//PRETTYPRINTING

/**
 * Generates a graph of the rulesets and their classing.
 *
 * @return a .dot file of the graph
*/
public String graph() {
	String result = "digraph \"RULE SETS-" + languageName + "\" {\n";
	//nodes
	Iterator ruleIter = nameToMeaningRuleSet.keySet().iterator();
	while (ruleIter.hasNext()) {
		String name = (String) ruleIter.next();
		MeaningRuleSet mrs = (MeaningRuleSet) nameToMeaningRuleSet.get(name);
		if (mrs.mySuper == null) {
			result = result + "\"" + name + "\";\n";
		} else {
			result = result + "\"" + name + "\" -> \"" + mrs.mySuper.name + "\";\n";
		}
	}
	return result + "}";
}

}
