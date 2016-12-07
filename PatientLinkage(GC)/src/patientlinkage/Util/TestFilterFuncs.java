/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientlinkage.Util;

import com.opencsv.CSVReader;
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
import static patientlinkage.Util.Util.bytes2boolean;
import static patientlinkage.Util.Util.resizeString;

/**
 *
 * @author cf
 */
public class TestFilterFuncs {

    public static Soundex sdx = new Soundex();

    public static void main(String[] args) {
        
        String file1 = "/Users/cf/GitHub/patientlinkage/data/Source14k_a_10K.csv";
        String file2 = "/Users/cf/GitHub/patientlinkage/data/Source14k_b_10K.csv";
        int[] lens = new int[]{0, 12, 11, 0, 0, 0, 9, 0, 0, 0, 0, 8};

        String[][] data1 = readCSVFile(file1, lens);
        String[][] data2 = readCSVFile(file2, lens);

        String[][] com_strs1 = combineProps(data1);
        String[][] com_strs2 = combineProps(data2);

        System.out.println("4 properties using 1 byte of SHA-256: " + getPatientLinkageNumByHash_1_byte(data1, data2, 1, 0));
        System.out.println("4 properties using summation of SHA-256: " + getPatientLinkageNumByHash_1_byte(data1, data2, 1, 1));
        System.out.println("4 properties using summation of all bytes: " + getPatientLinkageNumByHash_1_byte(data1, data2, 1, 2));
        System.out.println("4 properties using Pearson 8 bit hash: " + getPatientLinkageNumByHash_1_byte(data1, data2, 1, 3));

        System.out.println("4 combinations using 1 byte of SHA-256: " + getPatientLinkageNumByHash_1_byte(com_strs1, com_strs2, 1, 0));
        System.out.println("4 combinations using summation of SHA-256: " + getPatientLinkageNumByHash_1_byte(com_strs1, com_strs2, 1, 1));
        System.out.println("4 combinations using summation of all bytes: " + getPatientLinkageNumByHash_1_byte(com_strs1, com_strs2, 1, 2));
        System.out.println("4 combinations using Pearson 8 bit hash: " + getPatientLinkageNumByHash_1_byte(com_strs1, com_strs2, 1, 3));
        
        System.out.println("4 properties using summation of Huffman coding: " + getPatientLinkageNumByHuffman(data1, data2));
        System.out.println("4 properties using summation of hashes of Huffman coding: " + getPatientLinkageNumByHuffmanAndHash(data1, data2));
        System.out.println("4 properties using Pearson hashes: " + getPatientLinkageNumByHuffmanAndHashPerson(data1,data2));

    }

