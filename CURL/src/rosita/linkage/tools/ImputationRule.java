package rosita.linkage.tools;

import java.util.ArrayList;

public class ImputationRule {
	public ArrayList<Integer> FieldsWithData;
	public int FieldWithMissingData;
	public double ImputedValue;
	public int intUp;
	public int intDown;
	
	public ImputationRule(){
		FieldsWithData = new ArrayList<Integer>();
		intUp = 0;
		intDown = 0;
	}
	
	public void printContent(){
		System.out.print("Fields with data: ");
		for(int i=0;i<FieldsWithData.size();i++){
			System.out.print(FieldsWithData.get(i)+" ");
		}
		System.out.println();
		System.out.println("Field missing data: "+FieldWithMissingData);
		System.out.println("Imputed value: "+ImputedValue);
	}
}
