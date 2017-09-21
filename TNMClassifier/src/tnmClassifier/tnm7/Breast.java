package tnmClassifier.tnm7;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.BaseClassifier;

public class Breast extends BaseClassifier {
	public Breast(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.version = "7";
		this.location = "breast";
	}
	
    @Override public OWLClass[] orderedArray() {
        OWLDataFactory factory = this.env.getDataFactory();
        String iri = this.env.getOntologyIri(this.baseId);
        OWLClass[] ordered = new OWLClass[16];

        ordered[0] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pTisDCIS"));
        ordered[1] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pTisLCIS"));
        ordered[2] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pTisPaget"));
        ordered[3] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pTis"));
        ordered[4] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT4d"));
        ordered[5] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT4c"));
        ordered[6] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT4b"));
        ordered[7] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT4a"));
        ordered[8] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT4"));
        ordered[9] =  factory.getOWLClass(IRI.create(iri + "BreastTNM_pT3"));
        ordered[10] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pT2"));
        ordered[11] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pT1c"));
        ordered[12] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pT1b"));
        ordered[13] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pT1a"));
        ordered[14] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pT0"));
        ordered[15] = factory.getOWLClass(IRI.create(iri + "BreastTNM_pTX"));

        return ordered;
    }
    
    public Object verifyPlace(Object place) {
        if (place != null) {
            if (place.equals("Lobule of Lactiferous Gland")) {
                place = "LobuleOfLactiferousGland";
            }
            if (place.equals("Lactiferous Duct")) {
                place = "LactiferousDuct";
            }
            if (place.equals("Chest Wall")) {
                place = "ChestWall";
            }
            if (place.equals("Skin")) {
                place = "Skin";
            }
        }
        return place;
    }
    
    public OWLClassExpression addSize(float size) {
        OWLClassExpression qual = null;

        if (size > 50) {
            qual = this.addQuality("Length", "LengthAbove5cm");
        }
        if (size <= 50 && size > 20) {
            qual = this.addQuality("Length", "LengthBetween2and5cm");
        }
        if (size <= 20) {

            if (size <= 1) {
                qual = this.addQuality("Length", "LengthBelow0.1cm");
            }
            if (size > 1 && size <= 5) {
                qual = this.addQuality("Length", "LengthBetween0.1and0.5cm");
            }
            if (size > 5 && size <= 10) {
                qual = this.addQuality("Length", "LengthBetween0.5and1cm");
            }
            if (size > 10 && size <= 20) {
                qual = this.addQuality("Length", "LengthBetween1and2cm");
            }
        }
        return qual;
    }

    public void createIndividualPrimaryTumor(String indname, Object place, String quality, String value, boolean infla, boolean ulce, boolean paget, float size) throws OWLOntologyStorageException {
        OWLDataFactory factory = this.env.getDataFactory();
        Set<OWLClassExpression> res = new HashSet<>();

        OWLClassExpression tumorType = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "BreastTumor"));
        res.add(tumorType);

        OWLClassExpression conf = this.addQuality(quality, value);
        res.add(conf);
        if (paget) {
            tumorType = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "CarcinomaInSitu_Paget"));
            res.add(tumorType);
        } else {

            if (infla) {
                OWLClassExpression qual = this.addQuality("TumorQuality", "Inflammatory");
                res.add(qual);
            }
            if (ulce) {
                OWLClassExpression qual = this.addQuality("TumorQuality", "Ulcerating");
                res.add(qual);
            }
            if (place != null) {

                OWLClassExpression placePart = this.addSomePlace(verifyPlace(place).toString());
                res.add(placePart);
            }
            if (size != 0) {
                OWLClassExpression qual = this.addSize(size);
                res.add(qual);
            }
        }
        this.createIndividual(indname, factory, res);
    }
    
	 @Override protected Set<OWLClass> getTumorClasses() {
		 String iri = this.env.getOntologyIri(this.baseId);
		 Set<OWLClass> res = this.getSubClasses("BreastTumor", iri);
		 res.addAll(this.getSubClasses("BreastTumorAggregate", iri));
		 return res;
	 }

	 @Override protected Set<OWLClass> getClassificationClasses() {
		 return this.getSubClasses("RepresentationalUnitInBreastTNMClassification", this.env.getOntologyIri(this.baseId));
	 }
}
