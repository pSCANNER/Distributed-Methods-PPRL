package rosita.linkage.analysis;

import rosita.linkage.util.StringMatch;

public class LEVSimilarityCalculator extends SimilarityCalculator 
{

	public LEVSimilarityCalculator(float threshold)
	{
		super(Algorithm.LEV_DISTANCE, threshold);
	}

	@Override
	public void calculate() 
	{
		if (VERBOSE)
			System.out.println("Comparing values: " + value1 + " " + value2);
		
		this.similarity = StringMatch.getLEVMatchSimilarity(value1, value2);
	}
	

}
