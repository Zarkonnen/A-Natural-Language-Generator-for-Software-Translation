import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * A library class for providing lookup for vocabulary words.
 *
 * @author David Stark
 * @version 2005-11-25
*/
public class VocabularyServer {

	private static HashMap languageMap;
	private static String currentLanguageCode;
	private static HashSet allMeanings;

	/**
	 * This method loads the vocabulary from the external data file
	 * and links its meanings.
	 *
	 * @throws VocabularyFileNotFoundException
	*/
	public static void loadAndLinkVocabulary() {
		/*
		The Plan:
		1. find the data file in relation to the current jarfile
		2. load in the header to determine supported languages
		3. set up a hashmap for each language, and a hashmap from language codes to the language maps
		4. loop through the file, inserting new meanings into the hashmap and then having them define themselves
		5. loop through the meanings in any hashmap and have them link themselves
		*/
		
		//find the data file
		File vocabularyFile = new File(
			new File(
				new File(
					ClassLoader.getSystemResource("VocabularyServer.class").getPath().substring(5).replaceAll("\\%20", "\\ "))
				.getParentFile().getParentFile()
			, "data")
		, "vocabulary.txt");

		System.out.println(vocabularyFile);
				
		//attach a reader to it
		PeekingLineReader r = null;
		try {
			r = new PeekingLineReader(
				new BufferedReader(
					new FileReader(vocabularyFile)
				)
			, true, false);
		}
		catch (java.io.FileNotFoundException e) {
			throw new VocabularyFileNotFoundException();
		}
		
		/*
		The purpose of this hashmap is to map language codes such as "en" to hashmaps
		mapping from names to VocabularyMeanings. it is then filled with hashmaps
		for each language.
		*/
		languageMap = new HashMap();
		
		
		//throw an exception if the header is malformed
		if (r.peek().startsWith("language") == false) {
			throw new NoLanguagesDefinedInVocabularyException();
		}
		
		//create a hashMap for each language
		while(r.peek().startsWith("language")) {
			String lineParts[] = StringFunctions.splitLineAlongSpace(r.readLine());
			languageMap.put(lineParts[1], new HashMap());
		}
		
		/*
		Create a hashset for catching all meanings. This is needed for
		calling "link" on them later.
		*/
		allMeanings = new HashSet();
		
		/*
		Now, loop through each line in the MR. The way this works is that
		the outer loop goes over each meaning, and contains an inner loop
		that reads in the lines that define that meaning.
		*/
		while(r.peek() != null) {
			String meaningHeader = r.readLine();
			/*
			Check if the next line is "abstract" or "meaning", denoting the start of a new meaning definition.
			If not, something's gone wrong.
			*/
			if ((meaningHeader.equals("abstract") == false) && (meaningHeader.equals("meaning") == false)) {
				throw new UnexpectedLineInVocabularyException(meaningHeader, "'abstract' or 'meaning'", r.getLineNumber());
			}
			
			/*
			Check if the meaning is a root one.
			*/
			boolean isRoot = (r.peek().startsWith("root"));
			
			if (isRoot) {
				r.readLine(); //consume that line
			}
			
			/*
			Create a new VocabularyMeaning and add it to the pool of all meanings.
			*/
			VocabularyMeaning newMeaning = new VocabularyMeaning(meaningHeader.equals("abstract"), isRoot);
			allMeanings.add(newMeaning);
			
			/*
			Process info Strings.
			*/
			String infoLineParts[] = StringFunctions.splitLineAlongSpace(r.peek());
			while (infoLineParts[0].endsWith("_info")) {
				newMeaning.setInfoString(infoLineParts[1], infoLineParts[0].substring(0, infoLineParts[0].length() - 5));
				r.readLine();
				infoLineParts = StringFunctions.splitLineAlongSpace(r.peek());
			}
			
			/*
			Create a HashMap for the meaning to map languages to type names 
			*/
			HashMap nameMap = new HashMap();
			
			/*
			Here we now loop through the lines specifying what the meaning is called in each language.
			Any line that does not start with a keyword (abstract, meaning, super, field) is assumed
			to be a language/meaning name combination.
			*/
			while((r.peek() != null) && (r.peek().startsWith("abstract") == false) && (r.peek().startsWith("meaning") == false) && (r.peek().startsWith("super") == false) && (r.peek().startsWith("field") == false)) {
				//split the line
				String lineParts[] = StringFunctions.splitLineAlongSpace(r.readLine());
				//the first part of the line should be the language code, giving us the name-to-meaning map for that language
				HashMap lMap = (HashMap) languageMap.get(lineParts[0]);
				if (lMap == null) {
					String currentLineNumber = Integer.toString(r.getLineNumber());
					throw new VocabularyFileException("Vocabulary file, line " + currentLineNumber + ":\nThe language " + lineParts[0] + " is not listed in the header.");
				}
				//put this meaning into the name-to-meaning map for the language
				lMap.put(lineParts[1], newMeaning);
				//put the name of this meaning into the language-to-name map for this meaning
				nameMap.put(lineParts[0], lineParts[1]);
			}
			
			//give the meaning its language to name map
			newMeaning.setLanguageToNameMap(nameMap);
			
			//if the meaning has extra data, it is given the reader
			if ((r.peek() != null) && ((r.peek().startsWith("super")) || (r.peek().startsWith("field")))) {
				newMeaning.define(r);
			}
		}
		
		//close the reader
		r.close();

		//loop through the meanings to link them
		Iterator meaningIter = allMeanings.iterator();
		while (meaningIter.hasNext()) {
			((VocabularyMeaning) meaningIter.next()).link();
		}
	}
	
