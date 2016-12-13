/**
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.*;
import java.util.*;

/**
 * A very simple CSV parser released under a commercial-friendly license.
 * This just implements splitting a single line into fields.
 * 
 * @author Glen Smith
 * @author Rainer Pruy
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 * 
 */
public class CSVParser{
	private Scanner in;
	private Organizer o;
    private char separator;
    private File csvFile = null;;
    private final char quotechar;
    private final char escape;
    private final boolean strictQuotes;
    private String pending;
    private boolean inField = false;
    private boolean isOrdered = false;
    private final boolean ignoreLeadingWhiteSpace;
    /** The default separator to use if none is supplied to the constructor. */
    public static final char DEFAULT_SEPARATOR = ',';
    public static final int INITIAL_READ_SIZE = 128;
    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    /**
     * The default escape character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
   /**
    * The default strict quote behavior to use if none is supplied to the
    * constructor
    */
    public static final boolean DEFAULT_STRICT_QUOTES = false;
    
    /**
     * The default leading whitespace behavior to use if none is supplied to the
     * constructor
     */
    public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;
    
    /**
     * Constructs CSVParser using a comma for the separator.
     */
    public CSVParser() {
        this(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVParser with supplied separator.
     * @param separator
     *            the delimiter to use for separating entries.
     */
    public CSVParser(char separator) {
        this(separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }
    
    

    /**
     * Constructs CSVParser with supplied separator and quote char.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     */
    public CSVParser(char separator, char quotechar) {
        this(separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     */
    public CSVParser(char separator, char quotechar, char escape) {
        this(separator, quotechar, escape, DEFAULT_STRICT_QUOTES);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * Allows setting the "strict quotes" flag
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param strictQuotes
     *            if true, characters outside the quotes are ignored
     */
    public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes) {
        this(separator, quotechar, escape, strictQuotes, DEFAULT_IGNORE_LEADING_WHITESPACE);
    }
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * Allows setting the "strict quotes" and "ignore leading whitespace" flags
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param strictQuotes
     *            if true, characters outside the quotes are ignored
     * @param ignoreLeadingWhiteSpace
     *            if true, white space in front of a quote in a field is ignored
     */
    public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace) {
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.strictQuotes = strictQuotes;
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
    }
    public void setFile(String filePath) throws Exception{
    	csvFile = new File(filePath);
    	 in = new Scanner(csvFile);
    }
    public void setFile(File file) throws Exception{
    	csvFile = file;
    	in = new Scanner(csvFile);
    }
    /**
     * 
     * @return true if something was left over from last call(s)
     */
    public boolean isPending() {
    	return pending != null;
    }
    /**
     * Helper method to distinguish if the Header has been read and 
     * set in the global Organizer object
     */
    private void setOrder() throws Exception {
    	o = new Organizer();
    	ArrayList<String> patientInput = o.toArrayList(parseLine(in.nextLine()));
    	o.setActualOrder(patientInput);
    	isOrdered = true;
    }
    /**
     * Parses files into Patient objects and returns an ArrayList of 
     * of all the Patient that have been read. There is a fixed 35000 patients at a time
     * because more than this causes a Heap Overflow Exception
     * @return ArrayList of Patient with max size of 35000
     */
    public ArrayList<Patient> parseFile() throws Exception{
    	if (!isOrdered) {
    		setOrder();
    	}
     	ArrayList<Patient> patientList = new ArrayList<Patient>();
    	ArrayList<String> patientInput;
    	Patient p;
    	int count = 0;
//    	while(in.hasNextLine())
    	while(count <= 35000 && in.hasNextLine())
    	{
    		p = new Patient();
    		patientInput = o.toArrayList(parseLine(in.nextLine()));
    		patientInput = o.organize(patientInput);
    		
    		// Commented out, since read from file not used
    		//for(int i = 0; i < patientInput.size(); i++)
    		//	p.setInstance(i, patientInput.get(i));
    		
    		if(patientInput.size() >= 1)
    			patientList.add(p);
        	count++;
    	}
		return patientList;
    }
    /**
     * Helper method for the parseFile() method
     */
    public String[] parseLineMulti(String nextLine) throws IOException {
    	return parseLine(nextLine, true);
    }
    /**
     * Helper method for the parseFile() method
     */
    public String[] parseLine(String nextLine) throws IOException {
    	return parseLine(nextLine, false);
    }
    /**
     * Parses an incoming String and returns an array of elements.
     * 
     * @param nextLine
     *            the string to parse
     * @param multi
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String nextLine, boolean multi) throws IOException {

    	if (!multi && pending != null) {
    		pending = null;
    	}
    	
        if (nextLine == null) {
        	if (pending != null) {
        		String s = pending;
        		pending = null;
        		return new String[] {s};
        	} else {
        		return null;
        	}
        }

        List<String>tokensOnThisLine = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
        boolean inQuotes = false;
        if (pending != null) {
        	sb.append(pending);
        	pending = null;
        	inQuotes = true;
        }
        for (int i = 0; i < nextLine.length(); i++) {
        	
        	char c = nextLine.charAt(i);
        	if (c == this.escape) {
        		if( isNextCharacterEscapable(nextLine, inQuotes || inField, i) ){
        			sb.append(nextLine.charAt(i+1));
        			i++;
        		}
        	} else if (c == quotechar) {
        		if( isNextCharacterEscapedQuote(nextLine, inQuotes || inField, i) ){
        			sb.append(nextLine.charAt(i+1));
        			i++;
        		}else{
        			inQuotes = !inQuotes;

        			// the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                    if (!strictQuotes) {
                        if(i>2 //not on the beginning of the line
                                && nextLine.charAt(i-1) != this.separator //not at the beginning of an escape sequence
                                && nextLine.length()>(i+1) &&
                                nextLine.charAt(i+1) != this.separator //not at the	end of an escape sequence
                        ){

                            if (ignoreLeadingWhiteSpace && sb.length() > 0 && isAllWhiteSpace(sb)) {
                                sb = new StringBuilder(INITIAL_READ_SIZE);  //discard white space leading up to quote
                            } else {
                                sb.append(c);
                            }
  
                        }
                    }
        		}
                inField = !inField;
        	} else if (c == separator && !inQuotes) {
        		tokensOnThisLine.add(sb.toString());
        		sb = new StringBuilder(INITIAL_READ_SIZE); // start work on next token
                inField = false;
        	} else {
                if (!strictQuotes || inQuotes){
                    sb.append(c);
                    inField = true;
                }
        	}
        }
        // line is done - check status
        if (inQuotes) {
        	if (multi) {
        		// continuing a quoted section, re-append newline
        		sb.append("\n");
        		pending = sb.toString();
        		sb = null; // this partial content is not to be added to field list yet
        	} else {
        		throw new IOException("Un-terminated quoted field at end of CSV line");
        	}
        }
        if (sb != null) {
        	tokensOnThisLine.add(sb.toString());
        }
        return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);
        
    }
    public void setSeperator(char delimiter){
    	separator = delimiter;
    }
    /**  
     * precondition: the current character is a quote or an escape
	 * @param nextLine the current line
	 * @param inQuotes true if the current context is quoted
	 * @param i current index in line
	 * @return true if the following character is a quote
	 */
	private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
		return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
		    && nextLine.length() > (i+1)  // there is indeed another character to check.
		    && nextLine.charAt(i+1) == quotechar;
	}

	/**  
	 * precondition: the current character is an escape
	 * @param nextLine the current line
	 * @param inQuotes true if the current context is quoted
	 * @param i current index in line
	 * @return true if the following character is a quote
	 */
	protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
		return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
		    && nextLine.length() > (i+1)  // there is indeed another character to check.
		    && ( nextLine.charAt(i+1) == quotechar || nextLine.charAt(i+1) == this.escape);
	}
	
	/**
	 * precondition: sb.length() > 0
	 * @param sb A sequence of characters to examine
	 * @return true if every character in the sequence is whitespace
	 */
    protected boolean isAllWhiteSpace(CharSequence sb) {
        boolean result = true;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);

            if ( !Character.isWhitespace(c) ) {
                return false;
            }
        }
        return result;
    }
}
