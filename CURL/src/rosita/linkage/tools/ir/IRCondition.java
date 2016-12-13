package rosita.linkage.tools.ir;

import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cdc.components.AbstractDataSource;
import cdc.components.AbstractDistance;
import cdc.components.AbstractJoinCondition;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.gui.Configs;
import cdc.gui.GUIVisibleComponent;
import cdc.gui.components.table.TablePanel;
import cdc.utils.RJException;


public class IRCondition extends AbstractJoinCondition {

	public static class ListListener implements ListSelectionListener {

		private PropertyChangeListener listener;
		
		public ListListener(PropertyChangeListener propertyListener) {
			listener = propertyListener;
		}

		public void valueChanged(ListSelectionEvent arg0) {
			listener.propertyChange(new PropertyChangeEvent(arg0.getSource(), "list-selection", null, null));
		}

	}
	
	public static class DocumentChangedAction implements DocumentListener {
		private PropertyChangeListener listener;
		private JTextField list;
		public DocumentChangedAction(JTextField weight, PropertyChangeListener listener) {
				this.listener = listener;
				this.list = weight;
		}
		public void changedUpdate(DocumentEvent arg0) {
			listener.propertyChange(new PropertyChangeEvent(list, "text", null, null));
		}
		public void insertUpdate(DocumentEvent arg0) {
			listener.propertyChange(new PropertyChangeEvent(list, "text", null, null));
		}
		public void removeUpdate(DocumentEvent arg0) {
			listener.propertyChange(new PropertyChangeEvent(list, "text", null, null));
		}
	}

	public static class WeightedVisibleComponent extends GUIVisibleComponent {

		
		private static final String[] cols = {"Comparison method", "Left column", "Right column", "Weight", "Empty value score"};
		private static final int HEIGHT_PANEL_BELOW = 50;
		
		private int sumWeights = 0;
		private JLabel sumLabel = new JLabel();
		private JTextField acceptLevel = new JTextField(String.valueOf(100));
		private JLabel manualReviewBulb = new JLabel(Configs.bulbOff);
		private int manualReview = -1;
		
		private AbstractDataSource sourceA;
		private AbstractDataSource sourceB;
		
		private JPanel buffer;
		private TablePanel tablePanel;
		private Window parent;
		private IRCondition oldCondition;
		private DataColumnDefinition[] leftColumns;
		private DataColumnDefinition[] rightColumns;
		
		public WeightedVisibleComponent() {
			
			
		}
		
		public Object generateSystemComponent() throws RJException, IOException {
			return getJoinCondition();
		}

		private AbstractJoinCondition getJoinCondition() {
			Object[] data = tablePanel.getRows();
			DataColumnDefinition[] colsLeft = new DataColumnDefinition[data.length];
			DataColumnDefinition[] colsRight = new DataColumnDefinition[data.length];
			AbstractDistance[] distances = new AbstractDistance[data.length];
			double[] weights = new double[data.length];
			double[] emptyValues = new double[data.length];
			
			for (int i = 0; i < distances.length; i++) {
				Object[] row = (Object[]) data[i];
				distances[i] = (AbstractDistance)row[0];
				colsLeft[i] = (DataColumnDefinition)row[1];
				colsRight[i] = (DataColumnDefinition)row[2];
				weights[i] = Integer.parseInt((String)row[3]) / (double)100;
				emptyValues[i] = Double.parseDouble((String)row[4]);
			}
			Map props = new HashMap();
			props.put(PROP_ACCEPTANCE_LEVEL, acceptLevel.getText());
			if (manualReview != -1) {
				props.put(PROP_MANUAL_REVIEW, String.valueOf(manualReview));
			}
			IRCondition cond = new IRCondition(colsLeft, colsRight, distances, weights, emptyValues, props);
			cond.creator = this;
			return cond;
		}
		
		public JPanel getConfigurationPanel(Object[] objects, int sizeX, int sizeY) {
			return null;
		}

		private void restoreCondition(IRCondition oldCondition) {
			if (failToVerify(sourceA, oldCondition.getLeftJoinColumns()) || failToVerify(sourceB, oldCondition.getRightJoinColumns())) {
				return;
			}
			AbstractDistance[] dists = oldCondition.distances;
			DataColumnDefinition[] leftCols = oldCondition.getLeftJoinColumns();
			DataColumnDefinition[] rightCols = oldCondition.getRightJoinColumns();
			double[] weights = oldCondition.weights;
			double[] emptyValues = oldCondition.getEmptyMatchScore();
			for (int i = 0; i < rightCols.length; i++) {
				tablePanel.addRow(new Object[] {dists[i], leftCols[i], rightCols[i], String.valueOf((int)(weights[i]*100)), String.valueOf(emptyValues[i])});
				sumWeights += (int)(weights[i]*100);
			}
			acceptLevel.setText(String.valueOf(oldCondition.acceptanceThreshold));
			sumLabel.setText(String.valueOf(sumWeights));
			if (oldCondition.getProperty(PROP_MANUAL_REVIEW) != null) {
				acceptLevel.setEnabled(false);
				manualReview = Integer.parseInt(oldCondition.getProperty(PROP_MANUAL_REVIEW));
				manualReviewBulb.setIcon(Configs.bulbOn);
			}
		}

