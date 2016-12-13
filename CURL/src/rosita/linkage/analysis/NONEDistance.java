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

public class NONEDistance extends AbstractDistance{

	private static class NONEDistanceVisibleComponent extends GUIVisibleComponent {

		public Object generateSystemComponent() throws RJException, IOException {
			return new NONEDistance();
		}

		public JPanel getConfigurationPanel(Object[] objects, int sizeX, int sizeY) {
			return new ParamsPanel();
		}

		public Class getProducedComponentClass() {
			return PPRLDistance.class;
		}

		public String toString() {
			return "NONE Distance";
		}

		public boolean validate(JDialog dialog) {
			return true;
		}
	}
	
	public NONEDistance() {
		super(new HashMap());
	}
	
	private double distance(String s1, String s2) {

		return 1;
	}
	
	public NONEDistance(Map props) {
		super(props);
	}

	@Override
	public double distance(DataCell cellA, DataCell cellB) {
		
		String s1 = cellA.getValue().toString();
		String s2 = cellB.getValue().toString();
		
		return distance(s1, s2);
	}
}
