package tnmClassifier.tnm7;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.BaseClassifier;
import tnmClassifier.ClassifierEnvironment.ChangeMode;

public class Colorectal extends BaseClassifier {
	public Colorectal(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.version = "7";
		this.location = "colorectal";
	}
	
	private OWLClass[] numberOfMetatstaticRegionalLymphNodes(int numberOfLymphNodes) {
        OWLClass[] res = new OWLClass[2];
        OWLDataFactory factory = this.env.getDataFactory();

        if (numberOfLymphNodes < 4) {
            res[0] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWith1to3MetastaticRegionalLymphNodes"));
            if (numberOfLymphNodes == 1) {
                res[1] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "Cardinality1"));

            } else {
                res[1] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "Cardinality2or3"));
            }
        } else {
            res[0] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWith4orMoreMetastaticRegionalLymphNodes"));
            if (numberOfLymphNodes < 7) {

                res[1] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "Cardinality4to6"));
            } else {
                res[1] = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "Cardinality7orMore"));
            }
        }
        return res;
    }
	
	public void createIndividualRegionalLymphNodes(String indname, int nrlymph, boolean assessment) throws OWLOntologyStorageException {
        OWLDataFactory factory = this.env.getDataFactory();
        Set<OWLClassExpression> res = new HashSet<>();
        OWLClass tumor = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumAggregate"));
        
       
        if (assessment && nrlymph > 0) {
            res.add(this.numberOfMetatstaticRegionalLymphNodes(nrlymph)[0]);
            res.add(this.addQuality("Cardinality", replaceIri(this.numberOfMetatstaticRegionalLymphNodes(nrlymph)[1])));
            createIndividual(indname, factory, res);
        } else if (!assessment) {
            res.add(tumor);
            res.add(this.addQuality("AssessmentQuality", "NoAssessment"));
            createIndividual(indname, factory, res);

        } else if (nrlymph == 0) {
        	res.add(tumor);
            res.add(this.addQuality("AssessmentQuality", "NoEvidence"));
            createIndividual(indname, factory, res);
        } else {
            //Invalid value !!
        }
    }

	public Object verifyPlace(Object place) {
        if (place != null) {
            if (place.equals("Muscular Layer")) {
                place = "MuscularLayerOfLargeIntestine";
            }
            if (place.equals("Visceral Peritoneum")) {
                place = "VisceralPeritoneum";
            }
            if (place.equals("Other Organs/Structures")) {
                place = "other";
            }
            if (place.equals("Lamina Propria")) {
                place = "LaminaPropriaOfLargeIntestine";
            }
            if (place.equals("Subserosa")) {
                place = "SubserosaOfLargeIntestine";
            }
            if (place.equals("Submucosa")) {
                place = "SubmucosaOfLargeIntestine";
            }
            if (place.equals("Adventitia")) {
                place = "AdventitiaOfLargeIntestine";
            }
        }
        return place;
    }
	
	public void createIndividualPrimaryTumor(String indname, Object place, String quality, String tumorQualityValuesRegion) throws OWLOntologyStorageException {
        OWLDataFactory factory = this.env.getDataFactory();
        Set<OWLClassExpression> res = new HashSet<>();

        OWLClassExpression tumorType = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "ColonAndRectumTumor"));
        res.add(tumorType);

        OWLClassExpression qual = this.addQuality(quality, tumorQualityValuesRegion);
        res.add(qual);

        if (place != null) {

            OWLClassExpression placePart = addSomePlace(verifyPlace(place).toString());

            if (place.equals("other")) {
                placePart = addNotPlace("LargeIntestine");
                res.add(placePart);
                placePart = addSomePlace("Organ");
                res.add(placePart);
            } else {
                res.add(placePart);
            }

        }
        this.createIndividual(indname, factory, res);
    }

    public void createIndividualMetastasis(String indname, int organs, boolean peritoneum) throws OWLOntologyStorageException {
        OWLDataFactory factory = this.env.getDataFactory();
        Set<OWLClassExpression> res = new HashSet<>();
        Set<OWLClassExpression> res1 = new HashSet<>();
        OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
        OWLClass distant = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "DistantMetastasisOfColonAndRectumTumor"));
        if (organs == 0) {
            OWLClassExpression tumorType = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWithNoDistantMetastasis"));
            res.add(tumorType);
        } 
        else {
            OWLClassExpression tumorType = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWithDistantMetastasis"));
            res.add(tumorType);
            if (organs == 1) {
                if (peritoneum) {
                    OWLClassExpression some = factory.getOWLObjectSomeValuesFrom(hasPart, distant);
                    res1.add(some);
                    res1.add(addSomePlace("Peritoneum"));
                    OWLClassExpression intersect = factory.getOWLObjectIntersectionOf(res1);
                    res.add(intersect);
                } else {
                     OWLClassExpression type = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWithDistantMetastasisConfinedTo1Organ"));
                     res.add(type);
                }
            } else if (organs > 1) {
                if (peritoneum) {
                    res.add(addSomePlace("Peritoneum"));
                }
                OWLClassExpression type = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "TumorOfColonAndRectumWithDistantMetastasisInMoreThan1Organ"));
                res.add(type);       
            }
        }
        this.createIndividual(indname, factory, res);
    }
    
    @Override protected Set<OWLClass> getTumorClasses() {
    	String iri = this.env.getOntologyIri(this.baseId);
        Set<OWLClass> res = this.getSubClasses("ColonAndRectumTumor", iri);
        res.addAll(this.getSubClasses("TumorOfColonAndRectumAggregate", iri));
        return res;
    }

    @Override protected Set<OWLClass> getClassificationClasses() {
        return this.getSubClasses("RepresentationalUnitInColonRectumTNMClassification", this.env.getOntologyIri(this.baseId));
    }
    
    @Override public Map<String, List<String>>[] run() throws OWLOntologyStorageException, IOException {
    	String[] nextLine;
        int i = 0;
        
        Map<String, List<String>>[] result = new HashMap[this.dataReader.countLines(inputDataPath)];
        
        while ((nextLine = this.dataReader.nextLine()) != null) {
        	System.out.println(i);
            if (nextLine != null && i > 0) {
            	
            	result[i] = new HashMap<String, List<String>>();
            	String numLymphNodes = nextLine[this.dataReader.getIndex("Anzahl befallene LK")];
            	result[i].put("Colon7", new ArrayList<String>());
            	
                if (!numLymphNodes.equals("") && !numLymphNodes.equals("null")) {
                    this.createIndividualRegionalLymphNodes(Integer.toString(i), Integer.parseInt(numLymphNodes), true);
                } 
                else {
                    this.createIndividualRegionalLymphNodes(Integer.toString(i), -1, false);
                }
                result[i].get("Colon7").add(this.classify(Integer.toString(i)));
                this.env.tearDown(ChangeMode.TEMPORARY);
            }
            i++;
        }
		return result;
    }
    
    private String classify(String instanceName) {
        String classif = "";
        this.createReasoner(false);
        //oh.isConsistent();

        ArrayList<OWLClass> array = this.performClassification(instanceName);

        for (OWLClass classe : array) {
            if (classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "").replace("ColonRectumTNM_", "").contains("p")) {
                classif = classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "").replace("ColonRectumTNM_", "");
            }
        }
        return classif;
    } 
}
