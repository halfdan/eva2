package eva2.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;

/**
 * An array of Strings that can be selected and deselected. May be created directly from an Enum.
 * 
 * @author mkron
 *
 */
public class StringSelection implements Serializable {
	private static final long serialVersionUID = -1512329288445831907L;
	private String[] strObjects;
	private String[] toolTips;
	boolean[] selStates;
	private transient HashMap<String,Integer> stringToIndexHash = null;
	private transient Class<? extends Enum> enumClass = null;
	
	public StringSelection(String[] sArr, String[] tips) {
		strObjects = sArr;
		toolTips=tips;
		selStates=new boolean[sArr.length];
		stringToIndexHash = null;
		enumClass = null;
	}	
	
	public StringSelection(String[] sArr, String[] tips, int initialSel) {
		this(sArr, tips);
		if (initialSel<getLength()) setSelected(initialSel, true);
		enumClass = null;
	}
	
	public StringSelection(Enum<?> e, String[] tips) {
		strObjects = new String[e.getClass().getEnumConstants().length];
		toolTips=tips;
		selStates = new boolean[strObjects.length];
		for (int i = 0; i < strObjects.length; i++) {
			strObjects[i] = e.getClass().getEnumConstants()[i].toString();
		}
		setSelected(e.ordinal(), true);
		stringToIndexHash = null;
		enumClass = e.getClass();
	}
	
	public StringSelection(StringSelection stringSelection) {
		strObjects = stringSelection.strObjects.clone();
		selStates = stringSelection.selStates.clone();
		toolTips = stringSelection.toolTips.clone();
		stringToIndexHash = null;
		enumClass = stringSelection.enumClass;
	}

	/**
	 * Construct a string selection that allows all enum fields of the given type plus a list of additional
	 * strings to be selected. The enum fields will be first in the selection list. 
	 * 
	 * @param e
	 * @param headerFields
	 */
	public StringSelection(Enum<?> e, String[] enumTips,
			List<String> headerFields, String[] addTips) {
		this(ToolBox.appendEnumAndArray(e, headerFields.toArray(new String[headerFields.size()])),
				ToolBox.appendArrays(enumTips, addTips));
		enumClass = e.getClass();
	}

	public Object clone() {
		return new StringSelection(this);
	}
	
	public int getLength() {
		return strObjects.length;
	}
	
	public String getElement(int i) {
		return strObjects[i];
	}
	
	public String getElementInfo(int i) {
		if (toolTips!=null && (toolTips.length>i)) return toolTips[i];
		else return null;
	}
	
	public String[] getStrings() {
		return strObjects;
	}
	
	public boolean isSelected(int i) {
		return selStates[i];
	}
	
	/**
	 * Returns true if the given enum is selected (as its string representation)
	 * within the instance. This only works if the enum was used for the
	 * creation of this instance.
	 * 
	 * @param e
	 * @return
	 */
	public boolean isSelected(Enum<?> e) {
//		if (isSelected(e.ordinal())) {
//			return e.toString().equals(strObjects[e.ordinal()]);
//		} else return false;
		if (enumClass!=null) {
			if (e.getClass().equals(enumClass)) return isSelected(e.ordinal());
			else {
				System.err.println("Error, the string selection was constructed with a different enum class - invalid request (StringSelection.isSelected(Enum)");
				return false;
			}
		} else {
			System.err.println("Error, the string selection was constructed without an enum class - invalid request (StringSelection.isSelected(Enum)");
			return false;
		}
	}
	
	/**
	 * Check if a given string is selected within this instance. If the
	 * String is not found, false is returned.
	 * 
	 * @param str
	 * @return
	 */
	public boolean isSelected(String str) {
		if (stringToIndexHash == null) { // for some time efficiency...
			stringToIndexHash = new HashMap<String,Integer>(2*strObjects.length);
			for (int i=0; i<strObjects.length; i++) {
				stringToIndexHash.put(strObjects[i], i);
			}
		}
		Integer selIndex = stringToIndexHash.get(str);
		if (selIndex==null) {
			System.err.println("Error, unknown string for StringSelection: " + str + ", selectable were " + BeanInspector.toString(getStrings()));
			return false;
		} return isSelected(selIndex);
//		for (int i=0; i<strObjects.length; i++) {
//			if (strObjects[i].equals(str)) return isSelected(i);
//		}
//		return false;
	}
	
	public void setSelected(int i, boolean v) {
		selStates[i]=v;
	}	
	
	public void toggleSelected(int i) {
		selStates[i]=!selStates[i];
	}

	/**
	 * Apply the selection state of the given instance to this instance. Compares
	 * Strings and takes over the selection state if they are equal.
	 * 
	 * @param sel
	 */
	public void takeOverSelection(StringSelection sel) {
		// try to apply the same selection for equivalent string (should be in same order)
		int mismatchAt = -1;
		for (int i=0; i<sel.getLength() && i<getLength(); i++) { 
			// hope that elements are aligned at the beginning and take over selection state
			if (sel.getElement(i).equals(getElement(i))) {
//				System.out.println("Fit: " + getElement(i) + " / " + sel.getElement(i));
				setSelected(i, sel.isSelected(i));
			} else {
//				System.out.println(" - does not fit: " + getElement(i) + " vs " + sel.getElement(i));
				mismatchAt = i; // if elements are not aligned, start double loop search at that point
				break;
			}
		}
		if (mismatchAt>=0) {
			// double look search to find matching elements (equal strings)
			for (int i=mismatchAt; i<getLength(); i++) {
				for (int j=mismatchAt; j<sel.getLength(); j++) {
					if (sel.getElement(j).equals(getElement(i))) {
						// if strings match, take over the selection state
//						System.out.println("Fit: " + getElement(i) + " / " + sel.getElement(j));
						setSelected(i, sel.isSelected(j));
					}
				}
			}
		}
	}

	/**
	 * Return a sub-list of the selected items.
	 * 
	 * @return
	 */
	public String[] getSelected() {
		ArrayList<String> ret = new ArrayList<String>();
		for (int i=0; i<getLength(); i++) if (isSelected(i)) ret.add(getElement(i));
		return ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * Return a sub-list of the selected items paired up with the respective index.
	 * 
	 * @return
	 */
	public List<Pair<String,Integer>> getSelectedWithIndex() {
		ArrayList<Pair<String,Integer>> ret = new ArrayList<Pair<String,Integer>>();
		for (int i=0; i<getLength(); i++) if (isSelected(i)) ret.add(new Pair<String,Integer>(getElement(i), i));
		return ret;
	}

	/**
	 * Return only those selected fields which are members of the given enum.
	 * @param e
	 * @return
	 */
	public Enum[] getSelectedEnum(Enum[] e) {
		LinkedList<Integer> selectedList = new LinkedList<Integer>();
		for (int i=0; i<e.length; i++) {
			if (isSelected(e[i])) selectedList.add(i);
		}
		Enum[] ret = (Enum[]) java.lang.reflect.Array.newInstance(e[0].getClass(), selectedList.size());
		Iterator<Integer> iter = selectedList.iterator();
		int i=0;
		while (iter.hasNext()) {
			ret[i++]=e[iter.next()];
		}
		return ret;
	}
}