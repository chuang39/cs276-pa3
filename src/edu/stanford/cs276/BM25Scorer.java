package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 Scorer in Task 2.
 */
public class BM25Scorer extends AScorer {
	Map<Query,Map<String, Document>> queryDict; // query -> url -> document

	public BM25Scorer(Map<String,Double> idfs, Map<Query,Map<String, Document>> queryDict) {
		super(idfs);
		this.queryDict = queryDict;
		this.calcAverageLengths();
	}


	/////////////// Weights /////////////////
	double urlweight = -1;
	double titleweight  = -1;
	double bodyweight = -1;
	double headerweight = -1;
	double anchorweight = -1;

	/////// BM25 specific weights ///////////
	double burl=-1;
	double btitle=-1;
	double bheader=-1;
	double bbody=-1;
	double banchor=-1;

	double k1=-1;
	double pageRankLambda=-1;
	double pageRankLambdaPrime=-1;
	//////////////////////////////////////////

	/////// BM25 data structures - feel free to modify ///////

	Map<Document,Map<String,Double>> lengths; // Document -> field -> length
	Map<String,Double> avgLengths;  // field name -> average length
	Map<Document,Double> pagerankScores; // Document -> pagerank score

	//////////////////////////////////////////

	// Set up average lengths for bm25, also handles pagerank
	public void calcAverageLengths() {
		lengths = new HashMap<Document,Map<String,Double>>();
		avgLengths = new HashMap<String,Double>();
		pagerankScores = new HashMap<Document,Double>();
		
		/*
		 * @//TODO : Your code here
		 */
		
		//normalize avgLengths
		for (String tfType : this.TFTYPES) {
			/*
			 * @//TODO : Your code here
			 */
		}

	}

	////////////////////////////////////


	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d) {
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */
		
		return score;
	}

	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
	}


	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);

	    return getNetScore(tfs,q,tfQuery,d);
	}
	
}
