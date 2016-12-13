package rosita.linkage.tests;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
public class ListTest 
{
	public static void main(String[] args)
	{
		
		ArrayList<String> al = new ArrayList<String>();
		LinkedList<String> ll = new LinkedList<String>();
		
		al.add("Hello, ");
		ll.add("Hello, ");
		
		al.add("World!");
		ll.add("World!");
		
		al.add(" How are you?");
		ll.add(" How are you?");
		
	
		System.out.println("ArrayList:");
		while (al.size() != 0)
			System.out.print(al.remove(0));
	
		System.out.println();
		
		
		System.out.println("Iterator:");
		
		Iterator<String> it = ll.iterator();
		
		while (it.hasNext())
		{
			System.out.print(it.next());
			it.remove();
		}
		
		System.out.println();
	}
}
