import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.net.URL;
/**
 * The main class of the generator. It receives as input a meaning representation (MR)
 * and a code for the language the MR should be compiled to. It then loads the meaning
 * vocabulary (MV) and uses it to interpret the MR, loads the language rules (LR) for
 * the given language and uses those rules to emit the requested text.
 *
 * @author David Stark
 * @version 2006-04-08
*/
public class Generator {


	/**
	 * The main method of the generator.
	 *
	 * It takes three different types of argument:
	 * The first type are target languages. They are of the form
	 * "-languagecode".
	 * The second are source files, of the form "path/to/file".
	 * The third are debugger flags, of the form "--debugger".
	 * The following debugger flags are supported:
	 * --vocabularygraph prints out a .dot file of the fields and typing of the vocabulary in the given languages and then exits
	 * --rulesgraph prints out a .dot file of the rule typing in the given languages and then exits
	 * --inputgraph prints out a .dot file of the input in the given languages and then exits
	 *
	 * If there are no input files supplied, input is taken from stdin. Input is terminated by an empty line.
	 * If there are no target languages supplied, all possible target languages are compiled to.
	*/
    public static void main (String args[]) {
	
		ArrayList sourceFiles = new ArrayList();
		ArrayList targetLanguages = new ArrayList();
		ArrayList extraFlags = new ArrayList();
		
		extraFlags.add("gui"); //default to GUI

		sourceFiles.add("input.txt");
		
		/* loop through the arguments and sort them into their buckets */
		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith("--")) {
				extraFlags.add(args[i].substring(2));
			} else {
				if (args[i].startsWith("-")) {
					targetLanguages.add(args[i].substring(1));
				} else {
					sourceFiles.add(args[i]);
				}
			}
		
			i++;
		}
		
		/*
		First step, if the set of output languages is empty, fill it with all possible ones!
		*/
		if (targetLanguages.size() == 0) {
				targetLanguages = getAllLanguages();
		}
						
		//Tell the VocabularyServer to load and link the MV.
		VocabularyServer.loadAndLinkVocabulary();
		
		/*
		--vocabularygraph
		*/
		if (extraFlags.contains("vocabularygraph")) {
			Iterator iter = targetLanguages.iterator();
			while (iter.hasNext()) {
				System.out.println(VocabularyServer.fieldAndTypingGraph((String) iter.next()) + "\n\n");
			}
			
			return;
		}
		
		/*
		--rulesgraph
		*/
		if (extraFlags.contains("rulesgraph")) {
			Iterator iter = targetLanguages.iterator();
			while (iter.hasNext()) {
				LanguageRuleServer lr = LanguageServer.getLanguageRuleServer((String) iter.next());
				System.out.println(lr.graph() + "\n\n");
			}
			
			return;
		}
		
		
		/*
		--debugtrace
		*/
		if (extraFlags.contains("debugtrace")) {
			DebugTracer.setDoDebug(true);
		}
		
		/*
		Now load the input files into MRs.
		*/
		ArrayList mrs = new ArrayList();
		if ((sourceFiles.size() == 0) && (extraFlags.contains("gui") == false)) {
			mrs.add(new MeaningRepresentation()); //read from stdin
		} else {
			Iterator iter = sourceFiles.iterator();
			while (iter.hasNext()) {
				mrs.add(new MeaningRepresentation((String) iter.next()));
			}
		}
		
		/*
		--gui
		*/
		if (extraFlags.contains("gui")) {
			if (mrs.size() == 0) {
				GeneratorGUI gg = new GeneratorGUI();
				gg.setVisible(true);
			} else {
				GeneratorGUI gg = new GeneratorGUI((MeaningRepresentation) mrs.get(0));
				gg.setVisible(true);
			}
			return;
		}
		
		/*
		--inputgraph
		*/
		if (extraFlags.contains("inputgraph")) {
		Iterator mrIter = mrs.iterator();
			while (mrIter.hasNext()) {
				MeaningRepresentation mr = (MeaningRepresentation) mrIter.next();
				Iterator langIter = targetLanguages.iterator();
				while (langIter.hasNext()) {
					System.out.println(mr.graph((String) langIter.next()));
				}
			}
			
			return;
		}
		
		/*
		Now generate in the required languages.
		*/
		Iterator mrIter = mrs.iterator();
		while (mrIter.hasNext()) {
			MeaningRepresentation mr = (MeaningRepresentation) mrIter.next();
			Iterator langIter = targetLanguages.iterator();
			while (langIter.hasNext()) {
				try {
					System.out.println(mr.generate((String) langIter.next()));
				}
				catch (Exception e) {
					System.out.println("?");
				}
			}
		}
		
    }
	
	
	/*
	 * Looks up all implemented target languages.
	 *
	 * @return an arraylist of their language codes
	*/
	private static ArrayList getAllLanguages() {
		//find the data file
		File rulesFolder = new File(
			new File(
				new File(
					ClassLoader.getSystemResource("Generator.class").getPath().substring(5).replaceAll("\\%20", "\\ "))
				.getParentFile().getParentFile()
			, "data")
		, "languages");
							
		File files[] = rulesFolder.listFiles(new VisibleFilesOnly());
		
		ArrayList list = new ArrayList();
		int i = 0;
		while (i < files.length) {
			list.add(files[i].getName().substring(0, files[i].getName().indexOf(".")));
			i++;
		}
		
		return list;
	}
}
