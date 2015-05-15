package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.stanford.cs276.util.Pair;

public class ExtraCreditScorer extends SmallestWindowScorer {

	public ExtraCreditScorer(Map<String,Double> idfs) {
		super(idfs);
	}

	static Integer getPostingValueByPair(Map<String, List<Integer>> d, Pair<Integer, String> p) {
		// Pair's first value is the index and seconds value is the word of posting list.
		return d.get(p.getSecond()).get(p.getFirst());
	}
	
	/* For extra, we implemented following two heuristics.
	 * 
	 * 1. detection of repetitive words in query. It provides more accurate
	 * relevance evaluation when query contains repeated words.
	 * 
	 * 2. detection of order of minimum window. Previously smallest window doesn't consider
	 * the order of the query. If the order of the query matches the terms in web pages, it 
	 * should provide higher boost.
	 * 
	 */
	
	static public double checkWindowBody(Query q, Map<String, List<Integer>> d) {
		// if query and doc have different length for keys, doc must missed some
		// query terms. Return max length. Maybe we should use a set to remove duplicate
		// terms in query.

		int querylen = (new ArrayList<String>(new LinkedHashSet<String>(q.queryWords))).size();
		int doclen = d.size();
		if (querylen != doclen) {
			return Double.MAX_VALUE;
		}

		Comparator<Pair<Integer, String>> comparator = new Comparator<Pair<Integer, String>>() {
			@Override
			public int compare(Pair<Integer, String> x, Pair<Integer, String> y)
			{
			    return getPostingValueByPair(d, x).compareTo(getPostingValueByPair(d, y));
			}
		};
		PriorityQueue<Pair<Integer, String>> pq = new PriorityQueue<Pair<Integer, String>>(doclen, comparator);

		int curMax = 0;
		int winSize = Integer.MAX_VALUE;
		for (String s : d.keySet()) {
			curMax = Math.max(curMax, d.get(s).get(0));
			Pair<Integer, String> p = new Pair<Integer, String>(0, s);
			pq.add(p);
		}

		while (pq.size() == doclen) {
			Pair<Integer, String> curMinPair = pq.poll();
			winSize = Math.min(winSize, curMax - getPostingValueByPair(d, curMinPair)+1);
			String curString = curMinPair.getSecond();
			Integer curIndex= curMinPair.getFirst();
			if (d.get(curString).size()-1 > curIndex) {
				Pair<Integer, String> newPair = new Pair<Integer, String>(curIndex+1, curString);
				pq.add(newPair);
				curMax = Math.max(curMax, getPostingValueByPair(d, newPair));
			}

		}
		return winSize;
	}

	static public double checkWindowNonBody(Query q,String docstr,double curSmallestWindow) {
		/*
		 * Find the minimum window size of docstr for query q.
		 */
		List<String> queryWords = q.queryWords;
		List<String> docWords = new ArrayList<String>(Arrays.asList(docstr.split(" ")));
		Map<String, Integer> needFind = new HashMap<String, Integer>();	// key is the needed string, value is the number it is needed in query
	    Map<String, Integer> hasFound = new HashMap<String, Integer>();	// key is the needed string, value is the number it is found in sequence

	    // Initialize two hashmaps which are used to fine the minimum sequence window
	    for (int i = 0; i < queryWords.size(); i++) {
	    	hasFound.put(queryWords.get(i), 0);
            if (needFind.containsKey(queryWords.get(i))) {
            	needFind.put(queryWords.get(i), needFind.get(queryWords.get(i))+1);
            } else {
                needFind.put(queryWords.get(i), 1);
            }
        }

	    ArrayList<Integer> nexts = new ArrayList<Integer>();
        int right = 0, left = 0, found = 0; // notice here: right points to S while left points to next[]. next[] holds real index of S.

        String window = "";
        double winSize = curSmallestWindow;
        while (right < docWords.size()) {
            String s = docWords.get(right);
            if (!needFind.containsKey(s)) {     // We don't need this word in doc
                right++;
                continue;
            }

            nexts.add(right); right++;
            hasFound.put(s, hasFound.get(s)+1);
            if (hasFound.get(s) <= needFind.get(s)) found++;    // we found necessary char; otherwise, it is useful, but not necessary at this point

            if (found >= queryWords.size()) {    // got a window
                // Check how far we can move the left
                String leftString = docWords.get(nexts.get(left));
                while (hasFound.get(leftString) > needFind.get(leftString)) {
                    hasFound.put(leftString, hasFound.get(leftString)-1);
                    left++;
                    leftString = docWords.get(nexts.get(left));
                }
                if (right - nexts.get(left) < winSize) {
                    winSize = right - nexts.get(left);
                    window = docWords.get(nexts.get(left));
                    for (int i = nexts.get(left)+1; i <= right; i++) {
                    	window = window + " " + docWords.get(i);
                    }
                    String queryWord = String.join(" ", q.queryWords);
                    if (queryWord.equals(window))
                    	return 1;
                }
            }
        }


		return winSize;
	}


	public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {
		double score = 0.0;

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

			double dist = Double.MAX_VALUE;
			if (tfType.equals("body") && d.body_hits != null) {
				dist = SmallestWindowScorer.checkWindowBody(q, d.body_hits);
			} else if (tfType.equals("url") && d.url != null) {
				dist = SmallestWindowScorer.checkWindowNonBody(q, d.url, dist);
			} else if (tfType.equals("title") && d.title != null) {
				dist = SmallestWindowScorer.checkWindowNonBody(q, d.title, dist);
			} else if (tfType.equals("anchor") && d.anchors != null) {
				for (String anchorString : d.anchors.keySet())
					dist = SmallestWindowScorer.checkWindowNonBody(q, anchorString, dist);
			} else if (tfType.equals("header") && d.headers != null) {
				for (String headerString : d.headers)
					dist = SmallestWindowScorer.checkWindowNonBody(q, headerString, dist);
			}

			for (String term : termToFreq.keySet()) {
				Double weight = fieldToWeightDictWindow.get(tfType);
				if (dist < 10*q.queryWords.size()) {
					weight *= (1.0 + B * Math.pow((double)q.queryWords.size()/dist, boostmod));
				}
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

	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);

	    return getNetScore(tfs,q,tfQuery,d);
	}
}
