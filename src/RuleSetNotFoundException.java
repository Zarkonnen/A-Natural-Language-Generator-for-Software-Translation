/**
 * Thrown if the production rule for some meaning cannot be found.
 *
 * @author David Stark
 * @version 2006-03-27
*/
public class RuleSetNotFoundException extends RuntimeException {
	
	private String language;
	private String meaningName;
	private String errorLocation;
	
	/**
	 * Constructor.
	*/
	public RuleSetNotFoundException(String language, String meaningName, String errorLocation) {
		this.language = language;
		this.meaningName = meaningName;
		this.errorLocation = errorLocation;
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String getMessage() {
		return errorLocation + "The VocabularyMeaning called " + meaningName + " has no rule set in the language " + language + ".";
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String toString() {
		return getMessage();
	}
}
