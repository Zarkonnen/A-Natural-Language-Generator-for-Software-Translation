/**
 * The purpose of this class is to allow the generator to
 * output detailed messages detailing each step in the execution if so wished.
 *
 * @author David Stark
 * @version 2006-02-09
*/

public class DebugTracer {

private static boolean doDebug;
private static int recursionLevel;

/**
 * Static method for setting the doDebug boolean.
 *
 * @param doDebug the value to set dDebug to
*/
public static void setDoDebug(boolean doDebugValue) {
	doDebug = doDebugValue;
}

/**
 * Static method for querying the value of doDebug.
 *
 * @return whether debug trace information should be printed
*/
public static boolean doDebug() {
	return doDebug;
}

/**
 * Resets the current recursion level.
*/
public static void resetRL() {
	recursionLevel = 0;
}

/**
 * Increments the current recursion level.
*/
public static void incRL() {
	recursionLevel++;
}

/**
 * Decrements the current recursion level.
*/
public static void decRL() {
	recursionLevel--;
}

/**
 * Outputs as many space characters as the current recursion level.
*/
public static String getIndent() {
	StringBuffer indent = new StringBuffer();
	indent.ensureCapacity(recursionLevel);
	int i = 1;
	while (i <= recursionLevel) {
		indent.append("\t");
		i++;
	}
	return new String(indent);
}

}
