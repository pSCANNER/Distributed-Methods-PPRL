package rosita.linkage.analysis;

public abstract class SimilarityCalculator 
{
	protected static boolean VERBOSE = true;
	
	protected Algorithm algorithm;
	protected String value1;
	protected String value2;
	
	protected float similarity;
	protected float threshold = -1;

	public SimilarityCalculator(Algorithm algorithm, float threshold)
	{
		this.algorithm = algorithm;
		this.threshold = threshold;
	}

	public abstract void calculate();
	
	
	public MatchStatus getMatchStatus()
	{
		if (threshold < 0)
			return MatchStatus.NON_MATCH;
			
		if (similarity < threshold )
			return MatchStatus.NON_MATCH;
		
		return MatchStatus.MATCH;
	}
	
	public float getSimilarity()
	{
		return this.similarity;
	}

	
	public void setValues(String val1, String val2)
	{
		this.value1 = val1;
		this.value2 = val2;
	}


	
}