		private boolean failToVerify(AbstractDataSource source, DataColumnDefinition[] columns) {
			DataColumnDefinition[] sourceCols = source.getDataModel().getSortedOutputColumns();
			labelA: for (int i = 0; i < columns.length; i++) {
				for (int j = 0; j < sourceCols.length; j++) {
					if (columns[i].equals(sourceCols[j])) {
						continue labelA;
					}
				}
				return true;
			}
			return false;
		}

		public Class getProducedComponentClass() {
			return IRCondition.class;
		}

		public String toString() {
			return "Weighted join condition";
		}

		public boolean validate(JDialog dialog) {
			try {
				Integer.parseInt(acceptLevel.getText());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(dialog, "Acceptance level should be an integer value.");
				return false;
			}
			if (tablePanel.getRows().length == 0) {
				JOptionPane.showMessageDialog(dialog, "At least one condition is required.");
				return false;
			}
			if (Integer.parseInt(sumLabel.getText()) != 100) {
				JOptionPane.showMessageDialog(dialog, "Sum of weights have to be 100.");
				return false;
			}
			if (Integer.parseInt(acceptLevel.getText()) > 100) {
				JOptionPane.showMessageDialog(dialog, "Acceptance level cannot exceed 100.");
				return false;
			}
			return true;
		}
		
		public void restoreWeights(int[] weights) {
			for (int i = 0; i < weights.length; i++) {
				tablePanel.setValueAt(i, 3, weights[i] + "");
			}
		}
		
		public void setSize(int x, int y) {
			tablePanel.setPreferredSize(new Dimension(x, y - HEIGHT_PANEL_BELOW));
			buffer.setPreferredSize(new Dimension(x, y));
			buffer.setSize(x, y);
			parent.validate();
			parent.repaint();
		}

		public void windowClosing(JDialog parent) {
			// TODO Auto-generated method stub
			
		}

	}
	
	
	public static String PROP_ACCEPTANCE_LEVEL = "acceptance-level";	
	private static final String CONDITION_TAG = "condition";
	private static final String LEFT_ROW_TAG = "left-column";
	private static final String RIGHT_ROW_TAG = "right-column";
	private static final String WEIGHT_TAG = "weight";
	private static final String EMPTY_MATCH_SCORE_TAG = "empty-match-score";
	private static final String PROP_MANUAL_REVIEW = "manual-review-level";
	
	private DataColumnDefinition[] leftJoinColumns = new DataColumnDefinition[0];
	private DataColumnDefinition[] rightJoinColumns = new DataColumnDefinition[0];
	private AbstractDistance[] distances = new AbstractDistance[0];
	private double[] weights = new double[0];
	private double[] emptyMatchScore = new double[0];
	private int acceptanceThreshold = 100;
	private int manualReviewThreshold = -1;
	private WeightedVisibleComponent creator;
	
	public IRCondition(DataColumnDefinition[] leftJoinColumns, DataColumnDefinition[] rightJoinColumns,
			AbstractDistance[] distances, double[] weights, double[] emptyValues, Map properties) {
		super(properties);
		this.leftJoinColumns = leftJoinColumns;
		this.rightJoinColumns = rightJoinColumns;
		this.distances = distances;
		this.weights = weights;
		this.emptyMatchScore = emptyValues;
		acceptanceThreshold = Integer.parseInt(getProperty(PROP_ACCEPTANCE_LEVEL));
		if (getProperty(PROP_MANUAL_REVIEW) != null) {
			manualReviewThreshold = Integer.parseInt(getProperty(PROP_MANUAL_REVIEW));
		}
	}

	public IRCondition(Map properties) {
		super(properties);
		acceptanceThreshold = Integer.parseInt(getProperty(PROP_ACCEPTANCE_LEVEL));
		if (getProperty(PROP_MANUAL_REVIEW) != null) {
			manualReviewThreshold = Integer.parseInt(getProperty(PROP_MANUAL_REVIEW));
		}
	}
	
