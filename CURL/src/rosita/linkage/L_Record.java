package rosita.linkage;

public class L_Record 
{
	public String[] values;
	
	public L_Record (String[] inputValues)
	{
		this.values = new String[inputValues.length];
		for (int i = 0; i < inputValues.length; i++)
			this.values[i] = inputValues[i];
	}
	
	public int size()
	{
		return values.length;
	}
	
	public String get(int i)
	{
		return values[i];
	}

	public String[] getValues()
	{
		return values;
	}
}
