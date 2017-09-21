package tnmClassifier;

import java.util.List;
import java.util.Map;

public interface IOutputWriter {

	void output(Map<String, List<String>>[] out);

}