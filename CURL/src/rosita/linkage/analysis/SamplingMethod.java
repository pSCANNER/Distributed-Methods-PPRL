package rosita.linkage.analysis;

public class SamplingMethod {
	
	public static final String ALL = "ALL";
	public static final String TOP = "TOP";
	public static final String RANDOM = "RANDOM";
	
	public String Method;
	public int N;
	
	public SamplingMethod(String parMethod, int parN){
		Method = parMethod;
		N = parN;
	}
}
