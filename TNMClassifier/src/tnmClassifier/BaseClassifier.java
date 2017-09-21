package tnmClassifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import tnmClassifier.ClassifierEnvironment.ChangeMode;
import tnmClassifier.utils.CSVTools;
import tnmClassifier.utils.IDataReader;

/**
Autor: Oliver Brunner
Update SZ:  15.7.2017: performClassification (wenn mehrere Ontology-Klassen gefunden)
  2017-07-30: Methode createAndReturnIndividual(String indname, OWLDataFactory factory, Set<OWLClassExpression> res, String ontologyId) 
   2017-07-31: Methode   isIncludedIn(String organ) 
 
*/
public class BaseClassifier {
    protected IDataReader dataReader;
    protected String inputDataPath;
    protected ClassifierEnvironment env;
    protected OWLReasoner reasoner;
    protected String version;
    protected String location;
    protected String baseId;
    
    protected BaseClassifier(String inputDataPath) throws FileNotFoundException, IOException  {
    	this.inputDataPath = inputDataPath;
    	this.dataReader = new CSVTools(inputDataPath);
    	this.env = new ClassifierEnvironment();
    	this.baseId = "main";
    }
    
    /**
     * Returns OWLClassExpression like "isBearerOf some (projectsOnto only [regionValue])"
     * @author Fabio Franca, adapted by Susanne Zabka
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @return OWLClassExpression 
     */
	protected OWLClassExpression addQuality(String regionValue) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLObjectProperty isBearerOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
		OWLObjectProperty projectsOnto = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));
		// SomeQuality
		OWLClassExpression TumorQualityValuesRegion = factory
				.getOWLClass(IRI.create(this.env.getTNMOIri() + regionValue));

		OWLClassExpression onlyTumorQuality = factory.getOWLObjectAllValuesFrom(projectsOnto, TumorQualityValuesRegion);
		// val.add(onlyTumorQuality);
		OWLClassExpression qualityPart = factory.getOWLObjectSomeValuesFrom(isBearerOf, onlyTumorQuality);

		return qualityPart;
	}
	
    /**
     * Returns OWLClassExpression like "isBearerOf some [Quality] and (projectsOnto only [regionValue])"
     * for the base ontology
     * @author Oliver Brunner
     * @param quality expression for a "Quality"- OWLClass
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @return OWLClassExpression 
     */
    protected OWLClassExpression addQuality(String quality, String regionValue) {
    	return this.addQuality(quality, regionValue, this.baseId);
    }
    
    /** 
     * Returns OWLClassExpression like "isBearerOf some [Quality] and (projectsOnto only [regionValue])"
     * for the ontology specified
     * @author Fabio Franca, adapted by Oliver Brunner
     * @param quality expression for a "Quality"- OWLClass
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @param ontologyID  
     * @return OWLClassExpression 
     */
    protected OWLClassExpression addQuality(String quality, String regionValue, String ontologyId) {
        OWLDataFactory factory = this.env.getDataFactory();
        OWLObjectProperty isBearerOf = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
        OWLObjectProperty projectsOnto = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));

        Set<OWLClassExpression> val = new HashSet<>();
        Set<OWLClassExpression> qual = new HashSet<>();

        OWLClassExpression cQuality = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + quality));
        qual.add(cQuality);

        //SomeQuality
        OWLClassExpression TumorQualityValuesRegion = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + regionValue));

        OWLClassExpression someTumorQuality = factory.getOWLObjectSomeValuesFrom(projectsOnto, TumorQualityValuesRegion);
        val.add(someTumorQuality);
        //OnlyQuality
        OWLClassExpression allTumorQuality = factory.getOWLObjectAllValuesFrom(projectsOnto, TumorQualityValuesRegion);
        val.add(allTumorQuality);

        OWLObjectIntersectionOf valuesIntersect = factory.getOWLObjectIntersectionOf(val);
        qual.add(valuesIntersect);
        OWLObjectIntersectionOf qualityValuesIntersect = factory.getOWLObjectIntersectionOf(qual);
        OWLClassExpression qualityPart = factory.getOWLObjectSomeValuesFrom(isBearerOf, qualityValuesIntersect);

        //    System.out.println("final  :" + qualityPart.toString().replace(iri, "").replace(iribtl2, ""));
        return qualityPart;
    }
    
    /** 
     * Returns OWLClassExpression like "isIncludedIn"  ?? CHECK EVTL doppelte Methode????
     * for the ontology specified
     * @author Fabio Franca, adapted by Oliver Brunner  ???
     * @param quality expression for a "Quality"- OWLClass
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @param ontologyID  
     * @return OWLClassExpression 
     */

    protected OWLClassExpression addNotPlace(String place) {
    	return this.addNotPlace(place, this.baseId);
    }
    protected OWLClassExpression addNotPlace(String place, String ontologyId) {
        OWLDataFactory factory = this.env.getDataFactory();
        OWLClassExpression placePart;
        OWLObjectProperty isIncludedIn = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));
        OWLClassExpression tumorPlace = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + place));
        placePart = factory.getOWLObjectSomeValuesFrom(isIncludedIn, tumorPlace);
        return placePart.getObjectComplementOf();
    }
    
    protected OWLClassExpression addSomePlace(String place) {
    	return this.addNotPlace(place, this.baseId);
    }
    protected OWLClassExpression addSomePlace(String place, String ontologyId) {
        OWLDataFactory factory = this.env.getDataFactory();
        OWLClassExpression placePart;
        OWLObjectProperty isIncludedIn = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));

        if (place.equals("Organ")) {
            OWLClassExpression tumorPlace = factory.getOWLClass(IRI.create(this.env.getTNMOIri() + place));
            placePart = factory.getOWLObjectSomeValuesFrom(isIncludedIn, tumorPlace);
            return placePart;
        } else {
            OWLClassExpression tumorPlace = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + place));
            placePart = factory.getOWLObjectSomeValuesFrom(isIncludedIn, tumorPlace);
            return placePart;
        }
    }

    protected OWLClassExpression addOnlyPlace(String place) {
    	return this.addNotPlace(place, this.baseId);
    }
    protected OWLClassExpression addOnlyPlace(String place, String ontologyId) {
        OWLDataFactory factory = this.env.getDataFactory();
        OWLClassExpression placePart;
        OWLObjectProperty isIncludedIn = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));

        OWLClassExpression tumorPlace = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + place));
        placePart = factory.getOWLObjectAllValuesFrom(isIncludedIn, tumorPlace);
        return placePart;
    }
    //SZ: neu,2017-07-31
    /** 
     * Returns OWLClassExpression like "isIncludedIn"  ?? CHECK EVTL doppelte Methode????
     * for the base ontology specified
     * @author Susanne Zabka, adapted by Oliver Brunner  
     * @param organ expression for a "Quality"- OWLClass
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @param ontologyID  
     * @return OWLClassExpression 
     */

    protected OWLClassExpression isIncludedIn(String organ) {
    	return this.isIncludedIn(organ, this.baseId);
    }
    /** 
     * Returns OWLClassExpression like "isIncludedIn"  ?? CHECK EVTL doppelte Methode????
     * for the base ontology specified
     * @author Susanne Zabka, adapted by Oliver Brunner  
     * @param organ expression for a "Quality"- OWLClass
     * @param regionValue expression for a "ValueRegion"- OWLClass
     * @param ontologyID  
     * @return OWLClassExpression 
     */
	protected OWLClassExpression isIncludedIn(String organ,String ontologyId) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLObjectProperty isIncludedIn = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));
		OWLClassExpression organOwlClass = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + organ));
		OWLClassExpression isIncludedInSomeOrgan = factory.getOWLObjectSomeValuesFrom(isIncludedIn, organOwlClass);

		return isIncludedInSomeOrgan;
	}
	
    protected OWLClassExpression hasPart(String part) {
    	return this.hasPart(part, this.baseId);
    }
	protected OWLClassExpression hasPart(String part, String ontologyId) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLObjectProperty hasPart = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
		OWLClassExpression partOwlClass = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + part));
		OWLClassExpression andhasPartsome = factory.getOWLObjectSomeValuesFrom(hasPart, partOwlClass);

		return andhasPartsome;
	}
	
	protected OWLClassExpression notHasPart(String part) {
		return this.hasPart(part, this.baseId).getObjectComplementOf();
	}
	protected OWLClassExpression notHasPart(String part, String ontologyId) {
		return this.hasPart(part, ontologyId).getObjectComplementOf();
	}

	protected OWLClassExpression hasPartWithQuality(String part, String regionValue) {
		return this.hasPartWithQuality(part, regionValue, this.baseId);
	}
	protected OWLClassExpression hasPartWithQuality(String part, String regionValue, String ontologyId) { 
		OWLDataFactory factory = this.env.getDataFactory();
		OWLObjectProperty isBearerOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
		OWLObjectProperty projectsOnto = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));
		// SomeQuality
		OWLClassExpression TumorQualityValuesRegion = factory
				.getOWLClass(IRI.create(this.env.getTNMOIri() + regionValue));
		OWLClassExpression onlyTumorQuality = factory.getOWLObjectAllValuesFrom(projectsOnto, TumorQualityValuesRegion);
		// val.add(onlyTumorQuality);
		OWLClassExpression qualityPart = factory.getOWLObjectSomeValuesFrom(isBearerOf, onlyTumorQuality);

		OWLObjectProperty hasPart = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
		OWLClassExpression partOwlClass = factory.getOWLClass(IRI.create(this.env.getOntologyIri(ontologyId) + part));
		OWLClassExpression partwithquality = factory.getOWLObjectIntersectionOf(partOwlClass,qualityPart);
		OWLClassExpression andhasParts = factory.getOWLObjectSomeValuesFrom(hasPart, partwithquality);
		return andhasParts;
	}
	
	protected OWLClassExpression notAssessed() {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLClassExpression notAssessedExpression = factory
				.getOWLClass(IRI.create(this.env.getTNMOIri() + "NotAssessedMalignantAnatomicalStructure"));
		return notAssessedExpression;
	}

	protected OWLClassExpression notNotAssessed() {
		return this.notAssessed().getObjectComplementOf();
	}
    /** 
     * Returns OWLClassExpression like "[organ] or [organ] or ..."
     * @author Susanne Zabka
     * @param organlistOR a list of organs to be connected with OR
     * @return OWLClassExpression 
     */
	protected OWLClassExpression organlistOR(List<String> organlist) {
		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> organClasses = new HashSet<OWLClassExpression>();
		for (String element : organlist) {
			OWLClassExpression organ = factory.getOWLClass(IRI.create(this.env.getBodyPartIri() + element));
			organClasses.add(organ);
		}
		OWLObjectUnionOf organclassOrList = factory.getOWLObjectUnionOf(organClasses);
		return organclassOrList;
	}

	protected OWLClassExpression hasPartIncludedInSpecialOrganListWithExceptions(List<String> organlistOr, List<String> organlistExcludingOrgans, List<String> excludedOrganlist) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLClassExpression organClasses1 = organlistOR(organlistOr);
		OWLClassExpression organClasses2 = organlistOR(organlistExcludingOrgans);
		OWLClassExpression organClasses3 = organlistOR(excludedOrganlist).getObjectComplementOf();;
		OWLClassExpression organAndNotOrgans = factory.getOWLObjectIntersectionOf(organClasses2,organClasses3);
		OWLClassExpression specialorganclassList = factory.getOWLObjectUnionOf(organClasses1,organAndNotOrgans );
	
		OWLObjectProperty hasPart = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
		OWLObjectProperty isIncludedIn = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));
		
		OWLClassExpression includedInOrgans = factory.getOWLObjectSomeValuesFrom(isIncludedIn, specialorganclassList);
		OWLClassExpression hasPartIsincludedInOrgans = factory.getOWLObjectSomeValuesFrom(hasPart, includedInOrgans);

		return hasPartIsincludedInOrgans;
	}
	
	 /** 
     * Returns OWLClassExpression like "hasPart some (isIncludedIn only ([organ] or [organ] or ...))"
     * @author Susanne Zabka
     * @param organlistOR a list of organs to be connected with OR
     * @return OWLClassExpression 
     */
	protected OWLClassExpression hasPartIsIncludedIn(List<String> organlist) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLObjectProperty hasPart = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
		OWLObjectProperty isIncludedIn = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));

		OWLClassExpression organlistOR = organlistOR(organlist);
		OWLClassExpression includedInOrgans = factory.getOWLObjectSomeValuesFrom(isIncludedIn, organlistOR);
		OWLClassExpression hasPartIsincludedInOrgans = factory.getOWLObjectSomeValuesFrom(hasPart, includedInOrgans);

		return hasPartIsincludedInOrgans;
	}

	protected OWLClassExpression notHasPartIsIncludedIn(List<String> organlist) {
		return this.hasPartIsIncludedIn(organlist).getObjectComplementOf();
	}
	
    protected void createIndividual(String indname, OWLDataFactory factory, Set<OWLClassExpression> res) throws OWLOntologyStorageException {
    	this.createIndividual(indname, factory, res, this.baseId);
    }
    protected void createIndividual(String indname, OWLDataFactory factory, Set<OWLClassExpression> res, String ontologyId) throws OWLOntologyStorageException {
        OWLObjectIntersectionOf allParts = factory.getOWLObjectIntersectionOf(res);
        OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(this.env.getOntologyIri(ontologyId) + indname));
        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(allParts, ind);
        this.env
        	.addAxiom(ontologyId, ax, ChangeMode.TEMPORARY)
        	.save(ontologyId);
    }
    //neu, SZ, 2017-07-30, wird gebraucht f�r Bridge
    protected OWLNamedIndividual createAndReturnIndividual(String indname, OWLDataFactory factory, Set<OWLClassExpression> res, String ontologyId) throws OWLOntologyStorageException {
        OWLObjectIntersectionOf allParts = factory.getOWLObjectIntersectionOf(res);
        OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(this.env.getOntologyIri(ontologyId) + indname));
        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(allParts, ind);
        this.env
        	.addAxiom(ontologyId, ax, ChangeMode.TEMPORARY)
        	.save(ontologyId);
        return ind;
    }
    protected String replaceIri(OWLClass owlClass) {
    	return this.replaceIri(owlClass, this.baseId);
    }
    protected String replaceIri(OWLClass owlClass, String ontologyId) {
        String res = owlClass.toString().replace(this.env.getOntologyIri(ontologyId), "").replace("<", "").replace(">", "");
        return res;
    }
    
    protected OWLClass prioritizeT(ArrayList<OWLClass> raw) {
        OWLClass[] ordered = this.orderedArray();
        OWLClass[] unordered = new OWLClass[raw.size()];
        OWLClass res = null;
        int z = 0;
        for(OWLClass x : raw){
            unordered[z] = x;
            z++;
        }
        
        for (int i = 0; i < ordered.length; i++) {

            for (int j = 0; j < unordered.length; j++) {
                if (unordered[j] == ordered[i]) {
                    res = unordered[j];
                    System.out.println("RES "+res.toString()); 
                    return res;
                }
            }
        }
        return res ;
    }
    
    protected OWLClass[] orderedArray() {
        OWLClass[] ordered = new OWLClass[0];
        return ordered;
    }
    
    protected void startReasoner(){    
        this.createReasoner(false);
        //this.isConsistent();
    }

    protected Set<OWLClass> getSubClasses(String className , String iri) {
        OWLDataFactory fac = this.env.getDataFactory();
        OWLClass cl = fac.getOWLClass(IRI.create(iri + className));
        Set<OWLClass> subClses = this.reasoner.getSubClasses(cl, false).getFlattened();
        return subClses;
    }
    
    
    protected void createReasoner(boolean useConfig) {
    	this.createReasoner(useConfig, this.baseId);
    }
    protected void createReasoner(boolean useConfig, String ontologyId) {
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        Configuration configuration = new Configuration();

        configuration.ignoreUnsupportedDatatypes = true;
        if (useConfig) {
            this.reasoner = reasonerFactory.createReasoner(this.env.getOntology(ontologyId), config);
        } 
        else {
            this.reasoner = reasonerFactory.createReasoner(this.env.getOntology(ontologyId));
        }
    }

    protected ArrayList<OWLClass> performClassification(String indname) {
    	return this.performClassification(indname, this.baseId);
    }
    protected ArrayList<OWLClass> performClassification(String indname, String ontologyId) {
    	ArrayList<OWLClass> res = new ArrayList<>();
		ArrayList<OWLClass> p = new ArrayList<>();

		
		OWLDataFactory factory = this.env.getDataFactory();
	    OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(this.env.getOntologyIri(ontologyId) + indname));
	     
		Set<OWLClass>[] classes = this.getClasses();
		Set<OWLClass> classifications = classes[1];

		// --neu
		NodeSet<OWLClass> typesSet = this.reasoner.getTypes(ind, true);
		Set<OWLClass> resultSet = typesSet.getFlattened(); // Zwischenliste
															// falls Reasoner
															// mehrere Klassen
															// findet
		if (!typesSet.isSingleton()) { // mehr als eine Klasse gefunden

			for (OWLClass i : typesSet.getFlattened()) {
				if (!reasoner.getSubClasses(i, true).isBottomSingleton()) { // die
																			// "unterste
																			// Klasse"
																			// stehenlassen
					resultSet.remove(i);
					// System.out.println("The false Class subclass " +
					// i.getIRI().getFragment() + " "
					// + reasoner.getSubClasses(i, true).isBottomSingleton());
				}
				if (resultSet.isEmpty()) {
					// System.out.println("Mist. 2 Versuch mit Superklassen
					// vergleichen-----");
					resultSet = typesSet.getFlattened();
					NodeSet<OWLClass> checkSuperClasses = reasoner.getSuperClasses(i, false);
					//if (checkSuperClasses.containsEntity(i)) {  // geht nicht???
					//	System.out.println("ja das ist der �belt�ter:" + i.getIRI().getFragment());
					//}
					
					for (OWLClass j : checkSuperClasses.getFlattened()) {
						if (i == j) {
							resultSet.remove(i);
							// System.out.println("Klasse wird entfernt " +
							// i.getIRI().getFragment());

						}
					}
				}

			}
		}
		for (OWLClass j : resultSet) {
			//System.out.println("The reuslt " + j.getIRI().getFragment());
			p.add(j);
			// System.out.println("superclass "+ reasoner.getSuperClasses(i,true))j);
		}

		for (OWLClass inf : p) {
			Set<OWLClassAxiom> set = this.env.getOntology(ontologyId).getAxioms(inf);
			for (OWLClassAxiom x : set) {
				OWLAxiom axiom = x.getAxiomWithoutAnnotations();
				for (OWLClass classif : classifications) {
					if (axiom.toString().contains(classif.toString())) {
						res.add(classif);
					}
				}
			}
		}
		// return res; //geht wohl auch nur damit
		return this.eliminateDuplicates(res); // scheint auch ohne das zu
												// gehen...
	}

    
    //public void isConsistent() {
    //    this.reasoner.precomputeInferences();
    //    boolean consistent = this.reasoner.isConsistent();
    //    System.out.println("Consistent: " + consistent);
    //}

	private ArrayList<OWLClass> eliminateDuplicates(ArrayList<OWLClass> res) {       
		 HashSet<OWLClass> hs = new HashSet<OWLClass>();
		 hs.addAll(res);
		 res.clear();
		 res.addAll(hs);
		 return res;
	 }


	protected Set<OWLClass>[] getClasses() {
        Set<OWLClass>[] res = new HashSet[2];
        res[0] = this.getTumorClasses();
        res[1] = this.getClassificationClasses();
        return res;
    }
    
    protected Set<OWLClass> getTumorClasses() {
    	return null;
    }
    
    protected Set<OWLClass> getClassificationClasses() {
    	return null;
    }
    
    public Map<String, List<String>>[] run() throws OWLOntologyStorageException, IOException, OWLOntologyCreationException {
    	return null;
    }
    
    public void setUp() throws OWLOntologyCreationException {
    	//System.out.println(this.baseId+this.version+ this.location);
    	this.env
		.addBaseOntology(this.baseId, this.version, this.location)
		.addTNMOIriMapper()
		.addBodyPartsIriMapper();
    	
    	this.createReasoner(false);
    }
    
    public void tearDown() throws OWLOntologyStorageException {
    	this.env.tearDown(ChangeMode.PERMANENT);
    }
}
