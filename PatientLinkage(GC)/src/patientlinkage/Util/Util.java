/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.Util;

import patientlinkage.GarbledCircuit.PatientLinkageGadget;
import com.opencsv.CSVReader;
import flexsc.CompEnv;
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
import org.apache.commons.lang.StringUtils;
import patientlinkage.DataType.Helper;
import patientlinkage.DataType.PatientLinkage;
import java.util.HashMap;
import static java.util.Arrays.copyOf;

/**
 *
 * @author cf
 */
public class Util {

    public static final int BYTE_BITS = 8;
    
    public static HashMap<Character, boolean[]> codebook;
    
    public static void initialzingCodebook1(){
        codebook = new HashMap(40);
        
        for(char ch = 'a'; ch <= 'z'; ch++){
            codebook.put(ch, fromInt(ch - 'a', 6));
        }
        
        for (char ch = '0'; ch <= '9'; ch++) {
            codebook.put(ch, fromInt(ch - '0' + 'z' - 'a' + 1, 6));
        }
        
        codebook.put(' ', fromInt('9' - '0' + 'z' - 'a' + 2, 6));
    }
    
    public static boolean[] encodeStrByCodebook(String str, HashMap<Character, boolean[]> codebook, int len){
        char[] arr_ch = str.toCharArray();
        boolean[] ret_barr = new boolean[arr_ch.length * len];
        int index = 0;
        for(char ch:arr_ch){
            System.arraycopy(codebook.get(ch), 0, ret_barr, index * len, len);
            index++;
        }
        
        return ret_barr;
    }
    
    public static void main(String[] args) {
        initialzingCodebook1();
        
        String str = "abc0";
        boolean[] b_arr = encodeStrByCodebook(str, codebook, 6);
        
        System.out.println(Arrays.toString(b_arr));
    }
   

    public static boolean[][][] generateDummyArray(boolean[][][] src) {
        boolean[][][] retArr = new boolean[src.length][][];

        for (int i = 0; i < src.length; i++) {
            retArr[i] = new boolean[src[i].length][];
            for (int j = 0; j < src[i].length; j++) {
                retArr[i][j] = new boolean[src[i][j].length];
            }
        }
        return retArr;
    }

    public static boolean[][][] generateDummyArray(boolean[][][] src, int len) {
        boolean[][][] retArr = new boolean[len][][];
        int width = src[0].length;

        for (int i = 0; i < retArr.length; i++) {
            retArr[i] = new boolean[width][];
            for (int j = 0; j < width; j++) {
                retArr[i][j] = new boolean[src[0][j].length];
            }
        }
        return retArr;
    }

    public static int[][] linspace(int pt0, int pt1, int num_of_intervals) {
        assert num_of_intervals > 0 : "math1.linspace: num of intervals > 0";
        int[] ret = new int[num_of_intervals + 1];

        int[][] ret1 = new int[num_of_intervals][2];

        ret[0] = pt0;
        ret[num_of_intervals] = pt1;

        ret1[0][0] = pt0;
        ret1[num_of_intervals - 1][1] = pt1;

        int int_len = (pt1 - pt0) / num_of_intervals;

        for (int i = 1; i < num_of_intervals; i++) {
            ret[i] = ret[i - 1] + int_len;
            ret1[i - 1][1] = ret[i];
            ret1[i][0] = ret[i];
        }

        return ret1;
    }
    
    public static int getPtLnkCnts(int[][] ranges1, int opp_num){
        int ptLnkCnts = 0;
        
        for (int[] ranges11 : ranges1) {
            ptLnkCnts += (ranges11[1] - ranges11[0]) * opp_num;
        }
        
        return ptLnkCnts;
    }

    public static <T> T[][] unifyArray(Object[] input, CompEnv<T> eva, int len) {
        T[][] ret = eva.newTArray(len, 0);
        int index = 0;

        for (Object input1 : input) {
            T[][] tmp = (T[][]) input1;
            for (T[] tmp1 : tmp) {
                ret[index++] = tmp1;
            }
        }
        return ret;
    }

