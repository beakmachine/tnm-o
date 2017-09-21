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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.BaseClassifier;
import tnmClassifier.ClassifierEnvironment.ChangeMode;

/**
* @author Susanne Zabka Jun-Jul 2017, updates: Oliver Brunner
* Use pancreas TNM7 ontology to classify data 
* 
*/
public class Pancreas extends BaseClassifier {
	public Pancreas(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.version = "7";
		this.location = "pancreas";

	}

	public void createIndividualPrimaryTumor(String indname, String noAssessment, String noEvidence, String size,
			String confinement, String infiltration) throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "PancreasTumor"));

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
				res.add(this.addQuality("Confined"));
				if (size.equals("<=2cm")) {
					res.add(this.addQuality("SizeMax2cm"));
				}
				if (size.equals(">2cm")) {
					res.add(this.addQuality("SizeMoreThan2cm"));
				}
				createIndividual(indname, factory, res);
				//System.out.println("Individual T: " +res);

			}
			if (confinement.equals("invasive")) {
				res.add(this.addQuality("Invasive"));

				List<String> invasiveInOrganList = new ArrayList<String>();
				invasiveInOrganList.add("CeliacTrunk");
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
				this.env.getOntologyIri(this.baseId) + "PancreasTumorAggregateAsRelatedToMetastaticRegionalLymphNodes"));

		if (!assessment) { // NotAssessedMalignantAnatomicalStructure
			res.add(tumor);
			res.add(this.notAssessed());

			createIndividual(indname, factory, res);

		} else {
			if (assessment && nrlymph > 0) {

				res.add(tumor);
				res.add(this.notNotAssessed());
				res.add(this.hasPart("MetastaticRegionalLymphNodeOfPancreasTumor"));

				createIndividual(indname, factory, res);
			} else {
				nrlymph = 0;
				res.add(tumor);
				res.add(this.notNotAssessed());
				res.add(this.notHasPart("MetastaticRegionalLymphNodeOfPancreasTumor"));

				createIndividual(indname, factory, res);

			}
		}
	}

	public void createIndividualMetastasis(String indname, boolean metastaseVorhanden)
			throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(
				IRI.create(this.env.getOntologyIri(this.baseId) + "PancreasTumorAggregateAsRelatedToDistantMetastasis"));

		if (metastaseVorhanden) {
			res.add(tumor);
			res.add(this.hasPart("DistantMetastasisOfPancreasTumor"));
			createIndividual(indname, factory, res);
		} // kein else bei p-Klassifikation!

	}

	@Override
	protected Set<OWLClass> getTumorClasses() {
		String iri = this.env.getOntologyIri(this.baseId);
		Set<OWLClass> res = this.getSubClasses("PancreasTumor", iri);
		res.addAll(this.getSubClasses("PancreasTumorAggregate", iri));
		//System.out.println("res ..." + res);
		return res;
	}

	@Override
	protected Set<OWLClass> getClassificationClasses() {
		return this.getSubClasses("RepresentationalUnitInPancreasTNM7Classification", this.env.getOntologyIri(this.baseId));
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


				// distantMetastasisYesNo;
				String distantMetastasisStringYesNo = nextLine[this.dataReader.getIndex("Distant Metastasis")];

				String noAssessmentPrimaryTumor = nextLine[this.dataReader.getIndex("NoAssessment")];
				String noEvidencePrimaryTumor = nextLine[this.dataReader.getIndex("NoEvidence")];
				String confinement = nextLine[this.dataReader.getIndex("Confinement")];
				String size = nextLine[this.dataReader.getIndex("Size")];
				String infiltration = nextLine[this.dataReader
						.getIndex("Invasive in CeliacTrunk Or SuperiorMesentericArtery")];

				result[i].put("Pancreas7", new ArrayList<String>());

				this.createIndividualPrimaryTumor(Integer.toString(i), noAssessmentPrimaryTumor, noEvidencePrimaryTumor,
						size, confinement, infiltration);

				result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (!numLymphNodes.equals("") && !numLymphNodes.equals("null")) {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), Integer.parseInt(numLymphNodes), true);
					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				}

				if (!numLymphNodes.equals("") && !numLymphNodes.equals("null")) {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), Integer.parseInt(numLymphNodes), true);
					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				} else {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), -1, false);
					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				}
				result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (distantMetastasisStringYesNo.equals("yes")) {
					this.createIndividualMetastasis(Integer.toString(i), true);
					result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
					this.env.tearDown(ChangeMode.TEMPORARY);

				} // bei p: kein M0!!! undefined
				else {
					// this.createIndividualMetastasis(Integer.toString(i),
					// false);
					result[i].get("Pancreas7").add("undefined_for_pTNM");
					this.env.tearDown(ChangeMode.TEMPORARY);

				}
				// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));

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
			if (classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "").replace("PancreasTNM7_", "")
					.contains("p")) {
				classif = classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "")
						.replace("PancreasTNM7_", "");
			}
		}
		return classif;
	}
}
