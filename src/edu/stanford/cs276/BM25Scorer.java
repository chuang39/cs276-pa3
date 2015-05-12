package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	double urlweight = 1;
	double titleweight  = 1;
	double bodyweight = 1;
	double headerweight = 1;
	double anchorweight = 1;

	/////// BM25 specific weights ///////////
	double burl=1;
	double btitle=1;
	double bheader=1;
	double bbody=1;
	double banchor=1;

	double k1=1;
	double pageRankLambda=1;
	double pageRankLambdaPrime=1;
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
		
		// Get a list of all the documents
		// TODO maybe this should be a list?????
		
		Set<Document> docs = new HashSet<Document>();
		
		for (Query query : queryDict.keySet()) {
			
			for (String url : queryDict.get(query).keySet()) {
				docs.add(queryDict.get(query).get(url));
			}
		}
		
		int numDocs = docs.size();
		
		int sumUrls = 0;
		int sumTitles = 0;
		int sumBody = 0;
		int sumHeader = 0;
		int sumAnchors = 0;
		
		// Compute the length of each field of each doc
		
		for (Document doc : docs) {
			
			Map<String, Double> docLengths = new HashMap<String, Double>();
			
			// URL
			int lengthUrl = 0;
			if (doc.url != null) {
				lengthUrl = doc.url.split("\\W+").length;
			}
			docLengths.put(TFTYPES[0], (double) lengthUrl);
			sumUrls = sumUrls + lengthUrl;
			
			// Title
			int lengthTitle = 0;
			if (doc.title != null) {
				lengthTitle = doc.title.split("\\s+").length;
			}
			docLengths.put(TFTYPES[1], (double) lengthTitle);
			sumTitles = sumTitles + lengthTitle;

			// Body
			int lengthBody = 0;
			if (doc.body_hits != null) {
				lengthBody = doc.body_length;
			}
			docLengths.put(TFTYPES[2], (double) lengthBody);
			sumBody = sumBody + lengthBody;

			// Header
			int lengthHeader = 0;
			if (doc.headers != null) {
				for (String header : doc.headers) {
					lengthHeader = lengthHeader + header.split("\\s+").length;
				}
			}
			sumHeader = sumHeader + lengthHeader;
			
			docLengths.put(TFTYPES[3], (double) lengthHeader);

			// Anchor
			int lengthAnchor = 0;
			if (doc.anchors != null) {
				for (String anchor : doc.anchors.keySet()) {
					
					int count = doc.anchors.get(anchor);
					
					lengthAnchor = lengthAnchor + anchor.split("\\s+").length * count;
				}
			}
			docLengths.put(TFTYPES[4], (double) lengthAnchor);
			sumAnchors = sumAnchors + lengthAnchor;
			
			lengths.put(doc, docLengths);
		}
		
		// Compute averages
		
		// URL
		avgLengths.put(TFTYPES[0], ((double) sumUrls) / numDocs);

		// Title
		avgLengths.put(TFTYPES[1], ((double) sumTitles) / numDocs);

		// Body
		avgLengths.put(TFTYPES[2], ((double) sumBody) / numDocs);

		// Header
		avgLengths.put(TFTYPES[3], ((double) sumHeader) / numDocs);

		// Anchor
		avgLengths.put(TFTYPES[4], ((double) sumAnchors) / numDocs);
		
		//normalize avgLengths
//		for (String tfType : this.TFTYPES) {
//			/*
//			 * @//TODO : Your code here
//			 */
//		}

	}

	////////////////////////////////////


	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d) {
		
		/*
		 * @//TODO : Your code here
		 */
		
		double score = 0.0;
		
		// Compute w for each term
		
		for (String queryTerm : tfQuery.keySet()) {
			
			double wdt = computeWeightTerm(queryTerm, tfs);
			
			Double idfValue = idfs.containsKey(queryTerm) ? idfs.get(queryTerm) : idfs.get("#inexistent#term#");
			
			score = score + (wdt / (k1 + wdt))*idfValue;
		}
		
		// Add pagerank to the score
		score = score + pageRankLambda * Math.log(pageRankLambdaPrime + d.page_rank);
		
		return score;
	}

	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		
		// Convert raw tfs into normalized field term frequencies
		
		for (String field : tfs.keySet()) {
			Double docFieldLength = lengths.get(d).get(field);
			
//			if (docFieldLength == null) {
//				docFieldLength = 0.0;
//			}
			
			double avgLength = avgLengths.get(field);
			
			Map<String, Double> ftfs = tfs.get(field);
			
			double Bf = 0.0;
			
			// Get correct Bf weight
			if (field.equals(TFTYPES[0])) {
				Bf = burl;
			} else if (field.equals(TFTYPES[1])) {
				Bf = btitle;
			} else if (field.equals(TFTYPES[2])) {
				Bf = bbody;
			} else if (field.equals(TFTYPES[3])) {
				Bf = bheader;
			} else if (field.equals(TFTYPES[4])) {
				Bf = banchor;
			}
			
			for (String term : ftfs.keySet()) {
				
				ftfs.put(term, ftfs.get(term) / (1 + Bf*(docFieldLength / avgLength - 1)));
			}
		}
	}


	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);

	    return getNetScore(tfs,q,tfQuery,d);
	}
	
	/**
	 * Computes the weight wdt for the weight of term t in document d using the 
	 * normalized field term weights (ftfs) and the field weights (Wf's)
	 * 
	 * @param queryTerm The term for which the score will be computed
	 * @param tfs The ftfs for the terms in the document
	 * @return The score for the weight of queryTerm in the document
	 */
	private double computeWeightTerm(String queryTerm, Map<String,Map<String, Double>> tfs) {
		
		double wdt = 0.0;
		
		Map<String, Double> urlTfs = tfs.get(TFTYPES[0]);
		Map<String, Double> titleTfs = tfs.get(TFTYPES[1]);
		Map<String, Double> bodyTfs = tfs.get(TFTYPES[2]);
		Map<String, Double> headerTfs = tfs.get(TFTYPES[3]);
		Map<String, Double> anchorTfs = tfs.get(TFTYPES[4]);
		
		// Add weight from each field
		
		// URL
		if (urlTfs.containsKey(queryTerm)) {
			wdt = wdt + urlweight * urlTfs.get(queryTerm);
		}

		// Title
		if (titleTfs.containsKey(queryTerm)) {
			wdt = wdt + titleweight * titleTfs.get(queryTerm);
		}

		// Body
		if (bodyTfs.containsKey(queryTerm)) {
			wdt = wdt + bodyweight * bodyTfs.get(queryTerm);
		}

		// Header
		if (headerTfs.containsKey(queryTerm)) {
			wdt = wdt + headerweight * headerTfs.get(queryTerm);
		}

		// Anchor
		if (anchorTfs.containsKey(queryTerm)) {
			wdt = wdt + anchorweight * anchorTfs.get(queryTerm);
		}
		
		return wdt;
	}
	
}
