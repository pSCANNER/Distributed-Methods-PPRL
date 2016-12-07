/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.GarbledCircuit;

import patientlinkage.DataType.PatientLinkage4GadgetWsInputs;
import patientlinkage.DataType.PatientLinkageWssOutput;
import flexsc.CompEnv;
import flexsc.Gadget;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkageWssWithFilterSaveMemoryGadget<T> extends Gadget<T> {
    public static int all_progresses = 0;
    private static int progress = 0;
    private static final StringBuilder RES = new StringBuilder();

    public static void resetBar() {
        all_progresses = 0;
        progress = 0;
        RES.delete(0, RES.length());
    }

    @Override
    public Object secureCompute(CompEnv<T> e, Object[] o) throws Exception {
        T[][][] a = (T[][][]) ((PatientLinkage4GadgetWsInputs) o[0]).getInputs();
        T[][][] b = (T[][][]) ((PatientLinkage4GadgetWsInputs) o[1]).getInputs();

        T[][] weights_a = (T[][]) ((PatientLinkage4GadgetWsInputs) o[0]).getWs();
        T[][] weights_b = (T[][]) ((PatientLinkage4GadgetWsInputs) o[1]).getWs();

        T[] threshold_a = (T[]) ((PatientLinkage4GadgetWsInputs) o[0]).getThreshold();
        T[] threshold_b = (T[]) ((PatientLinkage4GadgetWsInputs) o[1]).getThreshold();
        
        boolean[][] mask = ((PatientLinkage4GadgetWsInputs) o[0]).getMask();
        
        int rows = a.length;
        int cols = b.length;

        T[][] ret_a = e.newTArray(rows, cols);
        T[][][] ret_b = e.newTArray(rows, cols, 0);
        
        final T[][] ws = e.newTArray(a[0].length, 0);

        PatientLinkageGCLib<T> pt_lib = new PatientLinkageGCLib<>(e);

        for (int k = 0; k < ws.length; k++) {
            ws[k] = pt_lib.add(weights_a[k], weights_b[k]);
        }
        T[] threshold = pt_lib.add(threshold_a, threshold_b);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                if (mask[i][j]) {
                    if (progress % 100 == 0 && all_progresses > 0) {
                        synchronized (RES) {
                            double tmp = (progress) * 100.0 / all_progresses;
                            System.out.print(String.format("[%s]%.2f%%\r", progress((int) tmp), tmp));
                        }
                    }
                    progress++;

                    ret_b[i][j] = pt_lib.matchRcvsWs(a[i], b[j], ws);
                    ret_a[i][j] = pt_lib.compare(ret_b[i][j], threshold);
                }
            }
        }
        
        return new PatientLinkageWssOutput<>(ret_a, ret_b);
    }
    
    public static String progress(int pct) {
        RES.delete(0, RES.length());
        pct /= 10;
        for (int i = 0; i <= pct; i++) {
            RES.append('#');
        }
        while (RES.length() <= 10) {
            RES.append(' ');
        }
        return RES.toString();
    }
}

