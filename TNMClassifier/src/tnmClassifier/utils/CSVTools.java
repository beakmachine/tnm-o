package tnmClassifier.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class CSVTools implements IDataReader {
	String pathCsv;
    String[] indices;
    BufferedReader csv;

    public CSVTools(String pathCsv) throws FileNotFoundException, IOException {
        this.pathCsv = pathCsv;
        FileReader fr = new FileReader(pathCsv);
        this.csv = new BufferedReader(fr);
        this.buildIndicesArray();
    }
    
    /* (non-Javadoc)
	 * @see tnmcr.IDataReader#nextLine()
	 */
    @Override
	public String[] nextLine() {
    	String next;
        try {
	        while ((next = this.csv.readLine()) != null) {
	            if (next != null) {
	                return next.split("\\,", -1);
	            }
	    	}
        }
        catch(IOException ex) {}
        return null;
    }
 
    private void buildIndicesArray() throws IOException {
    	String next = this.csv.readLine();
        this.indices = next.split("\\,", -1);
    }
    
    /* (non-Javadoc)
	 * @see tnmcr.IDataReader#getIndex(java.lang.String)
	 */
    @Override
	public int getIndex(String id) {
        int index = -1;

        for (int i = 0; (i < this.indices.length) && (index == -1); i++) {
        	if (this.indices[i].equals(id)) {
                index = i;
            }
        }
        return index;
    }

	/* (non-Javadoc)
	 * @see tnmcr.IDataReader#countLines(java.lang.String)
	 */
	@Override
	public int countLines(String filename) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        }
    }
}
