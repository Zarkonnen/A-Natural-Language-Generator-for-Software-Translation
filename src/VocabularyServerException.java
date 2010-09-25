/**
 * An exception for generally whining about the vocabulary server.
 *
 * @author David Stark
 * @version 2005-11-25
*/
public class VocabularyServerException extends RuntimeException {
	
	private String message;
	
	/**
	 * Constructor.
	 *
	 * @param m The message the exception should deliver.
	*/
	public VocabularyServerException(String m) {
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
