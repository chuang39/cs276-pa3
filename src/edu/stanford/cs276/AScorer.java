package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for a scorer. Need to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {
	
	Map<String,Double> idfs; // Map: term -> idf
    // Various types of term frequencies that you will need
	String[] TFTYPES = {"url","title","body","header","anchor"};
	
	public AScorer(Map<String,Double> idfs) {
		this.idfs = idfs;
	}
	
	// Score each document for each query.
	public abstract double getSimScore(Document d, Query q);
	
	// Handle the query vector
	public Map<String,Double> getQueryFreqs(Query q) {
		Map<String,Double> tfQuery = new HashMap<String, Double>(); // queryWord -> term frequency
		
		/*
		 * @//TODO : Your code here
		 */
		
		return tfQuery;
	}
	
	
	////////////// Initialization/Parsing Methods ///////////////
	
	/*
	 * @//TODO : Your code here
	 */

    /////////////////////////////////////////////////////////////
	
	
	/*/
	 * Creates the various kinds of term frequencies (url, title, body, header, and anchor)
	 * You can override this if you'd like, but it's likely that your concrete classes will share this implementation.
	 */
	public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) {
		// Map from tf type -> queryWord -> score
		Map<String,Map<String, Double>> tfs = new HashMap<String,Map<String, Double>>();
		
		////////////////////Initialization/////////////////////
		
		/*
		 * @//TODO : Your code here
		 */
		
	    ////////////////////////////////////////////////////////
		
		// Loop through query terms and increase relevant tfs. Note: you should do this to each type of term frequencies.
		for (String queryWord : q.queryWords) {
			/*
			 * @//TODO : Your code here
			 */
			
		}
		return tfs;
	}

}
