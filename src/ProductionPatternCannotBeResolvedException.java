/**
 * Is thrown when following a pattern fails.
*/

public class ProductionPatternCannotBeResolvedException extends RuntimeException {
	
	private String message;
	
	/**
	 * Constructor.
	 *
	 * @param pattern The pattern that cannot be resolved.
	 * @param location the file and line the pattern is in
	*/
	public ProductionPatternCannotBeResolvedException(String pattern, String location) {
		message = location + "The pattern " + pattern + " failed to match.";
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
