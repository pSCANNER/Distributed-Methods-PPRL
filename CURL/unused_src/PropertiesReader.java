import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;


public class PropertiesReader{
	private Properties p;
	private Hashtable<String, String> properties;
	
	public PropertiesReader(){
		p = new Properties();
		properties = new Hashtable<String, String>();
	}
	public PropertiesReader(String fileName){
		try{
			p = new Properties();
			p.load(new FileInputStream(fileName));
			properties = new Hashtable<String, String>();
			setKeys(p.keySet());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void loadFile(String fileName){
		try {
			p.load(new FileInputStream(fileName));
			setKeys(p.keySet());
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not load the file into input stream");
			e.printStackTrace();
		}
	}
	private ArrayList<String> upperCaseKeys(Set<Object> name){
		ArrayList<String> temp = new ArrayList<String>();
		for(Object o : name){
			String s = (String)o;
			temp.add(s.toUpperCase());
		}
		return temp;
	}
	private void setKeys(Set<Object> name){
		int i = 0;
		ArrayList<String> temp = upperCaseKeys(name);
		for(Object o : name){
			properties.put(temp.get(i), (String)p.get(o));
			i++;
		}
	}
	public String get(String key){
		return properties.get(key);
	}
	public ArrayList<String> keyList(){
		ArrayList<String> returnList = new ArrayList<String>();
		for(String s : properties.keySet())
			returnList.add(s);
		return returnList;
	}
	public String[] getList(){
		ArrayList<String> keys = keyList();
		String[] temp = new String[5];
		for(int i = 0; i < keys.size(); i++)
			temp[i] = properties.get(keys.get(i));
		return temp;
	}
}