    public static <T> T[][][] unifyArray1(Object[] input, CompEnv<T> eva, int len) {

        T[][][] ret = eva.newTArray(len, 0, 0);
        int index = 0;

        for (Object input1 : input) {
            T[][][] tmp = (T[][][]) input1;
            for (T[][] tmp1 : tmp) {
                ret[index++] = tmp1;
            }
        }
        return ret;
    }
    
    public static <T> T[] unifyArrayWithF(Object[] input, CompEnv<T> eva, int len) {
        T[] ret = eva.newTArray(len);
        int index = 0;
        
        for (Object input1 : input) {
            T[] tmp = (T[]) input1;
            for (T tmp1 : tmp) {
                ret[index++] = tmp1;
            }
        }

        return ret;
    }
    
    public static boolean[][][] extractArray(boolean[][][] arr1, ArrayList<PatientLinkage> ptl_arr, String role) {
        boolean[][][] res = new boolean[ptl_arr.size()][][];
        switch (role) {
            case "generator":
                int ind;
                for (int n = 0; n < ptl_arr.size(); n++) {
                    ind = ptl_arr.get(n).getI();
                    res[n] = arr1[ind];
                }
                break;
            case "evaluator":
                for (int n = 0; n < ptl_arr.size(); n++) {
                    ind = ptl_arr.get(n).getJ();
                    res[n] = arr1[ind];
                }
                break;
        }

        return res;
    }

    public static boolean[][][] encodeCobinationAsJAMIA4Criteria(String[][] data1, int[][] properties_bytes) {
        //12, 11, 9, 8
        assert data1[0].length == properties_bytes[0].length;
        boolean[][][] ret = new boolean[data1.length][properties_bytes.length][];

        for (int i = 0; i < data1.length; i++) {
            for (int j = 0; j < properties_bytes.length; j++) {
                String temp = "";
                for (int k = 0; k < properties_bytes[j].length; k++) {
                    temp += resizeString(data1[i][k], properties_bytes[j][k]);
                }
                ret[i][j] = bytes2boolean(temp.getBytes(StandardCharsets.US_ASCII));
            }
        }

        return ret;
    }

    public static String resizeString(String str, int len) {
        if (str.length() < len) {
            return StringUtils.rightPad(str, len);
        } else if (str.length() > len) {
            return StringUtils.substring(str, 0, len);
        } else {
            return str;
        }
    }

    public static boolean[] bytes2boolean(byte[] vals) {
        boolean[] ret = new boolean[BYTE_BITS * vals.length];

        for (int i = 0; i < vals.length; i++) {
            System.arraycopy(fromByte(vals[i]), 0, ret, i * BYTE_BITS, BYTE_BITS);
        }

        return ret;
    }

    public static boolean[] fromByte(byte value) {

        boolean[] res = new boolean[BYTE_BITS];
        for (int i = 0; i < BYTE_BITS; i++) {
            res[i] = (((value >> i) & 1) != 0);
        }
        return res;
    }

    public static boolean[] fromInt(int value, int width) {
        boolean[] res = new boolean[width];
        for (int i = 0; i < width; i++) {
            res[i] = (((value >> i) & 1) != 0);
        }

        return res;
    }

    public static int toInt(boolean[] value) {
        int res = 0;
        for (int i = 0; i < value.length; i++) {
            res = (value[i]) ? (res | (1 << i)) : res;
        }

        return res;
    }

    public static String[][] readAndProcessCSV(String FileName, int records_num) {
        String[][] data1 = null;
        int properties_num = 6;
        Soundex sdx = new Soundex();

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] nextLine;
            data1 = new String[records_num][properties_num];
            reader.readNext();
            int ind = 0;
            while ((nextLine = reader.readNext()) != null && ind < records_num) {
                data1[ind][0] = nextLine[1].toLowerCase();
                data1[ind][1] = nextLine[2].toLowerCase();
                data1[ind][2] = sdx.encode(nextLine[1]).toLowerCase();
                data1[ind][3] = sdx.encode(nextLine[2]).toLowerCase();
                data1[ind][4] = nextLine[6].replaceAll("-", "");
                data1[ind][5] = nextLine[11].replaceAll("-", "");

                ind++;

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data1;
    }

