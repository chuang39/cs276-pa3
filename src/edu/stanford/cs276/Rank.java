package edu.stanford.cs276;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import edu.stanford.cs276.util.Pair;

/**
 * The entry class for this programming assignment.
 */
public class Rank {
	/**
	 * Call this function to score and rank documents for some queries, using a specified scoring function.
	 */
	private static Map<Query,List<String>> score(Map<Query,Map<String, Document>> queryDict, String scoreType,
			Map<String,Double> idfs) {
		AScorer scorer = null;
		if (scoreType.equals("baseline"))
			scorer = new BaselineScorer();
		else if (scoreType.equals("cosine"))
			scorer = new CosineSimilarityScorer(idfs);
		else if (scoreType.equals("bm25"))
			scorer = new BM25Scorer(idfs,queryDict);
		else if (scoreType.equals("window"))
			// Feel free to change this to match your cosine scorer if you choose to build on top of that instead
			scorer = new SmallestWindowScorer(idfs,queryDict);
		else if (scoreType.equals("extra"))
			scorer = new ExtraCreditScorer(idfs);
		

		// Put completed rankings here
		Map<Query,List<String>> queryRankings = new HashMap<Query,List<String>>();
		
		for (Query query : queryDict.keySet()) {
			// Loop through urls for query, getting scores
			List<Pair<String,Double>> urlAndScores = new ArrayList<Pair<String,Double>>(queryDict.get(query).size());
			for (String url : queryDict.get(query).keySet()) {
				double score = scorer.getSimScore(queryDict.get(query).get(url), query);
				urlAndScores.add(new Pair<String,Double>(url,score));
			}

			// Sort urls for query based on scores
			Collections.sort(urlAndScores, new Comparator<Pair<String,Double>>() {
				@Override
				public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
					/*
					 * @//TODO : Your code here
					 */
					return -1;
				}	
			});
			
			// Put completed rankings into map
			List<String> curRankings = new ArrayList<String>();
			for (Pair<String,Double> urlAndScore : urlAndScores)
				curRankings.add(urlAndScore.getFirst());
			queryRankings.put(query, curRankings);
		}
		return queryRankings;
	}

	public static void printRankedResults(Map<Query,List<String>> queryRankings) {
		for (Query query : queryRankings.keySet()) {
			StringBuilder queryBuilder = new StringBuilder();
			for (String s : query.queryWords) {
				queryBuilder.append(s);
				queryBuilder.append(" ");
			}
			
			System.out.println("query: " + queryBuilder.toString());
			for (String res : queryRankings.get(query))
				System.out.println("  url: " + res);
		}	
	}
	
	// This method probably doesn't need to be included, but if you output to a file, it may be easier to immediately run ndcg.java to score your results
	public static void writeRankedResultsToFile(Map<Query,List<String>> queryRankings,String outputFilePath) {
		try {
			File file = new File(outputFilePath);
 
			// If file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (Query query : queryRankings.keySet())
			{
				StringBuilder queryBuilder = new StringBuilder();
				for (String s : query.queryWords)
				{
					queryBuilder.append(s);
					queryBuilder.append(" ");
				}
				
				String queryStr = "query: " + queryBuilder.toString() + "\n";
				System.out.print(queryStr);
				bw.write(queryStr);
				
				for (String res : queryRankings.get(query))
				{
					String urlString = "  url: " + res + "\n";
					System.out.print(urlString);
					bw.write(urlString);
				}
			}	
			
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {

		// To store the idfs for different words in the collection
		Map<String,Double> idfs = null;
		
		/*
		 * @//TODO : Your code here to handle idfs
		 */

		if (args.length < 2) {
			System.err.println("Insufficient number of arguments: <queryDocTrainData path> taskType");
		}

		String scoreType = args[1];
		
		if (!(scoreType.equals("baseline") || scoreType.equals("cosine") || scoreType.equals("bm25")
				|| scoreType.equals("extra") || scoreType.equals("window"))) {
			System.err.println("Invalid scoring type; should be either 'baseline', 'bm25', 'cosine', 'window', or 'extra'");
		}
			
		Map<Query,Map<String, Document>> queryDict=null;
		
		// Populate map with features from file
		try {
			queryDict = LoadHandler.loadTrainData(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Score documents for queries
		Map<Query,List<String>> queryRankings = score(queryDict, scoreType, idfs);
		
		// Print results and save them to file (This is not necessary)
		/*
		  String outputFilePath =  null;
		  writeRankedResultsToFile(queryRankings,outputFilePath);
		 */
		
		// Print results
		printRankedResults(queryRankings);
	}
}
