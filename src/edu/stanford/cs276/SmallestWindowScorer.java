package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.stanford.cs276.util.Pair;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 */
public class SmallestWindowScorer extends CosineSimilarityScorer {

	/////// Smallest window specific hyper-parameters ////////
	double B = 1.5;
	double boostmod = 2;

	static Map<String, Double> fieldToWeightDictWindow = new HashMap<String, Double>();

	double urlweight = 8;
	double titleweight = 8;
	double bodyweight = 1;
	double headerweight = 2.5;
	double anchorweight = 1.5;

	//////////////////////////////
	
	public SmallestWindowScorer(Map<String, Double> idfs) {
		super(idfs);
		handleSmallestWindow();
	}

	
	public void handleSmallestWindow() {
		/*
		 * @//TODO : Your code here
		 */
		fieldToWeightDictWindow.put("url", urlweight);
		fieldToWeightDictWindow.put("title", titleweight);
		fieldToWeightDictWindow.put("body", bodyweight);
		fieldToWeightDictWindow.put("header", headerweight);
		fieldToWeightDictWindow.put("anchor", anchorweight);
	}

	static Integer getPostingValueByPair(Map<String, List<Integer>> d, Pair<Integer, String> p) {
		// Pair's first value is the index and seconds value is the word of posting list.
		return d.get(p.getSecond()).get(p.getFirst());
	}
	
	static public double checkWindowBody(Query q, Map<String, List<Integer>> d) {
		// if query and doc have different length for keys, doc must missed some
		// query terms. Return max length. Maybe we should use a set to remove duplicate
		// terms in query.

		int querylen = q.queryWords.size();
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

        //String window = "";
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
                    //window = docWords[nexts.get(left), right]
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
		
		//double res = checkWindowBody(q, d.body_hits);
		return getNetScore(tfs,q,tfQuery,d);

	}

	public static void main(String [] args) {
		// Test for checkWindowNonBody
		double res = SmallestWindowScorer.checkWindowNonBody(new Query("academic schedule 2015 2016"), "stanford academic calendar 2015 2016 next year schedule affairs", Double.MAX_VALUE);
		System.out.print(res); // 7

		res = SmallestWindowScorer.checkWindowNonBody(new Query("academic schedule 2015 2016"), "stanford academic calendar 2015 16 next year sch affairs", Double.MAX_VALUE);
		System.out.print(res); // Integer.MAX_VALUE

		// Test for checkWindowBody.
		List<Integer> l1 = new ArrayList<Integer>(Arrays.asList(100, 126, 218, 273, 732, 1120, 2030, 2240)); // 2015
		List<Integer> l2 = new ArrayList<Integer>(Arrays.asList(170, 360, 552, 645, 1013, 1095, 1454, 1488)); // academic
		List<Integer> l3 = new ArrayList<Integer>(Arrays.asList(169, 337, 831, 839, 1284, 1292, 1747, 1755)); // schedule
		List<Integer> l4 = new ArrayList<Integer>(Arrays.asList(328, 336, 1980, 1988, 1996, 2007)); // 2016
		Map<String, List<Integer>> d = new HashMap<String, List<Integer>>();
		d.put("2015", l1);
		d.put("academic", l2);
		d.put("schedule", l3);
		d.put("2016", l4);

		res = SmallestWindowScorer.checkWindowBody(new Query("academic schedule 2015 2016"), d);
		System.out.print(res);
	}

}
