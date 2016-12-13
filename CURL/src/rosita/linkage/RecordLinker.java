package rosita.linkage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rosita.linkage.analysis.Algorithm;
import rosita.linkage.analysis.JWSimilarityCalculator;
import rosita.linkage.analysis.LEVSimilarityCalculator;
import rosita.linkage.analysis.MatchStatus;
import rosita.linkage.analysis.ScoredPair;
import rosita.linkage.analysis.SimilarityCalculator;

public class RecordLinker 
{
	private static float defaultExactThreshold = (float) 1.0;
	private static float defaultJWThreshold = (float) 0.8;
	private static float defaultLEVThreshold = (float) 0.7;
	
	// This number for the time being, has been chose arbitrarily
	private static float defaultPPRLThreshold = (float) 0.8;
	
	private List<MappedPair> mappedPairs;
	private Map<Algorithm, SimilarityCalculator> similarityCalculators;
	private Map<Algorithm, Float> thresholds;
	
	/**
	 * Class Constructor
	 * @param mappedPairs - A list keeping track of the relationship of which
	 * values in one record correspond to the other. 
	 */
	public RecordLinker(ArrayList<MappedPair> mappedPairs, HashMap<Algorithm, Float> thresholds)
	{
		this.mappedPairs = mappedPairs;
		this.similarityCalculators = new HashMap<Algorithm, SimilarityCalculator>();
		this.thresholds = thresholds;
		setup();
	}
	
	public RecordLinker(ArrayList<MappedPair> mappedPairs)
	{
		this.mappedPairs = mappedPairs;
		this.similarityCalculators = new HashMap<Algorithm, SimilarityCalculator>();
		this.thresholds = null;
		
		setup();
	}
	

	/**
	 * Iterate across the mapped pairs.  Determine which algorithm to use for each
	 * field, and then use the appropriate ScoreCalculator to determine the score
	 * @param r1 - The record corresponding to data set A
	 * @param l2 - The record corresponding to data set B
	 */
	public ArrayList<ScoredPair> calculateSimilarities(L_Record l1, L_Record l2)
	{
		ArrayList<ScoredPair> similarities = new ArrayList<ScoredPair>();

		SimilarityCalculator sc = null;
		Algorithm a = null;
		int indexA = -1;
		int indexB = -1;
		float similarity = -1;

		// find a score for each mapped pair
		for (MappedPair mp : mappedPairs)
		{
			// Get the proper calculator
			a = mp.getAlgorithm();
			sc = similarityCalculators.get(a);
			indexA = mp.getIndexA();
			
			indexB = mp.getIndexB();

			// Set the values in this calculator
			sc.setValues(l1.get(indexA), l2.get(indexB));

			// Calculate & log similarities
			sc.calculate();
			similarity = sc.getSimilarity();
			MatchStatus m = sc.getMatchStatus();
			similarities.add(new ScoredPair(a, similarity));

		}

		return similarities;
	}

	/**
	 * Once we have calculated scores for two records,
	 * analyze the results & find an appropriate match status for the records
	 * @param scoredPairs - A list of scores for two records.
	 * @return - the matching status of the two records.
	 */
	public MatchStatus getStatus(ArrayList<ScoredPair> scoredPairs)
	{
		float score;
		Algorithm algorithm;
		float threshold;

		MatchStatus ms = MatchStatus.MATCH;

		for (ScoredPair sp : scoredPairs)
		{
			score = sp.getSimilarity();
			algorithm = sp.getAlgorithm();
			threshold =  thresholds.get(algorithm);

			System.out.println("Score:" + score + " Algorithm:" + algorithm + " Threshold:" + threshold);

			if (score < threshold)
				ms = MatchStatus.NON_MATCH;

		}

		return ms;
	}

	/**
	 * This function detects which calculators we need & initializes them.
	 */
	private void setup()
	{
		// thresholds may not have been setup in the constructor, use default values
		if (this.thresholds == null)
		{
			this.thresholds = new HashMap<Algorithm, Float>();
			//thresholds.put(Algorithm.EXACT_MATCH, defaultExactThreshold);
			thresholds.put(Algorithm.JARO_WINKLER, defaultJWThreshold);
			thresholds.put(Algorithm.LEV_DISTANCE, defaultLEVThreshold);
			thresholds.put(Algorithm.PPRL, defaultPPRLThreshold);
		} 
		else 
		{
			// Thresholds may be setup, but some may be missing!
			//if (!thresholds.containsKey(Algorithm.EXACT_MATCH))
			//	thresholds.put(Algorithm.EXACT_MATCH, defaultExactThreshold);
			
			if (!thresholds.containsKey(Algorithm.JARO_WINKLER))
				thresholds.put(Algorithm.JARO_WINKLER, defaultJWThreshold);
			
			if (!thresholds.containsKey(Algorithm.LEV_DISTANCE))
				thresholds.put(Algorithm.LEV_DISTANCE, defaultLEVThreshold);
		}
		
		// Instantiate only the calculators that we need
		for (MappedPair mp : mappedPairs) 
		{
			Algorithm a = mp.getAlgorithm();
			float threshold = this.thresholds.get(a);
			
			//if (a == Algorithm.EXACT_MATCH) 
			//	similarityCalculators.put(Algorithm.EXACT_MATCH, new ExactSimilarityCalculator(threshold));
			if (a == Algorithm.JARO_WINKLER)
				similarityCalculators.put(Algorithm.JARO_WINKLER, new JWSimilarityCalculator(threshold));
			else if (a == Algorithm.LEV_DISTANCE)
				similarityCalculators.put(Algorithm.LEV_DISTANCE, new LEVSimilarityCalculator(threshold));
		}
	}


}
