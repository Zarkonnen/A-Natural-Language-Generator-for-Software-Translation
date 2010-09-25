import java.io.BufferedReader;

/**
 * This is a line reader designed for use in the reading-in phase of
 * the generator. It supports peeking ahead for the next line, and
 * ignores empty lines.
 *
 * @author David Stark
 * @version 2005-11-25
*/
public class PeekingLineReader {

	private BufferedReader r;
	private String nextLine;
	private boolean discardTabs;
	private boolean nullOnEmpty;
	private int lineNumber;
	private int nextLineNumber;
	
	/**
	 * The constructor. Takes a bufferedreader to read lines from.
	 *
	 * @param reader The bufferedreader to read from.
	 * @param discardTabs whether to discard tabs
	 * @throws InvalidBufferedReaderException
	*/
	public PeekingLineReader(BufferedReader reader, boolean discardTabs, boolean nullOnEmpty) {
		this.discardTabs = discardTabs;
		this.nullOnEmpty = nullOnEmpty;
		r = reader;
		nextLine = getNextLine();
		lineNumber = 0;
	}
	
	/**
	 * Returns the next non-empty line from the bufferedReader, or null.
	 *
	 * @return the next line
	 * @throws InvalidBufferedReaderException
	*/
	private String getNextLine() {
		lineNumber = nextLineNumber;
		String nextLine = null;
		try {
			if (nullOnEmpty) {
				nextLine = r.readLine();
				nextLineNumber++;
				
				if ((nextLine != null) && (nextLine.equals(""))) {
					nextLine = null;
				}
				
			} else {
				do {
					nextLine = r.readLine();
					nextLineNumber++;
				} while ((nextLine != null) && (nextLine.equals("")));
			}
		}
		catch (java.io.IOException e) {
			throw new InvalidBufferedReaderException(e);
		}
		
		if (nextLine == null) {
			return nextLine;
		} else {
			if (discardTabs) {
				return nextLine.replaceAll("\t", "");
			} else {
				return nextLine;
			}
		}
	}
	
	/**
	 * Reads the next line, ignoring empty lines.
	 *
	 * @return the next line in the buffer
	 * @throws InvalidBufferedReaderException
	*/
	public String readLine() {
		String currentLine = nextLine;
		nextLine = getNextLine();
		return currentLine;
	}
	
	/**
	 * Peeks ahead for the next line, ignoring empty lines.
	 * This does not cause the line to be consumed.
	 *
	 * @return the next line in the buffer
	 * @throws InvalidBufferedReaderException
	*/
	public String peek() {
		return nextLine;
	}
	
	/**
	 * Closes the input stream.
	 *
	 * @throws VocabularyFileStreamClosingException
	*/
	public void close() {
		try {
			r.close();
		}
		catch (java.io.IOException e) {
			throw new VocabularyFileStreamClosingException();
		}
	}
	
	/**
	 * For finding out which line we're currently on.
	 *
	 * @return the line number
	*/
	public int getLineNumber() {
		return lineNumber;
	}

}
