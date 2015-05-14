package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for a scorer. Need to be extended by each specific
 * implementation of scorers.
 */
public abstract class AScorer {

	Map<String, Double> idfs; // Map: term -> idf
	// Various types of term frequencies that you will need
	String[] TFTYPES = { "url", "title", "body", "header", "anchor" };

	public AScorer(Map<String, Double> idfs) {
		this.idfs = idfs;
	}

	// Score each document for each query.
	public abstract double getSimScore(Document d, Query q);

	// Handle the query vector
	public Map<String, Double> getQueryFreqs(Query q) {
		Map<String, Double> tfQuery = new HashMap<String, Double>(); // queryWord ->
																																	// term
																																	// frequency

		List<String> queryWords = q.queryWords;

		for (String queryWord : queryWords) {
			if (tfQuery.containsKey(queryWord)) {
				// Increment the term doc count
				tfQuery.put(queryWord, tfQuery.get(queryWord) + 1);
			} else {
				tfQuery.put(queryWord, 1.0);
			}
		}

		return tfQuery;
	}

	// //////////// Initialization/Parsing Methods ///////////////

	/*
	 * @//TODO : Your code here
	 */

	// ///////////////////////////////////////////////////////////

	/*
	 * / Creates the various kinds of term frequencies (url, title, body, header,
	 * and anchor) You can override this if you'd like, but it's likely that your
	 * concrete classes will share this implementation.
	 */
	public Map<String, Map<String, Double>> getDocTermFreqs(Document d, Query q) {
		// Map from tf type -> queryWord -> score
		Map<String, Map<String, Double>> tfs = new HashMap<String, Map<String, Double>>();

		// //////////////////Initialization/////////////////////

		// Initialize map from string to count for URL
		Map<String, Double> urlTfs = new HashMap<String, Double>();

		if (d.url != null) {
			List<String> urlTokens = Arrays.asList(d.url.toLowerCase().split("\\W+"));

			for (String urlToken : urlTokens) {
				if (urlTfs.containsKey(urlToken)) {
					// Increment the term doc count
					urlTfs.put(urlToken, urlTfs.get(urlToken) + 1);
				} else {
					urlTfs.put(urlToken, 1.0);
				}
			}
		}

		// Initialize map from string to count for title
		Map<String, Double> titleTfs = new HashMap<String, Double>();

		if (d.title != null) {
			List<String> titleTokens = Arrays.asList(d.title.toLowerCase().split(
					"\\s+"));

			for (String titleToken : titleTokens) {
				if (titleTfs.containsKey(titleToken)) {
					// Increment the term doc count
					titleTfs.put(titleToken, titleTfs.get(titleToken) + 1);
				} else {
					titleTfs.put(titleToken, 1.0);
				}
			}
		}

		// Initialize map from string to count for body
		Map<String, Double> bodyTfs = new HashMap<String, Double>();

		if (d.body_hits != null) {
			Map<String, List<Integer>> bodyHits = d.body_hits;

			for (String bodyHit : bodyHits.keySet()) {
				bodyTfs.put(bodyHit, (double) bodyHits.get(bodyHit).size());
			}
		}

		// Initialize map from string to count for header
		Map<String, Double> headerTfs = new HashMap<String, Double>();

		if (d.headers != null) {
			List<String> headers = d.headers;

			for (String header : headers) {
				List<String> headerTokens = Arrays.asList(header.toLowerCase().split(
						"\\s+"));

				for (String headerToken : headerTokens) {
					if (headerTfs.containsKey(headerToken)) {
						// Increment the term doc count
						headerTfs.put(headerToken, headerTfs.get(headerToken) + 1);
					} else {
						headerTfs.put(headerToken, 1.0);
					}
				}

			}
		}

		// Initialize map from string to count for anchor
		Map<String, Double> anchorTfs = new HashMap<String, Double>();

		if (d.anchors != null) {
			Map<String, Integer> anchors = d.anchors;

			for (String anchor : anchors.keySet()) {

				List<String> anchorTokens = Arrays.asList(anchor.toLowerCase().split(
						"\\s+"));

				int anchorCount = anchors.get(anchor);

				for (String anchorToken : anchorTokens) {
					if (anchorTfs.containsKey(anchorToken)) {
						// Increment the term doc count
						anchorTfs.put(anchorToken, anchorTfs.get(anchorToken) + anchorCount);
					} else {
						anchorTfs.put(anchorToken, (double) anchorCount);
					}
				}

			}
		}
		
		// Add an empty map for each type
		for (String tfType : TFTYPES) {
			HashMap<String, Double> stringToTf = new HashMap<String, Double>();
			tfs.put(tfType, stringToTf);
		}

		// //////////////////////////////////////////////////////

		// Loop through query terms and increase relevant tfs. Note: you should do
		// this to each type of term frequencies.
		for (String queryWord : q.queryWords) {

			// URL
			if (urlTfs.containsKey(queryWord)) {
				tfs.get(TFTYPES[0]).put(queryWord, urlTfs.get(queryWord));
			}

			// Title
			if (titleTfs.containsKey(queryWord)) {
				tfs.get(TFTYPES[1]).put(queryWord, titleTfs.get(queryWord));
			}

			// Body
			if (bodyTfs.containsKey(queryWord)) {
				tfs.get(TFTYPES[2]).put(queryWord, bodyTfs.get(queryWord));
			}

			// Header
			if (headerTfs.containsKey(queryWord)) {
				tfs.get(TFTYPES[3]).put(queryWord, headerTfs.get(queryWord));
			}

			// Anchor
			if (anchorTfs.containsKey(queryWord)) {
				tfs.get(TFTYPES[4]).put(queryWord, anchorTfs.get(queryWord));
			}

		}
		return tfs;
	}

}
