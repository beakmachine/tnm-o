package tnmClassifier.utils;

import java.io.File;

/**
*
* @author Oliver Brunner
*  Wrapper for all file system operations  
*/
public class FileSystemConnector {
	private String ontologyDirPath;
	
	public FileSystemConnector(){
		this.ontologyDirPath = "./../TNMO";
	}
	
	public String getTNMOPath() {
		return this.ontologyDirPath + "/TNM-O.owl";
	}
	
	public String getBodyPartsPath() {
		return this.ontologyDirPath + "/TNM-O_BodyParts.owl";
	}
	
	/**
	 * Prints all available TNMO classifiers of a given version to the console
	 * @param version The TNMO version you want to see tha available classifiers for 
	 */
	public void listOntologies(String version) {
		File folder = new File(this.ontologyDirPath + "/TNMO" + version);
		if (this.versionDoesNotExist(folder)) return;
		
		File[] listOfFiles = folder.listFiles();
		System.out.println("Available ontologies for TNMO v" + version + ":");
		if (this.directoryIsEmpty(listOfFiles)) return;
		
	    for (int i = 0; i < listOfFiles.length; i++) {
	        System.out.println(listOfFiles[i].getName());
	    }
	}
	
	private Boolean directoryIsEmpty(File[] listOfFiles) {
		if(listOfFiles.length == 0) {
			System.out.println("No ontologies are available in this version");
			return true;
		}
		return false;
	}
	
	private Boolean versionDoesNotExist(File folder) {
		if(!folder.exists()) {
			System.out.println("Error: The chosen version does not exist");
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether there exists a classifier for the given tumor location in the given TNMO version
	 * @param version TNMO version to check
	 * @param organ Tumor location to check
	 * @return True if there exists a classifier, false else
	 */
	public Boolean ontologyExists(String version, String organ){
		return new File(this.ontologyDirPath + "/TNMO" + version + "/" + organ.toLowerCase() + ".owl").exists();
	}
	
	/**
	 * Get a file handle of a specified ontology in a specified version
	 * @param version TNM version to load
	 * @param location Tumor location to load
	 * @return
	 */
	public File getOntologyFile(String version, String location) {
		return new File(this.ontologyDirPath + "/TNMO" + version + "/" + location.toLowerCase() + ".owl");
	}
}
