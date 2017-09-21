package tnmClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import tnmClassifier.utils.FileSystemConnector;

/**
 * 
 * @author Oliver Brunner, Juni-August 2017
 * Update: SZ 2017-07-27 tnmoIRI: .owl dazu
 * Update: SZ 2017-07-29 neue Methode addOntologyAsBaseOntology  
 * *
 */
public class ClassifierEnvironment {
	private OWLDataFactory dataFactory;
	private OWLOntologyManager manager;
	private FileSystemConnector fileSystem;
  //  private IRI tnmoIri = IRI.create("http://purl.org/tnmo/TNM-O");
    private IRI tnmoIri = IRI.create("http://purl.org/tnmo/TNM-O.owl"); //SZ. .owl dazu, 27.7.2017

    private IRI bodyPartIri = IRI.create("http://purl.org/tnmo/TNM-O_BodyParts.owl");
	private List<ITearDownFunction> temporaryChangeTearDownFunctions;
	private List<ITearDownFunction> permanentChangeTearDownFunctions;
	
	public ClassifierEnvironment() {
		this.manager = OWLManager.createOWLOntologyManager();
		this.dataFactory = this.manager.getOWLDataFactory();
		this.permanentChangeTearDownFunctions = new ArrayList<ITearDownFunction>();
		this.temporaryChangeTearDownFunctions = new ArrayList<ITearDownFunction>();
		this.fileSystem = new FileSystemConnector();
        this.baseOntologies = new HashMap<String, OWLOntology>();
        this.baseOntologyIris = new HashMap<String, String>();
	}
	
	public ClassifierEnvironment addIriMapper(String iri, String path) {
		return this.addIriMapper(IRI.create(iri), path);
	}
	
