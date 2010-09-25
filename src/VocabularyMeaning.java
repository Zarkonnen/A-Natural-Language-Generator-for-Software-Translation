import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

/**
 * Objects of this class describe a type in the vocabulary.
 * This includes information about the supertype, if it exists
 * and lookup functions for the type's fields.
 *
 * Do not confuse this with LanguageMeaning which collects the
 * production rules for a given VocabularyMeaning and language.
 * Or MRNode, a specific node in a meaning representation that
 * is an "instance" of a VocabularyMeaning.
 *
 * Note: There is an unfortunate dual terminology in use here:
 * Objects of this class are called both "Vocabulary Meanings"
 * and "types".
 * 
 * @author David Stark
 * @version 2006-03-28
*/
public class VocabularyMeaning {

	private boolean isAbstract;
	private boolean isRoot;
	private VocabularyMeaning superType;
	private HashMap languageToLabelToFieldMapMap;
	private ArrayList definitionEntries;
	private HashMap languageToNameMap;
	private HashMap languageToInfoMap;
	
	private static int iDcounter = 0; //for unique IDs.
	public int iD;
	
	/**
	For error tracking. This int defines which line of the vocabulary file
	the definition of this VM started at. This is then reported in exceptions
	thrown by this VM.
	*/
	private int startOfDefinitionLine;

	/**
	 * The constructor. Only needs to know if this is abstract or not.
	 *
	 * Note: abstract types are types that cannot be instantiated into
	 * terms. Though in practice, the generator currently does not
	 * check for this.
	 *
	 * @param isAbstract whether this is an abstract meaning
	 * @param isRoot whether this meaning or its subtypes can be used as root
	*/
	public VocabularyMeaning(boolean isAbstract, boolean isRoot) {
		this.isAbstract = isAbstract;
		this.isRoot = isRoot;
		definitionEntries = new ArrayList();
		languageToLabelToFieldMapMap = new HashMap();
		
		iD = iDcounter;
		iDcounter++;
	}
	
	/**
	 * Sets the passed hashmap as the language-to-name map.
	 * This hashmap allows the meaning to know its own names
	 * in various languages.
	 *
	 * @param map the hashmap
	*/
	public void setLanguageToNameMap(HashMap map) {
		languageToNameMap = map;
	}
	
	/**
	 * Returns the name of this type / VocabularyMeaning in the given language.
	 *
	 * @param language the language the name should be in
	 * @return the name of this meaning-type
	 * @throws LanguageNotImplementedException
	*/
	public String getName(String language) {
		if (languageToNameMap.containsKey(language)) {
			return (String) languageToNameMap.get(language);
		} else {
			throw new LanguageNotImplementedException(language);
		}
	}
	
	/**
	 * Returns an unique HashCode. Used for drawing field and typing graphs.
	*/
	public int hashCode() {
		return iD;
	}

	/**
	 * Uses the given peeking line reader to read off the data
	 * about itself. Not a constructor since the meaning needs
	 * to exist before this so it can be inserted appropriately.
	 * The definition lines are just read in, but not processed
	 * until link() is called.
	 *
	 * @param input the reader from which to read its definition
	*/
	public void define(PeekingLineReader input) {
		startOfDefinitionLine = input.getLineNumber() + 1;
		while ((input.peek() != null) && (input.peek().equals("meaning") == false) && (input.peek().equals("abstract") == false)) {
			definitionEntries.add(input.readLine());
		}
	}
	
