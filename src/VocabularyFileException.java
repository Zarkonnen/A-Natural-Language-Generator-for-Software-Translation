/**
 * An exception for generally whining about the vocabulary file, and format violations.
 *
 * @author David Stark
 * @version 2005-11-24
*/
public class VocabularyFileException extends RuntimeException {
	
	private String message;
	
	/**
	 * Constructor.
	 *
	 * @param m The message the exception should carry.
	*/
	public VocabularyFileException(String m) {
		message = m;
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns the message in this exception.
	*/
	public String toString() {
		return message;
	}
}
