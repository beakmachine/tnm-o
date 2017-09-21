package tnmClassifier;

import org.apache.commons.cli.CommandLine;

import tnmClassifier.utils.FileSystemConnector;

import java.io.File;

/**
*
* @author Oliver Brunner
* Check input of console for semantic errors
*/
public class InputChecker {
	private CommandLine cmd;
	private String err;
	private FileSystemConnector loader;
	public InputChecker(CommandLine cmd) {
		this.cmd = cmd;
		this.err = "";
		this.loader = new FileSystemConnector();
	}
	
	public String getError() {
		return this.err;
	}
	
	public Boolean inputIsValid(){
		Boolean result = true;
		if(this.cmd.hasOption("organ")){
			result = this.checkOrganInput();
		}
		if(result && this.cmd.hasOption("input")) {
			result = this.checkInputInput();
		}
		return result;
	}
	
	private Boolean checkOrganInput() {
		Boolean result = true;
		if(!this.loader.ontologyExists(this.cmd.getOptionValue("version"), this.cmd.getOptionValue("organ"))) {
			this.err = "Error: The chosen ontology (" 
						+ this.cmd.getOptionValue("organ") + " version " + this.cmd.getOptionValue("version") + ") does not exist." 
						+ " Try -l to see all possible options";
			result = false;
		} 
		else if(!this.cmd.hasOption("input")){
			this.err = "Error: No input data for the ontology specified, please use the -i option";
			result = false;
		}
		return result;
	}
	
	private Boolean checkInputInput() {
		Boolean result = true;
		if(!new File(this.cmd.getOptionValue("input")).exists()) {
			this.err = "Error: Input data does not exist";
			result = false;
		} else if (!this.cmd.hasOption("organ")){
			this.err = "Error: No ontology was specified, please use the -o option";
			result = false;
		}
		return result;
	}

}
