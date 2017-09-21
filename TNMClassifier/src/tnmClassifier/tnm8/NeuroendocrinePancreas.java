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
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import tnmClassifier.BaseClassifier;
import tnmClassifier.ClassifierEnvironment.ChangeMode;

/**
* @author Susanne Zabka Jun-Jul 2017, updates: Oliver Brunner
* Use pancreas TNM8n ontology to classify data 
* 
*/
public class NeuroendocrinePancreas extends BaseClassifier {
	public NeuroendocrinePancreas(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.version = "8";
		this.location = "pancreas_neuroendocrine";
	}

	public void createIndividualPrimaryTumor(String indname, String noAssessment, String noEvidence, String size,
			String confinement, String invasiveInBileDuctOrDuodenum, String invasiveInSerosa,
			String invasiveInOtherOrgan) throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory
				.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId) + "NeuroendocrinePancreasTumor"));

		res.add(tumor);

		if (noAssessment.equals("NoAssessment")) {
			res.add(this.notAssessed());
			createIndividual(indname, factory, res);
		} else {
			if (noEvidence.equals("NoEvidence")) {
				res.add(this.addQuality("NoEvidence"));
				createIndividual(indname, factory, res);
			}
			if (confinement.equals("confined")) {
				List<String> optinallyInvasiveOrganList = new ArrayList<String>();
				optinallyInvasiveOrganList.add("SetOfPancreaticIslets");
				optinallyInvasiveOrganList.add("PeripancreaticSoftTissue");
				res.add(this.hasPartIsIncludedIn(optinallyInvasiveOrganList));
				if (size.equals("<=2cm")) {
					res.add(this.addQuality("SizeMax2cm"));
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
				// res.add(this.addQualitySZ("Invasive"));

				if (invasiveInBileDuctOrDuodenum.equals("yes")) {
					List<String> invasiveInOrganList = new ArrayList<String>();
					invasiveInOrganList.add("BileDuct");
					invasiveInOrganList.add("Duodenum");

					res.add(this.hasPartIsIncludedIn(invasiveInOrganList));
				}
				if (invasiveInSerosa.equals("yes") || invasiveInOtherOrgan.equals("yes")) {
					List<String> possibleOrgans = new ArrayList<String>();
					possibleOrgans.add("Serosa");

					List<String> organlistExcludingOrgans = new ArrayList<String>();
					organlistExcludingOrgans.add("BodyPartAdjacentToPancreas");

					List<String> andNotorganlist = new ArrayList<String>();
					andNotorganlist.add("BileDuct");
					andNotorganlist.add("Duodenum");
					andNotorganlist.add("PeripancreaticSoftTissue");

					res.add(this.hasPartIncludedInSpecialOrganListWithExceptions(possibleOrgans,
							organlistExcludingOrgans, andNotorganlist));
				}
			}
			createIndividual(indname, factory, res);
		}
	}

	public void createIndividualRegionalLymphNodes(String indname, int nrlymph, boolean assessment)
			throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId)
				+ "NeuroendocrinePancreasTumorAggregateAsRelatedToMetastaticRegionalLymphNodes"));
		res.add(tumor);
		if (!assessment) { // NotAssessedMalignantAnatomicalStructure
			res.add(this.notAssessed());
			createIndividual(indname, factory, res);
		} else {
			if (assessment && nrlymph > 0) {
				res.add(tumor);
				res.add(this.notNotAssessed());
				res.add(this.hasPart("MetastaticRegionalLymphNodeOfNeuroendocrinePancreasTumor"));
				createIndividual(indname, factory, res);
			} else {
				// nrlymph = 0;
				res.add(tumor);
				res.add(this.notNotAssessed());
				res.add(this.notHasPart("MetastaticRegionalLymphNodeOfNeuroendocrinePancreasTumor"));
				createIndividual(indname, factory, res);

			}

		}
	}

	public void createIndividualMetastasis(String indname, boolean metastaseVorhanden,
			String distantMetastasisStringOrgan) throws OWLOntologyStorageException {

		OWLDataFactory factory = this.env.getDataFactory();
		Set<OWLClassExpression> res = new HashSet<>();
		OWLClass tumor = factory.getOWLClass(IRI.create(this.env.getOntologyIri(this.baseId)
				+ "NeuroendocrinePancreasTumorAggregateAsRelatedToDistantMetastasis"));

		if (metastaseVorhanden) {
			res.add(tumor);
			if (distantMetastasisStringOrgan.equals("") || distantMetastasisStringOrgan.equals(null)) {
				res.add(this.hasPart("DistantMetastasisOfNeuroendocrinePancreasTumor"));
			} else {
				if (distantMetastasisStringOrgan.equals("Liver")) {
					res.add(this.hasPart("LiverMetastasisOfNeuroendocrinePancreasTumor"));
					res.add(this.notHasPart("OtherThanLiverMetastasisOfNeuroendocrinePancreasTumor"));
				}
				if (distantMetastasisStringOrgan.equals("notLiver")) {
					res.add(this.notHasPart("LiverMetastasisOfNeuroendocrinePancreasTumor"));
					res.add(this.hasPart("OtherThanLiverMetastasisOfNeuroendocrinePancreasTumor"));
				}
				if (distantMetastasisStringOrgan.equals("LiverAndNotLiver")) {
					res.add(this.hasPart("LiverMetastasisOfNeuroendocrinePancreasTumor"));
					res.add(this.hasPart("OtherThanLiverMetastasisOfNeuroendocrinePancreasTumor"));
				}
			}
			createIndividual(indname, factory, res);
		} // kein else bei p-Klassifikation!

	}

	@Override
	protected Set<OWLClass> getTumorClasses() {
		String iri = this.env.getOntologyIri(this.baseId);
		Set<OWLClass> res = this.getSubClasses("NeuroendocrinePancreasTumor", iri);
		res.addAll(this.getSubClasses("NeuroendocrinePancreasTumorAggregate", iri));
		return res;
	}

	@Override
	protected Set<OWLClass> getClassificationClasses() {
		return this.getSubClasses("RepresentationalUnitInNeuroendocrinePancreasTNM8Classification",
				this.env.getOntologyIri(this.baseId));
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

				// Metastase;
				String distantMetastasisStringYesNo = nextLine[this.dataReader.getIndex("Distant Metastasis")];
				String distantMetastasisStringOrgan = nextLine[this.dataReader.getIndex("Metastasis in Organ")];
				// primary tumour
				String noAssessmentPrimaryTumor = nextLine[this.dataReader.getIndex("NoAssessment")];
				String noEvidencePrimaryTumor = nextLine[this.dataReader.getIndex("NoEvidence")];
				String confinement = nextLine[this.dataReader.getIndex("Confinement")];
				String size = nextLine[this.dataReader.getIndex("Size")];
				String invasiveInBileDuctOrDuodenum = nextLine[this.dataReader
						.getIndex("Invasive in BileDuct or Duodenum")];
				String invasiveInSerosa = nextLine[this.dataReader.getIndex("Invasive in Serosa")];
				String invasiveInOtherOrgan = nextLine[this.dataReader.getIndex("Invasive in OtherOrgan")];

				result[i].put("Pancreas8neuroendo", new ArrayList<String>());

				// -- Abfrage-Fehler-Behandlung noch dazumachen!!!
				this.createIndividualPrimaryTumor(Integer.toString(i), noAssessmentPrimaryTumor, noEvidencePrimaryTumor,
						size, confinement, invasiveInBileDuctOrDuodenum, invasiveInSerosa, invasiveInOtherOrgan);
				result[i].get("Pancreas8neuroendo").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (numLymphNodes.equals("") || numLymphNodes.equals("null") || noAssessment.equals("NoAssessment")) {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), -1, false);

					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				} else {
					this.createIndividualRegionalLymphNodes(Integer.toString(i), Integer.parseInt(numLymphNodes), true);
					// result[i].get("Pancreas7").add(this.classify(Integer.toString(i)));
				}
				result[i].get("Pancreas8neuroendo").add(this.classify(Integer.toString(i)));
				this.env.tearDown(ChangeMode.TEMPORARY);

				if (distantMetastasisStringYesNo.equals("yes")) {
					this.createIndividualMetastasis(Integer.toString(i), true, distantMetastasisStringOrgan);
					result[i].get("Pancreas8neuroendo").add(this.classify(Integer.toString(i)));
					this.env.tearDown(ChangeMode.TEMPORARY);

				} // bei p: kein M0!!! also eher weglassen
				else {
					// this.createIndividualMetastasis(Integer.toString(i),
					// false);
					result[i].get("Pancreas8neuroendo").add("undefined_for_pTNM");
					this.env.tearDown(ChangeMode.TEMPORARY);
				}
				// result[i].get("Pancreas8neuroendo").add(this.classify(Integer.toString(i)));
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
					.replace("NeuroendocrinePancreasTNM8_", "").contains("p")) {
				classif = classe.toStringID().replace(this.env.getOntologyIri(this.baseId), "")
						.replace("NeuroendocrinePancreasTNM8_", "");
			}
		}
		return classif;
	}
}
