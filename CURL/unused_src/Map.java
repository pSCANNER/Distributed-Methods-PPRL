
public class Map {
	String hook;
	BF filter;
	public Map(String input, BF filter) throws Exception{
		hook = "";
		//if(checkDOB(input))
			hook = Encryption.SHA1(noYear(input));
		//if last name is added to blocking uncomment the next line
		// this gives the first three letters of the 
		//last name
		//else
		//hook = Encryption.SHA1(input.substring(0,3));
		this.filter = filter;
	}
	public String getHook() {
		return hook;
	}
	public void setHook(String hook) {
		this.hook = hook;
	}
	public BF getFilter() {
		return filter;
	}
	public void setFilter(BF filter) {
		this.filter = filter;
	}
	public String toString(){
		return hook + "," + filter;
	}
	private String noYear(String input){
		int count = 0;
		String end = "";
		Character pos = new Character('0');
		for(int i = 0; i < input.length(); i++){
			if(count == 2){
				end = input.substring(0, i);
				break;
			}
			if(!pos.isDigit(input.charAt(i)))
				count++;
		}
		end = removeChar(end.toString());
		return end;
	}
	private String removeChar(String input){
		String end = "";
		Character pos = new Character('0');
		for(int i =0; i < input.length(); i++){
			if(pos.isDigit(input.charAt(i)))
				end += input.charAt(i);
		}
		return end.toString();
	}
	public boolean checkDOB(String in){
		Character c = new Character('r');
		for(int i =0; i < in.length(); i++){
			if(!c.isDigit(in.charAt(i)))
				return false;
		}
		return true;
	}
}
