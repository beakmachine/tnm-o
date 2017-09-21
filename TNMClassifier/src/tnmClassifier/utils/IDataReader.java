package tnmClassifier.utils;

import java.io.IOException;

public interface IDataReader {

	String[] nextLine();

	int getIndex(String id);

	int countLines(String filename) throws IOException;

}