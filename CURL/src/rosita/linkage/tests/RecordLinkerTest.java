package rosita.linkage.tests;

import java.util.ArrayList;

import rosita.linkage.L_Record;
import rosita.linkage.MappedPair;
import rosita.linkage.RecordLinker;
import rosita.linkage.analysis.Algorithm;
import rosita.linkage.analysis.ScoredPair;

public class RecordLinkerTest 
{
	/**
	 * For testing purposes only
	 * @param args - Command line arguments
	 */
	public static void main(String[] args)
	{
		// Setup MappedPair ArrayList
		ArrayList<MappedPair> mps = new ArrayList<MappedPair>();
		mps.add(new MappedPair("fn", 0, "FirstName", 0, Algorithm.JARO_WINKLER));
		mps.add(new MappedPair("ln", 1, "LastName", 2, Algorithm.JARO_WINKLER));
		//mps.add(new MappedPair("phone", 2, "PhoneNum", 1, Algorithm.EXACT_MATCH));

		for ( MappedPair mp : mps)
			System.out.println(mp);

		// Setup some sample records
		String[] s1 = {"Branden", "Abbott", "3606208204"};
		String[] s2 = {"Brandon", "3606208204", "Abbot"};
		L_Record l1 = new L_Record(s1);
		L_Record l2 = new L_Record(s2);
		
		// Create a RecordLinker
		RecordLinker rl = new RecordLinker(mps);

		// Calculate Similarities
		ArrayList<ScoredPair> sp = rl.calculateSimilarities(l1, l2);

		// Determine the match status
		System.out.println(rl.getStatus(sp));
		
		//Test this thing

	}
}
