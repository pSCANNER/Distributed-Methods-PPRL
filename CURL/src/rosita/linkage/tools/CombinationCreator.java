package rosita.linkage.tools;

import java.util.ArrayList;

public class CombinationCreator {

	int N = 0;
	int k = 0;
	int[] a;
	ArrayList<String> myValues;
	
	public void saveResult(){
		ArrayList<String> currentList= new ArrayList<String>();
		for(int i=1; i<=k;i++){
			currentList.add(myValues.get(a[i]-1));
		}
		results.add(currentList);
	}
	
	public void create(int i){
		for(int j=a[i-1]+1; j<=N-k+i;j++){
			a[i] = j;
			if(i==k){
				saveResult();
			}else{
				create(i+1);
			}
		}
	}
	
	public ArrayList<ArrayList<String>> results = null;
	
	public void generate(ArrayList<String> value, int parMinK){
		results = new ArrayList<ArrayList<String>>();
		myValues = value;
		N = value.size();
		a = new int[N+1];
		for (k=parMinK;k<N+1;k++){
			create(1);
		}
	}
}
