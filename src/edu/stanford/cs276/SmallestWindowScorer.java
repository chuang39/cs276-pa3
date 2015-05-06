package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 */
public class SmallestWindowScorer extends BM25Scorer {

	/////// Smallest window specific hyper-parameters ////////
	double B = -1;    	    
	double boostmod = -1;

	//////////////////////////////
	
	public SmallestWindowScorer(Map<String, Double> idfs,Map<Query,Map<String, Document>> queryDict) {
		super(idfs, queryDict);
		handleSmallestWindow();
	}

	
	public void handleSmallestWindow() {
		/*
		 * @//TODO : Your code here
		 */
	}

	
	public double checkWindow(Query q,String docstr,double curSmallestWindow,boolean isBodyField) {
		/*
		 * @//TODO : Your code here
		 */
		return -1;
	}
	
	
	@Override
	public double getSimScore(Document d, Query q) {
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);
		
		return 0;
	}

}
