package patientlinkage.Util;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <tt>Huffman</tt> class provides static methods for compressing and
 * expanding a binary input using Huffman codes over the 8-bit extended ASCII
 * alphabet.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/55compress">Section 5.5</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class Huffman {

//    // alphabet size of extended ASCII
//    private static final int R = 256;
    private static final int[] FREQ1 = new int[]{24368, 4529, 7417, 7726, 26118, 1779, 4232, 9943, 14004, 3703, 3176, 15069, 7218, 20293, 14445, 3884, 237, 23715, 14108, 9850, 4341, 1692, 4165, 439, 6973, 2008, 72, 77, 77, 90, 81, 89, 117, 88, 72, 74};
    private static final int[] FREQ2 = new int[]{29061, 20767, 13944, 6733, 8353, 8351, 7928, 8264, 8358, 8241};

    private static HashMap<Character, Integer> codebook1;
    private static HashMap<Character, Integer> codebook2;
    private static String[] codeTable1;
    private static String[] codeTable2;

    private static void initializeCodebook1() {
        codebook1 = new HashMap<>();
        int index = 0;
        for (char ch = 'a'; ch <= 'z'; ch++) {
            codebook1.put(ch, index++);
        }
        for (char ch = '0'; ch <= '9'; ch++) {
            codebook1.put(ch, index++);
        }
    }

    private static void initializeCodebook2() {
        codebook2 = new HashMap<>();
        int index = 0;

        for (char ch = '0'; ch <= '9'; ch++) {
            codebook2.put(ch, index++);
        }
    }

    public static void buildCodeTable1() {
        initializeCodebook1();
        // build Huffman trie
        Node root = buildTrie(FREQ1);
        // build code table
        codeTable1 = new String[FREQ1.length];
        buildCode(codeTable1, root, "");
    }

    public static void buildCodeTable2() {
        initializeCodebook2();
        // build Huffman trie
        Node root = buildTrie(FREQ2);
        // build code table
        codeTable2 = new String[FREQ2.length];
        buildCode(codeTable2, root, "");
    }

    public static int[] convertChar2Codebook1(char[] char_arr) {
        int[] ret_arr = new int[char_arr.length];

        for (int n = 0; n < ret_arr.length; n++) {
            ret_arr[n] = codebook1.get(char_arr[n]);
        }

        return ret_arr;
    }
    
    public static int[] convertChar2Codebook2(char[] char_arr) {
        int[] ret_arr = new int[char_arr.length];

        for (int n = 0; n < ret_arr.length; n++) {
            ret_arr[n] = codebook2.get(char_arr[n]);
        }

        return ret_arr;
    }

    // Do not instantiate.
    private Huffman() {
    }

    // Huffman trie node
    private static class Node implements Comparable<Node> {

        private final char ch;
        private final int freq;
        private final Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        // is the node a leaf node?
        private boolean isLeaf() {
            assert ((left == null) && (right == null)) || ((left != null) && (right != null));
            return (left == null) && (right == null);
        }

        // compare, based on frequency
        @Override
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }

    /**
     * Reads a sequence of 8-bit bytes from standard input; compresses them
     * using Huffman codes with an 8-bit alphabet; and writes the results to
     * standard output.
     *
     * @param s
     * @return
     */
