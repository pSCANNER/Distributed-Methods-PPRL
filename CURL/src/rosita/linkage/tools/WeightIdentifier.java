package rosita.linkage.tools;

public enum WeightIdentifier {
	MANUAL,
	EM;
	
	static public boolean isMember(WeightIdentifier aName) {
			WeightIdentifier[] aWIs = WeightIdentifier.values();
	       for (WeightIdentifier aWI : aWIs)
	           if (aWI.equals(aName))
	               return true;
	       return false;
	}

}
