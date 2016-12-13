package rosita.linkage.tests;

import rosita.linkage.util.StringMatch;

public class StringCompTest 
{
	
	public static void main(String[] args)
	{
		String fn1 = "Brandon";
		String fn2 = "Branden";
		
		String ln1 = "Abbott";
		String ln2 = "Abbot";
		
		boolean match;
		float similarity;

		// Exact Match
		match = StringMatch.exactMatch(fn1, fn2);
		similarity = StringMatch.getExactMatchSimilarity(fn1, fn2);
		System.out.println("FN Exact Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		match = StringMatch.exactMatch(ln1, ln2);
		similarity = StringMatch.getExactMatchSimilarity(ln1, ln2);
		System.out.println("LN Exact Match:" + match);
		System.out.println("Similarity:" + similarity);

		System.out.println("---------------");
		
		// JWC Match
		match = StringMatch.JWCMatch(fn1, fn2);
		similarity = StringMatch.getJWCMatchSimilarity(fn1, fn2);
		System.out.println("FN JWC Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		match = StringMatch.JWCMatch(ln1, ln2);
		similarity = StringMatch.getJWCMatchSimilarity(ln1, ln2);
		System.out.println("LN JWC Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		System.out.println("---------------");
		
		// LCS Match
		match = StringMatch.LCSMatch(fn1, fn2);
		similarity = StringMatch.getLCSMatchSimilarity(fn1, fn2);
		System.out.println("FN LCS Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		match = StringMatch.LCSMatch(ln1, ln2);
		similarity = StringMatch.getLCSMatchSimilarity(ln1, ln2);
		System.out.println("LN LCS Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		
		System.out.println("---------------");
		
		// LEV Match
		match = StringMatch.LEVMatch(fn1, fn2);
		similarity = StringMatch.getLEVMatchSimilarity(fn1, fn2);
		System.out.println("FN LEV Match:" + match);
		System.out.println("Similarity:" + similarity);
		
		match = StringMatch.LEVMatch(ln1, ln2);
		similarity = StringMatch.getLEVMatchSimilarity(ln1, ln2);
		System.out.println("LN LEV Match:" + match);
		System.out.println("Similarity:" + similarity);	
		
	}
}
