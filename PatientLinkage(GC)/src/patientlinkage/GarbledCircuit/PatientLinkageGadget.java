/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.GarbledCircuit;

import patientlinkage.DataType.PatientLinkage4GadgetInputs;
import circuits.IntegerLib;
import flexsc.CompEnv;
import flexsc.Gadget;
import flexsc.Mode;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import patientlinkage.parties.Env;
import patientlinkage.parties.Gen;
import patientlinkage.Util.Util;

/**
 *
 * @author cf
 * @param <T>
 */
public class PatientLinkageGadget<T> extends Gadget<T> {
    public static int all_progresses = 0;
    private static int progress = 0;
    private static final StringBuilder RES = new StringBuilder();
    
    public static void resetBar() {
        all_progresses = 0;
        progress = 0;
        RES.delete(0, RES.length());
    }
    
    public static String progress(int pct) {
        RES.delete(0, RES.length());
        pct/= 10;
        for (int i = 0; i <= pct; i++) {
            RES.append('#');
        }
        while (RES.length() <= 10) {
            RES.append(' ');
        }
        return RES.toString();
    }

    @Override
    public Object secureCompute(CompEnv<T> e, Object[] o) throws Exception {
        T[][][] a = (T[][][]) ((PatientLinkage4GadgetInputs) o[0]).getInputs();
        T[][][] b = (T[][][]) ((PatientLinkage4GadgetInputs) o[1]).getInputs();

        int rows = a.length;
        int cols = b.length;
        int numComps = a[0].length;

        T[][] ret = e.newTArray(rows, cols);
        IntegerLib<T> int_lib = new IntegerLib<>(e);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (progress % 1000 == 0 && all_progresses > 0) {
                    synchronized(RES){
                    double tmp =  (progress) * 100.0 / all_progresses;
                    System.out.print(String.format("[%s]%.2f%%\r", progress((int)tmp), tmp));
                    }
                }
                progress++;
                ret[i][j] = int_lib.eq(a[i][0], b[j][0]);
                for (int k = 1; k < numComps; k++) {
                    ret[i][j] = int_lib.or(ret[i][j], int_lib.eq(a[i][k], b[j][k]));
                }
            }
        }

        return ret;
    }


//    public static void run_thread(String[] args) {
//        String file_config = null;
//        String file_data = null;
//
//        String party ="nobody";
//        String addr = null;
//        int port = 54321;
//        int numOfTasks = 1;
//        int propnums = 0;
//        ArrayList<int[]> prop_array = new ArrayList<>();
//        
//        boolean[][][] data_bin;
//
//        if (args.length < 1) {
//            usagemain();
//            return;
//        }
//
//        for (int i = 0; i < args.length; i++) {
//            if (args[i].charAt(0) != '-') {
//                usagemain();
//                return;
//            }
//            try {
//                switch (args[i].replaceFirst("-", "")) {
//                    case "config":
//                        file_config = args[++i];
//                        break;
//                    case "data":
//                        file_data = args[++i];
//                        break;
//                    case "help":
//                        usagemain();
//                        break;
//                }
//            } catch (IndexOutOfBoundsException e) {
//                System.out.println("please input the configure file or data file!");
//            } catch (IllegalArgumentException e) {
//                System.out.println(args[i] + " is illegal input");
//            }
//        }
//        
//        String[] tmp = null;
//
//        try (FileReader fid_config = new FileReader(file_config); BufferedReader br_config = new BufferedReader(fid_config)) {
//            String line;
//            while ((line = br_config.readLine()) != null) {
//                String[] strs1 = line.split("\\|");
//                if (strs1.length < 1) {
//                    continue;
//                }
//
//                String str = strs1[0].trim();
//
//                if (str.equals("") || !str.contains(":")) {
//                    continue;
//                }
//
//                String[] strs2 = str.split(":");
//
//                switch (strs2[0].trim()) {
//                    case "party":
//                        party = strs2[1].trim();
//                        break;
//                    case "address":
//                        addr = strs2[1].trim();
//                        break;
//                    case "port":
//                        port = Integer.parseInt(strs2[1].trim());
//                        break;
//                    case "threshold":
//                        //threshold = Integer.parseInt(strs2[1].trim());
//                        break;
//                    case "threads":
//                        numOfTasks = Integer.parseInt(strs2[1].trim());
//                        break;
//                    case "records":
//                        propnums = Integer.parseInt(strs2[1].trim());
//                        break;
//                    case "com":
//                        tmp = strs2[1].trim().split("->");
//                        prop_array.add(getIntArrayFromStrs(tmp[0].trim()));
//                        //weights.add(Integer.parseInt(tmp[1].trim()));
//                        break;
//                    default:
//                        System.out.println("Please check the configure file!");
//                        throw new AssertionError();
//                }
//
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
//        } 
//        
//        int[][] array_int1 = new int[prop_array.size()][];
//        for(int i = 0; i < array_int1.length; i++){
//            array_int1[i] = prop_array.get(i);
//        }
//        
//        data_bin = Util.readAndEncode(file_data, array_int1, 3);
//        
//        switch (party) {
//            case "generator":
//                (new Gen<>(port, Mode.REAL, numOfTasks, data_bin, propnums)).implement();
//                break;
//            case "evaluator":
//                (new Env<>(addr, port, Mode.REAL, numOfTasks, data_bin, propnums)).implement();
//                break;
//            default:
//                throw new AssertionError();
//        }
//    }

//    public static void usagemain() {
//        String help_str
//                = ""
//                + String.format("     -config     <path>      : input configure file path\n")
//                + String.format("     -data       <path>      : input data file path\n")
//                + String.format("     -help                   : show help");
//        System.out.println(help_str);
//    }

    public static int[] getIntArrayFromStrs(String str) {
        String[] strs = str.split("\\s+");

        int[] retArr = new int[strs.length];

        for (int i = 0; i < retArr.length; i++) {
            String tmp = strs[i].trim();
            tmp = tmp.toLowerCase();
            if (tmp.charAt(0) == 's') {
                if (tmp.length() > 1) {
                    retArr[i] = Integer.MAX_VALUE - Integer.parseInt(tmp.substring(1));
                } else {
                    retArr[i] = Integer.MAX_VALUE;
                }
            } else {
                retArr[i] = Integer.parseInt(tmp);
            }
        }

        return retArr;
    }
    
//    public static class testTh implements Runnable{
//        String[] args;
//
//        public testTh(String[] args) {
//            this.args = args;
//        }
//
//        @Override
//        public void run() {
//            run_thread(args);
//        }
//    }

}
