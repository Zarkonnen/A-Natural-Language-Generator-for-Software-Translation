/**
 * A library for holding additional String functions.
 *
 * @author David Stark
 * @version 2005-11-24
*/
public class StringFunctions {
	/**
	 * Splits a line into two parts according to the first space.
	 *
	 * @param line the line to split
	 * @return an array of two strings
	*/
	public static String[] splitLineAlongSpace(String line) {
		String result[] = new String[2];
		int dividingLine = line.indexOf(" ");
		if (dividingLine == -1) {
			result[0] = line;
			result[1] = "";
		} else {
			result[0] = line.substring(0, dividingLine);
			result[1] = line.substring(dividingLine + 1);
		}
		return result;
	}
}
