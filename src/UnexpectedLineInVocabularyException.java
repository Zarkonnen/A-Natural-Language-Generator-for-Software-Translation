/**
 * This exception is used to notify that an unexpected line
 * turned up when parsing the vocabulary file.
 *
 * @author David Stark
 * @version 2005-11-24
*/
public class UnexpectedLineInVocabularyException extends RuntimeException {

	private String theLine;
	private String expectedLines;
	private int lineNumber;
	
	/**
	 * The constructor.
	 *
	 * @param line the line that was encountered
	 * @param expected a description of what was expected
	 * @param lineNumber the line on which the error was encountered
	*/
	public UnexpectedLineInVocabularyException(String line, String expected, int lineNumber) {
		theLine = line;
		expectedLines = expected;
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String getMessage() {
		return "Vocabulary file, line " + Integer.toString(lineNumber) + ":\nThe line '" + theLine + "' was encountered in the Vocabulary File. Expected was:" + expectedLines + ".";
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String toString() {
		return getMessage();
	}
}
