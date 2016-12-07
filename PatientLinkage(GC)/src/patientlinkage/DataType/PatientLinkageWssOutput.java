/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.DataType;

import flexsc.CompEnv;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkageWssOutput<T> {
    T[][] a;
    T[][][] b;
    public PatientLinkageWssOutput(CompEnv<T> e, int rows, int cols) {
        a = e.newTArray(rows, cols);
        b = e.newTArray(rows, cols, 0);
    }

    public PatientLinkageWssOutput(T[][] a, T[][][] b) {
        this.a = a;
        this.b = b;
    }

    public void setA(T[][] a) {
        this.a = a;
    }

    public void setB(T[][][] b) {
        this.b = b;
    }

    public T[][] getA() {
        return a;
    }

    public T[][][] getB() {
        return b;
    }
 
}
