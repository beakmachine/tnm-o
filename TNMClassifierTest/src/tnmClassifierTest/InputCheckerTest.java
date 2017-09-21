package tnmClassifierTest;

import static org.junit.Assert.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import tnmClassifier.InputChecker;

/**
*
* @author Oliver Brunner
*/
public class InputCheckerTest {
	private Options options;
	
	private void createOptions() {
        this.options = new Options();

        Option version = new Option("v", "version", true, "TNMO version (e.g. 6) or Bridge (e.g. bridge_7_8)");
        version.setRequired(true);
        options.addOption(version);
        
        Option organ = 	 new Option("o", "organ", true, "Target organ, e.g. lung, breast, ...");
        organ.setRequired(false);
        options.addOption(organ);
        
        Option input = 	 new Option("i", "input", true, "Input file path");
        input.setRequired(false);
        options.addOption(input);
	}
	
	private CommandLine getCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(this.options, args);
        } catch (ParseException e) {
        	fail("Error creating CommandLine Interface");
        	return null;
        }
        return cmd;
	}
	
	@Before
	public void setUp() {
		this.createOptions();
	}

	@Test
	public void testConstructor() {
		String[] args = {"-v", "7", "-o", "pancreas"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		
		assertNotNull("Constructor should create an object", target);
	}
	
	@Test
	public void testInputisValid_InputDataButNoOrganGiven() {
		String[] args = {"-v", "7", "-i", "./testData/empty.csv"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		boolean result = target.inputIsValid();
		
		assertFalse("Result should be 'False'", result);
		assertNotSame("There shoud be an error message set", "", target.getError());
	}
	
	@Test
	public void testInputisValid_OrganButNoInputDataGiven() {
		String[] args = {"-v", "7", "-o", "pancreas"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		boolean result = target.inputIsValid();
		
		assertFalse("Result should be 'False'", result);
		assertNotSame("There shoud be an error message set", "", target.getError());
	}

	@Test
	public void testInputisValid_ValidDataGiven() {
		String[] args = {"-v", "7", "-o", "pancreas", "-i", "./testData/empty.csv"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		boolean result = target.inputIsValid();
		
		assertTrue("Result should be 'True'", result);
		assertSame("Error message should be empty", "", target.getError());
	}
	
	@Test
	public void testInputisValid_InputFileDoesNotExist() {
		String[] args = {"-v", "7", "-o", "pancreas", "-i", "./testData/IDontExist.csv"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		boolean result = target.inputIsValid();
		
		assertFalse("Result should be 'False'", result);
		assertNotSame("There shoud be an error message set", "", target.getError());
	}
	
	@Test
	public void testInputisValid_OrganDoesNotExist() {
		String[] args = {"-v", "WeirdVersion", "-o", "pancreas", "-i", "./testData/empty.csv"};
		CommandLine cmd = this.getCommandLine(args);
		
		InputChecker target = new InputChecker(cmd);
		boolean result = target.inputIsValid();
		
		assertFalse("Result should be 'False'", result);
		assertNotSame("There shoud be an error message set", "", target.getError());
	}
}
