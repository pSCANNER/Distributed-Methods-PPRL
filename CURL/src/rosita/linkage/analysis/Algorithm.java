package rosita.linkage.analysis;


public enum Algorithm 
{
	ADDRESS_DISTANCE,
	DATE_DISTANCE,
	EQUAL_FIELDS_BOOLEAN_DISTANCE,
	PPRL,
	EDIT_DISTANCE,
	JARO_WINKLER,
	NUMERIC_DISTANCE,
	QGRAM_DISTANCE,
	SOUNDEX_DISTANCE,
	LEV_DISTANCE,
	NONE;
	
	static public boolean isMember(Algorithm aName) {
		Algorithm[] aWIs = Algorithm.values();
       for (Algorithm aWI : aWIs)
           if (aWI.equals(aName))
               return true;
       return false;
    }
}
