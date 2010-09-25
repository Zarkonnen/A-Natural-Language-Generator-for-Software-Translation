import java.util.HashMap;

/**
 * This class represents a field in a given VocabularyMeaning / type.
 *
 * @author David Stark
 * @version 2006-03-28
*/
public class MeaningField {
	private VocabularyMeaning type;
	private HashMap languageToNameMap;
	public static VocabularyMeaning IS_STRING = new VocabularyMeaning(true, true);
	public static VocabularyMeaning IS_INT = new VocabularyMeaning(true, true);
	public static VocabularyMeaning IS_ANY = new VocabularyMeaning(true, true);
	private HashMap languageToInfoMap;
	
	/*
	error tracking
	*/
	private int fieldDefinitionStartLine;
	
	/**
	 * Sets the type of this meaning. Throws an error if it
	 * is given contradictory information.
	 *
	 * @param meaningName the name of the type of this field
	 * @param nameLanguage the language that name is in
	 * @throws VocabularyFileException
	*/
	public void setType(String meaningName, String nameLanguage, int lineNumber) {
		VocabularyMeaning newType = null;
	
		if (meaningName.equals("*STRING")) {
			newType = IS_STRING;
			} else {
			if (meaningName.equals("*INT")) {
				newType = IS_INT;
			} else {
				if (meaningName.equals("*ANY")) {
					newType = IS_ANY;
				} else {
					String errorLine = Integer.toString(lineNumber);
					newType = VocabularyServer.lookupMeaning(meaningName, nameLanguage, "Vocabulary file, after line " + errorLine + ":\n");
				}
			}
		}
		
		if (type == null) {
			type = newType;
		} else {
			if (type != newType) {
				String ln = Integer.toString(lineNumber);
				throw new VocabularyFileException("Vocabulary, after line " + ln + "\nWhile creating a field: the type '" + meaningName + "' in '" + nameLanguage + "' conflicts with the previously specified type.");
			}
		}
	}
	
	/**
	 * Returns the type of this field.
	*/
	public VocabularyMeaning getType() {
		return type;
	}
	
	/**
	 * Use this to set the hashmap from language to field name.
	 *
	 * @param map the hashmap
	*/
	public void setLanguageToNameMap(HashMap map) {
		languageToNameMap = map;
	}
	
	/**
	 * Returns the name of this meaning field in the given language.
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
	
	public boolean isString() {
		return (type == IS_STRING);
	}
	
	public boolean isInt() {
		return (type == IS_INT);
	}
	
	public boolean isLiteral() {
		return ((type == IS_STRING) || (type == IS_INT));
	}
	
	public boolean isAny() {
		return (type == IS_ANY);
	}
}