	/**
	 * Tells this type / VM to read its definition lines, instantiating
	 * its fields and linking itself to its supertype.
	*/
	public void link() {
		/*
		The plan:
		First off, split the lines given into smaller chunks.
		The first chunk should be the supertype definition.
		Then have an arraylist of definitions for each field.
		Process the supertype definition.
		Then loop through the field definition arraylist:
			create a field, and a name map for it
			loop through the lines of the field definition:
				if it's a name, test if the map of that language already exists, if not, create it
					put that name into the language map, and into the name map for the field
				if it's a type, set the field to it
			set the field's name map
		.
		*/
	
	
		Iterator defIter = definitionEntries.iterator();
		if (defIter.hasNext()) {
			//define arraylists for holding the super and field definitions
			ArrayList superTypeDefinition = new ArrayList();
			ArrayList fieldDefinitions = new ArrayList();
			
			//get the first line
			String firstLine = (String) defIter.next();
						
			//check if the first line is "super"
			if (firstLine.equals("super")) {
				while (defIter.hasNext()) {
					String entry = (String) defIter.next();
					if (entry.equals("field")) {
						break; //finished with defining super
					} else {
						superTypeDefinition.add(entry);
					}
				}
			}
			
			//check if there's more to come
			if (defIter.hasNext()) {
				//split off the lines for fields now
				ArrayList currentFieldDefinition = new ArrayList();
				fieldDefinitions.add(currentFieldDefinition);
				while(defIter.hasNext()) {
					String entry = (String) defIter.next();
					if (entry.equals("field")) {
						currentFieldDefinition = new ArrayList();
						fieldDefinitions.add(currentFieldDefinition);
					} else {
						currentFieldDefinition.add(entry);
					}
				} //end looping through field def lines
			} //end checking for field defs
			
			//now process the blocks
			//first, define the supertype
			Iterator superIter = superTypeDefinition.iterator();
			while (superIter.hasNext()) {
				String lineParts[] = StringFunctions.splitLineAlongSpace((String) superIter.next());
				VocabularyMeaning aSuperType = VocabularyServer.lookupMeaning(lineParts[1], lineParts[0], "Vocabulary file, after line " + Integer.toString(startOfDefinitionLine) + ":\n");
				if ((superType != null) && (superType != aSuperType)) {
					throw new VocabularyFileException("Vocabulary file, after line " + Integer.toString(startOfDefinitionLine) + ":\nThe supertype " + lineParts[1] + " in " + lineParts[0] + " does not match with previous supertype values.");
				}
				superType = aSuperType;
			}
			
			//now, loop through the field definitions
			Iterator fieldDefIter = fieldDefinitions.iterator();
			while (fieldDefIter.hasNext()) {
				MeaningField currentMF = new MeaningField();
				HashMap languageToName = new HashMap();
				currentMF.setLanguageToNameMap(languageToName);
				ArrayList currentFieldDef = (ArrayList) fieldDefIter.next();
				Iterator lineIter = currentFieldDef.iterator();
				while (lineIter.hasNext()) {
					//read in the line and split it right away.
					String lineParts[] = StringFunctions.splitLineAlongSpace((String) lineIter.next());
					if (lineParts[0].endsWith("_info")) {
						currentMF.setInfoString(lineParts[1], lineParts[0].substring(0, lineParts[0].length() - 5));
					} else {
						if (lineParts[0].endsWith("_type")) {
							currentMF.setType(lineParts[1], lineParts[0].substring(0, lineParts[0].length() - 5), startOfDefinitionLine);
						} else {
							//part 0 is the language of the field label, part 1 is the field label
							if (VocabularyServer.languageAvailable(lineParts[0]) == false) {
								throw new VocabularyFileException("Vocabulary file, after line " + Integer.toString(startOfDefinitionLine) + ":\nThe language '" + lineParts[0] + "' is not listed as one of the implemented languages.");
							}
							languageToName.put(lineParts[0], lineParts[1]);
							//now add it to our local hashmap
							if (languageToLabelToFieldMapMap.containsKey(lineParts[0])) {
								((HashMap) languageToLabelToFieldMapMap.get(lineParts[0])).put(lineParts[1], currentMF);
							} else {
								//we don't have a hashmap for that language yet, so let's create it.
								HashMap newLabelToFieldMap = new HashMap();
								newLabelToFieldMap.put(lineParts[1], currentMF);
								languageToLabelToFieldMapMap.put(lineParts[0], newLabelToFieldMap);
							}
						}
					}
				}
			}
		} //end checking for entries
	} //end function

