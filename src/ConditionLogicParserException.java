/**
 * Is thrown when malformed logic is in a condition.
 * Things like A & | B, or !, or )A & B) | C.
 * Does not cover malformed pattern matching, as
 * the patterns are not evaluated during that
 * parse.
*/

public class ConditionLogicParserException extends RuntimeException {
	
	private String message;
	
	/**
	 * @param m The message
	*/
	public ConditionLogicParserException(String m) {
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
