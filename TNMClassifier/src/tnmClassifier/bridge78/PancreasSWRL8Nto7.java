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
* @author Susanne Zabka, Aug-2017
*         Purpose: test the bridge with SWRL-rules (read data from
*         CSV-table, create individuals for each tumour property,
*         classify, output on console
*         Transformation from TNM8n to TNM7
* 
*/
public class PancreasSWRL8Nto7 extends BaseClassifier {
	private String tumorTypeColumnHeader;
	private String tumorConfinedColumnHeader;
	private String tumorInvasiveInSoftTissueColumnHeader;
	private String tumorInvasiveInBileDuctColumnHeader;
	private String tumorInvasiveInDuodenumColumnHeader;
	private String tumorInvasiveInSerosaColumnHeader;
	private String tumorInvasiveInBodyPartAdjacentToPancreasColumnHeader;
	private String tumorInvasiveInCeliacTrunkColumnHeader;
	private String tumorInvasiveInSupMesArtColumnHeader;

	public PancreasSWRL8Nto7(String inputDataPath) throws FileNotFoundException, IOException {

		super(inputDataPath);
		this.location = "pancreas_swrl8nto7";
		this.version = "bridge_7_8";

		this.tumorTypeColumnHeader = "Tumortype";
		this.tumorConfinedColumnHeader = "Confinement";// ja
		this.tumorInvasiveInSoftTissueColumnHeader = "InvasiveInPeripancreaticSoftTissue"; // Ja
		this.tumorInvasiveInBileDuctColumnHeader = "InvasiveInBileDuct";
		this.tumorInvasiveInDuodenumColumnHeader = "InvasiveInDuodenum";
		this.tumorInvasiveInSerosaColumnHeader = "InvasiveInSerosa";
		this.tumorInvasiveInBodyPartAdjacentToPancreasColumnHeader = "InvasiveInBodyPartAdjacentToPancreas";

		this.tumorInvasiveInCeliacTrunkColumnHeader = "InvasiveInCeliacTrunk";
		this.tumorInvasiveInSupMesArtColumnHeader = "InvasiveInSuperiorMesentericArtery";
	}

	@Override
	public void setUp() throws OWLOntologyCreationException {

		this.env.addTNMOAsBase("TNMO").addOntology("bridge_7_8", "pancreas_swrl8nto7", "TNMO")
				.addOntology("7", "pancreas", "TNMO").addOntology("8", "pancreas_neuroendocrine", "TNMO")
		;

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
				result[i].put("Pancreas8n", new ArrayList<String>());

				// Einlesen der Daten aus der Tabelle:
				String stringTNM8nclass = nextLine[dataReader.getIndex(tumorTypeColumnHeader)];

				String confined = nextLine[dataReader.getIndex(tumorConfinedColumnHeader)];
				String softTissue = nextLine[dataReader.getIndex(tumorInvasiveInSoftTissueColumnHeader)];
				String bileDuct = nextLine[dataReader.getIndex(tumorInvasiveInBileDuctColumnHeader)];
				String duodenum = nextLine[dataReader.getIndex(tumorInvasiveInDuodenumColumnHeader)];
				String serosa = nextLine[dataReader.getIndex(tumorInvasiveInSerosaColumnHeader)];
				String bodyPartAdjacentToPancreas = nextLine[dataReader
						.getIndex(tumorInvasiveInBodyPartAdjacentToPancreasColumnHeader)];
				String celTrunk = nextLine[dataReader.getIndex(tumorInvasiveInCeliacTrunkColumnHeader)];
				String supMesArt = nextLine[dataReader.getIndex(tumorInvasiveInSupMesArtColumnHeader)];

				OWLClassExpression tnm8nclass = factory.getOWLClass(IRI.create(getPancreas8nIri() + stringTNM8nclass));
				OWLNamedIndividual tumor = factory
						.getOWLNamedIndividual(IRI.create(this.env.getTNMOIri() + "testTumor_" + i));
				OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(tnm8nclass, tumor);
				this.env.addAxiom("TNMO", ax, ChangeMode.TEMPORARY).save("TNMO");

				System.out.println("");
				System.out.println(
						"----- Nr. " + i + " ----------------------------------------------------------------");
				System.out.println("TNM8n (Vorgabe): " + stringTNM8nclass);
				System.out.println("Info eingelesen:  Confin: " + confined
						+ " in SoftTissue: " + softTissue + " Bileduct " +
				 bileDuct + " Duodenum " +duodenum + "Serosa "+serosa + "Other " +bodyPartAdjacentToPancreas);


				if (confined.equals("Confined"))
					addTumorConfined("Confined", tumor, i);

				if (softTissue.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("PeripancreaticSoftTissue", tumor, i);

				if (bileDuct.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("BileDuct", tumor, i);
				if (duodenum.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("Duodenum", tumor, i);
				if (serosa.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("Serosa", tumor, i);
				if (bodyPartAdjacentToPancreas.equals("yes"))
					addTumorHasPartIsIncludedInOrgan(
							"BodyPartAdjacentToPancreasNotSoftTissueOrVesselOrBileDuctOrDuodenum", tumor, i);

				if (celTrunk.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("CeliacTrunk", tumor, i);

				if (supMesArt.equals("yes"))
					addTumorHasPartIsIncludedInOrgan("SuperiorMesentericArtery", tumor, i);


				OWLClass representationUnitTNM7 = factory.getOWLClass(
						IRI.create(getPancreas7Iri() + "RepresentationalUnitInPancreasTNM7ClinicalClassification"));
				OWLNamedIndividual tnm7 = addTumorIsRepresentedbyTNMversion(representationUnitTNM7, tumor, i, "7");

				this.env.save("TNMO");
				// ----------------Reasoner Teil--------------

				OWLReasoner reasoner = reasonerFactory.createReasoner(this.env.getOntology("TNMO"));

				System.out.println("run reasoner: ");// =========================

				NodeSet<OWLClass> typesSetindividual = reasoner.getTypes(tumor, true);
				Set<OWLClass> restypesSetindividual = typesSetindividual.getFlattened(); // Zwischenliste
				for (OWLClass k : typesSetindividual.getFlattened()) {
					if (!k.getIRI().getFragment().equals(stringTNM8nclass))
						System.out.println("Individual (ermittelt): " + k.getIRI().getFragment());
				}

				NodeSet<OWLClass> typesSet7 = reasoner.getTypes(tnm7, true);
				Set<OWLClass> resultSet7 = typesSet7.getFlattened(); // Zwischenliste
				for (OWLClass k : typesSet7.getFlattened()) {
					if (k == representationUnitTNM7) {
						resultSet7.remove(i);
					} else {
						System.out.println("TNM7 (ermittelt):                " + k.getIRI().getFragment());

					}
				}
				if (!(resultSet7.size() == 1) && !(typesSet7.isSingleton())) { // mehr
																				// als
																				// eine
																				// Klasse
																				// gefunden
					for (OWLClass k : typesSet7.getFlattened()) {
						if (!reasoner.getSubClasses(k, true).isBottomSingleton()) { // die
																					// "unterste
																					// Klasse"
																					// stehenlassen
							resultSet7.remove(k);
						}
						if (resultSet7.isEmpty()) {
							resultSet7 = typesSet7.getFlattened();
							NodeSet<OWLClass> checkSuperClasses = reasoner.getSuperClasses(k, false);

							for (OWLClass j : checkSuperClasses.getFlattened()) {
								if (k == j) {
									resultSet7.remove(i);
								}
							}
						}
					}
					for (OWLClass j : resultSet7) {
					}

				}
				reasoner.dispose(); // Disposes of this reasoner. This frees up
									// any resources used by the reasoner and
									// detaches the reasoner as an
									// OWLOntologyChangeListener from the
									// OWLOntologyManager that manages the
									// ontologies contained within the reasoner.

			}
			this.env.tearDown(ChangeMode.TEMPORARY);
			i++;

		}

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


	public void addTumorConfined(String confinementValue, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual confinementIndividual = addIndividual("Confinement", "confinement", i, "TNMO");
		OWLNamedIndividual confinedValueIndividual = addIndividual(confinementValue, "confinedValue", i, "TNMO");

		OWLObjectProperty isBearerOf = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "isBearerOf"));
		OWLObjectProperty projectsOnto = factory
				.getOWLObjectProperty(IRI.create(this.env.getBioTopLight2Iri() + "projectsOnto"));

		OWLObjectPropertyAssertionAxiom isBearerOfConfinementAxiom = factory
				.getOWLObjectPropertyAssertionAxiom(isBearerOf, tumor, confinementIndividual);
		OWLObjectPropertyAssertionAxiom projectsOntoConfinedValueAxiom = factory
				.getOWLObjectPropertyAssertionAxiom(projectsOnto, confinementIndividual, confinedValueIndividual);

		this.env.addAxiom("TNMO", isBearerOfConfinementAxiom, ChangeMode.TEMPORARY).addAxiom("TNMO",
				projectsOntoConfinedValueAxiom, ChangeMode.TEMPORARY);

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

	public void addTumorHasPartIsIncludedInOrgan(String infiltratedOrgan, OWLNamedIndividual tumor, int i) {
		OWLDataFactory factory = this.env.getDataFactory();
		OWLNamedIndividual infiltratedOrganIndividual;
		if (infiltratedOrgan.equals("BodyPartAdjacentToPancreasNotSoftTissueOrVesselOrBileDuctOrDuodenum")) {
			infiltratedOrganIndividual = addIndividual(infiltratedOrgan, "infiltOrgan_", i, "SWRL8nto7");
		} else {
			infiltratedOrganIndividual = addIndividual(infiltratedOrgan, "infiltOrgan_", i, "BodyPart");

		}
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
				dummy, infiltratedOrganIndividual);

		this.env.addAxiom("TNMO", hasPartAxiom, ChangeMode.TEMPORARY).addAxiom("TNMO", isIncludedInAxiom,
				ChangeMode.TEMPORARY);

		return;
	}

	// check, ob man diese Info von woanders herholen kann
	public String getOntologyIRI(String whichOntology) {

		String ontologyIRI = "main";
		if (whichOntology.equals("Pancreas7"))
			ontologyIRI = getPancreas7Iri();

		if (whichOntology.equals("Pancreas8n"))
			ontologyIRI = getPancreas8nIri();

		if (whichOntology.equals("SWRL8nto7"))
			ontologyIRI = getSWRL8nto7Iri();

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

	public String getSWRL8nto7Iri() {
		return "http://purl.org/tnmo/pancreas_swrl_8nTo7.owl".concat("#");
	}

	public String getPancreas7Iri() {
		return "http://purl.org/tnmo/TNM-O_Pancreas_7.owl".concat("#");
	}

	public String getPancreas8nIri() {
		return "http://purl.org/tnmo/TNM-O_Pancreas_8neuroendocrine.owl".concat("#");
	}

}
