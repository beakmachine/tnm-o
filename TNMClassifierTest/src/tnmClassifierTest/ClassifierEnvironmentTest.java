package tnmClassifierTest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.ClassifierEnvironment;
import tnmClassifier.ClassifierEnvironment.ChangeMode;

public class ClassifierEnvironmentTest {

	private ClassifierEnvironment target;
	@Before
	public void setUp() {
		this.target = new ClassifierEnvironment();
	}
	@After
	public void tearDown() {
		try {
			this.target.tearDown(ChangeMode.PERMANENT);
		} catch (OWLOntologyStorageException e) {
			fail("Tear down failed");
		}
	}
	
	@Test
	public void testAddBaseOntology_VersionAndOrgan() {
		try {
			this.target.addBaseOntology("test", "7", "pancreas");
		} catch (OWLOntologyCreationException e) {
			fail("Adding base ontology should not fail");
		}
		assertNotNull("Base ontology should be defined", this.target.getOntology("test"));
		assertNotNull("Base ontology iri should be defined", this.target.getOntologyIri("test"));
	}
	
	@Test
	public void testAddBaseOntology_VersionAndOrgan_WrongOrgan() {
		boolean exceptionThrown = false;
		
		try {
			this.target.addBaseOntology("test", "7", "testOrgan");
		} catch (OWLOntologyInputSourceException e) {
			exceptionThrown = true;
		} catch (OWLOntologyCreationException e) {}
		
		assertTrue("Exception should have been thrown", exceptionThrown);
	}
	
	@Test
	public void testGetOntologyIri() {
		assertNull("Base ontology should not yet be defined", this.target.getOntology("test"));
		try {
			this.target.addBaseOntology("test", "7", "pancreas");
		} catch (OWLOntologyCreationException e) {
			fail("Adding base ontology should not fail");
		}
		assertNotNull("Base ontology iri should be defined", this.target.getOntologyIri("test"));
	}
	
	@Test
	public void testGetOntology() {
		assertNull("Base ontology should not yet be defined", this.target.getOntology("test"));
		try {
			this.target.addBaseOntology("test", "7", "pancreas");
		} catch (OWLOntologyCreationException e) {
			fail("Adding base ontology should not fail");
		}
		assertNotNull("Base ontology iri should be defined", this.target.getOntology("test"));
	}
}
