package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Skeleton code for the implementation of a Cosine Similarity Scorer in Task 1.
 */
public class CosineSimilarityScorer extends AScorer {

	public CosineSimilarityScorer(Map<String,Double> idfs) {
		super(idfs);

		fieldToWeightDict.put("url", urlweight);
		fieldToWeightDict.put("title", titleweight);
		fieldToWeightDict.put("body", bodyweight);
		fieldToWeightDict.put("header", headerweight);
		fieldToWeightDict.put("anchor", anchorweight);
	}

	/////////////// Weights //////////////////
	static Map<String, Double> fieldToWeightDict = new HashMap<String, Double>();

	double urlweight = 9;
	double titleweight = 10;
	double bodyweight = 1;
	double headerweight = 2;
	double anchorweight = 1.5;

	double smoothingBodyLength = 500; // Smoothing factor when the body length is 0.

	static boolean DEBUG_getNetScore = false;
	//////////////////////////////////////////

	public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {
		double score = 0.0;

		/*
		 * @//Done : Compute dot product
		 */

		if (DEBUG_getNetScore) System.out.format("*** url: %s, Query: %s\n", d.url, q.queryWords.toString());
		// Calculate query vector w/ tf-idf (qv)
		Map<String, Double> qv = new HashMap<String, Double>();
		boolean isSublinear = true;
		for (String term : tfQuery.keySet()) {
			Double tf_value = tfQuery.get(term);
			if (isSublinear)
				tf_value = 1 + Math.log(tf_value);

			// Each query term should be weighted using the idf values with laplace add-one smoothing
			Double idf_value = idfs.containsKey(term) ? idfs.get(term) : idfs.get("#inexistent#term#");
			qv.put(term, tf_value * idf_value);
		}
		if (DEBUG_getNetScore) {
			System.out.format("qv size=%d:", qv.size());
			for (Map.Entry<String, Double> entry : qv.entrySet()) {
				System.out.format("%s[%f], ", entry.getKey(), entry.getValue());
			}
			System.out.println();
		}
		
		// Calculate term score vector
		Map<String, Double> tsv = new HashMap<String, Double>();
		for (String tfType : tfs.keySet()) {
			Map<String, Double> termToFreq = tfs.get(tfType);

			for (String term : termToFreq.keySet()) {
				Double weight = fieldToWeightDict.get(tfType);

				if (!tsv.containsKey(term)) {
					tsv.put(term, weight * termToFreq.get(term));
				} else {
					tsv.put(term, tsv.get(term) + weight * termToFreq.get(term));
				}
			}
		}
		if (DEBUG_getNetScore) {
			System.out.format("tsv size=%d:", tsv.size());
			for (Map.Entry<String, Double> entry : tsv.entrySet()) {
				System.out.format("%s[%f], ", entry.getKey(), entry.getValue());
			}
			System.out.println();
		}
		
		// Get the dot product result here. Since for tsv some term's score vector may
		// not exist and qv must have every term, we iterate tsv to accumulate the score.
		// qv should has every term in tsv.
		for (String term : tsv.keySet()) {
			score += qv.get(term) * tsv.get(term);
		}
		
		if (DEBUG_getNetScore) System.out.println("score: "+score);
		return score;
	}

	// Normalize the term frequencies. Note that we should give uniform normalization to all fields as discussed
	// in the assignment handout.
	public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
		
		// Normalize document tf vectors by length with smoothing
		for (String tfType : tfs.keySet()) {
			
			Map<String, Double> termToFreq = tfs.get(tfType);
			
			for (String term : termToFreq.keySet()) {
				termToFreq.put(term, termToFreq.get(term) / (double)(d.body_length + smoothingBodyLength));
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

}