    public static boolean[][][] readAndEncode(String FileName, int[][] lens) {
        ArrayList<boolean[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        Soundex sdx = new Soundex();

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {

                String[] coms_strs = new String[lens.length];
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
                boolean[][] bool_arr = new boolean[coms_strs.length][];
                for (int j = 0; j < coms_strs.length; j++) {
                    bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
                }
                retArrList.add(bool_arr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        }

        boolean[][][] bool_ret = new boolean[retArrList.size()][][];
        for (int i = 0; i < bool_ret.length; i++) {
            bool_ret[i] = retArrList.get(i);
        }

        return bool_ret;
    }
    
    public static boolean[][][] readAndEncodeFilterCircuit(String FileName, int[][] lens, int len, int algorithm){
        boolean[][][] ret_arr = null;
        switch(algorithm){
            case 0:
                ret_arr = readAndEncodeByHash(FileName, lens, len);
                break;
            case 1:
                ret_arr = readAndEncodeByHuffmanAndHash(FileName, lens, len);
                break;
            default:
                throw new IllegalStateException("Illegal algorithm for encoding filter circuit");
        }
        
        return ret_arr;
    }

    public static boolean[][][] readAndEncodeByHash(String FileName, int[][] lens, int hash_len) {
        ArrayList<boolean[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        Soundex sdx = new Soundex();

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {

                String[] coms_strs = new String[lens.length];
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < properties_num; i++) {
                    String temp = strs[i].replace("-", "").toLowerCase();
                    for (int j = 0; j < coms_strs.length; j++) {
                        if (lens[j][i] > 65536) {
                            coms_strs[j] += sdx.soundex(temp).toLowerCase();
                        } else {
                            coms_strs[j] += resizeString(temp, lens[j][i]);
                        }
                    }
                }
                boolean[][] bool_arr = new boolean[coms_strs.length][];
                for (int j = 0; j < coms_strs.length; j++) {
 //                   bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
                    bool_arr[j] = copyOf(bytes2boolean(digest.digest(coms_strs[j].getBytes(StandardCharsets.UTF_8))), hash_len);
                }
                retArrList.add(bool_arr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        boolean[][][] bool_ret = new boolean[retArrList.size()][][];
        for (int i = 0; i < bool_ret.length; i++) {
            bool_ret[i] = retArrList.get(i);
        }

        return bool_ret;
    }
    
    public static boolean[][][] readAndEncodeByHuffmanAndHash(String FileName, int[][] lens, int len) {
        Huffman.buildCodeTable1();
        Huffman.buildCodeTable2();
        ArrayList<boolean[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        boolean[] mask = new boolean[properties_num];
        for (int[] len1 : lens) {
            for (int n = 0; n < len1.length; n++) {
                if (len1[n] > 0) {
                    mask[n] = true;
                }
            }
        }
        
        int prop_num_mask = 0;
        for(int n = 0; n < mask.length; n++){
            if(mask[n]){
                prop_num_mask++;
            }
        }
        
       
        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {

                String[] coms_strs = new String[prop_num_mask];
                int index = 0;
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < properties_num; i++) {
                    if(mask[i]){
                        String temp = strs[i].replace("-", "").toLowerCase();
                        coms_strs[index++] = temp;
                    }
                }
                boolean[][] bool_arr = new boolean[coms_strs.length][];
                for (int j = 0; j < coms_strs.length; j++) {
                    switch (j) {
                        case 0:
                        case 1:
                            bool_arr[j] = copyOf(Huffman.compress1(coms_strs[j]), len);
                            break;
                        case 2:
                            bool_arr[j] = copyOf(bytes2boolean(digest.digest(coms_strs[j].getBytes(StandardCharsets.UTF_8))), len);
                            break;
                        case 3:
                            bool_arr[j] = copyOf(Huffman.compress2(coms_strs[j].substring(2)), len);
                            break;
                        
                        default:
                            throw new IllegalStateException("Illegal state");
                    }
                }
                retArrList.add(bool_arr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        boolean[][][] bool_ret = new boolean[retArrList.size()][][];
        for (int i = 0; i < bool_ret.length; i++) {
            bool_ret[i] = retArrList.get(i);
        }

        return bool_ret;
    }
    
    public static Helper readAndEncodeByASCIIWithProps(String FileName, int[][] lens) {
        Helper ret = new Helper();
        ArrayList<boolean[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        Soundex sdx = new Soundex();

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] strs;
            ret.pros = reader.readNext();
            ret.updatingrules(lens);
            while ((strs = reader.readNext()) != null) {
                ret.IDs.add(strs[0]);
                String[] coms_strs = new String[lens.length];
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < properties_num; i++) {
                    String temp = strs[i].replace("-", "").toLowerCase();
                    for (int j = 0; j < coms_strs.length; j++) {
                        if (lens[j][i] > (Integer.MAX_VALUE/2)) {
                            coms_strs[j] += sdx.soundex(temp).toLowerCase() + resizeString(temp, Integer.MAX_VALUE - lens[j][i]);
                        } else {
                            coms_strs[j] += resizeString(temp, lens[j][i]);
                        }
                    }
                }
                boolean[][] bool_arr = new boolean[coms_strs.length][];
                for (int j = 0; j < coms_strs.length; j++) {
                    bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
//                    bool_arr[j] = encodeStrByCodebook(coms_strs[j], codebook, 6);
                    
                }
                retArrList.add(bool_arr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        }

        ret.data_bin = new boolean[retArrList.size()][][];
        for (int i = 0; i < ret.data_bin.length; i++) {
            ret.data_bin[i] = retArrList.get(i);
        }

        return ret;
    }
    
    public static Helper readAndEncodeByCodebook1WithProps(String FileName, int[][] lens) {
        Helper ret = new Helper();
        ArrayList<boolean[][]> retArrList = new ArrayList<>();
        int properties_num = lens[0].length;
        Soundex sdx = new Soundex();

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] strs;
            ret.pros = reader.readNext();
            ret.updatingrules(lens);
            while ((strs = reader.readNext()) != null) {
                ret.IDs.add(strs[0]);
                String[] coms_strs = new String[lens.length];
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < properties_num; i++) {
                    String temp = strs[i].replace("-", "").toLowerCase();
                    for (int j = 0; j < coms_strs.length; j++) {
                        if (lens[j][i] > (Integer.MAX_VALUE/2)) {
                            coms_strs[j] += sdx.soundex(temp).toLowerCase() + resizeString(temp, Integer.MAX_VALUE - lens[j][i]);
                        } else {
                            coms_strs[j] += resizeString(temp, lens[j][i]);
                        }
                    }
                }
                boolean[][] bool_arr = new boolean[coms_strs.length][];
                for (int j = 0; j < coms_strs.length; j++) {
//                    bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
                    bool_arr[j] = encodeStrByCodebook(coms_strs[j], codebook, 6);
                    
                }
                retArrList.add(bool_arr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PatientLinkageGadget.class.getName()).log(Level.SEVERE, null, ex);
        }

        ret.data_bin = new boolean[retArrList.size()][][];
        for (int i = 0; i < ret.data_bin.length; i++) {
            ret.data_bin[i] = retArrList.get(i);
        }

        return ret;
    }

    public static void usagemain() {
        String help_str
                = ""
                + String.format("     -config     <path>      : input configure file path\n")
                + String.format("     -data       <path>      : input data file path\n")
                + String.format("     -help                   : show help");
        System.out.println(help_str);
    }


}
