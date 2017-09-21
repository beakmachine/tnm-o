package tnmClassifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultOutput implements IOutputWriter {
	/* (non-Javadoc)
	 * @see tnmcr.IOutputWriter#output(java.util.Map)
	 */
	@Override
	public void output( Map<String, List<String>>[] out) {
		System.out.println("### RESULTS ###");
		for(int i = 0; i < out.length; ++i) {
			if(out[i] != null) {
				this.printInstance(out[i], i);
			}
		}
	}
	
	private void printInstance(Map<String, List<String>> out, int id) {
		System.out.println("Instance:" + Integer.toString(id));
		
		Iterator<Map.Entry<String, List<String>>> it = out.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> pair = it.next();
			String output = pair.getKey() + ": ";
			for(int i = 0; i < pair.getValue().size();++i ){
				output = output.concat(pair.getValue().get(i) + " ");
			}
			System.out.println(output);
			it.remove(); // avoids a ConcurrentModificationException
		}	
	}
}