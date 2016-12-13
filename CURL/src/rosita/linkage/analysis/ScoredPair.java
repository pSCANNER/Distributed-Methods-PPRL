package rosita.linkage.analysis;


public class ScoredPair 
{
	private Algorithm algorithm;
	private float similarity;
	
	public ScoredPair(Algorithm a, Float similarity)
	{
		this.algorithm = a;
		this.similarity = similarity;
	}
	
	public Algorithm getAlgorithm()
	{
		return this.algorithm;
	}
	
	public float getSimilarity()
	{
		return this.similarity;
	}
	
}
