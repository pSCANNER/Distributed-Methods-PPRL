package rosita.linkage.analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import cdc.components.AbstractDistance;
import cdc.datamodel.DataCell;
import cdc.gui.GUIVisibleComponent;
import cdc.gui.components.paramspanel.ParamsPanel;
import cdc.utils.RJException;

public class PPRLDistance extends AbstractDistance{
	
	private static class PPRLDistanceVisibleComponent extends GUIVisibleComponent {

		public Object generateSystemComponent() throws RJException, IOException {
			return new PPRLDistance(0);
		}

		public JPanel getConfigurationPanel(Object[] objects, int sizeX, int sizeY) {
			return new ParamsPanel();
		}

		public Class getProducedComponentClass() {
			return PPRLDistance.class;
		}

		public String toString() {
			return "PPRL Distance";
		}

		public boolean validate(JDialog dialog) {
			return true;
		}
	}
	
	private double dblThreshold = 0;
	
	public PPRLDistance(double parThreshold) {
		super(new HashMap());
		this.dblThreshold = parThreshold;
	}
	
	private double distance(String s1, String s2) {

		if(s1.equals(s2)){
			int oo = 0;
		}
		
		double result = 0;
		int hitsRecord = 0;
		int hitsLinkedRecord = 0;
		int hitsSimilar = 0;

		String sr = s1;
		String slr = s2;

		for (int i = 0; i < sr.length(); i++)
		{
			if (sr.charAt(i) == '1')
				hitsRecord++;
			if (slr.charAt(i) == '1')
				hitsLinkedRecord++;
			if (slr.charAt(i) == '1' && sr.charAt(i) == '1')
				hitsSimilar++;
		}

		if (hitsRecord == 0 || hitsLinkedRecord == 0)
			return 0.0;

		result = 2.0 * (double) hitsSimilar;
		result /= (double)(hitsRecord + hitsLinkedRecord);
		
		//if(result<this.dblThreshold) return 0;
		if(result<0.9) 
			return 0;
		else{
			int tttt = 0;
			tttt++;
		}
				
		
		//Convert the value to FRIL standard
		result = result * 100;
		
		return result;
	}
	
	public PPRLDistance(Map props) {
		super(props);
	}

	@Override
	public double distance(DataCell cellA, DataCell cellB) {
		
		String s1 = cellA.getValue().toString();
		String s2 = cellB.getValue().toString();
		
		return distance(s1, s2);
	}
	
	public boolean distanceSatisfied(DataCell cellA, DataCell cellB){
		String s1 = cellA.getValue().toString();
		String s2 = cellB.getValue().toString();
		
		return distance(s1, s2)>90;
	}
}
