package tnmClassifier.bridge78;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import tnmClassifier.BaseClassifier;
import tnmClassifier.ClassifierEnvironment.ChangeMode;
import tnmClassifier.utils.CSVTools;
import tnmClassifier.utils.IDataReader;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Susanne Zabka, Jul/Aug-2017, updates: Oliver Brunner 
 *         Purpose: test the bridge with SWRL-rules (read data from
 *         CSV-table, create individuals for each tumour property,
 *         classify, output on console
 *         Transformation from TNM7 to TNM8e and TNM8n
 * 
 */

public class PancreasSWRL7to8 extends BaseClassifier {
	private String tumorTypeColumnHeader;
	private String tumorOrganColumnHeader;
	private String tumorTorNorMColumnHeader;
	private String tumorSizeColumnHeader;
	private String tumorInvasiveInSoftTissueColumnHeader;
	private String tumorInvasiveInComHepArtColumnHeader;
	private String tumorMetaLKColumnHeader;
	private String tumorDistMetaOrganColumnHeader;

	public PancreasSWRL7to8(String inputDataPath) throws FileNotFoundException, IOException {
		super(inputDataPath);
		this.location = "pancreas_swrl7to8";
		this.version = "bridge_7_8";

		this.tumorTypeColumnHeader = "Tumortype";
		this.tumorOrganColumnHeader = "isIncludedIn";
		this.tumorTorNorMColumnHeader = "T or N or M";// ?
		this.tumorSizeColumnHeader = "Size";// ja
		this.tumorInvasiveInSoftTissueColumnHeader = "InvasiveInPeripancreaticSoftTissue"; // Ja
		this.tumorInvasiveInComHepArtColumnHeader = "InvasiveInCommonHepaticArtery";
		this.tumorMetaLKColumnHeader = "Meta LymphNodes";
		this.tumorDistMetaOrganColumnHeader = "Dist Meta Org";
	}

	@Override
	public void setUp() throws OWLOntologyCreationException {

		this.env.addTNMOAsBase("TNMO")
			.addOntology("bridge_7_8", "pancreas_swrl7to8", "TNMO")
			.addOntology("7", "pancreas", "TNMO")
			.addOntology("8", "pancreas_exocrine", "TNMO")
			.addOntology("8", "pancreas_neuroendocrine", "TNMO");
		
		}

	public Map<String, List<String>>[] run()
			throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		OWLDataFactory factory = this.env.getDataFactory();

		// ReasonerFactory vor der Schleife -Performance???
		OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		Configuration configuration = new Configuration(); //
		configuration.ignoreUnsupportedDatatypes = true;

		String[] nextLine;

		int i = 0;
		IDataReader dataReader = new CSVTools(inputDataPath);

		// Hinweis: Map nicht gebraucht, da Ausgabe direkt auf der Konsole
		Map<String, List<String>>[] result = new HashMap[dataReader.countLines(inputDataPath)];

		while ((nextLine = dataReader.nextLine()) != null) {
			if (nextLine != null && i >= 0) {
				result[i] = new HashMap<String, List<String>>();
				result[i].put("Pancreas7", new ArrayList<String>());

				// Einlesen der Daten aus der Tabelle:
				String stringTNM7class = nextLine[dataReader.getIndex(tumorTypeColumnHeader)];
				String torNorM = nextLine[dataReader.getIndex(tumorTorNorMColumnHeader)];
				String organ = nextLine[dataReader.getIndex(tumorOrganColumnHeader)];
				String size = nextLine[dataReader.getIndex(tumorSizeColumnHeader)];
				String softTissue = nextLine[dataReader.getIndex(tumorInvasiveInSoftTissueColumnHeader)];
				String comHepArt = nextLine[dataReader.getIndex(tumorInvasiveInComHepArtColumnHeader)];

				String metaLK = nextLine[dataReader.getIndex(tumorMetaLKColumnHeader)];
				String distMetaOrg = nextLine[dataReader.getIndex(tumorDistMetaOrganColumnHeader)];

				OWLClassExpression tnm7class = factory.getOWLClass(IRI.create(getPancreas7Iri() + stringTNM7class));
				OWLNamedIndividual tumor = factory
						.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + "testTumor_" + i));
				OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(tnm7class, tumor);
				this.env.addAxiom("TNMO", ax, ChangeMode.TEMPORARY).save("TNMO");

				if (torNorM.equals("T")) {
					addTumorIsIncludedInOrgan(organ, tumor, i);
				} else {
					addTumorIsMetastaticStructureOfOrgan(organ, tumor, i);
				}

				System.out.println("");
				System.out.println(
						"----- Nr. " + i + " ----------------------------------------------------------------");
				System.out.println("TNM7 (Vorgabe): " + stringTNM7class);
				if (torNorM.equals("T"))
					System.out.println("Info eingelesen (" + torNorM + "): Organ: " + organ + " Size: " + size
							+ " in SoftTissue: " + softTissue + " Infilt.ComHepArt: " + comHepArt);
				if (torNorM.equals("N") || torNorM.equals("M"))
					System.out.println("Info eingelesen (" + torNorM + "): Prim�rTumor-Organ: " + organ + " Number LK: "
							+ metaLK + " Dist.Meta in: " + distMetaOrg);

				
				if (!size.equals(""))
					addTumorHasSize(size, tumor, i);

				if (softTissue.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("PeripancreaticSoftTissue", tumor, i);

				if (comHepArt.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("CommonHepaticArtery", tumor, i);

				if (metaLK.equals("1<3"))
					addNumberofMetaLympNodes("Cardinality1to3", tumor, i);

				if (metaLK.equals(">4"))
					addNumberofMetaLympNodes("Cardinality4orMore", tumor, i);

				if (!distMetaOrg.equals("")) {
					addHasPartMetaLocation(distMetaOrg, tumor, i);

				}

				OWLClass representationUnitTNM8e = factory.getOWLClass(IRI.create(
						getPancreas8exoIri() + "RepresentationalUnitInExocrinePancreasTNM8ClinicalClassification"));
				OWLClass representationUnitTNM8n = factory.getOWLClass(IRI.create(getPancreas8neIri()
						+ "RepresentationalUnitInNeuroendocrinePancreasTNM8ClinicalClassification"));

				OWLNamedIndividual tnm8e = addTumorIsRepresentedbyTNMversion(representationUnitTNM8e, tumor, i, "8e");
				OWLNamedIndividual tnm8n = addTumorIsRepresentedbyTNMversion(representationUnitTNM8n, tumor, i, "8n");

				this.env.save("TNMO");
				// ----------------Reasoner Teil--------------

				OWLReasoner reasoner = reasonerFactory.createReasoner(this.env.getOntology("TNMO"));
				// --------test SWRL engine----------------
				// OWLOntology ontology = this.env.getOntology("SWRL7_8");
				// SWRLRuleEngine ruleEngine =
				// SWRLAPIFactory.createSWRLRuleEngine(ontology);
				// ruleEngine.infer();
				// OWLReasoner reasoner2 =
				// reasonerFactory.createReasoner(this.env.getOntology("TNMO"));

				System.out.println("run reasoner: ");// =========================

				// Hinweis: Reasoner-Teil war vorher hier
				// CHECK ob Classifier anders gemacht werden kann - ist auch
				// redundant...
				// --neu
				// NodeSet<OWLClass> typesSetindividual =
				// reasoner.getTypes(individual, true);
				NodeSet<OWLClass> typesSetindividual = reasoner.getTypes(tumor, true);
				Set<OWLClass> restypesSetindividual = typesSetindividual.getFlattened(); // Zwischenliste
				for (OWLClass k : typesSetindividual.getFlattened()) {
					if (!k.getIRI().getFragment().equals(stringTNM7class))
						System.out.println("Individual (ermittelt): " + k.getIRI().getFragment());
				}

				NodeSet<OWLClass> typesSet8e = reasoner.getTypes(tnm8e, true); 
				Set<OWLClass> resultSet8e = typesSet8e.getFlattened(); // Zwischenliste
				for (OWLClass k : typesSet8e.getFlattened()) {
					if (k == representationUnitTNM8e) {
						resultSet8e.remove(i);
					} else {
						System.out.println("TNM8e (ermittelt):                " + k.getIRI().getFragment());

					}
				}
				if (!(resultSet8e.size() == 1) && !(typesSet8e.isSingleton())) { // mehr
																					// als
																					// eine
																					// Klasse
																					// gefunden
					for (OWLClass k : typesSet8e.getFlattened()) {
						if (!reasoner.getSubClasses(k, true).isBottomSingleton()) { // die
																					// "unterste
																					// Klasse"
																					// stehenlassen
							resultSet8e.remove(k);
						}
						if (resultSet8e.isEmpty()) {
							resultSet8e = typesSet8e.getFlattened();
							NodeSet<OWLClass> checkSuperClasses = reasoner.getSuperClasses(k, false);

							for (OWLClass j : checkSuperClasses.getFlattened()) {
								if (k == j) {
									resultSet8e.remove(i);
								}
							}
						}
					}
					for (OWLClass j : resultSet8e) {
					}

				}
				NodeSet<OWLClass> typesSet8n = reasoner.getTypes(tnm8n, true);
				Set<OWLClass> resultSet8n = typesSet8n.getFlattened(); // Zwischenliste
				for (OWLClass k : typesSet8n.getFlattened()) {

					if (k == representationUnitTNM8n) {
						resultSet8n.remove(k);
					} else {
						System.out.println("TNM8n (ermittelt):                " + k.getIRI().getFragment());
					}
				}
				reasoner.dispose(); // Disposes of this reasoner. This frees up
									// any resources used by the reasoner and
									// detaches the reasoner as an
									// OWLOntologyChangeListener from the
									// OWLOntologyManager that manages the
									// ontologies contained within the reasoner.

			}
			// reasoner.dispose(); //Disposes of this reasoner. This frees up
			// any resources used by the reasoner and detaches the reasoner as
			// an OWLOntologyChangeListener from the OWLOntologyManager that
			// manages the ontologies contained within the reasoner.
			this.env.tearDown(ChangeMode.TEMPORARY);
			i++;

		}
		// reasoner.dispose(); //Disposes of this reasoner. This frees up any
		// resources used by the reasoner and detaches the reasoner as an
		// OWLOntologyChangeListener from the OWLOntologyManager that manages
		// the ontologies contained within the reasoner.

		return new Map[0]; // wird hier nicht gebraucht

	}

	public OWLNamedIndividual addTumorIsRepresentedbyTNMversion(OWLClass representationUnitTNM,
			OWLNamedIndividual tumor, int i, String tnmversion) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual tnm = factory
				.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + "TNM" + tnmversion + "_" + i));
		OWLObjectProperty isRepresentedBy = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isRepresentedBy"));
		OWLObjectPropertyAssertionAxiom isRepresentedByTNMproperty = factory
				.getOWLObjectPropertyAssertionAxiom(isRepresentedBy, tumor, tnm);
		OWLClassAssertionAxiom addTNMRepresentationUnit = factory.getOWLClassAssertionAxiom(representationUnitTNM, tnm);
		this.env.addAxiom("TNMO", addTNMRepresentationUnit, ChangeMode.TEMPORARY).addAxiom("TNMO",
				isRepresentedByTNMproperty, ChangeMode.TEMPORARY);
		return tnm;
	}

	public OWLNamedIndividual addTumorIsIncludedInOrgan(String organ, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLClass organClass = factory.getOWLClass(IRI.create(this.env.getBodyPartIri() + organ));

		OWLNamedIndividual organIndividual = factory
				.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + "_Organ" + i));
		OWLObjectProperty isIncludedIn = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));
		OWLObjectPropertyAssertionAxiom isIncludedInOrgan = factory.getOWLObjectPropertyAssertionAxiom(isIncludedIn,
				tumor, organIndividual);
		OWLClassAssertionAxiom addTumorIsIncludedInOrgan = factory.getOWLClassAssertionAxiom(organClass,
				organIndividual);
		this.env.addAxiom("TNMO", addTumorIsIncludedInOrgan, ChangeMode.TEMPORARY).addAxiom("TNMO", isIncludedInOrgan,
				ChangeMode.TEMPORARY);
		return organIndividual;
	}

	public void addTumorHasSize(String sizeValue, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual sizeIndividual = addIndividual("Size", "size", i, "TNMO");
		OWLNamedIndividual sizeValueIndividual = addIndividual(sizeValue, "sizeValue", i, "TNMO");

		OWLObjectProperty isBearerOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
		OWLObjectProperty projectsOnto = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));

		OWLObjectPropertyAssertionAxiom isBearerOfSizeAxiom = factory.getOWLObjectPropertyAssertionAxiom(isBearerOf,
				tumor, sizeIndividual);
		OWLObjectPropertyAssertionAxiom projectsOntoSizeValueAxiom = factory
				.getOWLObjectPropertyAssertionAxiom(projectsOnto, sizeIndividual, sizeValueIndividual);

		this.env.addAxiom("TNMO", isBearerOfSizeAxiom, ChangeMode.TEMPORARY).addAxiom("TNMO",
				projectsOntoSizeValueAxiom, ChangeMode.TEMPORARY);

		return;
	}

	public void addNumberofMetaLympNodes(String numberLK, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual cardinalityIndividual = addIndividual("Cardinality", "cardinality_", i, "TNMO");
		OWLNamedIndividual cardinalityValueIndividual = addIndividual(numberLK, "numberLK_", i, "TNMO");

		OWLObjectProperty isBearerOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
		OWLObjectProperty projectsOnto = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));

		OWLObjectPropertyAssertionAxiom isBearerOfQualityAxiom = factory.getOWLObjectPropertyAssertionAxiom(isBearerOf,
				tumor, cardinalityIndividual);
		OWLObjectPropertyAssertionAxiom projectsOntoQualityValueAxiom = factory
				.getOWLObjectPropertyAssertionAxiom(projectsOnto, cardinalityIndividual, cardinalityValueIndividual);

		this.env.addAxiom("TNMO", isBearerOfQualityAxiom, ChangeMode.TEMPORARY).addAxiom("TNMO",
				projectsOntoQualityValueAxiom, ChangeMode.TEMPORARY);

		return;
	}

	/**
	 * SZ, 2017-08-02 creates Individuals with structure
	 * "MetastaticLymphNode/DistMetastasis isPartOf MalignantAnatomicalStructure
	 * hasPart PancreasTumor isIncludedIn PrimaryTumorOrgan"
	 * 
	 * @param: String,
	 *             OWLNamedIndividual, OWLDataFactory, int
	 */
	public void addTumorIsMetastaticStructureOfOrgan(String primarytumorOrgan, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual malignantStructure = addIndividual("MalignantAnatomicalStructure",
				"MalignantAnatomicalStructure_", i, "TNMO");
		OWLNamedIndividual pancreas = addIndividual("PancreasTumor", "PancreasTumor_", i, "Pancreas7");
		addTumorIsIncludedInOrgan(primarytumorOrgan, pancreas, i);
		OWLObjectProperty isPartOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isPartOf"));

		OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));

		OWLObjectPropertyAssertionAxiom hasPartAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasPart,
				malignantStructure, pancreas);
		OWLObjectPropertyAssertionAxiom isPartOfAxiom = factory.getOWLObjectPropertyAssertionAxiom(isPartOf, tumor,
				malignantStructure);

		this.env.addAxiom("TNMO", hasPartAxiom, ChangeMode.TEMPORARY);
		this.env.addAxiom("TNMO", isPartOfAxiom, ChangeMode.TEMPORARY);

		return;
	}

	public void addHasPartMetaLocation(String distMetaOrg, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();

		OWLNamedIndividual meta1 = null;
		OWLNamedIndividual meta2 = null;
		if (distMetaOrg.equals("Liver")) {
			meta1 = addIndividual("LiverMetastasisOfNeuroendocrinePancreasTumor", "meta1Organ_", i, "Pancreas8ne");
			meta2 = addIndividual("NoOtherThanLiverMetastasisOfNeuroendocrinePancreasTumor", "meta2Organ_", i,
					"SWRL7_8");
		}
		if (distMetaOrg.equals("OtherNotLiver")) {
			meta1 = addIndividual("NoLiverMetastasisOfNeuroendocrinePancreasTumor", "meta1Organ_", i, "SWRL7_8");
			meta2 = addIndividual("OtherThanLiverMetastasisOfNeuroendocrinePancreasTumor", "meta2Organ_", i,
					"Pancreas8ne");
		}
		if (distMetaOrg.equals("LiverAndOther")) {
			meta1 = addIndividual("LiverMetastasisOfNeuroendocrinePancreasTumor", "meta1Organ_", i, "Pancreas8ne");
			meta2 = addIndividual("OtherThanLiverMetastasisOfNeuroendocrinePancreasTumor", "meta2Organ_", i,
					"Pancreas8ne");
		}

		OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));

		OWLObjectPropertyAssertionAxiom hasPartMeta1Axiom = factory.getOWLObjectPropertyAssertionAxiom(hasPart, tumor,
				meta1);
		OWLObjectPropertyAssertionAxiom hasPartMeta2Axiom = factory.getOWLObjectPropertyAssertionAxiom(hasPart, tumor,
				meta2);

		this.env.addAxiom("TNMO", hasPartMeta1Axiom, ChangeMode.TEMPORARY).addAxiom("TNMO", hasPartMeta2Axiom,
				ChangeMode.TEMPORARY);

		return;
	}

	public void addTumorHasPartIsIncludedInOrgan(String infiltratedOrgan, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual softTissue = addIndividual(infiltratedOrgan, "infiltOrgan_", i, "BodyPart");

		OWLNamedIndividual dummy = factory.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + "dummy_" + i));
		OWLClass thing = factory.getOWLThing();
		OWLClassAssertionAxiom adddummyAxiom = factory.getOWLClassAssertionAxiom(thing, dummy);

		this.env.addAxiom("TNMO", adddummyAxiom, ChangeMode.TEMPORARY);

		OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "hasPart"));
		OWLObjectProperty isIncludedIn = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isIncludedIn"));

		OWLObjectPropertyAssertionAxiom hasPartAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasPart, tumor,
				dummy);
		OWLObjectPropertyAssertionAxiom isIncludedInAxiom = factory.getOWLObjectPropertyAssertionAxiom(isIncludedIn,
				dummy, softTissue);

		this.env.addAxiom("TNMO", hasPartAxiom, ChangeMode.TEMPORARY).addAxiom("TNMO", isIncludedInAxiom,
				ChangeMode.TEMPORARY);

		return;
	}

	// check, ob man diese Info von woanders herholen kann
	public String getOntologyIRI(String whichOntology) {
		String ontologyIRI = "main";
		if (whichOntology.equals("Pancreas7"))
			ontologyIRI = getPancreas7Iri();

		if (whichOntology.equals("Pancreas8e"))
			ontologyIRI = getPancreas8exoIri();

		if (whichOntology.equals("Pancreas8ne"))
			ontologyIRI = getPancreas8neIri();

		if (whichOntology.equals("SWRL7_8"))
			ontologyIRI = getSWRL7to8Iri();

		if (whichOntology.equals("TNMO"))
			ontologyIRI = this.env.getTNMOIri();

		if (whichOntology.equals("BodyPart"))
			ontologyIRI = this.env.getBodyPartIri();

		return ontologyIRI;

	}

	public OWLNamedIndividual addIndividual(String individualClass, String individualName, int i,
			String whichOntology) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual individual = factory
				.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + individualName + i));
		OWLClass theClass = factory.getOWLClass(IRI.create(getOntologyIRI(whichOntology) + individualClass));
		OWLClassAssertionAxiom addIndividual = factory.getOWLClassAssertionAxiom(theClass, individual);
		this.env.addAxiom("TNMO", addIndividual, ChangeMode.TEMPORARY);
		return individual;
	}

	public String getSWRL7to8Iri() {
		return "http://purl.org/tnmo/PancreasSWRL_7to8.owl".concat("#");
	}

	public String getPancreas7Iri() {
		return "http://purl.org/tnmo/TNM-O_Pancreas_7.owl".concat("#");
	}

	public String getPancreas8exoIri() {
		return "http://purl.org/tnmo/TNM-O_Pancreas_8exocrine.owl".concat("#");
	}

	public String getPancreas8neIri() {
		return "http://purl.org/tnmo/TNM-O_Pancreas_8neuroendocrine.owl".concat("#");
	}

}
