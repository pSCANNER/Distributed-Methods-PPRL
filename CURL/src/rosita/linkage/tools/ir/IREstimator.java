package rosita.linkage.tools.ir;

import java.util.ArrayList;

import rosita.linkage.tools.ImputationRule;
import cdc.components.AbstractJoinCondition;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataRow;
import cdc.impl.distance.EqualFieldsDistance;
import cdc.utils.LogSink;
import cdc.utils.PrintUtils;
import cdc.utils.Props;

public class IREstimator {
	
	public static final double DEFAULT_EPSILON = Props.getDouble("em-epsilon");
	public static final double DEFAULT_M_i = Props.getDouble("em-mi");
	public static final double DEFAULT_U_i = Props.getDouble("em-ui");
	
	private ArrayList<ImputationRule> imputationRuleSet;
	
	public static class EMIteration {
		public double[] m;
		public double[] u;
		public double p;
	}
	
	public static boolean cancel = false;
	
	static LogSink sink = null;
	
	private AbstractJoinCondition condition;
	
	public void setLogSink(LogSink sink) {
		IREstimator.sink = sink;
	}
	
	public void log(String line) {
		if (sink != null) {
			sink.log(line);
		}
	} 
	
	public ArrayList<ImputationRule> runIRMethodBlocking(DataRow[][] rowsA, DataRow[][] rowsB, AbstractJoinCondition cond, double p, ArrayList<ImputationRule> ruleset) {
		System.out.println("Size: " + rowsA.length + " and " + rowsB.length);
		this.condition = cond;
		this.imputationRuleSet = ruleset;
		for (int n = 0; n < rowsA.length; n++) {
			DataRow[] r1 = rowsA[n];
			DataRow[] r2 = rowsB[n];
			for (int i = 0; i < r1.length; i++) {
				if (cancel) return null;
				for (int j = 0; j < r2.length; j++) {
					IREvaluatedCondition eval;
					if ((eval = (IREvaluatedCondition) condition.conditionSatisfied(r1[i], r2[j])).isSatisfied()) {
						recordARule(eval.matchString);
					}
				}
			}
		}
		
		return this.imputationRuleSet;
	}
	
	private void recordARule(String parMatch){
		for (int t=0;t<this.imputationRuleSet.size();t++){
			if(imputationRuleSet.get(t).ImputedValue==-1){
				boolean isQualified = true;
				for(int w=0; w<imputationRuleSet.get(t).FieldsWithData.size();w++){
					if(!parMatch.contains(imputationRuleSet.get(t).FieldsWithData.get(w)+"|")){
						isQualified = false;
					}
				}
				
				if(isQualified==true){
					if(parMatch.contains(imputationRuleSet.get(t).FieldWithMissingData+"|")){
						imputationRuleSet.get(t).intUp++;
					}
					imputationRuleSet.get(t).intDown++;
				}
			}
		}
	}
	
	private boolean checkData(boolean[][] input) {
		System.out.println("Comps: " + input.length);
		if (input.length < 10) {
			sink.log("Not sufficient data provided to EM method.");
			sink.log("Please use different sampling configuration.");
			sink.log("To do so, please hit cancel and restart the process.");
			return false;
		}
		return true;
	}

	public double[] runIRMethodAllToAll(DataRow[] rowsA, DataRow[] rowsB, AbstractJoinCondition cond, double p) {
		this.condition = cond;
		double[] m = new double[cond.getLeftJoinColumns().length];
		double[] u = new double[cond.getLeftJoinColumns().length];
		for (int i = 0; i < u.length; i++) {
			m[i] = DEFAULT_M_i;
			u[i] = DEFAULT_U_i;
		}
		boolean[][] input = new boolean[rowsA.length * rowsB.length][cond.getLeftJoinColumns().length];
		EqualFieldsDistance dst = new EqualFieldsDistance();
		for (int i = 0; i < rowsA.length; i++) {
			for (int j = 0; j < rowsB.length; j++) {
				if (cancel) return null;
				for (int k = 0; k < cond.getLeftJoinColumns().length; k++) {
					DataCell cellA = rowsA[i].getData(cond.getLeftJoinColumns()[k]);
					DataCell cellB = rowsB[j].getData(cond.getRightJoinColumns()[k]);
					input[rowsB.length * i + j][k] = dst.distanceSatisfied(cellA, cellB);
				}
			}
		}
		if (!checkData(input)) {
			return null;
		}
		return null;//runEMMethod(input, m, u, p, DEFAULT_EPSILON);
	}