	/**
	 * This method looks up a term in a given language.
	 *
	 * @param term The term to look up.
	 * @param language The language the term is in.
	 * @return The requested Meaning.
	 * @throws VocabularyServerException
	*/
	public static VocabularyMeaning lookupMeaning(String term, String language, String errorLocation) {
		/*
		First we get the map for the given language, which maps from names in that language to vocabulary meanings.
		We then ask it for the meaning, and if there is none, throw an exception.
		*/
		HashMap meaningMap = (HashMap) languageMap.get(language);
		if (meaningMap == null) {
			throw new VocabularyServerException(errorLocation + "Language '" + language + "' not found!");
		}
		VocabularyMeaning result = (VocabularyMeaning) meaningMap.get(term);
		if (result == null) {
			throw new VocabularyServerException(errorLocation + "The type '" + term + "' could not be found in the language '" + language + "'.");
		}
		return result;
	}
	
	/**
	 * Looks up a meaning and simply returns null if not found.
	 *
	 * @param term The term to look up.
	 * @param language The language the term is in.
	 * @return The requested Meaning or null if none found.
	 * @throws VocabularyServerException
	*/
	public static VocabularyMeaning lookupMeaningNullOnFail(String term, String language) {
		HashMap meaningMap = (HashMap) languageMap.get(language);
		if (meaningMap == null) {
			throw new VocabularyServerException("Language " + language + " not found!");
		}
		return (VocabularyMeaning) meaningMap.get(term);
	}
	
	/**
	 * Tests whether the given language exists.
	 *
	 * @param language the name of the language
	 * @return whether the language is listed
	*/
	public static boolean languageAvailable(String language) {
		return (languageMap.get(language) != null);
	}
	
	/**
	 * Gets all non-abstract subtypes of a type.
	*/
	public static ArrayList getAllSubtypes(VocabularyMeaning type) {
		ArrayList result = new ArrayList();
		Iterator mIter = allMeanings.iterator();
		while (mIter.hasNext()) {
			VocabularyMeaning m = (VocabularyMeaning) mIter.next();
			if ((m.isAbstract() == false) && (m.isMeOrSuper(type))) {
				result.add(m);
			}
		}
		return result;
	}
	
	/**
	 * Gets all root types.
	*/
	public static ArrayList getRootTypes() {
		ArrayList result = new ArrayList();
		Iterator mIter = allMeanings.iterator();
		while (mIter.hasNext()) {
			VocabularyMeaning m = (VocabularyMeaning) mIter.next();
			if ((m.isAbstract() == false) && (m.isRoot())) {
				result.add(m);
			}
		}
		return result;
	}
	
	
	

