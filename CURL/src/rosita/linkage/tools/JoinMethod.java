package rosita.linkage.tools;

public enum JoinMethod {
	NESTED_LOOP_JOIN,
	SORTED_NEIGHBOURHOOD,
	BLOCKING_SEARCH;
	
	static public boolean isMember(JoinMethod aName) {
		JoinMethod[] aWIs = JoinMethod.values();
       for (JoinMethod aWI : aWIs)
           if (aWI.equals(aName))
               return true;
       return false;
     }
}
