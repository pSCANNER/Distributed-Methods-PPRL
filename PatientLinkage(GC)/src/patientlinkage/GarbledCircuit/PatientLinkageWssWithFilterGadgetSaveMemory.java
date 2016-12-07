/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.GarbledCircuit;

import patientlinkage.DataType.PatientLinkage4GadgetWsInputs;
import flexsc.CompEnv;
import flexsc.Gadget;
import patientlinkage.DataType.PatientLinkageWssWithFilterOutput;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkageWssWithFilterGadgetSaveMemory<T> extends Gadget<T> {

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

        int index = 0;
        for (int m = 0; m < rows; m++) {
            for (int n = 0; n < cols; n++) {
                if (mask[m][n]) {
                    if (progress % 10 == 0 && all_progresses > 0) {
                        synchronized (RES) {
                            double tmp = (progress) * 100.0 / all_progresses;
                            System.out.print(String.format("[%s]%.2f%%\r", progress((int) tmp), tmp));
                        }
                    }
                    progress++;
                    index++;
                    ret_b[m][n] = pt_lib.matchRcvsWs(a[m], b[n], ws);
                    ret_a[m][n] = pt_lib.compare(ret_b[m][n], threshold);
                }
            }
        }

        T[] ret_a1 = e.newTArray(index);
        T[][] ret_b1 = e.newTArray(index, 0);
        index = 0;

        for (int m = 0; m < rows; m++) {
            for (int n = 0; n < cols; n++) {
                if (mask[m][n]) {
                    ret_a1[index++] = ret_a[m][n];
                    ret_b1[index++] = ret_b[m][n];
                }
            }
        }

        return new PatientLinkageWssWithFilterOutput<>(ret_a1, ret_b1);

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
