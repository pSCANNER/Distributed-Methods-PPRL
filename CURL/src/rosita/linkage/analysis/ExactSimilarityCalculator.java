package rosita.linkage.analysis;

import rosita.linkage.util.StringMatch;

public class ExactSimilarityCalculator extends SimilarityCalculator
{
	
	public ExactSimilarityCalculator(float threshold) 
	{
		super(Algorithm.EQUAL_FIELDS_BOOLEAN_DISTANCE, threshold);
		//super(Algorithm.EXACT_MATCH, threshold);
	}

	@Override
	public void calculate() 
	{
		if (VERBOSE)
			System.out.println("Comparing values: " + value1 + " " + value2);
		
		this.similarity = StringMatch.getExactMatchSimilarity(value1, value2);
	}

	

}
