package rosita.linkage.tests;

import java.util.ArrayList;

import rosita.linkage.filtering.BloomFilter;

public class MiscTests 
{
	public static void fpp()
	{
		// New BloomFilter
		BloomFilter<String> b0 = new BloomFilter<String>(100, 30);
		BloomFilter<String> b1 = new BloomFilter<String>(100, 11);
		String s = "BrandonAbb";
		// Add 11 elements to each
		b0.addAsBigrams(s);
		b1.addAsBigrams(s);

		// Get the FPP for both
		double d0 = b0.getFalsePositiveProbability();
		double d1 = b1.getFalsePositiveProbability();
		// Determine what the bitSetSize should be based off the FPP
		double m0 = -1 * (11 * Math.log(d0)) / (Math.pow(Math.log(2), 2)); 
		double m1 = -1 * (11 * Math.log(d1)) / (Math.pow(Math.log(2), 2)); 

		// Print out the stats for both
		System.out.println("n=11 - m0=" + m0 + " - fpp0=" + d0 + " - k0=" + b0.getK());
		System.out.println("n=11 - m1=" + m1 + " - fpp1=" + d1 + " - kl=" + b1.getK());

		// Determine how full each are
		int c0 = 0;
		int c1 = 0;
		for (int i = 0; i < 100; i++)
		{
			if (b0.getBit(i)) c0++;
			if (b1.getBit(i)) c1++;
		}
		System.out.println("c0=" + c0 + "  c1=" + c1);

		// Determine EA's BF set-up
		BloomFilter<String> b2 = new BloomFilter<String>(1000, 23);
		System.out.println("k=" +  b2.getK());
	}
	
	public void foo(ArrayList<Integer> ar)
	{
		for (int i = 0; i < ar.size(); i++)
			ar.get(i).reverse(0);
	}
	
	public static void main(String[] args)
	{
		ArrayList<Integer> ar = new ArrayList<Integer>();
		ar.add(new Integer(1234));
		System.out.println(ar.get(0));
	}
}
