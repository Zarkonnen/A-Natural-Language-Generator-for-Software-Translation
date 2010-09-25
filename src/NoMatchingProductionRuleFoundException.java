/**
 * Is thrown when no production rule can be found.
*/

public class NoMatchingProductionRuleFoundException extends RuntimeException {
	
	private String message;
	
	/**
	 * Constructor.
	 *
	 * @param match the name of the rule sought
	 * @param nodeName the type of node looked in
	 * @param location the line and file where the error occurred
	*/
	public NoMatchingProductionRuleFoundException(String match, String nodeName, String location) {
		message = location + "No matching rule called " + match + " found in " + nodeName + ".";
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
