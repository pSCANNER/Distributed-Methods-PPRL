package rosita.linkage.analysis;

import rosita.linkage.util.StringMatch;

public class JWSimilarityCalculator extends SimilarityCalculator
{
	public JWSimilarityCalculator(float threshold)
	{
		super(Algorithm.JARO_WINKLER, threshold);
	}

	@Override
	public void calculate() 
	{
		if (VERBOSE)
			System.out.println("Comparing values: " + value1 + " " + value2);
		
		this.similarity = StringMatch.getJWCMatchSimilarity(value1, value2);
	}
	
}