	public double calculateError(EMIteration old, EMIteration iter) {
		double error = 0;
		for (int i = 0; i < old.m.length; i++) {
			error += Math.abs(old.m[i] - iter.m[i]) + Math.abs(old.u[i] - iter.u[i]);
		}
		return error;
	}

	public EMIteration iterate(boolean[][] input, double[] m, double[] u, double p) {
		
		//E phase for probabilities m_i
		double[] g_m = computeGM(input, m, u, p);
		if (cancel) return null;
		
		//M phase for probabilities m_i
		double[] new_m = computeNewProbabilities(input, g_m, m.length);
		if (cancel) return null;
		
		//E phase for probabilities u_i
		double[] g_u = computeGU(input, m, u, p);
		if (cancel) return null;
		
		//M phase for probabilities u_i
		double[] new_u = computeNewProbabilities(input, g_u, m.length);
		
		double new_p = computeP(g_m);
		
		EMIteration em = new EMIteration();
		em.m = new_m;
		em.u = new_u;
		em.p = new_p;	
		return em;
	}
	
	private double[] computeGM(boolean[][] input, double[] m, double[] u, double p) {
		double[] g = new double[input.length];
		for (int i = 0; i < g.length; i++) {
			double numerator = p;
			double denominator1 = p;
			double denominator2 = 1-p;
			for (int j = 0; j < m.length; j++) {
				numerator *= quickPow(m[j], input[i][j]) * quickPow(1-m[j], !input[i][j]);
				denominator1 *= quickPow(m[j], input[i][j]) * quickPow(1-m[j], !input[i][j]);
				denominator2 *= quickPow(u[j], input[i][j]) * quickPow(1-u[j], !input[i][j]);
			}
			g[i] = numerator / (denominator1 + denominator2);
		}
		return g;
	}
	
	private double[] computeGU(boolean[][] input, double[] m, double[] u, double p) {
		double[] g = new double[input.length];
		for (int i = 0; i < g.length; i++) {
			double numerator = 1-p;
			double denominator1 = p;
			double denominator2 = 1-p;
			for (int j = 0; j < m.length; j++) {
				numerator *= quickPow(u[j], input[i][j]) * quickPow(1-u[j], !input[i][j]);
				denominator1 *= quickPow(m[j], input[i][j]) * quickPow(1-m[j], !input[i][j]);
				denominator2 *= quickPow(u[j], input[i][j]) * quickPow(1-u[j], !input[i][j]);
			}
			g[i] = numerator / (denominator1 + denominator2);
		}
		return g;
	}
	
	private double quickPow(double d, boolean b) {
		return b ? d : 1;
	}

	private double[] computeNewProbabilities(boolean[][] input, double[] g, int k) {
		double[] p = new double[k];
		for (int i = 0; i < p.length; i++) {
			double numerator = 0;
			double denominator = 0;
			for (int j = 0; j < g.length; j++) {
				numerator += g[j] * asInt(input[j][i]);
				denominator += g[j];
			}
			p[i] = numerator / denominator;
		}
		return p;
	}

	private double asInt(boolean b) {
		return b ? 1 : 0;
	}
	
	private double computeP(double[] g) {
		double sum = 0;
		for (int i = 0; i < g.length; i++) {
			sum += g[i];
		}
		return sum / (double)g.length;
	}
	
	public double[] weights(double[] m, double[] u) {
		double[] w = new double[m.length];
		for (int i = 0; i < w.length; i++) {
			if (u[i] == 0) {
				w[i] = 0;
			} else {
				w[i] = Math.log(m[i]/u[i]) / Math.log(2);
			}
		}
		System.out.println("Weights: " + PrintUtils.printArray(w));
		return w;
	}
	
	public ArrayList<ImputationRule> combineRules(ArrayList<ImputationRule> parRules) {
		for(int i=0; i<parRules.size();i++){
			for(int j=0; j<this.imputationRuleSet.size();j++){
				if(IRGenerator.compareImputationRules(parRules.get(i), this.imputationRuleSet.get(j))){
					this.imputationRuleSet.get(j).intUp+=parRules.get(i).intUp;
					this.imputationRuleSet.get(j).intDown+=parRules.get(i).intDown;
				}
			}
		}
		return this.imputationRuleSet;
	}
	
	public static void main(String[] args) {
		
	}

	
//	private static void summary(EMIteration iter) {
//		System.out.println("M: " + PrintUtils.printArray(iter.m));
//		System.out.println("U: " + PrintUtils.printArray(iter.u));
//		System.out.println("P: " + iter.p);
//	}
}