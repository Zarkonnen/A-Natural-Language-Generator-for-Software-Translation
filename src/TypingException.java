/**
 * Is thrown when a MR field is being filled with a child of an incorrect type.
*/

public class TypingException extends RuntimeException {
	
	private String message;
	
	/**
	 * Constructor.
	 *
	 * @param message The problem.
	*/
	public TypingException(String message) {
		this.message = message;
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
