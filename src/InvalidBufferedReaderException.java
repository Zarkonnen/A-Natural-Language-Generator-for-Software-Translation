/**
 * This exception is thrown when a BufferedReader fails in some manner.
 *
 * @author David Stark
 * @version 2005-11-24
*/
public class InvalidBufferedReaderException extends RuntimeException {
	
	private String message;
	
	/**
	 * A constructor that takes a previous exception and takes over its message.
	 *
	 * @param e The exception that triggered the throwing of this one.
	*/
	public InvalidBufferedReaderException(Exception e) {
		message = e.getMessage();
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
