/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.DataType;

/**
 *
 * @author cf
 */
public class PatientLinkage implements java.io.Serializable{
    int i;
    int j;
    float score;

    public PatientLinkage(int i, int j) {
        this.i = i;
        this.j = j;
        this.score = 0;
    }

    public PatientLinkage(int i, int j, float score) {
        this.i = i;
        this.j = j;
        this.score = score;
    }

    public void setScore(float score) {
        this.score = score;
    }
   
    
    public void setLinkage(int i, int j){
        this.i = i;
        this.j = j;
    }
    
    public int[] getLinkage(){
        return new int[]{i, j};
    }
    
    public float getScore(){
        return this.score;
    }
    
    public int getI(){
        return this.i;
    }
    
    public int getJ(){
        return this.j;
    }
}
