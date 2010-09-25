import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Stack;
import java.io.InputStreamReader;
import java.io.*;

/**
 * This class encapsulates a meaning representation, that is,
 * an user input that the generator will then translate into
 * some language.
 *
 * @author David Stark
 * @version 2005-11-25
*/
public class MeaningRepresentation {

	private MRNode rootNode;

	/**
	 * The constructor, which takes a file path to read the
	 * representation from.
	 *
	 * @param path the path of the file to read from
	 * @throws MeaningRepresentationFileNotFoundException
	*/
	public MeaningRepresentation(String path) {
		/*
		The plan is as follows.
		
		Acquire the file and cast a peeking reader on it.
		The first line is supposed to be a language. Abort if it isn't.
		The second line is the type of the root node. Put it in and push
		it onto a stack.
		For further lines, establish their stack level, and do accordingly.
		*/
		
		//find the data file
		File mrFile = new File(path);
				
		//attach a reader to it
		PeekingLineReader r = null;
		try {
			r = new PeekingLineReader(
				new BufferedReader(
					new FileReader(mrFile)
				)
			, false, false); //we *want* tabs, we *need* them!
			
			defineFromPeekingReader(r, path);
		}
		catch (java.io.FileNotFoundException e) {
			throw new MeaningRepresentationFileNotFoundException();
		}
	}
	
	/**
	 * Another constructor, which reads the MR from stdin.
	 *
	 * @throws MeaningRepresentationFileNotFoundException
	*/
	public MeaningRepresentation() {
		PeekingLineReader r = new PeekingLineReader(
				new BufferedReader(
					new InputStreamReader(System.in)
				)
			, false, true); //we *want* tabs, we *need* them!
			
			defineFromPeekingReader(r, "stdin");
	}
	
	/**
	 * A constructor for making an empty MR.
	*/
	public MeaningRepresentation(boolean empty) {
		rootNode = null;
	}
	
	/**
	 * A private method for actually reading in the MR from a
	 * PeekingLineReader either connected to stdin or a file.
	 *
	 * @param r the reader to read the MR from
	*/
	private void defineFromPeekingReader(PeekingLineReader r, String inputFileName) {
		/*
		This method loops through all the lines in an input file
		and defines a tree structure from it. It does this by
		keeping a stack of the terms / MRNodes we're currently in.
		So for example, after
		Statement
			Action eat
				Actor dog
		We have a stack of (dog, eat, Statement).
		*/
			
		/*
		First, determine in which language the MR is in.
		*/
		String language = r.readLine();
		if (VocabularyServer.languageAvailable(language) == false) {
			throw new LanguageNotImplementedException(language);
		}
		
		String errorLocation = "Input file " + inputFileName + ", line " + Integer.toString(r.getLineNumber()) + ":\n";
				
		/*
		Next, create the roon node from the first line, and initialise the stack with it at the top.
		*/
		rootNode = new MRNode(r.readLine(), language, null, errorLocation);
		Stack s = new Stack();
		s.push(rootNode);
		
		/*
		now iterate through the lines of the MR.
		We keep track of the number of tabs that prepend a line to determine which MRNode's field a line is in.
		For example, if we so far have had
		Statement
			Action eat
				Actor dog
					Number several
				Target fish
		Then by virtue of "Target fish" only having 2 tabs, we pop off "several" and "dog", and attach "fish" to "eat".
		*/
		while (r.peek() != null) {
			String line = r.readLine();
			int tabCount = 0;
			while (line.startsWith("\t")) {
				tabCount++;
				line = line.substring(1);
			}
			
			while (s.size() > tabCount) {
				s.pop();
			}
			
			/*
			Having figured out which node to attach the new line to, we split it, create it as a child of the current
			top stack node, and push it into the stack.
			*/
			String lineParts[] = StringFunctions.splitLineAlongSpace(line);
			errorLocation = "Input file " + inputFileName + ", line " + Integer.toString(r.getLineNumber()) + ":\n";
			s.push(((MRNode) s.peek()).addChild(lineParts[0], lineParts[1], language, errorLocation));
		}
	}
	
	/**
	 * Returns the root node of the meaning representation.
	 *
	 * @return the root node
	*/
	public MRNode getRootNode() {
		return rootNode;
	}
	
	/**
	 * Clears the root node.
	*/
	public void clearRootNode() {
		rootNode = null;
	}
	
	/**
	 * Sets the root node.
	*/
	public void setRootNode(MRNode root) {
		rootNode = root;
	}
	
	
	/**
	 * Does the generation.
	 *
	 * @param language the language to generate this MR in
	*/
	public String generate(String language) {
		LanguageRuleServer lr = LanguageServer.getLanguageRuleServer(language);
		MeaningRuleSet rootRules = lr.getRule(rootNode.getType(), "Input File, Initial Generation Request:\n");
		return rootRules.produce("*", rootNode);
	}
	
	/**
	 * Saves this MR to the specified file.
	 *
	 * @param file the file to which to save to
	*/
	public void saveTo(File f) {
		try {
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintStream out = new PrintStream(new FileOutputStream(f));
			out.println("en");
			out.println(rootNode.getType().getName("en"));
			rootNode.writeFieldsToStream(out, "en", "\t");
			out.close();
		}
		catch (Exception e) {
			System.err.println("Could not save file: " + e.toString());
		}
	}
	
	
	//PRETTYPRINTING
	
	
	/**
	 * Generates a graph of the MR.
	 *
	 * @param language the language the graph should be in
	 * @return a .dot file of the graph
	*/
	public String graph(String language) {
		String result = "graph MEANING_REPRESENTATION_" + language + " {\n";
		result = result + rootNode.graphNodes(language);
		return result + "}";
	}

}
