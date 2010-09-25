import java.util.HashMap;

/**
 * Serves LanguageRuleServers when given a language code.
 *
 * Remember:
 * LanguageServer contains LanguageRuleServers contains MeaningRuleSets contains MeaningRules.
*/
public class LanguageServer {

/*
Hashmap of all LanguageRuleServers.
*/
private static HashMap nameToRuleServer = new HashMap();

/**
 * Returns a language rule server for the given language identifier string. (en, de, etc.)
 * If the language's server does not exist yet, attempts to load it from file.
 *
 * @param languageName the code of the language, such as en or de
 * @return the language rule server for that language
*/
public static LanguageRuleServer getLanguageRuleServer(String languageName) {
	if (nameToRuleServer.containsKey(languageName)) {
		return (LanguageRuleServer) nameToRuleServer.get(languageName);
	} else {
		LanguageRuleServer newServer = new LanguageRuleServer(languageName);
		nameToRuleServer.put(languageName, newServer);
		return newServer;
	}
}

}