//    public static void compress1() {
//        // read the input
//        String s = BinaryStdIn.readString();
//        char[] input = s.toCharArray();
//
//        // tabulate frequency counts
//        int[] freq = new int[R];
//        for (int i = 0; i < input.length; i++)
//            freq[input[i]]++;
//
//        // build Huffman trie
//        Node root = buildTrie(freq);
//
//        // build code table
//        String[] st = new String[R];
//        buildCode(st, root, "");
//
//        // print trie for decoder
//        writeTrie(root);
//
//        // print number of bytes in original uncompressed message
//        BinaryStdOut.write(input.length);
//
//        // use Huffman code to encode input
//        for (int i = 0; i < input.length; i++) {
//            String code = st[input[i]];
//            for (int j = 0; j < code.length(); j++) {
//                if (code.charAt(j) == '0') {
//                    BinaryStdOut.write(false);
//                }
//                else if (code.charAt(j) == '1') {
//                    BinaryStdOut.write(true);
//                }
//                else throw new IllegalStateException("Illegal state");
//            }
//        }
//
//        // close output stream
//        BinaryStdOut.close();
//    }
    public static boolean[] compress1(String s) {
        // read the input
        int[] input = convertChar2Codebook1(s.toCharArray());
        int code_len = 0;
        for (int i = 0; i < input.length; i++) {
            code_len += codeTable1[input[i]].length();
        }

        boolean[] ret_arr = new boolean[code_len];
        int index = 0;

        // use Huffman code to encode input
        for (int i = 0; i < input.length; i++) {
            String code = codeTable1[input[i]];
            for (int j = 0; j < code.length(); j++) {
                switch (code.charAt(j)) {
                    case '0':
                        ret_arr[index++] = false;
                        break;
                    case '1':
                        ret_arr[index++] = true;
                        break;
                    default:
                        throw new IllegalStateException("Illegal state");
                }
            }
        }

        return ret_arr;
    }
    
    public static boolean[] compress2(String s) {
        // read the input
        int[] input = convertChar2Codebook2(s.toCharArray());
        int code_len = 0;
        for (int i = 0; i < input.length; i++) {
            code_len += codeTable2[input[i]].length();
        }

        boolean[] ret_arr = new boolean[code_len];
        int index = 0;

        // use Huffman code to encode input
        for (int i = 0; i < input.length; i++) {
            String code = codeTable2[input[i]];
            for (int j = 0; j < code.length(); j++) {
                switch (code.charAt(j)) {
                    case '0':
                        ret_arr[index++] = false;
                        break;
                    case '1':
                        ret_arr[index++] = true;
                        break;
                    default:
                        throw new IllegalStateException("Illegal state");
                }
            }
        }

        return ret_arr;
    }

    // build the Huffman trie given frequencies
    private static Node buildTrie(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<>();
        for (char i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                pq.insert(new Node(i, freq[i], null, null));
            }
        }

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0) {
                pq.insert(new Node('\0', 0, null, null));
            } else {
                pq.insert(new Node('\1', 0, null, null));
            }
        }

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        return pq.delMin();
    }

    // make a lookup table from symbols and their encodings
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left, s + '0');
            buildCode(st, x.right, s + '1');
        } else {
            st[x.ch] = s;
        }
    }

//    /**
//     * Reads a sequence of bits that represents a Huffman-compressed message from
//     * standard input; expands them; and writes the results to standard output.
//     */
//    public static void expand() {
//
//        // read in Huffman trie from input stream
//        Node root = readTrie(); 
//
//        // number of bytes to write
//        int length = BinaryStdIn.readInt();
//
//        // decode using the Huffman trie
//        for (int i = 0; i < length; i++) {
//            Node x = root;
//            while (!x.isLeaf()) {
//                boolean bit = BinaryStdIn.readBoolean();
//                if (bit) x = x.right;
//                else     x = x.left;
//            }
//            BinaryStdOut.write(x.ch, 8);
//        }
//        BinaryStdOut.close();
//    }
//    private static Node readTrie() {
//        boolean isLeaf = BinaryStdIn.readBoolean();
//        if (isLeaf) {
//            return new Node(BinaryStdIn.readChar(), -1, null, null);
//        }
//        else {
//            return new Node('\0', -1, readTrie(), readTrie());
//        }
//    }
    public static String readFromCSVFile(String FileName, int[] props) {
        String ret_str = "";
        try (CSVReader reader = new CSVReader(new FileReader(FileName))) {
            String[] strs;
            reader.readNext();
            while ((strs = reader.readNext()) != null) {
                for (int i = 0; i < props.length; i++) {
                    ret_str += strs[props[i]].replace("-", "").toLowerCase().substring(2);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret_str;
    }

    /**
     * Sample client that calls <tt>compress1()</tt> if the command-line argument
     * is "-" an <tt>expand()</tt> if it is "+".
     *
     * @param args
     */
    public static void main(String[] args) {
        String file1 = "/Users/cf/GitHub/patientlinkage/data/Source14k_a_10K.csv";
        String file2 = "/Users/cf/GitHub/patientlinkage/data/Source14k_b_10K.csv";
        int[] props = new int[]{11};

        String str = readFromCSVFile(file1, props);
        str += readFromCSVFile(file2, props);

        char[] ch = str.toCharArray();

        initializeCodebook2();

        int[] cnts = new int[10];

        for (int n = 0; n < str.length(); n++) {
            cnts[codebook2.get(ch[n])]++;
        }

        for (int i = 0; i < cnts.length; i++) {
            System.out.print(cnts[i] + ",");
        }

    }
}
