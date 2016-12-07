/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.Util;

import flexsc.CompPool;
import flexsc.Mode;
import gc.GCSignal;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import patientlinkage.DataType.Helper;
import patientlinkage.DataType.PatientLinkage;
import patientlinkage.GarbledCircuit.PatientLinkageGadget;
import static patientlinkage.GarbledCircuit.PatientLinkageGadget.getIntArrayFromStrs;
import static patientlinkage.Util.Util.fromInt;
import patientlinkage.parties.Env;
import patientlinkage.parties.EnvWss;
import patientlinkage.parties.Gen;
import patientlinkage.parties.GenWss;

/**
 *
 * @author cf
 */
public class Main {

    /**
     * starting linkage algorithm
     * @param args the passing parameters
     */
    public static void startLinkage(String[] args) {
        String file_config = null;
        String file_data = null;

        String party = "nobody";
        int ptl_encoding_method = 0;//0 for ascii, 1 for codebook1
        String addr = null;
        int port = -1;
        int threshold = 0;
        int threads = 1;
        int records = 0;
        int filter_hash_bits = 24;
        boolean filter = true;
        boolean step2_usingmask = false;
        int filter_circuit_type = 0; //0 for hash, 1 for huffman
        int data_len = 16;
        String results_save_path = null;

        ArrayList<int[]> prop_array = new ArrayList<>();
        ArrayList<Integer> ws = new ArrayList<>();
        ArrayList<PatientLinkage> res = null;

        boolean[][][] data_bin;
        
        ArrayList<String> PartyA_IDs;
        ArrayList<String> PartyB_IDs;
        
        int potential_linkage_num;
        double t_p = 0, t_a = 0;

        if (args.length < 1) {
            usagemain();
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) != '-') {
                usagemain();
                return;
            }
            try {
                switch (args[i].replaceFirst("-", "")) {
                    case "config":
                        file_config = args[++i];
                        break;
                    case "data":
                        file_data = args[++i];
                        break;
                    case "help":
                        usagemain();
                        break;
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("please input the configure file or data file!");
            } catch (IllegalArgumentException e) {
                System.out.println(args[i] + " is illegal input");
            }
        }

        String[] tmp = null;