	//PRETTYPRINTING:	
	
	/**
	 * Generates a graph of the typing, displayed in a given language.
	 *
	 * @param language the language to display the graph in
	 * @return a .dot file of the graph
	*/
	public static String typingGraph(String language) {
		String result = "digraph \"TYPING-" + language + "\" {\n";
		
		HashMap meaningMap = (HashMap) languageMap.get(language);
		
		//create the nodes
		Iterator i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String ID = new Integer(((VocabularyMeaning) meaningMap.get(key)).iD).toString();
			result = result + "\"" + ID + "\" [label=\"" + key + "\"];\n";
		}
		
		//create the links
		i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			VocabularyMeaning m = (VocabularyMeaning) meaningMap.get(key);
			VocabularyMeaning superMeaning = m.supertype();
			if (superMeaning != null) {
				String ID = new Integer(m.iD).toString();
				String superID = new Integer(superMeaning.iD).toString();
				result = result + "\"" + ID + "\" -> \"" + superID + "\";\n";
			}
		}
		
		return result + "}";
	}
	
	/**
	 * Generates a graph of what meanings have what fields.
	 *
	 * @param language the language to display the graph in
	 * @return a .dot file of the graph
	*/
	public static String fieldGraph(String language) {
		String result = "digraph \"FIELDS-" + language + "\" {\n";
		
		HashMap meaningMap = (HashMap) languageMap.get(language);
		
		//create the nodes
		Iterator i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String ID = new Integer(meaningMap.get(key).hashCode()).toString();
			result = result + "\"" + ID + "\" [label=\"" + key + "\"];\n";
		}
		
		//create the links
		i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			VocabularyMeaning m = (VocabularyMeaning) meaningMap.get(key);
			String ID = new Integer(m.hashCode()).toString();
			//acquire the children and loop through them
			HashMap children = m.fieldsMap(language);
			if (children != null) {
				Iterator childrenIter = children.keySet().iterator();
				while (childrenIter.hasNext()) {
					String fieldLabel = (String) childrenIter.next();
					String childID = new Integer(((MeaningField) children.get(fieldLabel)).getType().hashCode()).toString();
					result = result + "\"" + childID + "\" -> \"" + ID + "\" [label=\"" + fieldLabel + "\"];\n";
				}
			}
		}
		
		return result + "}";
	}
	
	/**
	 * Generates a graph of what meanings have what fields, and of the typing.
	 *
	 * @param language the language to display the graph in
	 * @return a .dot file of the graph
	*/
	public static String fieldAndTypingGraph(String language) {
		String result = "digraph \"FIELDSANDTYPING-" + language + "\" {\n";
		
		HashMap meaningMap = (HashMap) languageMap.get(language);
		
		//create the nodes
		Iterator i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String ID = new Integer(meaningMap.get(key).hashCode()).toString();
			result = result + "\"" + ID + "\" [label=\"" + key + "\"];\n";
		}
		
		//create the links
		i = meaningMap.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			VocabularyMeaning m = (VocabularyMeaning) meaningMap.get(key);
			String ID = new Integer(m.hashCode()).toString();
			VocabularyMeaning superMeaning = m.supertype();
			//display typing
			if (superMeaning != null) {
				String superID = new Integer(superMeaning.iD).toString();
				result = result + "\"" + ID + "\" -> \"" + superID + "\" [style=\"dotted\"];\n";
			}
			//acquire the children and loop through them
			HashMap children = m.fieldsMap(language);
			if (children != null) {
				Iterator childrenIter = children.keySet().iterator();
				while (childrenIter.hasNext()) {
					String fieldLabel = (String) childrenIter.next();
					String childID = new Integer(((MeaningField) children.get(fieldLabel)).getType().hashCode()).toString();
					result = result + "\"" + childID + "\" -> \"" + ID + "\" [label=\"" + fieldLabel + "\"];\n";
				}
			}
		}
		
		return result + "}";
	}
}