	/**
	 * Returns the field named. If the given field does not exist, throws an exception.
	 * 
	 * @param label the label of the requested field
	 * @param labelLanguage the language this label is in
	 * @param errorLocation the file and line from which this method was called
	 * @return the field named
	*/
	public MeaningField getField(String label, String labelLanguage, String errorLocation) {
		/*
		First, we have to find the hashmap for mapping label names to fields. For this, we look at the
		languageToLabelToFIeldMapMap. (great name, innit?)
		If we can't find such a map, this may simply because this VM doesn't have any fields, so we
		try at the supertype. If there is not supertype, the assumption is that we were supplied
		with an incorrect language code. So we throw an exception.
		*/
	
		HashMap labelToField = (HashMap) languageToLabelToFieldMapMap.get(labelLanguage);
		if (labelToField == null) {
			if (superType != null) {
				return superType.getField(label, labelLanguage, errorLocation);
			} else {
				throw new VocabularyServerException(errorLocation + "The language '" + labelLanguage + "' could not be found while trying to find a field called '" + label + "'.");
			}
		}
		
		/*
		Provided we have found a hashmap, we now look up the label in it, hopefully getting a
		MeaningField. If not, we try recursing up to the supertype. If that doesn't work, we 
		assume that we were given the label of a nonexistant field, and throw an exception.
		*/
		MeaningField result = (MeaningField) labelToField.get(label);
		if (result == null) {
			if (superType != null) {
				return superType.getField(label, labelLanguage, errorLocation);
			} else {
				throw new VocabularyServerException(errorLocation + "The field labelled '" + label + "' in the language '" + labelLanguage + "' could not be found.");
			}
		}
		
		return result;
	}
	
	
	/**
	 * Returns the field named. If the given field does not exist, returns null.
	 * This method is for use in path following, where the paths may have
	 * references to fields that do not exist in some VMs.
	 * 
	 * @param label the label of the requested field
	 * @param labelLanguage the language this label is in
	 * @return the field named
	*/
	public MeaningField getFieldNullOnFail(String label, String labelLanguage) {
		/*
		First, we have to find the hashmap for mapping label names to fields. For this, we look at the
		languageToLabelToFIeldMapMap. (great name, innit?)
		If we can't find such a map, this may simply because this VM doesn't have any fields, so we
		try at the supertype. If there is not supertype, the assumption is that we were supplied
		with an incorrect language code. So we just return null.
		*/
	
		HashMap labelToField = (HashMap) languageToLabelToFieldMapMap.get(labelLanguage);
		if (labelToField == null) {
			if (superType != null) {
				return superType.getFieldNullOnFail(label, labelLanguage);
			} else {
				return null;
			}
		}
		
		/*
		Provided we have found a hashmap, we now look up the label in it, hopefully getting a
		MeaningField. If not, we try recursing up to the supertype. If that doesn't work, we 
		assume that we were given the label of a nonexistant field, and return null.
		*/
		MeaningField result = (MeaningField) labelToField.get(label);
		if (result == null) {
			if (superType != null) {
				return superType.getFieldNullOnFail(label, labelLanguage);
			} else {
				return null;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Returns a whole HashMap of all fields in a given language.
	 * May return null if there are no fields.
	 *
	 * @param language the language you want the fields to be labelled in
	 * @return a hashmap of labels to fields
	*/
	public HashMap fieldsMap(String language) {
		return (HashMap) languageToLabelToFieldMapMap.get(language);
	}
	
	/**
	 * Compiles a HashSet of all fields in this VM and its supers.
	 *
	 * @param language the language used to look up the fields. Technically unnecessary.
	 * @return a hashset of fields
	*/
	public HashSet allFieldsSet(String language) {
		HashMap langMap = (HashMap) languageToLabelToFieldMapMap.get(language);
		if (superType == null) {
			if (langMap == null) {
				return new HashSet();
			} else {
				return new HashSet(langMap.values());
			}
		} else {
			if (langMap == null) {
				return superType.allFieldsSet(language);
			} else {
				HashSet resultSet = new HashSet(langMap.values());
				resultSet.addAll(superType.allFieldsSet(language));
				return resultSet;
			}
		}
	}
	
	/**
	 * Returns the supertype of this meaning, if there is one.
	 * If there is not, returns null.
	 *
	 * @return the supertype
	*/
	public VocabularyMeaning supertype() {
		return superType;
	}
	
	/**
	 * Checks whether the given VocabularyMeaning is either this one or one of its
	 * supertypes. If the given type is the *ANY special value, instantly
	 * returns true.
	 *
	 * @param type the VocabularyMeaning to check agains
	 * @return whether it matches
	*/
	public boolean isMeOrSuper(VocabularyMeaning type) {
		if (type == MeaningField.IS_ANY) {
			if (isAbstract) {
				return false;
			}
			//if (superType == null) {
				return true;
			//} else {
			//	return superType.isMeOrSuper(type);
			//}
		}
		if (this == type) {
			return true;
		}
		if (superType != null) {
			return superType.isMeOrSuper(type);
		}
		return false;
	}
	
	/**
	 * Returns whether this meaning is abstract.
	*/
	public boolean isAbstract() {
		return isAbstract;
	}
	
	/**
	 * Returns whether this meaning or its supertype is root.
	*/
	public boolean isRoot() {
		if (isRoot) {
			return true;
		}
		if (superType != null) {
			return superType.isRoot();
		}
		return false;
	}
	
	/**
	 * Sets the information string in the given language.
	*/
	public void setInfoString(String info, String language) {
		if (languageToInfoMap == null) {
			languageToInfoMap = new HashMap();
		}
		languageToInfoMap.put(language, info);
	}
	
	/**
	 * Retrieves the information string in a given language. Returns an empty string if no info found.
	*/
	public String getInfoString(String language) {
		if (languageToInfoMap == null) {
			return "";
		}
		if (languageToInfoMap.get(language) == null) {
			return "";
		}
		return (String) languageToInfoMap.get(language);
	}
	

}