    public static String[][] readCSVFile(String FileName, int[] lens) {
        ArrayList<String[]> retArrList = new ArrayList<>();

        int prop_num = 0;
        boolean[] mask = new boolean[lens.length];
        for (int i = 0; i < lens.length; i++) {
            if (lens[i] > 0) {
                prop_num++;
                mask[i] = true;
            }
        }

        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {
                String[] coms_strs = new String[prop_num];
                int index = 0;
                Arrays.fill(coms_strs, "");
                for (int i = 0; i < lens.length; i++) {
                    if (mask[i]) {
//                        String temp = Util.resizeString(strs[i].replace("-", "").toLowerCase(), lens[i]);
                        String temp = strs[i].replace("-", "").toLowerCase();
                        coms_strs[index++] = temp;
                    }
                }

                retArrList.add(coms_strs);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestFilterFuncs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestFilterFuncs.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[][] ret_str = new String[retArrList.size()][];
        for (int i = 0; i < ret_str.length; i++) {
            ret_str[i] = retArrList.get(i);
        }

        return ret_str;
    }

    public static String[][] combineProps(String[][] props) {
        String[][] com_strs = new String[props.length][4];

        for (int n = 0; n < com_strs.length; n++) {
            com_strs[n][0] = props[n][0] + props[n][1] + props[n][3];
            com_strs[n][1] = props[n][2] + props[n][3];
            com_strs[n][2] = props[n][1] + props[n][2];
            com_strs[n][3] = sdx.encode(props[n][0]) + sdx.encode(props[n][1]) + resizeString(props[n][0], 3) + resizeString(props[n][2], 3);
        }

        return com_strs;
    }

    public static byte[][][] toHash_SHA_256(String[][] com_strs, int len) {
        byte[][][] hash_bytes_arr = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            hash_bytes_arr = new byte[com_strs.length][com_strs[0].length][];

            for (int n = 0; n < com_strs.length; n++) {
                for (int k = 0; k < com_strs[n].length; k++) {
                    hash_bytes_arr[n][k] = Arrays.copyOf(digest.digest(com_strs[n][k].getBytes(StandardCharsets.UTF_8)), len);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestFilterFuncs.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hash_bytes_arr;

    }

    public static byte[][][] toHash_SHA_256(String[][] com_strs) {
        byte[][][] hash_bytes_arr = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            hash_bytes_arr = new byte[com_strs.length][com_strs[0].length][1];

            for (int n = 0; n < com_strs.length; n++) {
                for (int k = 0; k < com_strs[n].length; k++) {
                    hash_bytes_arr[n][k][0] = calSum(digest.digest(com_strs[n][k].getBytes(StandardCharsets.UTF_8)));
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestFilterFuncs.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hash_bytes_arr;

    }
    
    public static byte[][][] toPearson8Bit(String[][] com_strs) {
        byte[][][] hash_bytes_arr = new byte[com_strs.length][com_strs[0].length][1];

        for (int n = 0; n < com_strs.length; n++) {
            for (int k = 0; k < com_strs[n].length; k++) {
                hash_bytes_arr[n][k][0] = Pearson8Bit(com_strs[n][k].getBytes(StandardCharsets.UTF_8));
            }
        }

        return hash_bytes_arr;

    }

    public static int getPatientLinkageNumByHash_1_byte(String[][] com_strs1, String[][] com_strs2, int len, int alg) {
        byte[][][] b_com_strs1 = null;
        byte[][][] b_com_strs2 = null;
        switch (alg) {
            case 0:
                b_com_strs1 = toHash_SHA_256(com_strs1, len);
                b_com_strs2 = toHash_SHA_256(com_strs2, len);
                break;
            case 1:
                b_com_strs1 = toHash_SHA_256(com_strs1);
                b_com_strs2 = toHash_SHA_256(com_strs2);
                break;
            case 2:
                b_com_strs1 = getStringHashSum(com_strs1);
                b_com_strs2 = getStringHashSum(com_strs2);
                break;
            case 3:
                b_com_strs1 = toPearson8Bit(com_strs1);
                b_com_strs2 = toPearson8Bit(com_strs2);
                break;
            default:
                throw new IllegalStateException("Illegal state");

        }

        int num = 0;
        boolean flag;
        for (byte[][] com_strs11 : b_com_strs1) {
            for (byte[][] com_strs21 : b_com_strs2) {
                flag = false;
                for (int k = 0; k < com_strs11.length; k++) {
                    flag |= PatientLinkageHash.compBytes(com_strs11[k], com_strs21[k]);
                }
                if (flag) {
                    num++;
                }
            }
        }

        return num;
    }

    public static int getPatientLinkageNumByHuffman(String[][] com_strs1, String[][] com_strs2) {
        int num = 0;

        byte[][] byte_com_strs1 = new byte[com_strs1.length][4];
        byte[][] byte_com_strs2 = new byte[com_strs2.length][4];

        Huffman.buildCodeTable1();
        Huffman.buildCodeTable2();

        for (int n = 0; n < com_strs1.length; n++) {
            byte_com_strs1[n][0] = calSum(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][0].trim().toLowerCase())));
            byte_com_strs1[n][1] = calSum(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][1].trim().toLowerCase())));
            byte_com_strs1[n][2] = calSum(convertBoolean2Bytes(bytes2boolean(com_strs1[n][2].getBytes(StandardCharsets.UTF_8))));
            byte_com_strs1[n][3] = calSum(convertBoolean2Bytes(Huffman.compress2(com_strs1[n][3])));
        }

