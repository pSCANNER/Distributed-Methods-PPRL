/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.*;
import java.util.*;

/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class BF implements Serializable {
    //private BitSet bitset;
    private boolean[] bitset;
    private static int bitSetSize = 100;
    private static int expectedNumberOfFilterElements = 30; // expected (maximum) number of elements to be added
    private int numberOfAddedElements; // number of elements actually added to the Bloom filter
    private int k = (int) Math.round((bitSetSize / expectedNumberOfFilterElements) *
            Math.log(2.0));;
    /**
     * Constructs an empty Bloom filter.
     *
     * @param bitSetSize defines how many bits should be used for the filter.
     * @param expectedNumberOfFilterElements defines the maximum number of elements the filter is expected to contain.
     */
    public BF() {
        //bitset = new BitSet(bitSetSize);
        bitset = new boolean[bitSetSize];
        numberOfAddedElements = 0;
    }

    public BF(String input) throws Exception {
    	//bitset = new BitSet(bitSetSize);
    	bitset = new boolean[bitSetSize];
        numberOfAddedElements = 0;
        createId(input);
        if(input.equals(""))
        	for(int i = 0; i < bitset.length; i++)
        		if(bitset[i])
        			bitset[i] = false;
    }

    /**
     * Compares the contents of two instances to see if they are equal.
     *
     * @param obj is the object to compare to.
     * @return True if the contents of the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BF other = (BF) obj;        
        if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
            return false;
        }
        if (this.k != other.k) {
            return false;
        }
        if (this.bitSetSize != other.bitSetSize) {
            return false;
        }
        /*if (this.bitset != other.bitset && (this.bitset == null || !this.bitset.equals(other.bitset))) {
            return false;
        }*/
        return true;
    }

    /**
     * Calculates a hash code for this class.
     * @return hash code representing the contents of an instance of this class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        //hash = 61 * hash + (this.bitset != null ? this.bitset.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }


    /**
     * Calculates the expected probability of false positives based on
     * the number of expected filter elements and the size of the Bloom filter.
     * <br /><br />
     * The value returned by this method is the <i>expected</i> rate of false
     * positives, assuming the number of inserted elements equals the number of
     * expected elements. If the number of elements in the Bloom filter is less
     * than the expected value, the true probability of false positives will be lower.
     *
     * @return expected probability of false positives.
     */
    public double expectedFalsePositiveProbability() {
        return getFalsePositiveProbability(expectedNumberOfFilterElements);
    }

    /**
     * Calculate the probability of a false positive given the specified
     * number of inserted elements.
     *
     * @param numberOfElements number of inserted elements.
     * @return probability of a false positive.
     */
    public double getFalsePositiveProbability(double numberOfElements) {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-k * (double) numberOfElements
                        / (double) bitSetSize)), k);

    }

    /**
     * Get the current probability of a false positive. The probability is calculated from
     * the size of the Bloom filter and the current number of elements added to it.
     *
     * @return probability of false positives.
     */
    public double getFalsePositiveProbability() {
        return getFalsePositiveProbability(numberOfAddedElements);
    }


    /**
     * Returns the value chosen for K.<br />
     * <br />
     * K is the optimal number of hash functions based on the size
     * of the Bloom filter and the expected number of inserted elements.
     *
     * @return optimal k.
     */
    public int getK() {
        return k;
    }

    /**
     * Sets all bits to false in the Bloom filter.
     */
    public void clear() {
        //bitset.clear();
        numberOfAddedElements = 0;
    }
    /**
     * splits the input into bigrams to be fed to the that BF obj
     *
     * @param input in a String that is going to be broken into bigrams
     * and fed in to the add method
     * @return void because it will change the BF obj
     * @throws Exception 
     */
    public void createId(String input) throws Exception{
		char left, right;
		input = " " + input + " ";
		for(int i = 0; i < input.length()-1; i++){
			left  = input.charAt(i);
			right = input.charAt(i+1);
			add("" + left + right);
		}
	}
    /**
     * Adds an object to the Bloom filter. The output from the object's
     * toString() method is used as input to the hash functions.
     *
     * @param element is an element to register in the Bloom filter.
     * @throws Exception 
     */
    public void add(String element) throws Exception {
       long hash;
       String valString = element.toString();
       for (int x = 0; x < k; x++) {
           hash = Encryption.encrypt(valString + Integer.toString(x));
           hash = hash % (long)bitSetSize;
           //bitset.set(Math.abs((int)hash), true);
           bitset[Math.abs((int)hash)] = true;
       }
       numberOfAddedElements ++;
    }

    /**
     * Adds all elements from a Collection to the Bloom filter.
     * @param c Collection of elements.
     * @throws Exception 
     */
    public void addAll(Collection<String> c) throws Exception {
        for (String element : c)
            add(element);
    }

    /**
     * Returns true if the element could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param element element to check.
     * @return true if the element could have been inserted into the Bloom filter.
     * @throws Exception 
     */
    public boolean contains(String element) throws Exception {
       long hash;
       String valString = element.toString();
       for (int x = 0; x < k; x++) {
           hash = Encryption.encrypt(valString + Integer.toString(x));
           hash = hash % (long)bitSetSize;
           //if (!bitset.get(Math.abs((int)hash)))
           if(!bitset[Math.abs((int)hash)])
               return false;
       }
       return true;
    }

    /**
     * Returns true if all the elements of a Collection could have been inserted
     * into the Bloom filter. Use getFalsePositiveProbability() to calculate the
     * probability of this being correct.
     * @param c elements to check.
     * @return true if all the elements in c could have been inserted into the Bloom filter.
     * @throws Exception 
     */
    public boolean containsAll(Collection<String> c) throws Exception {
        for (String element : c)
            if (!contains(element))
                return false;
        return true;
    }

    /**
     * Read a single bit from the Bloom filter.
     * @param bit the bit to read.
     * @return true if the bit is set, false if it is not.
     */
    public boolean getBit(int bit) {
        //return bitset.get(bit);
        return bitset[bit];
    }

    /**
     * Set a single bit in the Bloom filter.
     * @param bit is the bit to set.
     * @param value If true, the bit is set. If false, the bit is cleared.
     */
    /*public void setBit(int bit, boolean value) {
        bitset.set(bit, value);
    }*/

    /**
     * Return the bit set used to store the Bloom filter.
     * @return bit set representing the Bloom filter.
     */
    /*public BitSet getBitSet() {
        return bitset;
    }*/

    /**
     * Returns the number of bits in the Bloom filter. Use count() to retrieve
     * the number of inserted elements.
     *
     * @return the size of the bitset used by the Bloom filter.
     */
    public int size() {
        return this.bitSetSize;
    }

    /**
     * Returns the number of elements added to the Bloom filter after it
     * was constructed or after clear() was called.
     *
     * @return number of elements added to the Bloom filter.
     */
    /*public int count() {
        return this.numberOfAddedElements;
    }*/

    /**
     * Returns the expected number of to be inserted into the filter.
     * This value is the same value as the one passed to the constructor.
     *
     * @return expected number of elements.
     */
    /*public int getExpectedNumberOfElements() {
        return expectedNumberOfFilterElements;
    }*/
    public String toString(){
    	/*String vector = "";
    	for(int i = 0; i < bitset.length(); i++){
    		if(bitset.get(i))
    			vector = vector + "1";
    		else
    			vector = vector + "0";
    	}
    	return vector;*/
    	String vector = "";
    	for(int i = 0; i < bitset.length; i++){
    		if(bitset[i])
    			vector = vector + "1";
    		else
    			vector = vector + "0";
    	}
    	return vector;
    }
    public int getOnes(){
    	/*int count = 0;
    	for(int i = 0; i < bitset.length(); i++){
    		if(bitset.get(i))
    			count++;
    	}
    	return count;*/
    	int count = 0;
    	for(int i = 0; i < bitset.length; i++)
    		if(bitset[i])
    			count++;
    	return count;
    }
}
