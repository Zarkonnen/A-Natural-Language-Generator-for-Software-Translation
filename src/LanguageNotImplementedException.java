/**
 * Thrown if a language is mentioned that is not implemented as an input.
 *
 * @author David Stark
 * @version 2006-02-16
*/
public class LanguageNotImplementedException extends RuntimeException {
	
	private String language;
	
	/**
	 * Constructor.
	 *
	 * @param language the language that is not implemented
	*/
	public LanguageNotImplementedException(String language) {
		this.language = language;
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String getMessage() {
		return "The language " + language + " is not an available input language. Note that the first line in an input file must always be the input language code.";
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String toString() {
		return getMessage();
	}
}
