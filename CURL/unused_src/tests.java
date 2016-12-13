
public class tests 
{
	public static void main( String args[] )
	{
		BloomFilterTest();
	}
	
	public static void BloomFilterTest()
	{
		
		// Test strings
		String s = "Brandon Abbott";
		String s2 = "Vijay Thurimella";
		
		// Previous encryption process
		BF bf1 = new BF(s);
		System.out.println("bf1:" + bf1.toString());

		// Try to duplicate current encryption process
		BloomFilter<String> bf2 = new BloomFilter<String>(100, 30);
		bf2.addAsBigrams(s);
		System.out.println("bf2:" + bf2.toString());
		
		// Test on second string
		bf1 =  new BF(s2);
		bf2.clear();
		bf2.addAsBigrams(s2);
		
		System.out.println("bf1:" + bf1.toString());
		System.out.println("bf2:" + bf2.toString());
		
		// Test on blank string
		bf2.clear();
		System.out.println("bf2:" + bf2.toString());
		BloomFilter<String> b = new BloomFilter<String>(100, 30);
		b.add("Hello");
		System.out.println(b.toString());
		
	}
	
	
}
