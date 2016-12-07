/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.Util;

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.language.Soundex;
import static patientlinkage.GarbledCircuit.PatientLinkageGadget.getIntArrayFromStrs;
import static patientlinkage.Util.Util.resizeString;

/**
 *
 * @author cf
 */
public class PatientLinkageHash {
    
    public static ArrayList<byte[][]> readAndEncodeWithProps(String FileName, int[][] lens){
        ArrayList<byte[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        Soundex sdx = new Soundex();
        String res = "";

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            
            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {
                String[] coms_strs = new String[lens.length];
                byte[][] hash_bytes = new byte[lens.length][];
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < properties_num; i++) {
                    String temp = strs[i].replace("-", "").toLowerCase();
                    for (int j = 0; j < coms_strs.length; j++) {
                        if (lens[j][i] > 65536) {
                            coms_strs[j] += sdx.soundex(temp);
                        } else {
                            coms_strs[j] += resizeString(temp, lens[j][i]);
                        }
                    }
                }
                
                for(int k = 0; k < coms_strs.length; k++){
                    hash_bytes[k] = digest.digest(coms_strs[k].getBytes(StandardCharsets.UTF_8));
                }
                
                retArrList.add(hash_bytes);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageHash.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(PatientLinkageHash.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return retArrList;
        
    }
    
    public static void usagemain() {
        String help_str
                = ""
                + String.format("     -config     <path>      : input configure file path\n")
                + String.format("     -data       <path>      : input data file path\n")
                + String.format("     -help                   : show help");
        System.out.println(help_str);
    }
    
    public static boolean compBytes(byte[] arr1, byte[] arr2){
        if(arr1.length != arr2.length){
            return false;
        }
        
        for(int k = 0; k < arr1.length; k++){
            if(arr1[k] != arr2[k]){
                return false;
            }
        }
        
        return true;
    }
        
    public static void startLinkage(String[] args, String[] arg1) {
        String file_config = null;
        String file_data1 = null;
        String file_data2 = null;

        ArrayList<int[]> prop_array = new ArrayList<>();

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
                        file_data1 = args[++i];
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
        
        for (int i = 0; i < arg1.length; i++) {
            if (arg1[i].charAt(0) != '-') {
                usagemain();
                return;
            }
            try {
                switch (arg1[i].replaceFirst("-", "")) {
                    case "config":
                        file_config = arg1[++i];
                        break;
                    case "data":
                        file_data2 = arg1[++i];
                        break;
                    case "help":
                        usagemain();
                        break;
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("please input the configure file or data file!");
            } catch (IllegalArgumentException e) {
                System.out.println(arg1[i] + " is illegal input");
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

                switch (strs2[0].trim()) {
                    case "com":
                        tmp = strs2[1].trim().split("->");
                        prop_array.add(getIntArrayFromStrs(tmp[0].trim()));
                        break;
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageHash.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageHash.class.getName()).log(Level.SEVERE, null, ex);
        }

        int[][] array_int1 = new int[prop_array.size()][];
        for (int i = 0; i < array_int1.length; i++) {
            array_int1[i] = prop_array.get(i);
//            System.out.println(Arrays.toString(array_int1[i]));
        }
        
        ArrayList<byte[][]> arr_list1 = readAndEncodeWithProps(file_data1, array_int1);
        ArrayList<byte[][]> arr_list2 = readAndEncodeWithProps(file_data2, array_int1);
       
        
        int size1 = arr_list1.size();
        int size2 = arr_list2.size();
        
        int num_of_matched = 0;
        
        for(int m = 0; m < size1; m++){
            byte[][] byte1 = arr_list1.get(m);
            for(int n = 0; n < size2; n++){
                boolean is_matched = false;
                byte[][] byte2 = arr_list2.get(n);
                for(int k = 0; k < byte1.length; k++){
                    is_matched |= compBytes(byte1[k], byte2[k]);
                }
                
                if(is_matched){
                    num_of_matched++;
                }
            }
        }
        
        System.out.println("The number of matched results: " + num_of_matched++);
    }
    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        String[] args0 = {"-config", "./configs/config_gen_1K.txt", "-data", "./data/Source14k_a_1K.csv"};
        String[] args1 = {"-config", "./configs/config_eva_1K.txt", "-data", "./data/Source14k_b_1K.csv"};
        startLinkage(args0, args1);
        t1 = System.currentTimeMillis() - t1;
        
        System.out.println("The running time is " + t1);
    }
    
}