        try (FileReader fid_config = new FileReader(file_config); BufferedReader br_config = new BufferedReader(fid_config)) {
            String line;
            while ((line = br_config.readLine()) != null) {
                String[] strs1 = line.split("\\|");
                if (strs1.length < 1) {
                    continue;
                }

                String str = strs1[0].trim();

                if (str.equals("") || !str.contains(":")) {
                    continue;
                }

                String[] strs2 = str.split(":");

                switch (strs2[0].trim().toLowerCase()) {
                    case "party":
                        party = strs2[1].trim();
                        break;
                    case "address":
                        addr = strs2[1].trim();
                        break;
                    case "port":
                        port = Integer.parseInt(strs2[1].trim());
                        break;
                    case "threshold":
                        threshold = Integer.parseInt(strs2[1].trim());
                        break;
                    case "threads":
                        threads = Integer.parseInt(strs2[1].trim());
                        break;
                    case "filter":
                        filter = Integer.parseInt(strs2[1].trim()) == 1;
                        break;
                    case "step_two_using_mask":
                        step2_usingmask = Integer.parseInt(strs2[1].trim()) == 1;
                        break;
                    case "filter hash type":
                        filter_circuit_type = Integer.parseInt(strs2[1].trim());
                        break;
                    case "filter hash bits":
                        filter_hash_bits = Integer.parseInt(strs2[1].trim());
                        break;
                    case "records # of the opposite party":
                        records = Integer.parseInt(strs2[1].trim());
                        break;
                    case "results save path":
                        results_save_path = strs2[1].trim();
                        break;
                    case "rule":
                        tmp = strs2[1].trim().split("->");
                        prop_array.add(getIntArrayFromStrs(tmp[0].trim()));
                        
                        int ws_tmp = Integer.parseInt(tmp[1].trim());
                        if(ws_tmp > 0 ){
                            filter = false;
                        }
                        ws.add(ws_tmp);
                        break;
                    case "patient linkage encoding":
                        switch(strs2[1].trim().toLowerCase()){
                            case "ascii":
                                ptl_encoding_method = 0;
                                break;
                            case "codebook1":
                                ptl_encoding_method = 1;
                                break;
                            default:
                                System.out.println("Does not support " + strs2[1].trim() + " for patient linkage algorithm coding.");
                                throw new AssertionError();
                        }
                        break;
                    default:
                        System.out.println("no property" + strs2[0].trim() + ", please check the configure file!");
                        throw new AssertionError();
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        }

        int[][] array_int1 = new int[prop_array.size()][];
        for (int i = 0; i < array_int1.length; i++) {
            array_int1[i] = prop_array.get(i);
        }
        
        Helper help1 = null;
        
        switch (ptl_encoding_method) {
            case 0:
                help1 = Util.readAndEncodeByASCIIWithProps(file_data, array_int1);
                break;
            case 1:
                Util.initialzingCodebook1();
                help1 = Util.readAndEncodeByCodebook1WithProps(file_data, array_int1);
                break;
        }
        
        if(filter_hash_bits > 0){
            data_bin = Util.readAndEncodeFilterCircuit(file_data, array_int1, filter_hash_bits, filter_circuit_type);
        }else{
            data_bin = help1.data_bin;
        }
        potential_linkage_num = data_bin.length * records;

        boolean[][] ws_bin = new boolean[ws.size()][];
        for (int i = 0; i < ws_bin.length; i++) {
            ws_bin[i] = fromInt(ws.get(i), data_len);
        }
        boolean[] threshold_bin = fromInt(threshold, data_len);
        CompPool.MaxNumberTask = threads;

        switch (party) {
            case "generator":
                PartyA_IDs = help1.IDs;
                if(filter){
                    System.out.println("start filtering linkage ...");
                    long t0 = System.currentTimeMillis();
                    //boolean[][][] f_data_bin = Util.readAndEncodeFilterCircuit(file_data, array_int1, filter_hash_bits, filter_circuit_type);
                    Gen<GCSignal> gen = new Gen<>(port, Mode.REAL, threads, data_bin, records, step2_usingmask, PartyA_IDs);
                    gen.implement();
                    long t1 = System.currentTimeMillis() - t0;
                    t_p = t1 / 1e3;
                    System.out.println("The running time of filtering is " + t_p + " seconds.");
                    potential_linkage_num = gen.getNumOfMatched();
                    System.out.println("Potential linkage number: " + potential_linkage_num);
                    
                    res = gen.getRes();
                    PartyB_IDs = gen.getPartyB_IDs();
                    t_a = t_p;
                }else{
                    System.out.println("start patientlinkage algorithm ...");
                    long t0 = System.currentTimeMillis();
                    GenWss<GCSignal> gen = new GenWss<>(port, Mode.REAL, threads, data_bin, ws_bin, threshold_bin, records, PartyA_IDs, null);
                    gen.implement();
                    res = gen.getLinkage();
                    long t1 = System.currentTimeMillis() - t0;
                    t_a = t1 / 1e3;
                    System.out.println("The running time of patientlinkage algorithm is " + t_a + " seconds.");
                    PartyB_IDs = gen.getPartyB_IDs();
                }
                break;
            case "evaluator":
                PartyB_IDs = help1.IDs;
                if(filter){ 
                    System.out.println("start patientlinkage algorithm ...");
                    long t0 = System.currentTimeMillis();
                    //boolean[][][] f_data_bin = Util.readAndEncodeFilterCircuit(file_data, array_int1, filter_hash_bits, filter_circuit_type);
                    Env<GCSignal> eva = new Env<>(addr, port, Mode.REAL, threads, data_bin, records, step2_usingmask, PartyB_IDs);
                    eva.implement();
                    long t1 = System.currentTimeMillis() - t0;
                    t_p = t1 / 1e3;
                    System.out.println("The running time of patientlinkage algorithm is " + t_p + " seconds.");
                    potential_linkage_num = eva.getNumOfMatched();
                    System.out.println("Potential linkage number: " + potential_linkage_num);
                    
                    res = eva.getRes();
                    PartyA_IDs = eva.getPartyA_IDs();
                    t_a = t_p;
                }else{
                    System.out.println("start patientlinkage algorithm ...");
                    long t0 = System.currentTimeMillis();
                    EnvWss<GCSignal> eva = new EnvWss<>(addr, port, Mode.REAL, threads, data_bin, ws_bin, threshold_bin, records, PartyB_IDs, null);
                    eva.implement();
                    res = eva.getLinkage();
                    long t1 = System.currentTimeMillis() - t0;
                    t_a = t1 / 1e3;
                    System.out.println("The running time of patientlinkage algorithm is " + t_a + " seconds!");
                    PartyA_IDs = eva.getPartyA_IDs();
                }
                break;
            default:
                throw new AssertionError();
        }
        
        String str="";
        
        if (res != null) {
            str += "----------------------------------\n";
            for(int m = 0; m < help1.rules.length; m++){
                if (filter) {
                    str += String.format("Rule %d is %s.\n", m + 1, help1.rules[m]);
                } else {
                    str += String.format("Rule %d is %s, and the weight is %d.\n", m + 1, help1.rules[m], ws.get(m));
                    str += String.format("\nThe threshold is %d.\n", threshold);
                }
            }
            
            str += "----------------------------------\n";
            if (filter) {
                str += "linkage \n";
            } else {
                str += "linkage " + "\t\t\tscore\n";
            }
            str += "ID A(index)  ID B(index)\n\n";
            for (int n = 0; n < res.size(); n++) {
                int[] link0 = res.get(n).getLinkage();
                if (filter) {
                    str += String.format("%s(%d) <--> %s(%d)\n", PartyA_IDs.get(link0[0]), link0[0], PartyB_IDs.get(link0[1]), link0[1]);
                } else {
                    str += String.format("%s(%d) <--> %s(%d) \t\t%d\n", PartyA_IDs.get(link0[0]), link0[0], PartyB_IDs.get(link0[1]), link0[1], (int) res.get(n).getScore());
                }
            }
            str += String.format("The number of final matches records: %d.\n", res.size());
            str += "-----------------------------------\n";
        }
        System.out.println(str);
        
        if(results_save_path != null){
            try (FileWriter writer = new FileWriter(results_save_path)) {
                writer.write(str);
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("The running time of patient linkage circuit is " + t_a + " seconds!");
    }

    public static void usagemain() {
        String help_str
                = ""
                + String.format("     -config     <path>      : input configure file path\n")
                + String.format("     -data       <path>      : input data file path\n")
                + String.format("     -help                   : show help");
        System.out.println(help_str);
    }
    
    public static void simulation(){
        String[] args0 = {"-config", "./configs/config_gen_1K.txt", "-data", "./data/Source14k_a_1K.csv"};
        String[] args1 = {"-config", "./configs/config_eva_1K.txt", "-data", "./data/Source14k_b_1K.csv"};

        Thread t_gen = new Thread(() -> {
            startLinkage(args0);
        });
        Thread t_eva = new Thread(() -> {
            startLinkage(args1);
        });



        long t0 = System.currentTimeMillis();
        try {
            t_gen.start();
            t_eva.start();
            t_gen.join();
            t_eva.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        long t1 = System.currentTimeMillis() - t0;

        System.out.println("The total running time is " + t1 / 1e3 + " seconds!");
    }

    public static void main(String[] args){
        
        long t0 = System.currentTimeMillis();
        if ("sim".equals(args[0])) {
            simulation();
        } else {
            startLinkage(args);
        }
        long t1 = System.currentTimeMillis() - t0;
        System.out.println("The total running time is " + t1 / 1e3 + " seconds!");
    }
}
