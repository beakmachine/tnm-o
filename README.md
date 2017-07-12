# tnm-o

## The TNM Ontology

*tnm-o* is the abbreviation for the **TNM Ontology**. The TNM Ontology represents the TNM Classification of Malignant Tumours (TNM) in description logic (OWL). For a detailed report on *tnm-o* see: https://jbiomedsem.biomedcentral.com/articles/10.1186/s13326-016-0106-9. 

Objective of this project is to represent the complete TNM Classification system in description logic and provide formal transformation rules between versions. Based on this representation, data on clinical and pathological findings can be classified automatically by documentation systems and (partially) transformed between TNM versions.

*tnm-o* consists of a stack of hierarchically organized modules:

 * **BioTop** Toplevel Domain Ontology (https://biotopontology.github.io/) defines the foundational classes and relations for *tnm-o* and thus its underlying ontological framework.
 * **TNM-O**: a hub ontology providing common classes and definitions which are used by all organ-specific modules of *tnm-o*.  TNM-O is the hup in which other modules *can be* loaded depending on the application context.
 * **TNM-Anatomy**: an ontology providing definitions for common anatomical entities
 * **Organ specific modules**: representations of TNM definitions according to the organ-specific structure of the TNM classification system for different versions
 * **Transformation rules**: organ-specific, formal rules for the transformation between different versions


### Organ-specific modules 

| represented organ | v6 | v7 | v8 |
| :-----------------|:--:|:--:|:--:|
| Breast            |    | X  |    |
| Colon/ Rectum     | X  | X  |    |
| Lung              | X  | X  |    |
| Pancreas          |    | X  | X  |


### Contributors

 * Martin Boeker (University of Freiburg)
 * Peter Bronsert (University of Freiburg)
 * Oliver Brunner (University of Freiburg)
 * Rita Faria
 * Fabio Franca
 * Stefan Schulz (Medical University of Graz)
 * Susanne Zabka (University of Freiburg)acy
 
 This repository is under construction.
 
 Legacy repository of this project: http://purl.org/tnmo/
