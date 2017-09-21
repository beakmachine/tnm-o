package tnmClassifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.*;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.utils.FileSystemConnector;

/**
*
* @author Oliver Brunner
* Change SZ, 25.7.2017: in  getInstanceBuilder _ (Unterstrich dazu)
*                                      new    case "pancreas_7":            before:  case "pancreas7": 	
*                                                   pancreas_exocrine_8"
*                                                   pancreas_neuroendocrine_8
*    SZ, 28+29.7.2017: case "pancreas_bridge_7_8"		case "pancreas_swrl7to8_bridge_7_8":    
*    SZ, Aug 2017: swlr8 to 7                                    
*/
public class Main {
	public static void main(String[] args) throws OWLOntologyStorageException, FileNotFoundException, IOException, OWLOntologyCreationException {
		Options options = Main.createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }
        
        InputChecker checker = new InputChecker(cmd);
        if(!checker.inputIsValid()) {
        	System.out.println(checker.getError());
            System.exit(1);
            return;
        }
        
        Main.processHelpOption(cmd, formatter, options);
        Main.processListAllOption(cmd);
        
        if(!cmd.hasOption("list") && !cmd.hasOption("help") ) {
        	System.out.println("Classifying...");
 	        BaseClassifier classifier = Main.getInstanceBuilder(cmd.getOptionValue("version"), cmd.getOptionValue("organ"), cmd.getOptionValue("input"));        
	        System.out.println("Set up environment");    
	        classifier.setUp();
	        Map<String, List<String>>[] results = classifier.run();
	        System.out.println("Finished Classification --  Tear down environment");
	        classifier.tearDown();
	        
	        new DefaultOutput().output(results);
        }
    }
	
	private static Options createOptions() {
        Options options = new Options();

        Option listAll = new Option("l", "list", false, "List all available ontologies");
        listAll.setRequired(false);
        options.addOption(listAll);

        Option version = new Option("v", "version", true, "TNMO version (e.g. 6) or Bridge (e.g. bridge_7_8)");
        version.setRequired(true);
        options.addOption(version);
        
        Option organ = 	 new Option("o", "organ", true, "Target organ, e.g. lung, breast, ...");
        organ.setRequired(false);
        options.addOption(organ);
        
        Option help =    new Option("h", "help", false, "Show help");
        help.setRequired(false);
        options.addOption(help);
        
        Option input = 	 new Option("i", "input", true, "Input file path");
        input.setRequired(false);
        options.addOption(input);

        Option output =  new Option("r", "result", true, "Result file");
        output.setRequired(false);
        options.addOption(output);

    	return options;
	}
	
	private static void processHelpOption(CommandLine cmd, HelpFormatter formatter, Options options) {
        if(cmd.hasOption("help")) {
            formatter.printHelp("utility-name", options);
        }
	}
	
	private static void processListAllOption(CommandLine cmd) {
        if(cmd.hasOption("list")) {
        	new FileSystemConnector().listOntologies(cmd.getOptionValue("version"));
        }
	}
	
	private static BaseClassifier getInstanceBuilder(String version, String organ, String inputDataPath) throws FileNotFoundException, IOException {
		String ontologyIdentifier = organ.toLowerCase() + "_" + version;
		switch(ontologyIdentifier){
			// TNM 7
			case "breast_7": 						return new tnmClassifier.tnm7.Breast(inputDataPath);
			case "colorectal_7": 					return new tnmClassifier.tnm7.Colorectal(inputDataPath);
			case "pancreas_7": 						return new tnmClassifier.tnm7.Pancreas(inputDataPath);
			// TNM Bridge 7 8				
			case "pancreasbridge_bridge_7_8": 		return new tnmClassifier.bridge78.Pancreas(inputDataPath);
			case "pancreas_swrl7to8_bridge_7_8": 	return new tnmClassifier.bridge78.PancreasSWRL7to8(inputDataPath);
			case "pancreas_swrl8eto7_bridge_7_8": 	return new tnmClassifier.bridge78.PancreasSWRL8Eto7(inputDataPath);
			case "pancreas_swrl8nto7_bridge_7_8": 	return new tnmClassifier.bridge78.PancreasSWRL8Nto7(inputDataPath);
			// TNM 8
			case "pancreas_exocrine_8": 			return new tnmClassifier.tnm8.ExocrinePancreas(inputDataPath);
			case "pancreas_neuroendocrine_8": 		return new tnmClassifier.tnm8.NeuroendocrinePancreas(inputDataPath);
		}
		return null;
	}
}
