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
public class PatientLinkage4GadgetInputs<T> {

    public static int all_progresses = 0;
    private static int progress = 0;
    private static StringBuilder RES = new StringBuilder();

    public static String progress(int pct) {
        PatientLinkage4GadgetInputs.RES.delete(0, PatientLinkage4GadgetInputs.RES.length());
        int numPounds = pct/10;
        for (int i = 0; i <= numPounds; i++) {
            PatientLinkage4GadgetInputs.RES.append('#');
        }
        while (PatientLinkage4GadgetInputs.RES.length() <= 10) {
            PatientLinkage4GadgetInputs.RES.append(' ');
        }
        return PatientLinkage4GadgetInputs.RES.toString();
    }
    
    private T[][][] Inputs;
    int th_ID;
    
    public static void resetBar(){
        all_progresses = 0;
        progress = 0;
        RES = new StringBuilder();
    }

    public PatientLinkage4GadgetInputs(T[][][] Inputs) {
        this.Inputs = Inputs;
    }

    public PatientLinkage4GadgetInputs(boolean[][][] Inputs, CompEnv<T> gen, String role, int th_ID) {
        this.th_ID = th_ID;
        this.Inputs = gen.newTArray(Inputs.length, Inputs[0].length, 0);
        try {
            switch (role) {
                case "Alice":
                    for (int i = 0; i < Inputs.length; i++) {
                        if (progress % 100 == 0) {
                            double tmp = (progress) * 100.0 / all_progresses;
//                            System.out.println("tmp:" + tmp + "; progress:" + progress + "; all " + all_progresses);
                            System.out.print(String.format("[%s]%.2f%%\r", progress((int) tmp), tmp));
                        }
                        progress++;
                        for (int j = 0; j < Inputs[i].length; j++) {

                            this.Inputs[i][j] = gen.inputOfAlice(Inputs[i][j]);

                        }
                    }
                    break;
                case "Bob":
                    for (int i = 0; i < Inputs.length; i++) {
                        if (progress % 100 == 0) {
                            double tmp =  ((float) progress) * 100 / all_progresses;
//                            System.out.println("tmp:" + tmp + "; progress:" + progress + "; all " + all_progresses);
                            System.out.print(String.format("[%s]%.2f%%\r", progress((int)tmp), tmp));
                        }
                        progress++;
                        for (int j = 0; j < Inputs[i].length; j++) {

                            this.Inputs[i][j] = gen.inputOfBob(Inputs[i][j]);
                        }
                    }
                    break;
                default:
                    System.exit(1);
            }
        } catch (Exception ex) {
            Logger.getLogger(PatientLinkage4GadgetInputs.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public T[][][] getInputs() {
        return Inputs;
    }
    

}
