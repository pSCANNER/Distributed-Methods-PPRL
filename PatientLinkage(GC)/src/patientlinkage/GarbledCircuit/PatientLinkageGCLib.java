/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.GarbledCircuit;

import flexsc.CompEnv;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkageGCLib<T> extends circuits.IntegerLib<T> {

    static final int S = 0;
    static final int COUT = 1;

    public PatientLinkageGCLib(CompEnv<T> e) {
        super(e);
    }

    public T[] getWs(T[] a, T[] b, T[] w) throws Exception {
        T[] ret = env.newTArray(w.length);
        T flag = eq(a, b);
        
        for (int i = 0; i < ret.length; i++) {
            ret[i] = and(w[i], flag);
        }
        return ret;
    }

    public T[] matchRcvsWs(T[][] a, T[][] b, T[][] w) throws Exception {
        T[] ret = getWs(a[0], b[0], w[0]);
        for (int i = 1; i < a.length; i++) {
            T[] tmp = getWs(a[i], b[i], w[i]);
            ret = add(ret, tmp);
        }
        return ret;
    }
}
