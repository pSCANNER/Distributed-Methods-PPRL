package rosita.linkage.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
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

import rosita.linkage.FRILLinker;
import rosita.linkage.analysis.PPRLDistance;
import cdc.components.AbstractDataSource;
import cdc.components.AbstractDistance;
import cdc.components.AbstractJoinCondition;
import cdc.components.EvaluatedCondition;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.gui.Configs;
import cdc.gui.GUIVisibleComponent;
import cdc.gui.OptionDialog;
import cdc.gui.components.table.TablePanel;
import cdc.gui.components.uicomponents.ManualReviewConfigDialog;
import cdc.gui.wizards.AbstractWizard;
import cdc.impl.em.EMWizard;
import cdc.utils.RJException;


public class S_WeightedJoinCondition extends AbstractJoinCondition {

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
		private S_WeightedJoinCondition oldCondition;
		private DataColumnDefinition[] leftColumns;
		private DataColumnDefinition[] rightColumns;
		
		public WeightedVisibleComponent() {
			acceptLevel.setPreferredSize(new Dimension(40, 20));
			acceptLevel.setHorizontalAlignment(JTextField.CENTER);
			sumLabel.setPreferredSize(new Dimension(40, 20));
			sumLabel.setHorizontalAlignment(JLabel.CENTER);
			tablePanel = new TablePanel(cols);
			tablePanel.multiselectionAllowed(false);
			tablePanel.addAddButtonListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				}
			});
			tablePanel.addEditButtonListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				}
			});
			tablePanel.addRemoveButtonListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Object[] selected = tablePanel.getSelectedRows();
						int[] ids = tablePanel.getSelectedRowId();
						tablePanel.clearSelection();
						for (int i = 0; i < selected.length; i++) {
							sumWeights -= Integer.parseInt((String)((Object[])selected[i])[3]);
							tablePanel.removeRow(ids[i]);
						}
						((JButton)e.getSource()).setEnabled(false);
						sumLabel.setText(String.valueOf(sumWeights));
					}
				});
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
			S_WeightedJoinCondition cond = new S_WeightedJoinCondition(colsLeft, colsRight, colsLeft, colsRight, distances, distances, weights, weights, emptyValues, 0, 100, null, props);
			cond.creator = this;
			return cond;
		}
		
		public JPanel getConfigurationPanel(Object[] objects, int sizeX, int sizeY) {
			
			sourceA = (AbstractDataSource) objects[0];
			sourceB  = (AbstractDataSource) objects[1];
			parent = (Window) objects[2];
			oldCondition = (objects[3] instanceof S_WeightedJoinCondition) ? (S_WeightedJoinCondition)objects[3] : null;
			leftColumns = sourceA.getDataModel().getSortedOutputColumns();
			rightColumns = sourceB.getDataModel().getSortedOutputColumns();
			
			JPanel weightsSumPanel = new JPanel(new GridBagLayout());
			
			JLabel label = new JLabel("Current sum of weights: ");
			weightsSumPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
			weightsSumPanel.add(sumLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,10,0,0), 0, 0));
			
			label = new JLabel("Acceptance level: ");
			weightsSumPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
			weightsSumPanel.add(acceptLevel, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,10,0,0), 0, 0));
			
			sumLabel.addPropertyChangeListener("text", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					int sum = Integer.parseInt((String)evt.getNewValue());
					if (sum != 100) {
						((JLabel)evt.getSource()).setForeground(Color.RED);
					} else {
						((JLabel)evt.getSource()).setForeground(Color.BLACK);
					}
				}
			});
			sumLabel.setText(String.valueOf(sumWeights));
			JButton emButton = new JButton("Run EM method");
			emButton.setToolTipText("Run Expectation-Maximization method to suggest weights");
			emButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int[] weights = new EMWizard().emWizard((AbstractWizard)parent, sourceA, sourceB, getJoinCondition());
					Object[] rows = tablePanel.getRows();
					if (rows != null && weights != null) {
						int sum = 0;
						for (int i = 0; i < rows.length; i++) {
							Object[] row = (Object[])rows[i];
							row[3] = String.valueOf(weights[i]);
							tablePanel.replaceRow(i, row);
							sum += weights[i];
						}
						sumLabel.setText(String.valueOf(sum));
						sumWeights = sum;
					}
				}
			});
			emButton.setPreferredSize(new Dimension(emButton.getPreferredSize().width, 20));
			weightsSumPanel.add(emButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,10,0,25), 0, 0));
			
			JButton reviewButton = new JButton("Manual review");
			reviewButton.setToolTipText("Configure manual review process");
			reviewButton.setPreferredSize(new Dimension(emButton.getPreferredSize().width, 20));
			reviewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ManualReviewConfigDialog dialog = new ManualReviewConfigDialog(parent, Integer.parseInt(acceptLevel.getText()), manualReview);
					if (dialog.getResult() == OptionDialog.RESULT_OK) {
						acceptLevel.setText(String.valueOf(dialog.getAcceptanceLevel()));
						manualReview = dialog.getManualReviewLevel();
						acceptLevel.setEnabled(manualReview == -1);
						manualReviewBulb.setIcon(manualReview != -1 ? Configs.bulbOn : Configs.bulbOff);
					}
				}
			});
			weightsSumPanel.add(reviewButton, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,10,0,25), 0, 0));
			
			JPanel reviewBulbPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			reviewBulbPanel.add(manualReviewBulb);
			reviewBulbPanel.add(new JLabel("Manual review"));
			weightsSumPanel.add(reviewBulbPanel, new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,25), 0, 0));
			
			buffer = new JPanel(new GridBagLayout());			
			buffer.add(tablePanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
			buffer.add(weightsSumPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,10,0,0), 0, 0));
			
			sumWeights = 0;
			tablePanel.removeAllRows();
			if (oldCondition != null) {
				restoreCondition(oldCondition);
			}
			
			return buffer;
		}

		private void restoreCondition(S_WeightedJoinCondition oldCondition) {
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
			return S_WeightedJoinCondition.class;
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
	private DataColumnDefinition[] leftJoinColumns_backup = new DataColumnDefinition[0];
	private DataColumnDefinition[] rightJoinColumns_backup = new DataColumnDefinition[0];
	private AbstractDistance[] distances = new AbstractDistance[0];
	private AbstractDistance[] distances_backup = new AbstractDistance[0];
	private double[] weights = new double[0];
	private double[] weights_backup = new double[0];
	private double[] emptyMatchScore = new double[0];
	private int acceptanceThreshold = 100;
	private int manualReviewThreshold = -1;
	private double pprlThreshold;
	private int bloomSize;
	private WeightedVisibleComponent creator;
	private ArrayList<ImputationRule> ImputationRuleSet = null;
	
	public S_WeightedJoinCondition(DataColumnDefinition[] leftJoinColumns, DataColumnDefinition[] rightJoinColumns, DataColumnDefinition[] leftJoinColumns_Backup, DataColumnDefinition[] rightJoinColumns_Backup,
			AbstractDistance[] distances, AbstractDistance[] distances_backup, double[] weights,double[] weights_backup, double[] emptyValues, double pprlThreshold, int bloomSize, ArrayList<ImputationRule> imputationRuleSet, Map properties) {
		super(properties);
		this.leftJoinColumns = leftJoinColumns;
		this.rightJoinColumns = rightJoinColumns;
		
		this.leftJoinColumns_backup = leftJoinColumns_Backup;
		this.rightJoinColumns_backup = rightJoinColumns_Backup;
		
		this.distances = distances;
		this.distances_backup = distances_backup;
		this.weights = weights;
		this.weights_backup = weights_backup;
		this.emptyMatchScore = emptyValues;
		this.pprlThreshold = pprlThreshold;
		this.bloomSize = bloomSize;
		acceptanceThreshold = Integer.parseInt(getProperty(PROP_ACCEPTANCE_LEVEL));
		ImputationRuleSet = imputationRuleSet;
		if (getProperty(PROP_MANUAL_REVIEW) != null) {
			manualReviewThreshold = Integer.parseInt(getProperty(PROP_MANUAL_REVIEW));
		}
	}
	
	public void setImputationRuleSet(ArrayList<ImputationRule> parImputationRuleSet){
		ImputationRuleSet = parImputationRuleSet;
	}

	public S_WeightedJoinCondition(Map properties) {
		super(properties);
		acceptanceThreshold = Integer.parseInt(getProperty(PROP_ACCEPTANCE_LEVEL));
		if (getProperty(PROP_MANUAL_REVIEW) != null) {
			manualReviewThreshold = Integer.parseInt(getProperty(PROP_MANUAL_REVIEW));
		}
	}
	
	public EvaluatedCondition conditionSatisfied(DataRow rowA, DataRow rowB) {
		double value = 0.0;
		double weightsToGo = 1.0;
		DataColumnDefinition[] rowACols = getRowAcols(rowA);
		DataColumnDefinition[] rowBCols = getRowAcols(rowB);
		ArrayList<Integer> missingFieldList = new ArrayList<Integer>();
		ArrayList<Integer> noMissingFieldList = new ArrayList<Integer>();
		double MissingFieldHandlingOption = emptyMatchScore[0];
		double TotalMissingWeight = 0;
		double TotalNoMissinggWeight = 0;
		
		
		double TotalNoMissingA = 0;
		//Look for fields with missing data
		//find column with missing value
		
//		if(rowA.getData(rowBCols[0]).getValue().toString().trim().startsWith("01110111110100001101111000011110100001110111000010101100000011101111100110101110000011111101010011111") && rowB.getData(rowBCols[6]).getValue().toString().trim().startsWith("11110110100101001101111000001110100111110101000110111110011011101111101010111100101111111111000111101")){
		//if(rowA.getData(rowACols[5]).getValue().toString().trim().toUpperCase().contains("DESTINEE") && rowB.getData(rowBCols[5]).getValue().toString().trim().toUpperCase().contains("DESTINY")){
		//if(rowA.getData(rowACols[6]).getValue().toString().trim().startsWith("01110111110100001101111000011110100001110111000010101100000011101111100110101110000011111101010011111") && rowB.getData(rowBCols[6]).getValue().toString().trim().startsWith("11110110100101001101111000001110100111110101000110111110011011101111101010111100101111111111000111101")){
		//if(rowB.getData(rowBCols[0]).getValue().toString().trim().startsWith("1001111111101111101110111001110111111110111111011111011011101011111011001111100111111111111011011011000010111001111111111111010111101010011011110011011011111101111111111101111010110110011111111111111110111101011111011011111101111111111111111110001111110111")){
		/*if(.trim().equals("1886") && rowB.getData()[6].getValue().toString().trim().equals("1886")){
			int o= 1;
			o++;			
		}*/

		//encryptionFilter = new BloomFilter<String>(defaultBitSetSize, defaultExpectedNumberOfElements);
		
		for (int i = 0;i < rowACols.length; i++){
			DataCell tempCellA = rowA.getData(rowACols[i]);
			DataCell tempCellB = rowB.getData(rowBCols[i]);
			
			/*if(tempCellA.getValue().equals("3186 HELIPORT LOOP") && tempCellB.getValue().equals("706 TORI LANE")){
				int kkk = 0;
				kkk++;
			}*/
			
			if(tempCellA.isEmpty(rowACols[i]) || tempCellB.isEmpty(rowBCols[i])){
				missingFieldList.add(i);
				TotalMissingWeight += weights[i];
			}else{
				noMissingFieldList.add(i);
				TotalNoMissinggWeight += weights[i];
			}
			
			if(!tempCellA.isEmpty(rowACols[i])){
				TotalNoMissingA += weights[i];
			}
		}
		
		
		
		if(missingFieldList.size()>0){
			
			if(MissingFieldHandlingOption==FRILLinker.ATTRIBUTE_DEDUCTION){
				
				ArrayList<Integer> currentMissingFields = new ArrayList<Integer>();
				double newWeights[] = new double[rowACols.length];
				for(int j = 0; j<rowACols.length; j++){
					newWeights[j]=	weights[j];   
				}
				
				for (int i = 0;i < rowACols.length; i++){
					DataCell tempCellA = rowA.getData(rowACols[i]);
					DataCell tempCellB = rowB.getData(rowBCols[i]);
					
					
					
					if(tempCellA.isEmpty(rowACols[i]) || tempCellB.isEmpty(rowBCols[i])){
						double dblTempTotalWeights = 0;
						currentMissingFields.add(i);
						//Calculate total weight
						for(int j = 0; j<rowACols.length; j++){
							if(!currentMissingFields.contains(j))
							{
								dblTempTotalWeights += newWeights[j];
							}
						}
						
						//Redistribute the weight of the field with the missing value to other fields
						
						for(int j = 0; j<rowACols.length; j++){
							if(!currentMissingFields.contains(j))
							{
								newWeights[j] = newWeights[j] + (newWeights[j]/dblTempTotalWeights)*newWeights[i];   
							}
						}
						
						for(int j = 0; j<rowACols.length; j++){
							if(currentMissingFields.contains(j))
							{
								newWeights[j] = 0;   
							}
						}
						
					}
				}
				
				for (int i = 0; i < rowACols.length; i++) {
					DataCell cellA = rowA.getData(rowACols[i]);
					DataCell cellB = rowB.getData(rowBCols[i]);
					if(!currentMissingFields.contains(i)){
						value += distances[i].distance(cellA, cellB) * newWeights[i];
					}
				}
				
				//Equation #2
				if(TotalMissingWeight>=TotalNoMissinggWeight*0.5){
					value = 0;
				}
				
				//Final filter: if the total weights of the missing fields>backup fields the value = 0;
				/*double finalWithDataWeight = 0;
				double finalWithoutDataWeight = 0;
				for(int i=0; i<rowACols.length; i++){
					if(!currentMissingFields.contains(i)){
						finalWithDataWeight+=weights[i];
					}else{
						finalWithoutDataWeight +=weights[i];
					}
				}
				
				if(finalWithoutDataWeight>finalWithDataWeight){
					value=0;
				}*/
				
				//For testing purpose
				/*if(value>=60 && value<80){
					for (int i = 0; i < leftJoinColumns.length; i++) {
						DataCell cellA = rowA.getData(leftJoinColumns[i]);
						DataCell cellB = rowB.getData(rightJoinColumns[i]);
						if(!currentMissingFields.contains(i)){
							System.out.println("A: "+cellA.getValue().toString()+" B:"+ cellB.getValue().toString()+" distance: "+distances[i].distance(cellA, cellB) * newWeights[i]);
						}
					}
				}*/	
				
			}else if(MissingFieldHandlingOption== FRILLinker.DISTANCE_IMPUTATION){
				Boolean isImputationQualified = true;
				for (int i = 0; i < rowACols.length; i++) {
					DataCell cellA = rowA.getData(rowACols[i]);
					DataCell cellB = rowB.getData(rowBCols[i]);
					
					if(!missingFieldList.contains(i)){
						double tempDistance =distances[i].distance(cellA, cellB);
						double localThreshold = 80;
						
						//PPRL distance is more permissive
						if (distances[i].getClass().equals(PPRLDistance.class)){
							localThreshold = 85;
						}
						
						if(tempDistance<localThreshold){
							isImputationQualified = false;
						}
						value += tempDistance * weights[i];
					}
				}
				
				if(isImputationQualified){
					for (int i = 0; i < rowACols.length; i++) {
						DataCell cellA = rowA.getData(rowACols[i]);
						DataCell cellB = rowB.getData(rowBCols[i]);
						if(missingFieldList.contains(i))
						{
							double ImputedValue = 0;
							ImputationRule myRule = new ImputationRule();
							myRule.FieldsWithData = noMissingFieldList;
							myRule.FieldWithMissingData = i;
							for(int j=0;j<ImputationRuleSet.size();j++){
								if(compareImputationRules(myRule, ImputationRuleSet.get(j))){
									ImputedValue = ImputationRuleSet.get(j).ImputedValue;
								}
							}
							
							value += ImputedValue * weights[i];
						}
					}
				}
				
				//Final filter: if the total weights of the missing fields>backup fields the value = 0;
				/*double finalWithDataWeight = 0;
				double finalWithoutDataWeight = 0;
				for(int i=0; i<rowACols.length; i++){
					if(!missingFieldList.contains(i)){
						finalWithDataWeight+=weights[i];
					}else{
						finalWithoutDataWeight +=weights[i];
					}
				}
				
				if(finalWithoutDataWeight>finalWithDataWeight){
					value=0;
				}*/
				
				
				if(value>50 && value<80){
					int test = 0;
					test++;
				}
				
			}else if(MissingFieldHandlingOption == FRILLinker.BACKUP_FULL || MissingFieldHandlingOption == FRILLinker.BACKUP_OPTIMAL){
				ArrayList<Integer> missingFieldList_backup = new ArrayList<Integer>();
				
				/*if(rowA.getData(rowACols[2]).getValue().equals("PRICHARD") && rowB.getData(rowBCols[2]).getValue().equals("RICHARD")){
					int o= 1;
					o++;			
				}*/
				
				double newWeights_backup[] = new double[leftJoinColumns_backup.length];
				for (int i = 0;i < leftJoinColumns_backup.length; i++){
					newWeights_backup[i] = weights_backup[i];
				}
				
				for (int i = 0;i < leftJoinColumns_backup.length; i++){
					DataCell tempCellA = rowA.getData(leftJoinColumns_backup[i]);
					DataCell tempCellB = rowB.getData(rightJoinColumns_backup[i]);
					
					if(tempCellA.getValue().equals("KAOHERINE.W.CRAFTON@DODGEIT.COM") && tempCellB.getValue().equals("706 TORI LANE")){
						int kkk = 0;
						kkk++;
					}
					
					if(tempCellA.isEmpty(leftJoinColumns_backup[i]) || tempCellB.isEmpty(rightJoinColumns_backup[i])){
						double dblTempTotalWeights = 0;
						missingFieldList_backup.add(i);
						//Calculate total weight
						for(int j = 0; j<leftJoinColumns_backup.length; j++){
							if(!missingFieldList_backup.contains(j))
							{
								dblTempTotalWeights += newWeights_backup[j];
							}
						}
						
						//Redistribute the weight of the field with the missing value to other fields
						
						for(int j = 0; j<leftJoinColumns_backup.length; j++){
							if(!missingFieldList_backup.contains(j))
							{
								newWeights_backup[j] = newWeights_backup[j] + (newWeights_backup[j]/dblTempTotalWeights)*newWeights_backup[i];   
							}
						}
						
						for(int j = 0; j<leftJoinColumns_backup.length; j++){
							if(missingFieldList_backup.contains(j))
							{
								newWeights_backup[j] = 0;   
							}
						}
					}
				}
				
				//Apply new weights
				
				for (int i = 0; i < leftJoinColumns_backup.length; i++) {
					DataCell cellA = rowA.getData(leftJoinColumns_backup[i]);
					DataCell cellB = rowB.getData(rightJoinColumns_backup[i]);
					if(!missingFieldList_backup.contains(i)){
						value += distances_backup[i].distance(cellA, cellB) * newWeights_backup[i];
					}
				}
				
				
				//Final filter: if the total weights of the missing fields>backup fields the value = 0;
				double finalBackupWeight = 0;
				double finalMissingPrimeWeight = 0;
				for(int i=0; i<leftJoinColumns_backup.length; i++){
					if(!missingFieldList_backup.contains(i) && i>leftJoinColumns.length-1){
						finalBackupWeight+=weights_backup[i];
					}else if(missingFieldList_backup.contains(i) && i<leftJoinColumns.length){
						finalMissingPrimeWeight +=weights_backup[i];
					}
				}
				
				if(finalMissingPrimeWeight>finalBackupWeight){
					value=0;
				}
				
				
				//For testing
				if(value>100){
					for (int i = 0; i < leftJoinColumns_backup.length; i++) {
						DataCell cellA = rowA.getData(leftJoinColumns_backup[i]);
						DataCell cellB = rowB.getData(rightJoinColumns_backup[i]);
						if(!missingFieldList_backup.contains(i)){
							System.out.println("A: "+cellA.getValue().toString()+" B:"+ cellB.getValue().toString()+" distance: "+distances_backup[i].distance(cellA, cellB) * newWeights_backup[i]);
						}
					}
				}
			}else if(MissingFieldHandlingOption>=0){
				for (int i = 0; i < rowACols.length; i++) {
					DataCell cellA = rowA.getData(rowACols[i]);
					DataCell cellB = rowB.getData(rowBCols[i]);
					
					if (cellA.isEmpty(rowACols[i]) || cellB.isEmpty(rowBCols[i])) {
						value += emptyMatchScore[i] * 100 * weights[i];
					} else {
						value += distances[i].distance(cellA, cellB) * weights[i];
					}
					
					if (TotalNoMissingA == 0 && value>0){
						int hhh = 0;
						hhh++;
					}
					
					if(value>100){
						//int test = 0;
						//test++;
						value = 100;
					}
					
					weightsToGo -= weights[i];
					if (this.isCanUseOptimisticEval() && value + weightsToGo*100 < acceptanceThreshold) {
						return new EvaluatedCondition(false, false, (int)value);
					}
				}
				if(value>100){
					//int test = 0;
					//test++;
					value = 100;
				}
			}
		}else{
			for (int i = 0; i < rowACols.length; i++) {
				DataCell cellA = rowA.getData(rowACols[i]);
				DataCell cellB = rowB.getData(rowBCols[i]);
				if ((cellA.isEmpty(rowACols[i]) || cellB.isEmpty(rowBCols[i]))) {
					value += emptyMatchScore[i] * 100 * weights[i];
				} else {
					value += distances[i].distance(cellA, cellB) * weights[i];
					//if (value>0 && !rowA.getData()[5].getValue().toString().equals(rowB.getData()[5].getValue().toString())){
					//	int ppp=0;
					//	ppp++;
					//}
				}
				if(value>100){
					//int test = 0;d
					//test++;
					value = 100;
//					111110111111011111111111111111101110111011110111111111011111111101110111110011110111101011111110011111101111111011101111
//					111010111011100101111011111110111111101111001110111011111111111111101011100001101111111111011110111111011111111111111010					
				}
				weightsToGo -= weights[i];
				if (this.isCanUseOptimisticEval() && value + weightsToGo*100 < acceptanceThreshold) {
					return new EvaluatedCondition(false, false, (int)value);
				}
			}
			
			
		}
		return new EvaluatedCondition(value >= acceptanceThreshold, manualReviewThreshold != -1 ? value <= manualReviewThreshold : false, (int)value);
	}
	
	public Boolean compareImputationRules(ImputationRule rule1, ImputationRule rule2){
		Boolean result = true;
		
		for (int i:rule2.FieldsWithData){
			if(!rule1.FieldsWithData.contains(i)){
				result = false;
			}
		}

		for (int i:rule1.FieldsWithData){
			if(!rule2.FieldsWithData.contains(i)){
				result = false;
			}
		}
		
		if(rule1.FieldWithMissingData!=rule2.FieldWithMissingData){
			result = false;
		}

		return result;
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
	
	public DataColumnDefinition[] getLeftJoinColumnsBackup() {
		return leftJoinColumns_backup;
	}

	public DataColumnDefinition[] getRightJoinColumnsBackup() {
		return rightJoinColumns_backup;
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
	
	public AbstractDistance[] getDistanceFunctionsBackup() {
		return this.distances_backup;
	}

	public double[] getWeights() {
		return weights;
	}
	
	public double getPPRLThreshold() {
		return this.pprlThreshold;
	}
	
	public int getBloomSize() {
		return this.bloomSize;
	}
	
	public double[] getWeightsBackup() {
		return weights_backup;
	}
	
	public double[] getEmptyMatchScore() {
		return emptyMatchScore;
	}
	
	public ArrayList<ImputationRule> getImputationRuleSet() {
		return ImputationRuleSet;
	}
	
	public Object clone() {
		return new S_WeightedJoinCondition(getLeftJoinColumns(), getRightJoinColumns(), getLeftJoinColumnsBackup(), getRightJoinColumnsBackup(), getDistanceFunctions(), getDistanceFunctionsBackup(), getWeights(),getWeightsBackup(), getEmptyMatchScore(), getPPRLThreshold(), getBloomSize() ,getImputationRuleSet(), getProperties());
	}

	public static JComponent getLeftAttributeComponent(Object[] objects) {
		return (JComponent) objects[3];
	}
	
	public static JComponent getRightAttributeComponent(Object[] objects) {
		return (JComponent) objects[4];
	}

	@Override
	public void saveToXML(Document doc, Element node) {
		// TODO Auto-generated method stub
		
	}
	
}