	public ClassifierEnvironment addIriMapper(IRI iri, String path) {
		this.manager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(path)));
		return this;
	}
	
	public ClassifierEnvironment addTNMOIriMapper() {
		this.manager.addIRIMapper(new SimpleIRIMapper(this.tnmoIri, IRI.create(this.fileSystem.getTNMOPath())));
		return this;
	}
	
	public ClassifierEnvironment addBodyPartsIriMapper() {
		this.manager.addIRIMapper(new SimpleIRIMapper(this.bodyPartIri, IRI.create(this.fileSystem.getBodyPartsPath())));
		return this;
	}
	
	public ClassifierEnvironment addBaseOntology(String id, String path) throws OWLOntologyCreationException {
		File file = new File(path);
		OWLOntology ontology = this.manager.loadOntologyFromOntologyDocument(file);
		this.addBaseOntology(id, ontology);
		return this;
	}
	
	public ClassifierEnvironment addTNMOAsBase(String id) throws OWLOntologyCreationException {
		return this.addBaseOntology(id, this.fileSystem.getTNMOPath());
	}
	
	public ClassifierEnvironment addBaseOntology(String id, String version, String location) throws OWLOntologyCreationException {
		File file = this.fileSystem.getOntologyFile(version, location);
		OWLOntology ontology = this.manager.loadOntologyFromOntologyDocument(file);
		this.addBaseOntology(id, ontology);
		return this;
	}

	public ClassifierEnvironment addOntology(String version, String location, String baseId) throws OWLOntologyCreationException {
		File file = this.fileSystem.getOntologyFile(version, location);
		return this.addOntology(file, baseId);
	}
	public ClassifierEnvironment addOntology(String path, String baseId) throws OWLOntologyCreationException {
		File file = new File(path);
		return this.addOntology(file, baseId);
	}
	private ClassifierEnvironment addOntology(File file, String baseId) throws OWLOntologyCreationException {
		OWLOntology ontology = this.manager.loadOntologyFromOntologyDocument(file);
		IRI iri = ontology.getOntologyID().getOntologyIRI();
		this.addIriMapper(iri, file.getPath());		
		
		OWLImportsDeclaration importDeclaration = this.dataFactory.getOWLImportsDeclaration(iri);
		
		this.manager.applyChange(new AddImport(this.getOntology(baseId), importDeclaration));
		this.permanentChangeTearDownFunctions.add(new RemoveImport(this.getOntology(baseId), importDeclaration, this.manager));
		return this;
	}
	//SZ, neue Methode, Neue Ontologie laden und importieren (wird f�r Bridge gebraucht - evtl. doch nicht...)
	public ClassifierEnvironment addOntologyAsBaseOntology(String id,String iriString, String path, String baseId) throws OWLOntologyCreationException {
		addBaseOntology(id, path);
		IRI iri = IRI.create(iriString); 
		this.addIriMapper(iri, path);
		OWLImportsDeclaration importDeclaration = this.dataFactory.getOWLImportsDeclaration(iri);
		this.manager.applyChange(new AddImport(this.getOntology(baseId), importDeclaration));
		this.permanentChangeTearDownFunctions.add(new RemoveImport(this.getOntology(baseId), importDeclaration, this.manager));
		return this;
	}

	

	public ClassifierEnvironment save(String id) throws OWLOntologyStorageException {
		this.manager.saveOntology(this.getOntology(id));
		return this;
	}
	
	public ClassifierEnvironment addAxiom(String id, OWLAxiom axiom, ChangeMode mode) {
		this.manager.addAxiom(this.getOntology(id), axiom);
		List<ITearDownFunction> functions = mode == ChangeMode.TEMPORARY ? this.temporaryChangeTearDownFunctions : this.permanentChangeTearDownFunctions;
		functions.add(new RemoveAddedAxiom(this.getOntology(id), axiom, this.manager));
		return this;
	}
	
	private ClassifierEnvironment save(OWLOntology ontology) throws OWLOntologyStorageException {
		this.manager.saveOntology(ontology);
		return this;
	}
	
	public ClassifierEnvironment tearDown(ChangeMode mode) throws OWLOntologyStorageException {
		for(int i = 0; i < this.temporaryChangeTearDownFunctions.size(); ++i ){
			this.temporaryChangeTearDownFunctions.get(i).run();
		}
		this.temporaryChangeTearDownFunctions.clear();
		
		if(mode == ChangeMode.PERMANENT) {
			for(int i = 0; i < this.permanentChangeTearDownFunctions.size(); ++i ){
				this.permanentChangeTearDownFunctions.get(i).run();
			}
			this.permanentChangeTearDownFunctions.clear();
			this.saveBaseOntologies();
		}
		return this;
	}
	
	private void saveBaseOntologies() throws OWLOntologyStorageException {
		Iterator<Map.Entry<String, OWLOntology>> it = this.baseOntologies().entrySet().iterator();
		while (it.hasNext()) {
			this.save(it.next().getValue());
		}
	}
	
    public String getTNMOIri() {
        return this.tnmoIri.toString().concat("#");
    }
    
    public String getBioTopLight2Iri() {
    	return "http://purl.org/biotop/btl2.owl#";
	}
    
    public String getBodyPartIri() {
        return this.bodyPartIri.toString().concat("#");
    }
    
    /**
     * Returns the iri (with appended '#') of a specified ontology
     * ATTENTION: This only works for methods loaded as base ontologies
     * @param id The id under which the ontology was added
     * @return The ontology iri (with attached '#')
     */
    public String getOntologyIri(String id) {
    	return this.baseOntologyIris.get(id);
    }
    
    public OWLDataFactory getDataFactory() {
    	return this.dataFactory;
    }
    
    private Map<String, OWLOntology> baseOntologies;
    private Map<String, String> baseOntologyIris;
    public void addBaseOntology(String id, OWLOntology ontology) {
    	this.baseOntologies.put(id, ontology);
    	this.baseOntologyIris.put(id, ontology.getOntologyID().getOntologyIRI().toString().concat("#"));
    }
    
    public OWLOntology getOntology(String id) {
        return this.baseOntologies.get(id);
    }
    
    public Map<String, OWLOntology> baseOntologies() {
    	return this.baseOntologies;
    }


    
    /**
     * The mode decides when a change to the environment should be cleaned up
     * PERMANENT: Changes are cleaned up after all instances are processed
     * TEMPORARY: Changes are cleaned up after each instance (e.g. to remove specific individuals)
     * @author oli
     * 
     */
    public enum ChangeMode {
    	TEMPORARY, PERMANENT
    } 
    
	private interface ITearDownFunction {
		void run();
	}
	
	private class RemoveImport implements ITearDownFunction {
		private OWLOntology ontology;
		private OWLImportsDeclaration importDeclaraton;
		private OWLOntologyManager manager;
		
		public RemoveImport(OWLOntology ontology, OWLImportsDeclaration importDeclaration, OWLOntologyManager manager) {
			this.ontology = ontology;
			this.importDeclaraton = importDeclaration;
			this.manager = manager;
		}
		
		@Override
		public void run() {
			this.manager.applyChange(new org.semanticweb.owlapi.model.RemoveImport(ontology, this.importDeclaraton));
		}
	}
	
	private class RemoveAddedAxiom implements ITearDownFunction {
		private OWLOntology ontology;
		private OWLAxiom axiom;
		private OWLOntologyManager manager;
		
		public RemoveAddedAxiom(OWLOntology ontology, OWLAxiom axiom, OWLOntologyManager manager) {
			this.ontology = ontology;
			this.axiom = axiom;
			this.manager = manager;
		}
		
		@Override
		public void run() {
			this.manager.applyChange(new org.semanticweb.owlapi.model.RemoveAxiom(ontology, this.axiom));
		}
	}
}