	/*public EvaluatedCondition conditionSatisfied(DataRow rowA, DataRow rowB) {
		double value = 0.0;
		double weightsToGo = 1.0;
		DataColumnDefinition[] rowACols = getRowAcols(rowA);
		DataColumnDefinition[] rowBCols = getRowAcols(rowB);
		for (int i = 0; i < rowACols.length; i++) {
			DataCell cellA = rowA.getData(rowACols[i]);
			DataCell cellB = rowB.getData(rowBCols[i]);
			if (emptyMatchScore[i] != 0 && (cellA.isEmpty(rowACols[i]) || cellB.isEmpty(rowBCols[i]))) {
				value += emptyMatchScore[i] * 100 * weights[i];
			} else {
				value += distances[i].distance(cellA, cellB) * weights[i];
			}
			weightsToGo -= weights[i];
			if (this.isCanUseOptimisticEval() && value + weightsToGo*100 < acceptanceThreshold) {
				return new EvaluatedCondition(false, false, (int)value);
			}
		}
	
		return new EvaluatedCondition(value >= acceptanceThreshold, manualReviewThreshold != -1 ? value <= manualReviewThreshold : false, (int)value);
	}*/
	
	
	public IREvaluatedCondition conditionSatisfied(DataRow rowA, DataRow rowB) {
		
		String strMatch = "";
		String strNonMatch = "";
		double EligibleData = 0;
		double EligibleMissing = 0;
		
		
		DataColumnDefinition[] rowACols = getRowAcols(rowA);
		DataColumnDefinition[] rowBCols = getRowAcols(rowB);
		for (int i = 0; i < rowACols.length; i++) {
			DataCell cellA = rowA.getData(rowACols[i]);
			DataCell cellB = rowB.getData(rowBCols[i]);

			//LocalThreshold is a variable that indicates a match in the creation of imputation methods
			double LocalThreshold = 80;

			if(distances[i].distance(cellA, cellB) > LocalThreshold){
				strMatch += i + "|";
				//If two records have at least one matching field, their distance will be saved
				EligibleData += weights[i];
			}else{
				strNonMatch += i + "|";
				EligibleMissing +=weights[i];
			}
		}
		
		return new IREvaluatedCondition(EligibleData>EligibleMissing, false, EligibleData>EligibleMissing?100:0, strMatch);
	}
	
	private DataColumnDefinition[] getRowAcols(DataRow rowA) {
		if (leftJoinColumns[0].getSourceName().equals(rowA.getSourceName())) {
			return leftJoinColumns;
		} else {
			return rightJoinColumns;
		}
	}

	public void addCondition(DataColumnDefinition left, DataColumnDefinition right, AbstractDistance distance, int weight, double emptyMatchScore) {
		DataColumnDefinition[] leftJoinColumns = new DataColumnDefinition[this.leftJoinColumns.length + 1];
		System.arraycopy(this.leftJoinColumns, 0, leftJoinColumns, 0, this.leftJoinColumns.length);
		leftJoinColumns[leftJoinColumns.length - 1] = left;
		this.leftJoinColumns = leftJoinColumns;
		
		DataColumnDefinition[] rightJoinColumns = new DataColumnDefinition[this.rightJoinColumns.length + 1];
		System.arraycopy(this.rightJoinColumns, 0, rightJoinColumns, 0, this.rightJoinColumns.length);
		rightJoinColumns[rightJoinColumns.length - 1] = right;
		this.rightJoinColumns = rightJoinColumns;
		
		AbstractDistance[] distances = new AbstractDistance[this.distances.length + 1];
		System.arraycopy(this.distances, 0, distances, 0, this.distances.length);
		distances[distances.length - 1] = distance;
		this.distances = distances;
		
		double[] weights = new double[this.weights.length + 1];
		System.arraycopy(this.weights, 0, weights, 0, this.weights.length);
		weights[distances.length - 1] = weight / (double)100;
		this.weights = weights;
		
		double[] empty = new double[this.emptyMatchScore.length + 1];
		System.arraycopy(this.emptyMatchScore, 0, empty, 0, this.emptyMatchScore.length);
		empty[distances.length - 1] = emptyMatchScore;
		this.emptyMatchScore = empty;
	}

	public DataColumnDefinition[] getLeftJoinColumns() {
		return leftJoinColumns;
	}

	public DataColumnDefinition[] getRightJoinColumns() {
		return rightJoinColumns;
	}

	public void saveToXML(Document doc, Element node) {
		
	}

	
	public static AbstractJoinCondition fromXML(AbstractDataSource leftSource, AbstractDataSource rightSource, Element node) throws RJException {
		
		return null;
	}
	
	private static DataColumnDefinition findByName(DataColumnDefinition[] dataModel, String name) {
		for (int i = 0; i < dataModel.length; i++) {
			if (dataModel[i].getColumnName().equals(name)) {
				return dataModel[i];
			}
		}
		return null;
	}

	public static GUIVisibleComponent getGUIVisibleComponent() {
		return new WeightedVisibleComponent();
	}

	public void setWeights(int[] weights) {
		for (int i = 0; i < weights.length; i++) {
			this.weights[i] = weights[i];
		}
		if (creator != null) {
			creator.restoreWeights(weights);
		}
	}

	public AbstractDistance[] getDistanceFunctions() {
		return this.distances;
	}

	public double[] getWeights() {
		return weights;
	}
	
	public double[] getEmptyMatchScore() {
		return emptyMatchScore;
	}
	
	public Object clone() {
		return new IRCondition(getLeftJoinColumns(), getRightJoinColumns(), getDistanceFunctions(), getWeights(), getEmptyMatchScore(), getProperties());
	}

	public static JComponent getLeftAttributeComponent(Object[] objects) {
		return (JComponent) objects[3];
	}
	
	public static JComponent getRightAttributeComponent(Object[] objects) {
		return (JComponent) objects[4];
	}
	
}
