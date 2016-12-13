package rosita.linkage.tools.ir;

import cdc.components.EvaluatedCondition;

public class IREvaluatedCondition extends EvaluatedCondition{
	public String matchString = "";
	public IREvaluatedCondition(boolean satisfied, boolean manualReview, int confidence, String parMatchString){
		super(satisfied, manualReview, confidence);
		matchString = parMatchString;
	}

}
