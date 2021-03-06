/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the FRIL Framework.
 *
 * The Initial Developers of the Original Code are
 * The Department of Math and Computer Science, Emory University and 
 * The Centers for Disease Control and Prevention.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */ 


package cdc.impl.join.blocking;

import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.utils.StringUtils;

/**
 * An implementation of blocking function that generates the same buckets
 * from the records having the same prefix of value of attributes used for blocking.
 * Note that this function takes prefix of only the first attribute (of columns provide
 * more than one attribute, the later attributes are simply ignored.
 * This blocking function allows to create blocks from records that have for instance
 * the same first three letters of the last name.
 * @author Pawel Jurczyk
 *
 */
public class PrefixBlockingFunction implements BlockingFunction {
	
	/**
	 * The attributes used for generating a block descriptor for input record
	 */
	private DataColumnDefinition[][] columns;
	
	/**
	 * The length of prefix (how many charasters shoulb be used as a prefix).
	 */
	private int prefixLength;
	
	/**
	 * The column describe attributes for both data sources (@see BlockingFunction).
	 * @param columns attributes of records that are used to generate block descriptors.
	 * @param prefixLength length of the prefix
	 */
	public PrefixBlockingFunction(DataColumnDefinition[][] columns, int prefixLength) {
		this.columns = columns;
		this.prefixLength = prefixLength;
	}
	
	/**
	 * The implementation of hash function (@see BlockingFunction).
	 */
	public String hash(DataRow value, int id) {
		String val = value.getData(columns[0][id]).getValue().toString();
		if (StringUtils.isNullOrEmpty(val)) {
			return null;
		}
		if (val.length() < prefixLength) {
			return val;
		} else {
			return val.substring(0, prefixLength);
		}
	}

	public DataColumnDefinition[][] getColumns() {
		return columns;
	}

	/**
	 * Function that compares two prefix blocking functions.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof PrefixBlockingFunction)) {
			return false;
		}
		PrefixBlockingFunction that = (PrefixBlockingFunction)obj;
		return this.prefixLength == that.prefixLength && equalAttributes(that);
	}

	/**
	 * Just a helper function for the equals
	 * @param that
	 * @return
	 */
	private boolean equalAttributes(PrefixBlockingFunction that) {
		if (columns[0].length != that.columns[0].length) {
			return false;
		}
		for (int i = 0; i < columns[0].length; i++) {
			if (!columns[0][i].equals(that.columns[0][i]) || !columns[1][i].equals(that.columns[1][i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a prefix length of this function.
	 * @return
	 */
	public int getPrefixLength() {
		return prefixLength;
	}
	
}
