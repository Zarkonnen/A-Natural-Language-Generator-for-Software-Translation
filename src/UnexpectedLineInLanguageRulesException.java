/**
 * This exception is used to notify that an unexpected line
 * turned up when parsing a language rules file.
 *
 * @author David Stark
 * @version 2006-03-27
*/
public class UnexpectedLineInLanguageRulesException extends RuntimeException {

	private String theLine;
	private String expectedLines;
	private String language;
	private String lineNumber;
	
	/**
	 * The constructor.
	 *
	 * @param language the language the rules are for
	 * @param line the line that was encountered
	 * @param expected a description of what was expected
	 * @param lineNumber the line number the error occurred on
	*/
	public UnexpectedLineInLanguageRulesException(String language, String line, String expected, int lineNumber) {
		this.language = language;
		theLine = line;
		expectedLines = expected;
		this.lineNumber = Integer.toString(lineNumber);
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String getMessage() {
		return "Rules file for " + language + ", line " + lineNumber + ": The line '" + theLine + "' was encountered. Expected was:" + expectedLines + ".";
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String toString() {
		return getMessage();
	}
}
