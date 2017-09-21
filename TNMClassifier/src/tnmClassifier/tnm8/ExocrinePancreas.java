package tnmClassifier.tnm8;

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
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.BaseClassifier;
import tnmClassifier.ClassifierEnvironment.ChangeMode;

/**
* @author Susanne Zabka Jun-Jul 2017, updates: Oliver Brunner
* Use pancreas TNM8e ontology to classify data 
* 
*/
public class ExocrinePancreas extends BaseClassifier {
	public ExocrinePancreas(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.version = "8";
		this.location = "pancreas_exocrine";
	}


	public void createIndividualPrimaryTumor(String indname, String noAssessment, String noEvidence, String size,
			String confinement, String infiltration) throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "ExocrinePancreasTumor"));

		res.add(tumor);

		if (noAssessment.equals("NoAssessment")) {
			res.add(this.notAssessed());
			createIndividual(indname, factory, res);
		} else {
			if (noEvidence.equals("NoEvidence")) {
				res.add(this.addQuality("NoEvidence"));
				createIndividual(indname, factory, res);
			}
			if (confinement.equals("in-situ")) {
				res.add(this.addQuality("CarcinomaInSitu"));
				createIndividual(indname, factory, res);
			}
			if (confinement.equals("confined")) {
				List<String> optinallyInvasiveOrganList = new ArrayList<String>();
				optinallyInvasiveOrganList.add("ExocrinePancreas");
				optinallyInvasiveOrganList.add("PeripancreaticSoftTissue");
				res.add(this.hasPartIsIncludedIn(optinallyInvasiveOrganList));
				if (size.equals("<=2cm")) {
					res.add(this.addQuality("SizeMax2cm"));
				}
				if (size.equals("<=0.5cm")) {
					res.add(this.addQuality("SizeMax5mm"));
					}
				if (size.equals("0.5-1cm")) {
					res.add(this.addQuality("Size5mmTo1cm"));
				}
				if (size.equals("1-2cm")) {
					
					res.add(this.addQuality("Size1to2cm"));
				}
				if (size.equals("2-4cm")) {
					res.add(this.addQuality("Size2to4cm"));
				}
				if (size.equals(">4cm")) {
	
					res.add(this.addQuality("SizeMoreThan4cm"));
				}
				createIndividual(indname, factory, res);

			}
			if (confinement.equals("invasive")) {
				res.add(this.addQuality("Invasive"));

				List<String> invasiveInOrganList = new ArrayList<String>();
				invasiveInOrganList.add("CeliacTrunk");
				invasiveInOrganList.add("CommonHepaticArtery");
				invasiveInOrganList.add("SuperiorMesentericArtery");

				if (infiltration.equals("no")) {
					res.add(this.notHasPartIsIncludedIn(invasiveInOrganList));
				}
				if (infiltration.equals("yes")) {
					res.add(this.hasPartIsIncludedIn(invasiveInOrganList));
				}

				createIndividual(indname, factory, res);
			}
		}
	}

	public void createIndividualRegionalLymphNodes(String indname, int nrlymph, boolean assessment)
			throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(IRI.create(
				this.env.getOntologyIri(this.baseId) + "ExocrinePancreasTumorAggregateAsRelatedToMetastaticRegionalLymphNodes"));
		res.add(tumor);
		if (!assessment) { // NotAssessedMalignantAnatomicalStructure
			res.add(this.notAssessed());
			createIndividual(indname, factory, res);
		} else {
			if (nrlymph == 0) {
				res.add(this.notHasPart("MetastaticRegionalLymphNodeOfExocrinePancreasTumor"));
				createIndividual(indname, factory, res);
			} else {
				if (nrlymph > 0 && nrlymph < 4) {
					
					res.add(this.hasPartWithQuality("MetastaticRegionalLymphNodeOfExocrinePancreasTumor", "Cardinality1to3"));
					//OWLClass LK = factory.getOWLClass(IRI.create(
						//	this.config.getOntologyIri() + "MetastaticRegionalLymphNodeOfExocrinePancreasTumor"));
					//OWLClassExpression test = factory.getOWLObjectIntersectionOf(LK, this.addQualitySZ("Cardinality1to3"));
					} else {
					res.add(this.hasPartWithQuality("MetastaticRegionalLymphNodeOfExocrinePancreasTumor", "Cardinality4orMore"));
					//res.add(this.hasPart("MetastaticRegionalLymphNodeOfExocrinePancreasTumor"));
					//res.add(this.addQualitySZ("Cardinality4orMore"));
				}

				createIndividual(indname, factory, res);

			}
		}
	}

	public void createIndividualMetastasis(String indname, boolean metastaseVorhanden)
			throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(
				IRI.create(this.env.getOntologyIri(this.baseId) + "ExocrinePancreasTumorAggregateAsRelatedToDistantMetastasis"));

		if (metastaseVorhanden) {
			res.add(tumor);
			res.add(this.hasPart("DistantMetastasisOfExocrinePancreasTumor"));
			createIndividual(indname, factory, res);
		} // kein else bei p-Klassifikation!

	}

	@Override
	protected Set<OWLClass> getTumorClasses() {
		String iri = this.env.getOntologyIri(this.baseId);
		Set<OWLClass> res = this.getSubClasses("ExocrinePancreasTumor", iri);
		res.addAll(this.getSubClasses("ExocrinePancreasTumorAggregate", iri));
		return res;
	}

	@Override
	protected Set<OWLClass> getClassificationClasses() {
		return this.getSubClasses("RepresentationalUnitInExocrinePancreasTNM8Classification", this.env.getOntologyIri(this.baseId));
	}

	@Override
	public Map<String, List<String>>[] run() throws OWLOntologyStorageException, IOException {
		String[] nextLine;
		int i = 0;

		Map<String, List<String>>[] result = new HashMap[this.dataReader.countLines(inputDataPath)];

		while ((nextLine = this.dataReader.nextLine()) != null) {
			if (nextLine != null && i >= 0) {

				result[i] = new HashMap<String, List<String>>();

				// numLymphNodes;
				String numLymphNodes = nextLine[this.dataReader.getIndex("Number MetaLymphNodes")];
				String noAssessment = nextLine[this.dataReader.getIndex("NoAssessment")];

				// distantMetastasisYesNo;
				String distantMetastasisStringYesNo = nextLine[this.dataReader.getIndex("Distant Metastasis")];

				String noAssessmentPrimaryTumor = nextLine[this.dataReader.getIndex("NoAssessment")];
				String noEvidencePrimaryTumor = nextLine[this.dataReader.getIndex("NoEvidence")];
				String confinement = nextLine[this.dataReader.getIndex("Confinement")];
				String size = nextLine[this.dataReader.getIndex("Size")];
				String infiltration = nextLine[this.dataReader
						.getIndex("Invasive in CeliacTrunk Or SuperiorMesentericArtery")];


				result[i].put("Pancreas8exo", new ArrayList<String>());

				this.createIndividualPrimaryTumor(Integer.toString(i), noAssessmentPrimaryTumor, noEvidencePrimaryTumor,
						size, confinement, infiltration);

				result[i].get("Pancreas8exo").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (numLymphNodes.equals("") || numLymphNodes.equals("null")|| noAssessment.equals("NoAssessment")) {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), -1, false);

					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				} else {					
					this.createIndividualRegionalLymphNodes(Integer.toString(i), Integer.parseInt(numLymphNodes), true);
					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				}
				result[i].get("Pancreas8exo").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (distantMetastasisStringYesNo.equals("yes")) {
					this.createIndividualMetastasis(Integer.toString(i), true);
					result[i].get("Pancreas8exo").add(this.classify(Integer.toString(i)));
					this.env.tearDown(ChangeMode.TEMPORARY);

				} // bei p: kein M0!!! also eher weglassen
				else {
					// this.createIndividualMetastasis(Integer.toString(i),
					// false);
					result[i].get("Pancreas8exo").add("undefined_for_pTNM");
					this.env.tearDown(ChangeMode.TEMPORARY);

				}
			}
			i++;
		}
		return result;
	}

	private String classify(String instanceName) {
		String classif = "";
		this.createReasoner(false);
		// oh.isConsistent();

		ArrayList<OWLClass> array = this.performClassification(instanceName);

		for (OWLClass classe : array) {
			if (classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "")
					.replace("ExocrinePancreasTNM8_", "").contains("p")) {
				classif = classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "")
						.replace("ExocrinePancreasTNM8_", "");
			}
		}
		return classif;
	}
}
