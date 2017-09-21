package tnmClassifierUtilsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import tnmClassifier.utils.FileSystemConnector;

public class FileSystemConnectorTest {

	private FileSystemConnector target;
	
	@Before
	public void setUp() {
		this.target = new FileSystemConnector();
	}
	
	@Test
	public void testFileSystemConnectorConstructor() {
		assertNotNull("Should return an object", new FileSystemConnector());
	}

	@Test
	public void testOntologyExists() {
		assertTrue("valid combination should return 'true'", this.target.ontologyExists("7", "pancreas"));
		assertFalse("Wrong organ should return 'false'", this.target.ontologyExists("7", "testOrgan"));
		assertFalse("Wrong version should return 'false'", this.target.ontologyExists("testVersion", "pancreas"));
		assertFalse("Wrong version and organ should return 'false'", this.target.ontologyExists("testVersion", "testOrgan"));
	}
	
//	@Test
//	public void testGetOntologyFile_OrganDoesNotExist() {
//		boolean exceptionThrown = false;
//		
//		try {
//			this.target.getOntologyFile("7", "testOrgan");
//		} catch (Exception e) {
//			exceptionThrown = true;
//		}
//		
//		assertTrue("Exception should have been thrown", exceptionThrown);
//	}

}