        for (int n = 0; n < com_strs2.length; n++) {
            byte_com_strs2[n][0] = calSum(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][0].trim().toLowerCase())));
            byte_com_strs2[n][1] = calSum(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][1].trim().toLowerCase())));
            byte_com_strs2[n][2] = calSum(convertBoolean2Bytes(bytes2boolean(com_strs2[n][2].getBytes(StandardCharsets.UTF_8))));
            byte_com_strs2[n][3] = calSum(convertBoolean2Bytes(Huffman.compress2(com_strs2[n][3])));
        }

        boolean flag;
        for (byte[] com_strs11 : byte_com_strs1) {
            for (byte[] com_strs21 : byte_com_strs2) {
                flag = false;
                for (int k = 0; k < com_strs11.length; k++) {
                    flag |= (com_strs11[k] == com_strs21[k]);
                }
                if (flag) {
                    num++;
                }
            }
        }

        return num;
    }

    public static int getPatientLinkageNumByHuffmanAndHash(String[][] com_strs1, String[][] com_strs2) {
        int num = 0;

        byte[][] byte_com_strs1 = new byte[com_strs1.length][4];
        byte[][] byte_com_strs2 = new byte[com_strs2.length][4];

        Huffman.buildCodeTable1();
        Huffman.buildCodeTable2();

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestFilterFuncs.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int n = 0; n < com_strs1.length; n++) {
            byte_com_strs1[n][0] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][0].trim().toLowerCase()))));
            byte_com_strs1[n][1] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][1].trim().toLowerCase()))));
            byte_com_strs1[n][2] = calSum(digest.digest(convertBoolean2Bytes(bytes2boolean(com_strs1[n][2].getBytes(StandardCharsets.UTF_8)))));
            byte_com_strs1[n][3] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress2(com_strs1[n][3]))));
        }

        for (int n = 0; n < com_strs2.length; n++) {
            byte_com_strs2[n][0] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][0].trim().toLowerCase()))));
            byte_com_strs2[n][1] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][1].trim().toLowerCase()))));
            byte_com_strs2[n][2] = calSum(digest.digest(convertBoolean2Bytes(bytes2boolean(com_strs2[n][2].getBytes(StandardCharsets.UTF_8)))));
            byte_com_strs2[n][3] = calSum(digest.digest(convertBoolean2Bytes(Huffman.compress2(com_strs2[n][3]))));
        }

        boolean flag;
        for (byte[] com_strs11 : byte_com_strs1) {
            for (byte[] com_strs21 : byte_com_strs2) {
                flag = false;
                for (int k = 0; k < com_strs11.length; k++) {
                    flag |= (com_strs11[k] == com_strs21[k]);
                }
                if (flag) {
                    num++;
                }
            }
        }

        return num;
    }

    public static int getPatientLinkageNumByHuffmanAndHashPerson(String[][] com_strs1, String[][] com_strs2) {
        int num = 0;

        byte[][] byte_com_strs1 = new byte[com_strs1.length][4];
        byte[][] byte_com_strs2 = new byte[com_strs2.length][4];

        Huffman.buildCodeTable1();
        Huffman.buildCodeTable2();

        for (int n = 0; n < com_strs1.length; n++) {
            byte_com_strs1[n][0] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][0].trim().toLowerCase())));
            byte_com_strs1[n][1] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress1(com_strs1[n][1].trim().toLowerCase())));
            byte_com_strs1[n][2] = Pearson8Bit(convertBoolean2Bytes(bytes2boolean(com_strs1[n][2].getBytes(StandardCharsets.UTF_8))));
            byte_com_strs1[n][3] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress2(com_strs1[n][3])));
        }

        for (int n = 0; n < com_strs2.length; n++) {
            byte_com_strs2[n][0] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][0].trim().toLowerCase())));
            byte_com_strs2[n][1] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress1(com_strs2[n][1].trim().toLowerCase())));
            byte_com_strs2[n][2] = Pearson8Bit(convertBoolean2Bytes(bytes2boolean(com_strs2[n][2].getBytes(StandardCharsets.UTF_8))));
            byte_com_strs2[n][3] = Pearson8Bit(convertBoolean2Bytes(Huffman.compress2(com_strs2[n][3])));
        }

        boolean flag;
        for (byte[] com_strs11 : byte_com_strs1) {
            for (byte[] com_strs21 : byte_com_strs2) {
                flag = false;
                for (int k = 0; k < com_strs11.length; k++) {
                    flag |= (com_strs11[k] == com_strs21[k]);
                }
                if (flag) {
                    num++;
                }
            }
        }

        return num;
    }

    public static byte calSum(byte[] arr) {
        int ret = 0;

        for (byte arr1 : arr) {
            ret += arr1;
        }

        return (byte)(ret%256);
    }

    public static byte[][][] getStringHashSum(String[][] strs) {
        byte[][][] ret_arr = new byte[strs.length][strs[0].length][1];

        for (int i = 0; i < ret_arr.length; i++) {
            for (int j = 0; j < ret_arr[i].length; j++) {
                ret_arr[i][j][0] = calSum(strs[i][j].getBytes(StandardCharsets.UTF_8));
            }
        }

        return ret_arr;
    }

    public static byte[] convertBoolean2Bytes(boolean[] bl_arr) {

        int byte_len = (bl_arr.length + 7) / 8;
        byte[] arrs = new byte[byte_len];
        boolean[] bl_arr_src = new boolean[byte_len * 8];
        System.arraycopy(bl_arr, 0, bl_arr_src, 0, bl_arr.length);
        boolean[] bl_tmp = new boolean[8];

        for (int i = 0; i < byte_len; i++) {
            System.arraycopy(bl_arr_src, i * 8, bl_tmp, 0, 8);
            arrs[i] = (byte) Util.toInt(bl_tmp);
        }

        return arrs;
    }

    public static byte Pearson8Bit(String str) {
        byte[] byte_arr = str.getBytes(StandardCharsets.UTF_8);
        int h = T[byte_arr[0] % 256];
        for (int i = 0; i < byte_arr.length; i++) {
            h = T[(h ^ byte_arr[i]) % 256];
        }

        return (byte) h;
    }
    
    public static byte Pearson8Bit(byte[] byte_arr1) {
        int[] byte_arr = new int[byte_arr1.length];
        for(int n = 0; n < byte_arr1.length; n++){
            byte_arr[n] = byte_arr1[n] >= 0 ? byte_arr1[n] :byte_arr1[n] + 256;
        }
        
        int h = T[byte_arr[0] % 256];
        for (int i = 0; i < byte_arr.length; i++) {
            h = T[(h ^ byte_arr[i]) % 256];
        }

        return (byte) h;
    }

    static int[] T = new int[]{
        98, 6, 85, 150, 36, 23, 112, 164, 135, 207, 169, 5, 26, 64, 165, 219,
        61, 20, 68, 89, 130, 63, 52, 102, 24, 229, 132, 245, 80, 216, 195, 115,
        90, 168, 156, 203, 177, 120, 2, 190, 188, 7, 100, 185, 174, 243, 162, 10,
        237, 18, 253, 225, 8, 208, 172, 244, 255, 126, 101, 79, 145, 235, 228, 121,
        123, 251, 67, 250, 161, 0, 107, 97, 241, 111, 181, 82, 249, 33, 69, 55,
        59, 153, 29, 9, 213, 167, 84, 93, 30, 46, 94, 75, 151, 114, 73, 222,
        197, 96, 210, 45, 16, 227, 248, 202, 51, 152, 252, 125, 81, 206, 215, 186,
        39, 158, 178, 187, 131, 136, 1, 49, 50, 17, 141, 91, 47, 129, 60, 99,
        154, 35, 86, 171, 105, 34, 38, 200, 147, 58, 77, 118, 173, 246, 76, 254,
        133, 232, 196, 144, 198, 124, 53, 4, 108, 74, 223, 234, 134, 230, 157, 139,
        189, 205, 199, 128, 176, 19, 211, 236, 127, 192, 231, 70, 233, 88, 146, 44,
        183, 201, 22, 83, 13, 214, 116, 109, 159, 32, 95, 226, 140, 220, 57, 12,
        221, 31, 209, 182, 143, 92, 149, 184, 148, 62, 113, 65, 37, 27, 106, 166,
        3, 14, 204, 72, 21, 41, 56, 66, 28, 193, 40, 217, 25, 54, 179, 117,
        238, 87, 240, 155, 180, 170, 242, 212, 191, 163, 78, 218, 137, 194, 175, 110,
        43, 119, 224, 71, 122, 142, 42, 160, 104, 48, 247, 103, 15, 11, 138, 239
    };
}
