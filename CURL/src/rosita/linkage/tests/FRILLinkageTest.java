package rosita.linkage.tests;

import rosita.linkage.FRILLinker;

public class FRILLinkageTest {

	public static void main(String[] args) 
	{
		String str_lnk_file = "";
		
		for(int i =0; i<args.length; i++){
			if (args[i].split("\\=")[0].toLowerCase().equals("file")){
				str_lnk_file = args[i].split("\\=")[1].trim();
			}
		}
		
		if(str_lnk_file.length()==0){
			System.out.print("Linkage configuration file is missing!!!");
			System.exit(0);
		}
		
		FRILLinker myFRILLinker = new FRILLinker(str_lnk_file);
		myFRILLinker.link();
		
		
	}
	

}
