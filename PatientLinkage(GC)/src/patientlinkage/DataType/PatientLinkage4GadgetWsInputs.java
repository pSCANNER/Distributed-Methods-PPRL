/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.DataType;

import flexsc.CompEnv;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkage4GadgetWsInputs<T> extends PatientLinkage4GadgetInputs<T> {

    private T[][] weights;
    private T[] threshold;
    private boolean[][] mask1;

    public PatientLinkage4GadgetWsInputs(boolean[][][] Inputs, boolean[][] weights, boolean[] threshold, CompEnv<T> gen, String role, int th_ID, boolean[][] mask) {
        super(Inputs, gen, role, th_ID);
        this.weights = gen.newTArray(weights.length, 0);
        
        try {
            switch (role) {
                case "Alice":
                    for (int i = 0; i < weights.length; i++) {
                        this.weights[i] = gen.inputOfAlice(weights[i]);   
                    }
                    this.threshold = gen.inputOfAlice(threshold);
                    break;
                case "Bob":
                    for (int i = 0; i < weights.length; i++) {
                        this.weights[i] = gen.inputOfBob(weights[i]);
                    }
                    this.threshold = gen.inputOfBob(threshold);
                    break;
                default:
                    System.exit(1);
            }
        } catch (Exception ex) {
            Logger.getLogger(PatientLinkage4GadgetInputs.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.mask1 = mask;

    }
    
    public T[][] getWs(){
        return this.weights;
    }
    
    public T[] getThreshold(){
        return this.threshold;
    }
    
    public boolean[][] getMask(){
        return this.mask1;
    }
}
